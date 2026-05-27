import { useCallback, useEffect, useState } from "react";
import { fetchAdminVisits } from "@lib/apiServices";
import { supabase } from "@lib/supabaseClient";

export const FILTERS = [
  "All",
  "Scheduled",
  "Checked-in",
  "Checked-out",
  "Cancelled",
  "Missed",
];

const statusParamMap = {
  Scheduled: "scheduled",
  "Checked-in": "checked-in",
  "Checked-out": "checked-out",
  Cancelled: "cancelled",
  Missed: "missed",
};

export const useAllVisitsList = (user) => {
  const [activeFilter, setActiveFilter] = useState("All");
  const [visits, setVisits] = useState([]);
  const [allVisits, setAllVisits] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadVisits = useCallback(async () => {
    try {
      setLoading(true);
      const allData = await fetchAdminVisits(null);
      setAllVisits(allData);
      const statusParam = statusParamMap[activeFilter] || null;
      const filteredData = statusParam ? await fetchAdminVisits(statusParam) : allData;
      setVisits(filteredData);
    } catch (error) {
      console.error("[useAllVisitsList] Error loading admin visits:", error);
    } finally {
      setLoading(false);
    }
  }, [activeFilter]);

  useEffect(() => {
    loadVisits();
  }, [loadVisits]);

  useEffect(() => {
    const condoId = user?.condo?.condoId;
    if (!user || !condoId) return;

    const channel = supabase
      .channel("admin-visits-channel")
      .on(
        "postgres_changes",
        {
          event: "INSERT",
          schema: "public",
          table: "visits",
          filter: `condo_id=eq.${condoId}`,
        },
        () => {
          loadVisits();
        }
      )
      .on(
        "postgres_changes",
        {
          event: "UPDATE",
          schema: "public",
          table: "visits",
          filter: `condo_id=eq.${condoId}`,
        },
        () => {
          loadVisits();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user, loadVisits]);

  return {
    activeFilter,
    setActiveFilter,
    visits,
    allVisits,
    loading,
    loadVisits,
  };
};
