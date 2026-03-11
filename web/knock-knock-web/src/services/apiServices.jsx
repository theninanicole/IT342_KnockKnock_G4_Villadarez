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

export default api;
