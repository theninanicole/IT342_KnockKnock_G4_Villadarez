import React, { useContext, useState } from "react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import { Search } from "lucide-react";
import { getVisitById, generateVisitQr } from "@api/apiServices";
import VisitDetailsModal from "@components/shared/VisitDetailsModal";
import EditVisitModal from "@components/visitor/EditVisitModal";
import { AuthContext } from "@store/AuthContext";
import { useMyVisits } from "@hooks/useMyVisits";
import VisitFilterGroup from "@components/visitor/VisitFilterGroup";
import VisitCard from "@components/shared/VisitCard";
import SearchInput from "@components/shared/SearchInput";
import { toast } from "react-toastify";

export default function VisitorMyVisits() {
  const { user } = useContext(AuthContext);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editVisit, setEditVisit] = useState(null);
  const {
    visits,
    loading,
    searchTerm,
    setSearchTerm,
    activeFilter,
    setActiveFilter,
    filteredVisits,
    counts,
    loadVisits,
  } = useMyVisits(user);

  const handleRowClick = async (visit) => {
    try {
      const fullVisit = await getVisitById(visit.id || visit.visitId);
      setSelectedVisit(fullVisit);
      setDetailsOpen(true);
    } catch (error) {
      console.error("[VisitorMyVisits] Error fetching visit details:", error);
    }
  };

  const handleGenerateQr = async (visitId) => {
    try {
      const updatedVisit = await generateVisitQr(visitId);
      setSelectedVisit(updatedVisit);
      toast.success("QR code generated successfully!");
    } catch (error) {
      console.error("[VisitorMyVisits] Error generating QR code:", error);
      toast.error("Failed to generate QR code");
    }
  };

  // Handler to open edit modal from details modal
  const handleEditVisit = (visit) => {
    setEditVisit(visit);
    setEditModalOpen(true);
  };

  // Handler after successful update
  const handleVisitUpdated = async () => {
    setEditModalOpen(false);
    // Fetch the updated visit and update selectedVisit for immediate UI feedback
    if (editVisit?.visitId) {
      const updated = await getVisitById(editVisit.visitId);
      if (updated) setSelectedVisit(updated);
    }
    setDetailsOpen(false);
    // Add a small delay to ensure backend updates propagate
    setTimeout(() => {
      loadVisits();
    }, 400);
  };

  return (
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role="visitor" />
      <div className="flex flex-col flex-1">
        <TopBar title="My Visits" />
        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
          <div className="w-full max-w-5xl mx-auto space-y-6">
            {/* Search & Filters */}
            <div className="w-full space-y-3">
              <SearchInput
                value={searchTerm}
                onChange={setSearchTerm}
                placeholder="Search reference, condominium, or unit..."
                className="flex items-center px-4 py-3 bg-gray-50/80 rounded-xl border border-gray-100 shadow-sm transition-colors focus-within:bg-white focus-within:border-gray-300"
              />

              <VisitFilterGroup
                activeFilter={activeFilter}
                counts={counts}
                onFilterChange={setActiveFilter}
              />
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
                  <VisitCard
                    key={visit.id || visit.visitId}
                    visit={visit}
                    onClick={handleRowClick}
                  />
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
        onGenerateQR={handleGenerateQr}
        onEdit={handleEditVisit}
        onCancelVisit={() => {}}
      />

      <EditVisitModal
        isOpen={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        visit={editVisit}
        onVisitUpdated={handleVisitUpdated}
      />
    </div>
  );
}
