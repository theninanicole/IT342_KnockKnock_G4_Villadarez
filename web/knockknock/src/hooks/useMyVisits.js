import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchMyVisits } from "@api/apiServices";
import { supabase } from "@api/supabaseClient";
import { useSupabaseRealtime } from "./useSupabaseRealtime";
import { normalizeStatus } from "@utils/visitHelpers";

export const useMyVisits = (user) => {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [activeFilter, setActiveFilter] = useState("all");

  const loadVisits = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchMyVisits();
      if (Array.isArray(data) && data.length > 0) {
        setVisits(data);
      } else {
        setVisits([]);
      }
    } catch (error) {
      console.error("[useMyVisits] Error loading visits:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadVisits();
  }, [loadVisits]);

  // Real-time updates for visits (INSERT/UPDATE)
  const userId = user?.id;
  useSupabaseRealtime(
    userId ? supabase : null,
    {
      event: "INSERT",
      table: "visits",
      filter: userId ? `user_id=eq.${userId}` : undefined,
    },
    loadVisits
  );
  useSupabaseRealtime(
    userId ? supabase : null,
    {
      event: "UPDATE",
      table: "visits",
      filter: userId ? `user_id=eq.${userId}` : undefined,
    },
    loadVisits
  );

  const filteredVisits = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();

    return visits.filter((visit) => {
      const code = normalizeStatus(visit.status);

      if (activeFilter !== "all") {
        switch (activeFilter) {
          case "scheduled":
            if (code !== "SCHEDULED") return false;
            break;
          case "checked-in":
            if (code !== "CHECKED-IN") return false;
            break;
          case "checked-out":
            if (code !== "CHECKED-OUT") return false;
            break;
          case "cancelled":
            if (code !== "CANCELLED") return false;
            break;
          case "missed":
            if (code !== "MISSED") return false;
            break;
          default:
            break;
        }
      }

      if (!term) return true;

      const haystack = [
        visit.referenceNumber,
        visit.condoName,
        visit.unitNumber,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return haystack.includes(term);
    });
  }, [visits, searchTerm, activeFilter]);

  const counts = useMemo(() => {
    const base = {
      total: visits.length,
      scheduled: 0,
      checkedIn: 0,
      checkedOut: 0,
      cancelled: 0,
      missed: 0,
    };

    visits.forEach((v) => {
      const code = normalizeStatus(v.status);
      if (code === "SCHEDULED") base.scheduled += 1;
      if (code === "CHECKED-IN") base.checkedIn += 1;
      if (code === "CHECKED-OUT") base.checkedOut += 1;
      if (code === "CANCELLED") base.cancelled += 1;
      if (code === "MISSED") base.missed += 1;
    });

    return base;
  }, [visits]);

  return {
    visits,
    loading,
    searchTerm,
    setSearchTerm,
    activeFilter,
    setActiveFilter,
    filteredVisits,
    counts,
    loadVisits,
  };
};
