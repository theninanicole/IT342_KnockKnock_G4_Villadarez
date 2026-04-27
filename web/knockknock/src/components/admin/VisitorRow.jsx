import React from "react";
import { ChevronRight } from "lucide-react";
import StatusPill from "@components/shared/StatusPill";

export default function VisitorRow({ visit, type, onAction, onClick }) {
  const isClickable = typeof onClick === "function";

  const handleRowClick = () => {
    if (isClickable) {
      onClick(visit);
    }
  };

  const handleActionClick = (e) => {
    e.stopPropagation();
    if (typeof onAction === "function") {
      onAction(visit.visitId);
    }
  };

  return (
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

      {type === "checked-in" && (
        <button
          className="bg-gray-100 hover:bg-gray-200 text-gray-600 px-5 py-1.5 rounded-full text-xs font-medium transition-colors"
          onClick={handleActionClick}
        >
          Check-Out
        </button>
      )}

      {type === "scheduled" && (
        <div className="flex items-center gap-4">
          <StatusPill status={visit.status} />
          <ChevronRight className="w-4 h-4 text-gray-300" />
        </div>
      )}
    </div>
  );
}
