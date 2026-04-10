import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const loginUser = async (credentials) => {
  const response = await api.post("/auth/login", credentials);
  return response.data;
};

export const registerVisitor = async (userData) => {
  const response = await api.post("/auth/register/visitor", userData);
  return response.data;
};

export const registerAdmin = async (adminData) => {
  const response = await api.post("/auth/register/condo-admin", adminData);
  return response.data;
};

export const loginWithGoogle = async (idToken) => {
  const response = await api.post("/auth/oauth/google", { idToken });
  return response.data;
};

export const registerVisitorWithGoogle = async (idToken) => {
  const response = await api.post("/auth/register/visitor/google", { idToken });
  return response.data;
};

export const registerAdminWithGoogle = async ({ idToken, condoName, condoAddress, condoContact }) => {
  const response = await api.post("/auth/register/condo-admin/google", {
    idToken,
    condoName,
    condoAddress,
    condoContact,
  });
  return response.data;
};

export const getCurrentUser = async () => {
  const response = await api.get("/auth/me");
  return response.data;
};

export const updateUserProfile = async (profileData) => {
  const response = await api.put("/users/profile", profileData);
  return response.data;
};

export const changeUserPassword = async (passwordData) => {
  const response = await api.put("/users/change-password", passwordData);
  return response.data;
};

export const fetchCondos = async () => {
  try {
    const response = await api.get("/condos");
    return response.data;
  } catch (error) {
    console.error("Error fetching condos - Full error:", error);
    throw error;
  }
};

export const createVisit = async (visitData) => {
  try {
    const config = {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    };
    const response = await api.post("/visits", visitData, config);
    return response.data;
  } catch (error) {
    console.error("Error creating visit - Full error:", error);
    throw error;
  }
};

export const fetchVisits = async () => {
  try {
    const response = await api.get("/visits");
    console.log("Visits fetched:", response.data);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error fetching visits - Full error:", error);
    console.error("Error response data:", error.response?.data);
    return [];
  }
};

export const fetchMyVisits = async () => {
  try {
    const response = await api.get("/visits/my-visits");
    const data = response.data?.visits;
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("Error fetching my visits - Full error:", error);
    console.error("Error response data:", error.response?.data);
    return [];
  }
};

export const fetchCondoVisits = async (condoId) => {
  if (!condoId) {
    console.warn("fetchCondoVisits called without condoId");
    return [];
  }

  try {
    const response = await api.get(`/visits/condo/${condoId}`);
    console.log("Condo visits fetched:", response.data);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error fetching condo visits - Full error:", error);
    console.error("Error response data:", error.response?.data);
    return [];
  }
};

export const fetchAdminVisits = async (status) => {
  try {
    const params = {};
    if (status) {
      params.status = status;
    }

    const response = await api.get("/admin/visits", { params });
    const data = response.data?.visits;
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("Error fetching admin visits - Full error:", error);
    console.error("Error response data:", error.response?.data);
    return [];
  }
};

export const getVisitById = async (visitId) => {
  try {
    const response = await api.get(`/visits/${visitId}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching visit - Full error:", error);
    throw error;
  }
};

export const findVisitByReference = async (referenceNumber) => {
  if (!referenceNumber) return null;
  try {
    const response = await api.get(`/visits/reference/${encodeURIComponent(referenceNumber)}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching visit by reference - Full error:", error);
    throw error;
  }
};

export const checkInVisit = async (visitId) => {
  try {
    const response = await api.post(`/visits/${visitId}/check-in`);
    return response.data;
  } catch (error) {
    console.error("Error checking in visit - Full error:", error);
    throw error;
  }
};

export const checkOutVisit = async (visitId) => {
  try {
    const response = await api.post(`/visits/${visitId}/check-out`);
    return response.data;
  } catch (error) {
    console.error("Error checking out visit - Full error:", error);
    throw error;
  }
};

export const generateVisitQr = async (visitId) => {
  try {
    const response = await api.post(`/visits/${visitId}/qr`);
    return response.data;
  } catch (error) {
    console.error("Error generating visit QR - Full error:", error);
    throw error;
  }
};

export const fetchStatusHistory = async () => {
  try {
    const response = await api.get("/admin/visits-history");
    const data = response.data?.history;
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("Error fetching status history - Full error:", error);
    console.error("Error response data:", error.response?.data);
    return [];
  }
};

export const fetchNotifications = async ({ isRead, limit = 50 } = {}) => {
  try {
    const params = {};
    if (typeof isRead === "boolean") {
      params.isRead = isRead;
    }
    if (limit) {
      params.limit = limit;
    }

    const response = await api.get("/notifications", { params });
    const data = response.data?.notifications;
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("Error fetching notifications - Full error:", error);
    console.error("Error response data:", error.response?.data);
    return [];
  }
};

export const markNotificationRead = async (notifId) => {
  const response = await api.put(`/notifications/${notifId}/read`);
  return response.data;
};

export default api;
