import React, { useContext, useEffect, useMemo, useState } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import "./Dashboard.css";
import { ChevronRight, Search, MapPin } from "lucide-react";
import { fetchMyVisits, getVisitById } from "../services/apiServices";
import StatusPill from "../components/StatusPill";
import VisitDetailsModal from "../components/VisitDetailsModal";
import { supabase } from "../services/supabaseClient";
import { AuthContext } from "../context/AuthContext";

const formatDate = (dateString) => {
  if (!dateString) return "-";
  const date = new Date(dateString);
  if (Number.isNaN(date.getTime())) return String(dateString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

const dummyVisits = [
  {
    id: "1",
    referenceNumber: "KK-BGC-2026-000190",
    condoName: "Uptown Residences",
    unitNumber: "12I",
    visitDate: "2026-03-18",
    status: "SCHEDULED",
  },
  {
    id: "2",
    referenceNumber: "KK-ONE-2026-000190",
    condoName: "One Archer's Place",
    unitNumber: "25L",
    visitDate: "2026-03-03",
    status: "CHECKED-OUT",
  },
  {
    id: "3",
    referenceNumber: "KK-BAS-2026-000190",
    condoName: "Baseline Center",
    unitNumber: "09A",
    visitDate: "2026-02-28",
    status: "CHECKED-OUT",
  },
  {
    id: "4",
    referenceNumber: "KK-BGC-2026-000191",
    condoName: "Uptown Residences",
    unitNumber: "12I",
    visitDate: "2026-02-23",
    status: "CANCELLED",
  },
  {
    id: "5",
    referenceNumber: "KK-BGC-2026-000192",
    condoName: "Uptown Residences",
    unitNumber: "12I",
    visitDate: "2026-02-05",
    status: "MISSED",
  },
];

const normalizeStatus = (status) => {
  if (!status) return "";
  return String(status).trim().toUpperCase();
};

export default function VisitorMyVisits() {
  const { user } = useContext(AuthContext);
  const [visits, setVisits] = useState(dummyVisits);
  const [loading, setLoading] = useState(false);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [activeFilter, setActiveFilter] = useState("all");

  const loadVisits = async () => {
    setLoading(true);
    try {
      const data = await fetchMyVisits();
      if (Array.isArray(data) && data.length > 0) {
        setVisits(data);
      } else {
        setVisits([]);
      }
    } catch (error) {
      console.error("[VisitorMyVisits] Error loading visits:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // Initial load
    loadVisits();
  }, []);

  // Supabase Realtime: refresh when this visitor's visits change
  useEffect(() => {
    if (!user || !user.id) return;

    const channel = supabase
      .channel("visitor-visits-channel")
      .on(
        "postgres_changes",
        {
          event: "INSERT",
          schema: "public",
          table: "visits",
          filter: `user_id=eq.${user.id}`,
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
          filter: `user_id=eq.${user.id}`,
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

  const filteredVisits = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();

    return visits.filter((visit) => {
      const code = normalizeStatus(visit.status);

      if (activeFilter !== "all") {
        switch (activeFilter) {
          case "scheduled":
            if (code !== "SCHEDULED") return false;
            break;
          case "checked-in":
            if (code !== "CHECKED-IN") return false;
            break;
          case "checked-out":
            if (code !== "CHECKED-OUT") return false;
            break;
          case "cancelled":
            if (code !== "CANCELLED") return false;
            break;
          case "missed":
            if (code !== "MISSED") return false;
            break;
          default:
            break;
        }
      }

      if (!term) return true;

      const haystack = [
        visit.referenceNumber,
        visit.condoName,
        visit.unitNumber,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return haystack.includes(term);
    });
  }, [visits, searchTerm, activeFilter]);

  const counts = useMemo(() => {
    const base = {
      total: visits.length,
      scheduled: 0,
      checkedIn: 0,
      checkedOut: 0,
      cancelled: 0,
      missed: 0,
    };

    visits.forEach((v) => {
      const code = normalizeStatus(v.status);
      if (code === "SCHEDULED") base.scheduled += 1;
      if (code === "CHECKED-IN") base.checkedIn += 1;
      if (code === "CHECKED-OUT") base.checkedOut += 1;
      if (code === "CANCELLED") base.cancelled += 1;
      if (code === "MISSED") base.missed += 1;
    });

    return base;
  }, [visits]);

  const handleRowClick = async (visit) => {
    try {
      const fullVisit = await getVisitById(visit.id || visit.visitId);
      setSelectedVisit(fullVisit);
      setDetailsOpen(true);
    } catch (error) {
      console.error("[VisitorMyVisits] Error fetching visit details:", error);
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role="visitor" />
      <div className="main-content">
        <TopBar title="My Visits" />
        <div className="content-inner">
          <div className="w-full max-w-5xl mx-auto space-y-6">
            {/* Search & Filters */}
            <div className="w-full space-y-3">
              <div className="flex items-center px-4 py-3 bg-white rounded-xl border border-gray-100 shadow-sm">
                <Search className="w-5 h-5 text-gray-400 mr-3" />
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search reference, condominium, or unit..."
                  className="w-full bg-transparent border-none outline-none text-sm text-gray-900 placeholder:text-gray-400"
                />
              </div>

              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => setActiveFilter("all")}
                  className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
                    activeFilter === "all"
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                  }`}
                >
                  All ({counts.total})
                </button>
                <button
                  type="button"
                  onClick={() => setActiveFilter("scheduled")}
                  className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
                    activeFilter === "scheduled"
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                  }`}
                >
                  Scheduled ({counts.scheduled})
                </button>
                <button
                  type="button"
                  onClick={() => setActiveFilter("checked-in")}
                  className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
                    activeFilter === "checked-in"
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                  }`}
                >
                  Checked-in ({counts.checkedIn})
                </button>
                <button
                  type="button"
                  onClick={() => setActiveFilter("checked-out")}
                  className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
                    activeFilter === "checked-out"
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                  }`}
                >
                  Checked-out ({counts.checkedOut})
                </button>
                <button
                  type="button"
                  onClick={() => setActiveFilter("cancelled")}
                  className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
                    activeFilter === "cancelled"
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                  }`}
                >
                  Cancelled ({counts.cancelled})
                </button>
                <button
                  type="button"
                  onClick={() => setActiveFilter("missed")}
                  className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
                    activeFilter === "missed"
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                  }`}
                >
                  Missed ({counts.missed})
                </button>
              </div>
            </div>

            {/* Visit Cards */}
            <div className="space-y-4">
              {loading ? (
                <div className="p-6 text-sm text-gray-400 text-center bg-white rounded-[24px] border border-gray-100 shadow-sm">
                  Loading your visits...
                </div>
              ) : filteredVisits.length === 0 ? (
                <div className="flex flex-col items-center justify-center bg-white rounded-[24px] border border-gray-100 shadow-sm py-16 px-6 text-center space-y-3">
                  <Search className="w-10 h-10 text-gray-300" />
                  <p className="text-sm font-medium text-gray-700">No visits found</p>
                </div>
              ) : (
                filteredVisits.map((visit) => (
                  <div
                    key={visit.id || visit.visitId}
                    className="visit-card bg-white border border-slate-200 rounded-2xl px-6 py-5 shadow-sm hover:shadow-md hover:border-slate-300 transition-all duration-200 cursor-pointer"
                    onClick={() => handleRowClick(visit)}
                  >
                    <div className="flex justify-between items-center gap-4">
                      {/* Left: condo name first, then reference, then unit/date */}
                      <div className="flex flex-col gap-1">
                        <div className="flex items-center gap-2 text-sm text-slate-900">
                          <MapPin className="w-4 h-4 text-slate-400" />
                          <span className="text-[15px] font-semibold">
                            {visit.condoName}
                          </span>
                        </div>
                        <p className="text-[13px] text-slate-400 font-mono">
                          {visit.referenceNumber}
                        </p>
                        <div className="flex items-center gap-2 text-[13px] text-slate-400">
                          <span>{visit.unitNumber}</span>
                          <span className="w-1 h-1 rounded-full bg-slate-300 inline-block" />
                          <span>{formatDate(visit.visitDate)}</span>
                        </div>
                      </div>

                      {/* Right: status + arrow */}
                      <div className="flex items-center gap-3 shrink-0">
                        <StatusPill status={visit.status} />
                        <ChevronRight className="w-4 h-4 text-slate-400" />
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Results Footer */}
            <p className="text-[13px] text-gray-400 mt-2 text-center w-full">
              Showing {filteredVisits.length} of {visits.length} visits
            </p>
          </div>
        </div>
      </div>

      <VisitDetailsModal
        isOpen={detailsOpen}
        onClose={() => setDetailsOpen(false)}
        visit={selectedVisit}
        onGenerateQR={() => {}}
        onEdit={() => {}}
        onCancelVisit={() => {}}
      />
    </div>
  );
}
