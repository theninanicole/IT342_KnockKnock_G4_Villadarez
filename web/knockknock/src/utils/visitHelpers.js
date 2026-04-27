export const normalizeStatus = (status) => {
  if (!status) return "";
  return String(status).trim().toUpperCase();
};
