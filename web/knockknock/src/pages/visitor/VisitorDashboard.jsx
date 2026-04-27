import React from "react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import NewVisitModal from "@components/visitor/NewVisitModal";
import VisitDetailsModal from "@components/shared/VisitDetailsModal";
import EditVisitModal from "@components/visitor/EditVisitModal";
import VisitCard from "@components/shared/VisitCard";
import WelcomeCard from "@components/visitor/WelcomeCard";
import { Files } from "lucide-react";
import { useVisitorDashboard } from "@hooks/useVisitorDashboard";

export default function VisitorDashboard() {
  const {
    user,
    visits,
    selectedVisit,
    isModalOpen,
    isDetailsModalOpen,
    isEditModalOpen,
    handleOpenNewVisitModal,
    handleCloseNewVisitModal,
    handleVisitCreated,
    handleViewDetails,
    handleCloseDetailsModal,
    handleGenerateQR,
    handleEditVisit,
    handleVisitUpdated,
    handleCloseEditModal,
    handleCancelVisit,
  } = useVisitorDashboard();

  return (
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role="visitor" />
      <div className="flex flex-col flex-1">
        <TopBar title="Dashboard" />
        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
          <WelcomeCard
            title={`Welcome, ${(user?.fullName?.split(" ")[0]) || "User"}!`}
            primaryActionLabel="+ New Visit"
            onPrimaryAction={handleOpenNewVisitModal}
            icon={<Files size={120} strokeWidth={1} />}
          />

          <div className="mb-6">
            <h4 className="text-lg font-bold text-slate-900 mb-3">Upcoming Visits</h4>
            {visits.length === 0 ? (
              <div className="bg-slate-50 border border-dashed border-slate-200 rounded-2xl p-8 text-center">
                <p className="text-slate-400 text-sm">No upcoming visits found.</p>
              </div>
            ) : (
              <div className="flex flex-col gap-3">
                {visits.map((visit) => (
                  <VisitCard
                    key={visit.visitId}
                    visit={visit}
                    onClick={() => handleViewDetails(visit)}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <NewVisitModal 
        isOpen={isModalOpen}
        onClose={handleCloseNewVisitModal}
        onVisitCreated={handleVisitCreated}
      />

      <VisitDetailsModal
        isOpen={isDetailsModalOpen}
        onClose={handleCloseDetailsModal}
        visit={selectedVisit}
        onGenerateQR={handleGenerateQR}
        onEdit={handleEditVisit}
        onCancelVisit={handleCancelVisit}
      />

      <EditVisitModal
        isOpen={isEditModalOpen}
        onClose={handleCloseEditModal}
        visit={selectedVisit}
        onVisitUpdated={handleVisitUpdated}
      />
    </div>
  );
}