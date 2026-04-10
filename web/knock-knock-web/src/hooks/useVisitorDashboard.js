import { useState } from "react";
import { useAuth } from "@hooks/useAuth";
import { useUpcomingVisits } from "@hooks/useUpcomingVisits";
import { toast } from "react-toastify";

// Layer 2: "Brain" for the Visitor Dashboard page
export const useVisitorDashboard = () => {
  const { user } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedVisit, setSelectedVisit] = useState(null);

  const { visits, setVisits, refreshVisits } = useUpcomingVisits(user);

  const handleOpenNewVisitModal = () => {
    setIsModalOpen(true);
  };

  const handleCloseNewVisitModal = () => {
    setIsModalOpen(false);
  };

  const handleVisitCreated = () => {
    refreshVisits();
    setIsModalOpen(false);
  };

  const handleViewDetails = (visit) => {
    console.log("View details for visit:", visit.visitId);
    setSelectedVisit(visit);
    setIsDetailsModalOpen(true);
  };

  const handleCloseDetailsModal = () => {
    setIsDetailsModalOpen(false);
  };

  const handleGenerateQR = async (visitId) => {
    try {
      console.log("[VisitorDashboard] Generating QR code for visit:", visitId);

      const qrCodeValue = JSON.stringify({
        visitId,
        referenceNumber: selectedVisit?.referenceNumber,
        timestamp: new Date().toISOString(),
      });

      const updatedVisit = {
        ...selectedVisit,
        qrCode: qrCodeValue,
      };

      setSelectedVisit(updatedVisit);

      setVisits((prev) =>
        prev.map((v) => (v.visitId === visitId ? updatedVisit : v))
      );

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
    const isScheduled = visit.status === "SCHEDULED";

    if (!isScheduled || isPast) {
      toast.error("This visit can no longer be edited.");
      return;
    }

    setSelectedVisit(visit);
    setIsEditModalOpen(true);
    setIsDetailsModalOpen(false);
  };

  const handleVisitUpdated = () => {
    refreshVisits();
    setIsEditModalOpen(false);
  };

  const handleCloseEditModal = () => {
    setIsEditModalOpen(false);
  };

  const handleCancelVisit = async (visitId) => {
    try {
      console.log("[VisitorDashboard] Cancelling visit:", visitId);

      setVisits((prev) =>
        prev.map((v) =>
          v.visitId === visitId ? { ...v, status: "CANCELLED" } : v
        )
      );

      console.log("[VisitorDashboard] Visit cancelled successfully");
    } catch (error) {
      console.error("[VisitorDashboard] Error cancelling visit:", error);
      toast.error("Failed to cancel visit");
      throw error;
    }
  };

  return {
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
  };
};
