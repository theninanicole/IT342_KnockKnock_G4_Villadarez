export const groupNotificationsByTime = (notifications) => {
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

export const formatRelativeTime = (value) => {
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
