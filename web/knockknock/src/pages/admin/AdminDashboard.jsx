import React, { useContext, useState } from "react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import { AuthContext } from "@store/AuthContext";
import AdminVisitDetailsModal from "@components/admin/AdminVisitDetailsModal";
import VerificationCard from "@components/admin/VerificationCard";
import VisitorListSection from "@components/admin/VisitorListSection";
import VisitorRow from "@components/admin/VisitorRow";
import QRScanner from "@components/admin/QRScanner";
import { useAdminVisits } from "@hooks/useAdminVisits";

export default function AdminDashboard() {
  const { user } = useContext(AuthContext);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [qrOpen, setQrOpen] = useState(false);
  const {
    loading,
    searchTerm,
    setSearchTerm,
    currentlyCheckedIn,
    todaysVisitors,
    reloadVisits,
    handleCheckOut,
    handleSearchVerify,
  } = useAdminVisits(user);
  
  const openDetailsForVisit = (visit) => {
    setQrOpen(false);
    setSelectedVisit(visit);
    setDetailsOpen(true);
  };

  // Prevent multiple triggers from QR scan
  const qrScanHandledRef = React.useRef(false);
  const handleQrScanSuccess = (decodedText) => {
    if (qrScanHandledRef.current) return;
    qrScanHandledRef.current = true;
    setQrOpen(false); // Close QR scanner immediately
    setSearchTerm(decodedText);
    handleSearchVerify(openDetailsForVisit, decodedText);
  };

  return (
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role="ADMIN" />
      <div className="flex flex-col flex-1">
        <TopBar title="Dashboard" />

        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
          <div className="w-full max-w-7xl mx-auto space-y-8">
            
            {/* Verification Card */}
            <VerificationCard
              searchTerm={searchTerm}
              onSearchChange={setSearchTerm}
              onVerify={() => handleSearchVerify(openDetailsForVisit, searchTerm)}
              onQrClick={() => setQrOpen(true)}
            />

            {/* Currently Checked-In Section */}
            <VisitorListSection
              title="Currently Checked-In"
              indicatorColorClass="bg-[#10B981]"
              loading={loading}
              items={currentlyCheckedIn}
              emptyMessage="No visitors are currently checked-in."
              renderItem={(visit) => (
                <VisitorRow
                  visit={visit}
                  type="checked-in"
                  onAction={handleCheckOut}
                />
              )}
            />

            {/* Today's Visitors Section */}
            <VisitorListSection
              title="Today's Visitors"
              indicatorColorClass="bg-blue-600"
              loading={loading}
              items={todaysVisitors}
              emptyMessage="No visitors scheduled for today."
              renderItem={(visit) => (
                <VisitorRow
                  visit={visit}
                  type="scheduled"
                  onClick={openDetailsForVisit}
                />
              )}
            />

          </div>
        </div>
      </div>
      <AdminVisitDetailsModal
        isOpen={detailsOpen}
        onClose={() => {
          setDetailsOpen(false);
          setQrOpen(false);
          setTimeout(() => { qrScanHandledRef.current = false; }, 300);
        }}
        visit={selectedVisit}
        onCheckedIn={async () => {
          try {
            await reloadVisits();
          } catch (error) {
            console.error("Error refreshing visits after check-in", error);
          }
        }}
      />
      {qrOpen && (
        <QRScanner
          onScanSuccess={handleQrScanSuccess}
          onClose={() => {
            setQrOpen(false);
            // Reset QR scan handled state so scanner can be used again
            setTimeout(() => { qrScanHandledRef.current = false; }, 300);
          }}
        />
      )}
    </div>
  );
}