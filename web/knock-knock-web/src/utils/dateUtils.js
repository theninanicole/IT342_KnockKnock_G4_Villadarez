export const getTodayKey = () => {
  try {
    return new Date().toLocaleDateString("en-CA"); // e.g. 2026-03-30
  } catch {
    return null;
  }
};

export const getVisitDateKey = (visitDate) => {
  if (!visitDate) return null;
  if (typeof visitDate === "string") {
    return visitDate.slice(0, 10);
  }
  if (Array.isArray(visitDate) && visitDate.length >= 3) {
    const [year, month, day] = visitDate;
    const mm = String(month).padStart(2, "0");
    const dd = String(day).padStart(2, "0");
    return `${year}-${mm}-${dd}`;
  }
  return null;
};

export const formatDate = (dateValue) => {
  if (!dateValue) return "-";
  try {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) return String(dateValue);
    return date.toLocaleDateString("en-CA");
  } catch {
    return String(dateValue);
  }
};

export const formatTimestamp = (isoString) => {
  if (!isoString) return "-";
  try {
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return String(isoString);
    const datePart = date.toLocaleDateString("en-CA");
    const timePart = date.toLocaleTimeString("en-US", {
      hour: "numeric",
      minute: "2-digit",
      second: "2-digit",
      hour12: true,
    });
    return `${datePart}, ${timePart}`;
  } catch {
    return String(isoString);
  }
};

export const formatJoinedDate = (value) => {
  if (!value) return "—";
  try {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  } catch {
    return String(value);
  }
};
