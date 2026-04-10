import { useState, useEffect, useCallback } from "react";
import { getAllVisits } from "@services/visitService";
import { supabase } from "@api/supabaseClient";
import { useSupabaseRealtime } from "./useSupabaseRealtime";

export const useUpcomingVisits = (user) => {
  const [visits, setVisits] = useState([]);

  const fetchVisitsList = useCallback(async () => {
    try {
      const data = await getAllVisits();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const upcomingVisits = data.filter((visit) => {
        if (!visit.visitDate) return false;
        const visitDate = new Date(visit.visitDate);
        visitDate.setHours(0, 0, 0, 0);
        return visitDate >= today;
      });

      const sortedVisits = upcomingVisits.sort((a, b) => {
        return new Date(a.visitDate) - new Date(b.visitDate);
      });

      setVisits(sortedVisits);
    } catch (error) {
      console.error("Error fetching visits:", error);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchVisitsList();
  }, [fetchVisitsList]);

  const userId = user?.id;

  const handleRealtimeChange = useCallback(() => {
    fetchVisitsList();
  }, [fetchVisitsList]);

  useSupabaseRealtime(
    userId ? supabase : null,
    {
      event: "INSERT",
      table: "visits",
      filter: userId ? `user_id=eq.${userId}` : undefined,
    },
    handleRealtimeChange
  );

  useSupabaseRealtime(
    userId ? supabase : null,
    {
      event: "UPDATE",
      table: "visits",
      filter: userId ? `user_id=eq.${userId}` : undefined,
    },
    handleRealtimeChange
  );

  return { visits, setVisits, refreshVisits: fetchVisitsList };
};
