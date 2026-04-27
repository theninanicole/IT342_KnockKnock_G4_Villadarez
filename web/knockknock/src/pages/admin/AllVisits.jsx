import React, { useContext } from "react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import { AuthContext } from "@store/AuthContext";
import VisitFilterBar from "@components/admin/VisitFilterBar";
import AllVisitRow from "@components/admin/AllVisitRow";
import { useAllVisitsList } from "@hooks/useAllVisitsList";

export default function AdminVisits() {
  const { user } = useContext(AuthContext);
  const { activeFilter, setActiveFilter, visits, loading } = useAllVisitsList(user);

  return (
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role="ADMIN" />
      <div className="flex flex-col flex-1">
        <TopBar title="Visits" />
        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
          <div className="w-full max-w-7xl mx-auto">
            <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-end gap-4 mb-6">
                <VisitFilterBar
                  activeFilter={activeFilter}
                  onFilterChange={setActiveFilter}
                />
              </div>

              <div className="grid grid-cols-[2fr_1fr_1fr] gap-4 items-center text-sm font-semibold text-gray-500 pb-4 border-b border-gray-100">
                <div>Visitor</div>
                <div>Date</div>
                <div>Status</div>
              </div>

              {loading ? (
                <div className="py-8 text-sm text-gray-400 text-center">Loading visits...</div>
              ) : visits.length === 0 ? (
                <div className="py-8 text-sm text-gray-400 text-center">No visits found</div>
              ) : (
                visits.map((visit) => (
                  <AllVisitRow key={visit.visitId} visit={visit} />
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
