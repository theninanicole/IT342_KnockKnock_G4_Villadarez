import React, { useContext } from "react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import { AuthContext } from "@store/AuthContext";
import StatusHistoryRow from "@components/admin/StatusHistoryRow";
import { useStatusHistory } from "@hooks/useStatusHistory";

export default function StatusHistory() {
  const { user } = useContext(AuthContext);
  const { history, loading } = useStatusHistory(user);

  return (
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role="ADMIN" />
      <div className="flex flex-col flex-1">
        <TopBar title="Status History" />
        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
          <div className="w-full max-w-7xl mx-auto space-y-8">
            <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">

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
                history.map((item, index) => (
                  <StatusHistoryRow key={index} item={item} />
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
