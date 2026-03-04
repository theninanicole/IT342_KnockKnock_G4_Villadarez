import axios from "axios";

const API_URL = "http://localhost:8080/api/auth";

export const loginUser = async (credentials) => {
  const response = await axios.post(`${API_URL}/login`, credentials);
  return response.data;
};

export const registerVisitor = async (userData) => {
  const response = await axios.post(`${API_URL}/register/visitor`, userData);
  return response.data;
};

export const registerAdmin = async (adminData) => {
  const response = await axios.post(`${API_URL}/register/condo-admin`, adminData);
  return response.data;
};
