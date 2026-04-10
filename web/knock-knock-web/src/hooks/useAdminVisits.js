import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchCondoVisits, findVisitByReference, checkOutVisit } from "@api/apiServices";
import { toast } from "react-toastify";
import { supabase } from "@api/supabaseClient";
import { getTodayKey, getVisitDateKey } from "@utils/dateUtils";

const resolveCondoId = (user) => {
  return (
    user?.condo?.condoId ||
    user?.condoId ||
    (user?.condo && user.condo.id) ||
    null
  );
};

export const useAdminVisits = (user) => {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const condoId = resolveCondoId(user);

  const loadVisits = useCallback(async () => {
    if (!condoId) {
      console.warn("[useAdminVisits] No condoId available; skipping condo visits fetch");
      return;
    }
    setLoading(true);
    try {
      const data = await fetchCondoVisits(condoId);
      setVisits(data);
    } catch (error) {
      console.error("[useAdminVisits] Error loading condo visits", error);
    } finally {
      setLoading(false);
    }
  }, [condoId]);

  useEffect(() => {
    if (!user || !condoId) return;

    loadVisits();
  }, [user, condoId, loadVisits]);

  useEffect(() => {
    if (!user || !condoId) return;

    const channel = supabase
      .channel("admin-dashboard-visits-channel")
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
  }, [user, condoId, loadVisits]);

  const handleCheckOut = useCallback(
    async (visitId) => {
      try {
        await checkOutVisit(visitId);
        toast.success("Visitor checked out successfully");
        await loadVisits();
      } catch (error) {
        console.error("[useAdminVisits] Error checking out visit:", error);
        const message =
          error?.response?.data?.message ||
          error?.response?.data?.error ||
          "Failed to check out visitor";
        toast.error(message);
      }
    },
    [loadVisits]
  );

  const handleSearchVerify = useCallback(
    async (onSuccess, referenceOverride) => {
      const reference = (referenceOverride ?? searchTerm)?.trim();
      if (!reference) return;
      try {
        const visit = await findVisitByReference(reference);
        if (!visit) {
          alert("Visit not found for that reference number");
          return;
        }
        if (visit.status !== "SCHEDULED") {
          alert("Visit is not in SCHEDULED status");
          return;
        }
        if (typeof onSuccess === "function") {
          onSuccess(visit);
        }
      } catch (error) {
        console.error("[useAdminVisits] Error searching visit by reference", error);
        alert("Visit not found or you do not have access to it");
      }
    },
    [searchTerm]
  );

  const currentlyCheckedIn = useMemo(() => {
    return visits.filter((visit) => visit.status === "CHECKED-IN");
  }, [visits]);

  const todaysVisitors = useMemo(() => {
    const todayKey = getTodayKey();
    return visits.filter((visit) => {
      const visitKey = getVisitDateKey(visit.visitDate);
      if (!visitKey || !todayKey || visitKey !== todayKey) return false;
      if (visit.status === "CHECKED-IN") return false;
      return true;
    });
  }, [visits]);

  return {
    loading,
    searchTerm,
    setSearchTerm,
    currentlyCheckedIn,
    todaysVisitors,
    reloadVisits: loadVisits,
    handleCheckOut,
    handleSearchVerify,
  };
};
