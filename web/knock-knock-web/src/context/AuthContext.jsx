/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect } from "react";
import { setSupabaseSession, clearSupabaseSession } from "../services/supabaseClient";
import { getCurrentUser } from "../services/apiServices";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    if (typeof window === "undefined") return null;
    try {
      const storedUser = window.localStorage.getItem("user");
      return storedUser ? JSON.parse(storedUser) : null;
    } catch {
      return null;
    }
  });

  // Restore Supabase session on app startup if user is already logged in
  useEffect(() => {
    const restoreSupabaseSession = async () => {
      if (typeof window === "undefined") return;
      
      const token = window.localStorage.getItem("token");
      if (token) {
        console.log("[AuthProvider] Restoring Supabase session from localStorage");
        const sessionSet = await setSupabaseSession(token);
        if (sessionSet) {
          console.log("[AuthProvider] Supabase session restored successfully");
        } else {
          console.warn("[AuthProvider] Failed to restore Supabase session");
        }
      }
    };

    restoreSupabaseSession();
  }, []);

  // Ensure we always have the latest user details (including condo for admins)
  useEffect(() => {
    const ensureFullUser = async () => {
      if (typeof window === "undefined") return;

      const token = window.localStorage.getItem("token");
      if (!token) return;

      // If user is missing or admin without condo details, refresh from backend
      if (!user || (user.role === "CONDOMINIUM_ADMIN" && !user.condo)) {
        try {
          console.log("[AuthProvider] Refreshing current user from /auth/me");
          const data = await getCurrentUser();
          const apiUser = data.user || data;

          const enrichedUser = {
            id: apiUser.id,
            email: apiUser.email,
            fullName: apiUser.fullName,
            role: apiUser.role,
            contactNumber: apiUser.contactNumber,
            authProvider: apiUser.authProvider,
            condo: apiUser.condo || null,
          };

          setUser(enrichedUser);
          window.localStorage.setItem("user", JSON.stringify(enrichedUser));
        } catch (error) {
          console.error("[AuthProvider] Failed to refresh current user", error);
        }
      }
    };

    ensureFullUser();
  }, [user]);

  const login = async (userData, jwtToken) => {
    try {
      // Set user data
      setUser(userData);
      if (typeof window !== "undefined") {
        window.localStorage.setItem("user", JSON.stringify(userData));
        if (jwtToken) {
          window.localStorage.setItem("token", jwtToken);
        }
      }

      // Set Supabase session for RLS policies to work
      if (jwtToken) {
        const sessionSet = await setSupabaseSession(jwtToken);
        if (sessionSet) {
          console.log("[AuthContext] Supabase session set successfully");
        } else {
          console.warn("[AuthContext] Failed to set Supabase session - file operations may fail");
        }
      }
    } catch (error) {
      console.error("[AuthContext] Login error:", error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      setUser(null);
      if (typeof window !== "undefined") {
        window.localStorage.removeItem("user");
        window.localStorage.removeItem("token");
      }

      // Clear Supabase session
      await clearSupabaseSession();
      console.log("[AuthContext] Logged out successfully");
    } catch (error) {
      console.error("[AuthContext] Logout error:", error);
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};