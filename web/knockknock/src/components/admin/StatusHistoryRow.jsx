import React from "react";
import { ChevronRight } from "lucide-react";
import StatusPill from "@components/shared/StatusPill";
import { parseTransition } from "@utils/historyHelpers";
import { formatTimestamp } from "@utils/dateUtils";

export default function StatusHistoryRow({ item }) {
  const { from, to } = parseTransition(item.transition);

  return (
    <div className="grid grid-cols-[2fr_2fr_1fr] gap-4 items-center py-4 border-b border-gray-50 last:border-0">
      <div className="flex flex-col">
        <span className="text-[15px] font-medium text-gray-900">
          {item.visitorName || "Unknown visitor"}
        </span>
        <span className="text-[13px] text-gray-400 mt-0.5">
          {item.referenceNumber}
        </span>
      </div>

      <div className="flex items-center gap-2">
        {from && <StatusPill status={from} />}
        {from && to && <ChevronRight className="w-4 h-4 text-gray-300" />}
        {to && <StatusPill status={to} />}
      </div>

      <div className="text-[14px] text-gray-600">
        {formatTimestamp(item.timestamp)}
      </div>
    </div>
  );
}
