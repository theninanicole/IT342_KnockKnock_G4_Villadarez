import React, { useContext, useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import NewVisitModal from "../components/NewVisitModal";
import VisitDetailsModal from "../components/VisitDetailsModal";
import EditVisitModal from "../components/EditVisitModal";
import StatusPill from "../components/StatusPill";
import { Plus, Files } from "lucide-react";
import "./Dashboard.css";
import { AuthContext } from "../context/AuthContext";
import { fetchVisits } from "../services/apiServices";
import { toast } from "react-toastify";
import { supabase } from "../services/supabaseClient";

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
};

const generateVisitId = (referenceNumber) => {
  return referenceNumber || "N/A";
};

export default function VisitorDashboard() {
  const { user } = useContext(AuthContext);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [visits, setVisits] = useState([]);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  const fetchVisitsList = async () => {
    try {
      const data = await fetchVisits();
      // Only keep visits whose date is today or in the future
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const upcomingVisits = data.filter((visit) => {
        if (!visit.visitDate) return false;
        const visitDate = new Date(visit.visitDate);
        visitDate.setHours(0, 0, 0, 0);
        return visitDate >= today;
      });

      // Sort upcoming visits by visitDate in ascending order (earliest first)
      const sortedVisits = upcomingVisits.sort((a, b) => {
        return new Date(a.visitDate) - new Date(b.visitDate);
      });

      setVisits(sortedVisits);
    } catch (error) {
      console.error("Error fetching visits:", error);
    }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchVisitsList();
  }, []);

  // Supabase Realtime: refresh upcoming visits on inserts/updates for this visitor
  useEffect(() => {
    if (!user || !user.id) return;

    const channel = supabase
      .channel("visitor-dashboard-visits-channel")
      .on(
        "postgres_changes",
        {
          event: "INSERT",
          schema: "public",
          table: "visits",
          filter: `user_id=eq.${user.id}`,
        },
        () => {
          fetchVisitsList();
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
          fetchVisitsList();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user]);

  const handleVisitCreated = () => {
    fetchVisitsList();
  };

  const handleViewDetails = (visit) => {
    console.log("View details for visit:", visit.visitId);
    setSelectedVisit(visit);
    setIsDetailsModalOpen(true);
  };

  const handleGenerateQR = async (visitId) => {
    try {
      console.log("[VisitorDashboard] Generating QR code for visit:", visitId);
      
      // Mock QR code generation - in real app, call backend API
      // const response = await api.post(`/visits/${visitId}/generate-qr`);
      // const updatedVisit = response.data;
      
      // For now, we'll use a mock QR code value
      const qrCodeValue = JSON.stringify({
        visitId,
        referenceNumber: selectedVisit?.referenceNumber,
        timestamp: new Date().toISOString(),
      });
      
      // Update the selected visit with QR code
      const updatedVisit = {
        ...selectedVisit,
        qrCode: qrCodeValue,
      };
      
      setSelectedVisit(updatedVisit);
      
      // Update visits list
      setVisits(visits.map(v => v.visitId === visitId ? updatedVisit : v));
      
      console.log("[VisitorDashboard] QR code generated successfully");
    } catch (error) {
      console.error("[VisitorDashboard] Error generating QR code:", error);
      toast.error("Failed to generate QR code");
      throw error;
    }
  };

  const handleEditVisit = (visit) => {
    console.log("[VisitorDashboard] Edit visit:", visit.visitId);

    const visitDateValue = visit.visitDate || visit.dateOfVisit;
    const visitDateObj = visitDateValue ? new Date(visitDateValue) : null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const isPast = visitDateObj ? visitDateObj < today : false;
    const isScheduled = visit.status === 'SCHEDULED';

    // Only allow editing if visit date is not past today and status is SCHEDULED
    if (!isScheduled || isPast) {
      toast.error("This visit can no longer be edited.");
      return;
    }

    setSelectedVisit(visit);
    setIsEditModalOpen(true);
    setIsDetailsModalOpen(false);
  };

  const handleVisitUpdated = () => {
    fetchVisitsList();
  };

  const handleCancelVisit = async (visitId) => {
    try {
      console.log("[VisitorDashboard] Cancelling visit:", visitId);
      
      // API call would go here
      // await api.patch(`/visits/${visitId}`, { status: 'CANCELLED' });
      
      // Update local state
      setVisits(visits.map(v => 
        v.visitId === visitId 
          ? { ...v, status: 'CANCELLED' }
          : v
      ));
      
      console.log("[VisitorDashboard] Visit cancelled successfully");
    } catch (error) {
      console.error("[VisitorDashboard] Error cancelling visit:", error);
      toast.error("Failed to cancel visit");
      throw error;
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role="visitor" />
      <div className="main-content">
        <TopBar title="Dashboard" />
        <div className="content-inner">
          <div className="welcome-card">
            <div className="welcome-text">
              <h1>Welcome, {(user?.fullName?.split(" ")[0]) || "User"}!</h1>
              <button 
                onClick={() => setIsModalOpen(true)}
                className="new-visit-btn">
                <span>+ New Visit</span>
              </button>
            </div>
            {/* SVG Illustration replaces emoji */}
            <div className="welcome-illustration">
              <Files size={120} strokeWidth={1} />
            </div>
          </div>

          <div className="mb-6">
            <h4 className="text-lg font-bold text-slate-900 mb-3">Upcoming Visits</h4>
            {visits.length === 0 ? (
              <div className="bg-slate-50 border border-dashed border-slate-200 rounded-2xl p-8 text-center">
                <p className="text-slate-400 text-sm">No upcoming visits found.</p>
              </div>
            ) : (
              <div className="flex flex-col gap-3">
                {visits.map((visit) => (
                  <div
                    key={visit.visitId}
                    onClick={() => handleViewDetails(visit)}
                    className="bg-white border border-slate-200 rounded-2xl px-6 py-5 shadow-sm hover:shadow-md hover:border-slate-300 transition-all duration-200 cursor-pointer"
                  >
                    <div className="flex justify-between items-center gap-4">
                      {/* Left */}
                      <div className="flex flex-col">
                        <h3 className="text-[15px] font-semibold text-slate-900 mb-1.5">
                          {visit.condo.name}
                        </h3>
                        <p className="text-[13px] text-slate-400 font-mono mb-1">
                          {generateVisitId(visit.referenceNumber)}
                        </p>
                        <p className="text-[13px] text-slate-400 flex items-center gap-1.5">
                          {visit.unitNumber}
                          <span className="w-1 h-1 rounded-full bg-slate-300 inline-block" />
                          {formatDate(visit.visitDate)}
                        </p>
                      </div>

                      {/* Right */}
                      <div className="flex items-center gap-3 shrink-0">
                        <StatusPill status={visit.status} />
                        <svg
                          className="w-4 h-4 text-slate-400"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          strokeWidth={2}
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                        </svg>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <NewVisitModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onVisitCreated={handleVisitCreated}
      />

      <VisitDetailsModal
        isOpen={isDetailsModalOpen}
        onClose={() => setIsDetailsModalOpen(false)}
        visit={selectedVisit}
        onGenerateQR={handleGenerateQR}
        onEdit={handleEditVisit}
        onCancelVisit={handleCancelVisit}
      />

      <EditVisitModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        visit={selectedVisit}
        onVisitUpdated={handleVisitUpdated}
      />
    </div>
  );
}