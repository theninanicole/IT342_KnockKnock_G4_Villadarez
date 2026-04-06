import React, { useContext, useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import { Search, QrCode, ChevronRight } from "lucide-react";
import "./Dashboard.css"; 
import { AuthContext } from "../context/AuthContext";
import { fetchCondoVisits, findVisitByReference, checkOutVisit } from "../services/apiServices";
import { toast } from "react-toastify";
import AdminVisitDetailsModal from "../components/AdminVisitDetailsModal";
import StatusPill from "../components/StatusPill";
import { supabase } from "../services/supabaseClient";

export default function AdminDashboard() {
  const { user } = useContext(AuthContext);

  const [searchTerm, setSearchTerm] = useState("");
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [detailsOpen, setDetailsOpen] = useState(false);

  const loadVisits = async () => {
    const condoId =
      user?.condo?.condoId ||
      user?.condoId ||
      (user?.condo && user.condo.id) ||
      null;

    if (!condoId) {
      console.warn("[AdminDashboard] No condoId available for admin; skipping condo visits fetch");
      return;
    }
    setLoading(true);
    try {
      const data = await fetchCondoVisits(condoId);
      setVisits(data);
    } catch (error) {
      console.error("Error loading condo visits for admin dashboard", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVisits();
  }, [user]);

  // Supabase Realtime: refresh dashboard when visits for this condo change
  useEffect(() => {
    const condoId =
      user?.condo?.condoId ||
      user?.condoId ||
      (user?.condo && user.condo.id) ||
      null;

    if (!user || !condoId) return;

    const channel = supabase
      .channel("admin-dashboard-visits-channel")
      .on(
        "postgres_changes",
        {
          event: "INSERT",
          schema: "public",
          table: "visits",
          filter: `condo_id=eq.${condoId}`,
        },
        () => {
          loadVisits();
        }
      )
      .on(
        "postgres_changes",
        {
          event: "UPDATE",
          schema: "public",
          table: "visits",
          filter: `condo_id=eq.${condoId}`,
        },
        () => {
          loadVisits();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user]);

  const getTodayKey = () => {
    try {
      return new Date().toLocaleDateString("en-CA"); // e.g. 2026-03-30
    } catch {
      return null;
    }
  };

  const getVisitDateKey = (visitDate) => {
    if (!visitDate) return null;
    if (typeof visitDate === "string") {
      return visitDate.slice(0, 10);
    }
    if (Array.isArray(visitDate) && visitDate.length >= 3) {
      const [year, month, day] = visitDate;
      const mm = String(month).padStart(2, "0");
      const dd = String(day).padStart(2, "0");
      return `${year}-${mm}-${dd}`;
    }
    return null;
  };

  const todayKey = getTodayKey();

  const filteredVisits = visits.filter((visit) => {
    const ref = visit.referenceNumber || "";
    return ref.toLowerCase().includes(searchTerm.toLowerCase());
  });

  const currentlyCheckedIn = filteredVisits.filter(
    (visit) => visit.status === "CHECKED-IN"
  );

  const todaysVisitors = filteredVisits.filter((visit) => {
    const visitKey = getVisitDateKey(visit.visitDate);
    return (
      visitKey &&
      todayKey &&
      visitKey === todayKey &&
      visit.status !== "CHECKED-IN"
    );
  });

  const openDetailsForVisit = (visit) => {
    setSelectedVisit(visit);
    setDetailsOpen(true);
  };

  const handleCheckOut = async (visitId) => {
    try {
      await checkOutVisit(visitId);
      toast.success("Visitor checked out successfully");

      const condoId =
        user?.condo?.condoId ||
        user?.condoId ||
        (user?.condo && user.condo.id) ||
        null;

      if (condoId) {
        const data = await fetchCondoVisits(condoId);
        setVisits(data);
      }
    } catch (error) {
      console.error("[AdminDashboard] Error checking out visit:", error);
      const message =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Failed to check out visitor";
      toast.error(message);
    }
  };

  const handleSearchVerify = async () => {
    if (!searchTerm.trim()) return;
    try {
      const visit = await findVisitByReference(searchTerm.trim());
      if (!visit) {
        alert("Visit not found for that reference number");
        return;
      }
      if (visit.status !== "SCHEDULED") {
        alert("Visit is not in SCHEDULED status");
        return;
      }
      openDetailsForVisit(visit);
    } catch (error) {
      console.error("Error searching visit by reference", error);
      alert("Visit not found or you do not have access to it");
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role="ADMIN" />
      <div className="main-content">
        <TopBar title="Dashboard" />

        <div className="content-inner">
          
          {/* Expanded Container Width: Changed from max-w-[900px] to max-w-7xl (1280px) */}
          <div className="w-full max-w-7xl mx-auto space-y-8">
            
            {/* Verification Card */}
            <div className="bg-white rounded-2xl border border-gray-100 p-12 shadow-sm flex flex-col items-center">
              <h3 className="text-[22px] font-bold text-gray-900 mb-2">Verification</h3>
              <p className="text-gray-500 text-[15px] mb-8">Scan QR or enter reference number presented by the visitor.</p>
              
              {/* Expanded Search Bar Width: Changed from max-w-2xl to max-w-4xl */}
              <div className="flex items-center gap-4 w-full max-w-4xl">
                <div className="flex-1 flex items-center bg-gray-50/80 rounded-xl px-4 py-3.5 border border-gray-100 transition-colors focus-within:bg-white focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-500/10">
                  <Search className="text-gray-400 w-5 h-5 mr-3 flex-shrink-0" />
                  <input
                    type="text"
                    placeholder="Search reference number..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="bg-transparent border-none outline-none w-full text-sm text-gray-900 placeholder:text-gray-400"
                  />
                </div>
                <button className="bg-blue-600 hover:bg-blue-700 text-white p-3.5 rounded-xl transition-colors shadow-sm flex-shrink-0">
                  <QrCode className="w-6 h-6" />
                </button>
                <button
                  className="bg-[#111827] hover:bg-gray-800 text-white px-10 py-3.5 rounded-xl font-medium text-[15px] transition-colors shadow-sm flex-shrink-0"
                  onClick={handleSearchVerify}
                >
                  Verify
                </button>
              </div>
            </div>

            {/* Currently Checked-In Section */}
            <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">
              <div className="flex items-center gap-2.5 mb-6">
                <div className="w-2.5 h-2.5 rounded-full bg-[#10B981]"></div>
                <h4 className="text-[18px] font-bold text-gray-900">Currently Checked-In</h4>
              </div>

              {loading ? (
                <p className="text-gray-500 text-[15px] py-4">Loading visits...</p>
              ) : currentlyCheckedIn.length === 0 ? (
                <p className="text-gray-500 text-[15px] py-4">No visitors are currently checked-in.</p>
              ) : (
                <div className="flex flex-col">
                  {currentlyCheckedIn.map((visit) => (
                    <div
                      key={visit.visitId}
                      className="flex justify-between items-center py-4 border-b border-gray-50 last:border-0 last:pb-0 first:pt-0"
                    >
                      <div>
                        <div className="text-[15px] font-medium text-gray-900">{visit.visitor?.fullName}</div>
                        <div className="text-[13px] text-gray-400 font-mono mb-1 mt-0.5">{visit.referenceNumber}</div>
                      </div>
                      <button
                        className="bg-gray-100 hover:bg-gray-200 text-gray-600 px-5 py-1.5 rounded-full text-xs font-medium transition-colors"
                        onClick={() => handleCheckOut(visit.visitId)}
                      >
                        Check-Out
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Today's Visitors Section */}
            <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">
              <div className="flex items-center gap-2.5 mb-6">
                <div className="w-2.5 h-2.5 rounded-full bg-blue-600"></div>
                <h4 className="text-[18px] font-bold text-gray-900">Today's Visitors</h4>
              </div>

              {loading ? (
                <p className="text-gray-500 text-[15px] py-4">Loading visits...</p>
              ) : todaysVisitors.length === 0 ? (
                <p className="text-gray-500 text-[15px] py-4">No visitors scheduled for today.</p>
              ) : (
                <div className="flex flex-col">
                  {todaysVisitors.map((visit) => (
                    <div
                      key={visit.visitId}
                      className="flex justify-between items-center py-4 border-b border-gray-50 last:border-0 last:pb-0 first:pt-0 cursor-pointer"
                      onClick={() => openDetailsForVisit(visit)}
                    >
                      <div>
                        <div className="text-[15px] font-medium text-gray-900">{visit.visitor?.fullName}</div>
                        <div className="text-[13px] text-gray-400 font-mono mb-1 mt-0.5">{visit.referenceNumber}</div>
                      </div>
                      <div className="flex items-center gap-4">
                        <StatusPill status={visit.status} />
                        <ChevronRight className="w-4 h-4 text-gray-300" />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </div>
        </div>
      </div>
      <AdminVisitDetailsModal
        isOpen={detailsOpen}
        onClose={() => setDetailsOpen(false)}
        visit={selectedVisit}
        onCheckedIn={async () => {
          const condoId =
            user?.condo?.condoId ||
            user?.condoId ||
            (user?.condo && user.condo.id) ||
            null;
          if (condoId) {
            try {
              const data = await fetchCondoVisits(condoId);
              setVisits(data);
            } catch (error) {
              console.error("Error refreshing visits after check-in", error);
            }
          }
        }}
      />
    </div>
  );
}