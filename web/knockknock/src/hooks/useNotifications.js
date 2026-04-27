import { useCallback, useEffect, useState } from "react";
import { fetchNotifications, markNotificationRead } from "@api/apiServices";
import { supabase } from "@api/supabaseClient";

export const useNotifications = (user) => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [markingAll, setMarkingAll] = useState(false);

  const loadNotifications = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await fetchNotifications({ limit: 50 });
      setNotifications(data);
    } catch (err) {
      console.error("[useNotifications] Failed to load notifications", err);
      setError("Failed to load notifications");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadNotifications();
  }, [loadNotifications]);

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
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user]);

  const handleNotificationClick = useCallback(
    async (notif) => {
      if (notif.isRead) return;

      setNotifications((prev) =>
        prev.map((n) => (n.notifId === notif.notifId ? { ...n, isRead: true } : n))
      );

      try {
        await markNotificationRead(notif.notifId);
      } catch (err) {
        console.error("[useNotifications] Failed to mark notification as read", err);
        setNotifications((prev) =>
          prev.map((n) => (n.notifId === notif.notifId ? { ...n, isRead: false } : n))
        );
      }
    },
    []
  );

  const handleMarkAllAsRead = useCallback(async () => {
    const unread = notifications.filter((n) => !n.isRead);
    if (unread.length === 0) return;

    setMarkingAll(true);
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));

    try {
      await Promise.all(unread.map((n) => markNotificationRead(n.notifId)));
    } catch (err) {
      console.error("[useNotifications] Failed to mark all notifications as read", err);
    } finally {
      setMarkingAll(false);
    }
  }, [notifications]);

  return {
    notifications,
    loading,
    error,
    markingAll,
    handleNotificationClick,
    handleMarkAllAsRead,
    reloadNotifications: loadNotifications,
  };
};
