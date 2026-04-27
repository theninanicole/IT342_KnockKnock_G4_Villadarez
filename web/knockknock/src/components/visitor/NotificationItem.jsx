import React from "react";
import { Bell, Check, X, AlertCircle } from "lucide-react";
import { formatRelativeTime } from "@utils/notificationHelpers";

const getNotificationVisual = (notif) => {
  const text = `${notif.title || ""} ${notif.message || ""}`.toLowerCase();
  const type = (notif.type || "").toLowerCase();

  if (
    type === "status_update" ||
    text.includes("check-in") ||
    text.includes("checked-in")
  ) {
    return {
      bgClass: "bg-emerald-50",
      icon: <Check size={18} className="text-emerald-600" />,
    };
  }

  if (text.includes("cancel") || text.includes("denied") || text.includes("rejected")) {
    return {
      bgClass: "bg-rose-50",
      icon: <X size={18} className="text-rose-600" />,
    };
  }

  if (type === "system") {
    return {
      bgClass: "bg-sky-50",
      icon: <AlertCircle size={18} className="text-sky-600" />,
    };
  }

  return {
    bgClass: "bg-gray-100",
    icon: <Bell size={18} className="text-gray-500" />,
  };
};

export default function NotificationItem({ notif, onClick }) {
  const { bgClass, icon } = getNotificationVisual(notif);

  const handleClick = () => {
    if (typeof onClick === "function") {
      onClick(notif);
    }
  };

  return (
    <div
      onClick={handleClick}
      className="flex items-start gap-4 py-4 border-b border-gray-50 last:border-0 cursor-pointer hover:bg-gray-50/50 transition-colors -mx-4 px-4 rounded-xl"
    >
      <div className="flex items-start">
        <div
          className={`w-2 h-2 rounded-full mt-3 mr-2 shrink-0 ${
            notif.isRead ? "bg-transparent" : "bg-red-500"
          }`}
        />
        <div
          className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${bgClass}`}
        >
          {icon}
        </div>
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-baseline gap-2 flex-wrap">
          <p className="text-[14px] font-semibold text-blue-600 truncate">
            {notif.title}
          </p>
          <span className="text-[12px] text-gray-400">
            {formatRelativeTime(notif.createdAt)}
          </span>
        </div>
        <p className="mt-0.5 text-[14px] text-gray-600 leading-snug">
          {notif.message}
        </p>
      </div>
    </div>
  );
}
