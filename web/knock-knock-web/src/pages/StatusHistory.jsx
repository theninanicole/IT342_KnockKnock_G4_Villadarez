import React, { useContext, useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import "./Dashboard.css";
import { ChevronRight } from "lucide-react";
import { fetchStatusHistory } from "../services/apiServices";
import StatusPill from "../components/StatusPill";
import { supabase } from "../services/supabaseClient";
import { AuthContext } from "../context/AuthContext";

const formatTimestamp = (isoString) => {
  if (!isoString) return "-";
  try {
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return String(isoString);
    const datePart = date.toLocaleDateString("en-CA");
    const timePart = date.toLocaleTimeString("en-US", {
      hour: "numeric",
      minute: "2-digit",
      second: "2-digit",
      hour12: true,
    });
    return `${datePart}, ${timePart}`;
  } catch {
    return String(isoString);
  }
};

const parseTransition = (transition) => {
  if (!transition || typeof transition !== "string") {
    return { from: null, to: null };
  }

  let parts = transition.split("→");
  if (parts.length !== 2) {
    parts = transition.split("->");
  }
  if (parts.length !== 2) {
    return { from: transition, to: null };
  }

  return { from: parts[0].trim(), to: parts[1].trim() };
};

export default function StatusHistory() {
  const { user } = useContext(AuthContext);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const data = await fetchStatusHistory();
      setHistory(data);
    } catch (error) {
      console.error("Error loading status history:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  // Supabase Realtime: refresh history when visits for this admin's condo are updated
  useEffect(() => {
    const condoId = user?.condo?.condoId;
    if (!user || !condoId) return;

    const channel = supabase
      .channel("admin-history-channel")
      .on(
        "postgres_changes",
        {
          event: "UPDATE",
          schema: "public",
          table: "visits",
          filter: `condo_id=eq.${condoId}`,
        },
        () => {
          loadHistory();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user]);

  return (
    <div className="dashboard-layout">
      <Sidebar role="ADMIN" />
      <div className="main-content">
        <TopBar title="Status History" />
        <div className="content-inner">
          <div className="w-full max-w-7xl mx-auto space-y-8">
            <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">
              <h1 className="text-2xl font-bold text-gray-900 mb-8">Status History</h1>

              <div className="grid grid-cols-[2fr_2fr_1fr] gap-4 items-center text-sm font-semibold text-gray-500 pb-4 border-b border-gray-100">
                <div>Visitor</div>
                <div>Status Change</div>
                <div>Timestamp</div>
              </div>

              {loading ? (
                <div className="py-8 text-sm text-gray-400 text-center">Loading status history...</div>
              ) : history.length === 0 ? (
                <div className="py-8 text-sm text-gray-400 text-center">No status history records found.</div>
              ) : (
                history.map((item, index) => {
                  const { from, to } = parseTransition(item.transition);

                  return (
                    <div
                      key={index}
                      className="grid grid-cols-[2fr_2fr_1fr] gap-4 items-center py-4 border-b border-gray-50 last:border-0"
                    >
                      <div className="flex flex-col">
                        <span className="text-[15px] font-medium text-gray-900">
                          {item.visitorName || "Unknown visitor"}
                        </span>
                        <span className="text-[13px] text-gray-400 mt-0.5">
                          {item.referenceNumber}
                        </span>
                      </div>

                      <div className="flex items-center gap-2">
                        {from && (
                          <StatusPill status={from} />
                        )}
                        {from && to && (
                          <ChevronRight className="w-4 h-4 text-gray-300" />
                        )}
                        {to && (
                          <StatusPill status={to} />
                        )}
                      </div>

                      <div className="text-[14px] text-gray-600">
                        {formatTimestamp(item.timestamp)}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
