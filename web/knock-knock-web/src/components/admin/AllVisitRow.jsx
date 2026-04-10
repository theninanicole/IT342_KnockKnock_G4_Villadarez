import React from "react";
import StatusPill from "@components/shared/StatusPill";
import { formatDate } from "@utils/dateUtils";

export default function AllVisitRow({ visit }) {
  return (
    <div className="grid grid-cols-[2fr_1fr_1fr] gap-4 items-center py-4 border-b border-gray-50 last:border-0">
      <div className="flex flex-col">
        <span className="text-[15px] font-medium text-gray-900">
          {visit.visitor?.fullName || "Unknown visitor"}
        </span>
        <span className="text-[13px] text-gray-400 mt-0.5">
          {visit.referenceNumber}
        </span>
      </div>
      <div className="text-[14px] text-gray-600">{formatDate(visit.visitDate)}</div>
      <div>
        <StatusPill status={visit.status} className="w-fit" />
      </div>
    </div>
  );
}
