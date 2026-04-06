import React, { useContext, useEffect, useMemo, useState } from "react";
import { Bell, Check, X, AlertCircle } from "lucide-react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import "./Dashboard.css";
import { AuthContext } from "../context/AuthContext";
import { fetchNotifications, markNotificationRead } from "../services/apiServices";
import { supabase } from "../services/supabaseClient";

const groupNotificationsByTime = (notifications) => {
  const groups = {
    Today: [],
    "This Week": [],
    Older: [],
  };

  const now = new Date();

  notifications.forEach((notif) => {
    if (!notif.createdAt) {
      groups.Older.push(notif);
      return;
    }
    const date = new Date(notif.createdAt);
    if (Number.isNaN(date.getTime())) {
      groups.Older.push(notif);
      return;
    }

    const diffMs = now.getTime() - date.getTime();
    const diffDays = diffMs / (1000 * 60 * 60 * 24);

    const isSameDay =
      now.getFullYear() === date.getFullYear() &&
      now.getMonth() === date.getMonth() &&
      now.getDate() === date.getDate();

    if (isSameDay) {
      groups.Today.push(notif);
    } else if (diffDays < 7) {
      groups["This Week"].push(notif);
    } else {
      groups.Older.push(notif);
    }
  });

  return groups;
};

const formatRelativeTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const diffMs = Date.now() - date.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);

  if (diffSeconds < 60) return "\u00b7 just now";

  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `\u00b7 ${diffMinutes} min ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `\u00b7 ${diffHours} h ago`;

  const diffDays = Math.floor(diffHours / 24);
  return `\u00b7 ${diffDays} d ago`;
};

const getNotificationVisual = (notif) => {
  const text = `${notif.title || ""} ${notif.message || ""}`.toLowerCase();
  const type = (notif.type || "").toLowerCase();

  if (type === "status_update" || text.includes("check-in") || text.includes("checked-in")) {
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

export default function VisitorNotifications() {
  const { user } = useContext(AuthContext);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [markingAll, setMarkingAll] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        const data = await fetchNotifications({ limit: 50 });
        setNotifications(data);
      } catch (err) {
        console.error("Failed to load notifications", err);
        setError("Failed to load notifications");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  // Supabase Realtime: listen for new notifications for this visitor
  useEffect(() => {
    if (!user || !user.id) return;

    const channel = supabase
      .channel("visitor-notifications-channel")
      .on(
        "postgres_changes",
        {
          event: "INSERT",
          schema: "public",
          table: "notifications",
          filter: `user_id=eq.${user.id}`,
        },
        (payload) => {
          const row = payload?.new;
          if (!row) return;

          // Map snake_case from DB to camelCase used in frontend
          const mapped = {
            notifId: row.notif_id,
            type: row.type,
            title: row.title,
            message: row.message,
            isRead: row.is_read,
            createdAt: row.created_at,
          };

          setNotifications((prev) => [mapped, ...prev]);
        }
      )
      .subscribe((status) => {
        if (status === "SUBSCRIBED") {
          console.log("[VisitorNotifications] Subscribed to Supabase notifications channel");
        }
      });

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user]);

  const handleNotificationClick = async (notif) => {
    if (notif.isRead) return;

    setNotifications((prev) =>
      prev.map((n) => (n.notifId === notif.notifId ? { ...n, isRead: true } : n))
    );

    try {
      await markNotificationRead(notif.notifId);
    } catch (err) {
      console.error("Failed to mark notification as read", err);
      // Revert if API fails
      setNotifications((prev) =>
        prev.map((n) => (n.notifId === notif.notifId ? { ...n, isRead: false } : n))
      );
    }
  };

  const handleMarkAllAsRead = async () => {
    const unread = notifications.filter((n) => !n.isRead);
    if (unread.length === 0) return;

    setMarkingAll(true);
    // Optimistic update
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));

    try {
      await Promise.all(unread.map((n) => markNotificationRead(n.notifId)));
    } catch (err) {
      console.error("Failed to mark all notifications as read", err);
      // In case of error, reload from server on next visit; keep UI as read.
    } finally {
      setMarkingAll(false);
    }
  };

  const sortedNotifications = useMemo(() => {
    return [...notifications].sort((a, b) => {
      const da = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const db = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return db - da;
    });
  }, [notifications]);

  const grouped = useMemo(
    () => groupNotificationsByTime(sortedNotifications),
    [sortedNotifications]
  );

  const hasNotifications = sortedNotifications.length > 0;

  return (
    <div className="dashboard-layout">
      <Sidebar role={user?.role || "VISITOR"} />
      <div className="main-content">
        <TopBar title="Notifications" />
        <div className="content-inner">
          <div className="max-w-3xl mx-auto mt-4 sm:mt-8 pb-10">
            {loading ? (
              <p className="text-sm text-gray-500">Loading notifications...</p>
            ) : error ? (
              <p className="text-sm text-red-500">{error}</p>
            ) : !hasNotifications ? (
              <div className="bg-white rounded-[24px] border border-dashed border-gray-200 shadow-sm py-16 flex flex-col items-center justify-center text-center">
                <Bell className="w-16 h-16 text-gray-300 stroke-[1.2]" />
                <p className="mt-4 text-sm text-gray-500">No notifications</p>
              </div>
            ) : (
              <div className="bg-white rounded-[24px] border border-gray-100 shadow-sm p-6 sm:p-8">
                <div className="flex items-center justify-between gap-4">
                  <h2 className="text-[22px] font-bold text-gray-900">Notifications</h2>
                  <button
                    type="button"
                    onClick={handleMarkAllAsRead}
                    disabled={markingAll}
                    className="text-sm font-medium text-blue-600 hover:text-blue-700 hover:bg-blue-50 px-3 py-1.5 rounded-lg transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {markingAll ? "Marking..." : "Mark all as read"}
                  </button>
                </div>

                {(["Today", "This Week", "Older"]).map((groupKey) => {
                  const items = grouped[groupKey] || [];
                  if (items.length === 0) return null;

                  return (
                    <div key={groupKey} className="mt-8">
                      <div className="text-sm font-bold text-gray-900 mb-4">{groupKey}</div>
                      <div>
                        {items.map((notif) => {
                          const { bgClass, icon } = getNotificationVisual(notif);
                          return (
                            <div
                              key={notif.notifId}
                              onClick={() => handleNotificationClick(notif)}
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
                        })}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
