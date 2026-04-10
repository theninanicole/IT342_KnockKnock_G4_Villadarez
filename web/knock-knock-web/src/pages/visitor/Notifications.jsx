import React, { useContext, useMemo } from "react";
import { Bell } from "lucide-react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import { AuthContext } from "@store/AuthContext";
import NotificationItem from "@components/visitor/NotificationItem";
import { useNotifications } from "@hooks/useNotifications";
import { groupNotificationsByTime } from "@utils/notificationHelpers";

export default function VisitorNotifications() {
  const { user } = useContext(AuthContext);
  const {
    notifications,
    loading,
    error,
    markingAll,
    handleNotificationClick,
    handleMarkAllAsRead,
  } = useNotifications(user);

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
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role={user?.role || "VISITOR"} />
      <div className="flex flex-col flex-1">
        <TopBar title="Notifications" />
        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
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
                        {items.map((notif) => (
                          <NotificationItem
                            key={notif.notifId}
                            notif={notif}
                            onClick={handleNotificationClick}
                          />
                        ))}
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
