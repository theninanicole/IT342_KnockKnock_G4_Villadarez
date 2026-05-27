import React from "react";
import { CheckCircle2, Clock, XCircle, AlertCircle, Zap } from "lucide-react";

export default function AllVisitsStatsCard({ visits }) {
  const totalVisits = visits.length;
  const scheduledCount = visits.filter((v) => v.status?.toLowerCase() === "scheduled").length;
  const checkedInCount = visits.filter((v) => v.status?.toLowerCase() === "checked-in").length;
  const checkedOutCount = visits.filter((v) => v.status?.toLowerCase() === "checked-out").length;
  const cancelledCount = visits.filter((v) => v.status?.toLowerCase() === "cancelled").length;
  const missedCount = visits.filter((v) => v.status?.toLowerCase() === "missed").length;

  const stats = [
    {
      label: "Total Visits",
      value: totalVisits,
      icon: <Zap size={20} className="text-blue-600" />,
      bgColor: "bg-white",
    },
    {
      label: "Scheduled",
      value: scheduledCount,
      icon: <Clock size={20} className="text-yellow-600" />,
      bgColor: "bg-white",
    },
    {
      label: "Checked-in",
      value: checkedInCount,
      icon: <CheckCircle2 size={20} className="text-green-600" />,
      bgColor: "bg-white",
    },
    {
      label: "Checked-out",
      value: checkedOutCount,
      icon: <CheckCircle2 size={20} className="text-slate-600" />,
      bgColor: "bg-white",
    },
    {
      label: "Cancelled",
      value: cancelledCount,
      icon: <XCircle size={20} className="text-red-600" />,
      bgColor: "bg-white",
    },
    {
      label: "Missed",
      value: missedCount,
      icon: <AlertCircle size={20} className="text-orange-600" />,
      bgColor: "bg-white",
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
      {stats.map((stat, index) => (
        <div
          key={index}
          className={`${stat.bgColor} rounded-xl border border-gray-100 p-5 shadow-sm`}
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">{stat.label}</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">{stat.value}</p>
            </div>
            <div className="p-3 bg-white rounded-lg border border-gray-100">
              {stat.icon}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
