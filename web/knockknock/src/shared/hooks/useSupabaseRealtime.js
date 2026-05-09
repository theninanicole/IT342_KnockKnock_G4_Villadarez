import { useEffect } from "react";

export const useSupabaseRealtime = (supabaseClient, { event, schema = "public", table, filter }, onPayload) => {
  useEffect(() => {
    if (!supabaseClient || !event || !table || !onPayload) return;

    const channel = supabaseClient
      .channel(`${table}-${event}-channel`)
      .on(
        "postgres_changes",
        {
          event,
          schema,
          table,
          filter,
        },
        onPayload
      )
      .subscribe();

    return () => {
      supabaseClient.removeChannel(channel);
    };
  }, [supabaseClient, event, schema, table, filter, onPayload]);
};
