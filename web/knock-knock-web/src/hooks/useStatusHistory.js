import { useCallback, useEffect, useState } from "react";
import { fetchStatusHistory } from "@api/apiServices";
import { supabase } from "@api/supabaseClient";

export const useStatusHistory = (user) => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadHistory = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchStatusHistory();
      setHistory(data);
    } catch (error) {
      console.error("[useStatusHistory] Error loading status history:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  useEffect(() => {
    const condoId = user?.condo?.condoId;
    if (!user || !condoId) return;

    const channel = supabase
      .channel("admin-history-channel")
      .on(
        "postgres_changes",
        {
          event: "UPDATE",
          schema: "public",
          table: "visits",
          filter: `condo_id=eq.${condoId}`,
        },
        () => {
          loadHistory();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user, loadHistory]);

  return {
    history,
    loading,
    reloadHistory: loadHistory,
  };
};
