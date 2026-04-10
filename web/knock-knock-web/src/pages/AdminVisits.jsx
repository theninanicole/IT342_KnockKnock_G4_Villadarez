import React, { useContext, useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import "./Dashboard.css";
import { fetchAdminVisits } from "../services/apiServices";
import StatusPill from "../components/StatusPill";
import { supabase } from "../services/supabaseClient";
import { AuthContext } from "../context/AuthContext";

const FILTERS = ["All", "Scheduled", "Checked-in", "Checked-out", "Cancelled"];

const statusParamMap = {
  "Scheduled": "scheduled",
  "Checked-in": "checked-in",
  "Checked-out": "checked-out",
  "Cancelled": "cancelled",
};

export default function AdminVisits() {
  const { user } = useContext(AuthContext);
  const [activeFilter, setActiveFilter] = useState("All");
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadVisits = async () => {
    try {
      setLoading(true);
      const statusParam = statusParamMap[activeFilter] || null;
      const data = await fetchAdminVisits(statusParam);
      setVisits(data);
    } catch (error) {
      console.error("Error loading admin visits:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVisits();
  }, [activeFilter]);

  // Supabase Realtime: refresh when visits for this admin's condo change
  useEffect(() => {
    const condoId = user?.condo?.condoId;
    if (!user || !condoId) return;

    const channel = supabase
      .channel("admin-visits-channel")
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
  }, [user, activeFilter]);

  const formatDate = (dateValue) => {
    if (!dateValue) return "-";
    try {
      const date = new Date(dateValue);
      if (Number.isNaN(date.getTime())) return String(dateValue);
      return date.toLocaleDateString("en-CA");
    } catch {
      return String(dateValue);
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role="ADMIN" />
      <div className="main-content">
        <TopBar title="Visits" />
        <div className="content-inner">
          <div className="w-full max-w-7xl mx-auto">
            <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
                <h1 className="text-2xl font-bold text-gray-900">Visits</h1>
                <div className="inline-flex items-center bg-gray-50/80 p-1 rounded-xl border border-gray-100">
                  {FILTERS.map((filter) => {
                    const isActive = activeFilter === filter;
                    return (
                      <button
                        key={filter}
                        type="button"
                        onClick={() => setActiveFilter(filter)}
                        className={`px-4 py-2 text-sm font-medium rounded-lg transition-all ${
                          isActive
                            ? "bg-white text-blue-600 shadow-sm"
                            : "text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
                        }`}
                      >
                        {filter}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="grid grid-cols-[2fr_1fr_1fr] gap-4 items-center text-sm font-semibold text-gray-500 pb-4 border-b border-gray-100">
                <div>Visitor</div>
                <div>Date</div>
                <div>Status</div>
              </div>

              {loading ? (
                <div className="py-8 text-sm text-gray-400 text-center">Loading visits...</div>
              ) : visits.length === 0 ? (
                <div className="py-8 text-sm text-gray-400 text-center">No visits found for this filter.</div>
              ) : (
                visits.map((visit) => (
                  <div
                    key={visit.visitId}
                    className="grid grid-cols-[2fr_1fr_1fr] gap-4 items-center py-4 border-b border-gray-50 last:border-0"
                  >
                    <div className="flex flex-col">
                      <span className="text-[15px] font-medium text-gray-900">
                        {visit.visitor?.fullName || "Unknown visitor"}
                      </span>
                      <span className="text-[13px] text-gray-400 mt-0.5">{visit.referenceNumber}</span>
                    </div>
                    <div className="text-[14px] text-gray-600">{formatDate(visit.visitDate)}</div>
                    <div>
                      <StatusPill status={visit.status} className="w-fit" />
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
