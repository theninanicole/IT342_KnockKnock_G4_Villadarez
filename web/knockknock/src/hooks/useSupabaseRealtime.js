import { useEffect } from "react";

// Generic hook to subscribe to a Supabase realtime channel
// Example usage: useSupabaseRealtime(supabase, { table: 'visits', event: 'INSERT', filter: `user_id=eq.${user.id}` }, onPayload)
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
