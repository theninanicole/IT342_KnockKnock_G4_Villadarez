import React, { useState } from "react";
import { ChevronRight } from "lucide-react";
import StatusPill from "@components/ui/StatusPill";

import CheckOutModal from "./CheckOutModal";

export default function VisitorRow({ visit, type, onAction, onClick }) {
  const [showCheckOutModal, setShowCheckOutModal] = useState(false);
  const isClickable = true;

  const handleRowClick = () => {
    if (type === "checked-in") {
      setShowCheckOutModal(true);
    }
    if (typeof onClick === "function") {
      onClick(visit);
    }
  };

  const handleCheckOut = (v) => {
    setShowCheckOutModal(false);
    if (typeof onAction === "function") {
      onAction(v.visitId);
    }
  };

  const handleCloseModal = () => setShowCheckOutModal(false);

  return (
    <>
      <div
        className={`flex justify-between items-center py-4 border-b border-gray-50 last:border-0 last:pb-0 first:pt-0 ${
          isClickable ? "cursor-pointer" : ""
        }`}
        onClick={handleRowClick}
      >
        <div>
          <div className="text-[15px] font-medium text-gray-900">
            {visit.visitor?.fullName}
          </div>
          <div className="text-[13px] text-gray-400 font-mono mb-1 mt-0.5">
            {visit.referenceNumber}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <StatusPill status={visit.status} />
          <ChevronRight className="w-4 h-4 text-gray-300" />
        </div>
      </div>
      {showCheckOutModal && type === "checked-in" && (
        <CheckOutModal
          isOpen={showCheckOutModal}
          onClose={handleCloseModal}
          onCheckOut={handleCheckOut}
          visit={visit}
        />
      )}
    </>
  );
}
