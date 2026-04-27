import React from "react";
import { ChevronRight, MapPin } from "lucide-react";
import StatusPill from "@components/shared/StatusPill";
import { formatDate } from "@utils/dateUtils";

export default function VisitCard({ visit, onClick }) {
  if (!visit) return null;

  const handleClick = () => {
    if (typeof onClick === "function") {
      onClick(visit);
    }
  };

  return (
    <div
      className="visit-card bg-white border border-slate-200 rounded-2xl px-6 py-5 shadow-sm hover:shadow-md hover:border-slate-300 transition-all duration-200 cursor-pointer"
      onClick={handleClick}
    >
      <div className="flex justify-between items-center gap-4">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2 text-sm text-slate-900">
            <MapPin className="w-4 h-4 text-slate-400" />
            <span className="text-[15px] font-semibold">
              {visit.condoName || visit.condo?.name}
            </span>
          </div>
          <p className="text-[13px] text-slate-400 font-mono">
            {visit.referenceNumber || "N/A"}
          </p>
          <div className="flex items-center gap-2 text-[13px] text-slate-400">
            <span>{visit.unitNumber}</span>
            <span className="w-1 h-1 rounded-full bg-slate-300 inline-block" />
            <span>{visit.visitDate ? formatDate(visit.visitDate) : "No date"}</span>
          </div>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          <StatusPill status={visit.status} />
          <ChevronRight className="w-4 h-4 text-slate-400" />
        </div>
      </div>
    </div>
  );
}
