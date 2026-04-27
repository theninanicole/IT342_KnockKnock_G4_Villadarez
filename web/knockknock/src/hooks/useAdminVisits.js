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
          setTimeout(() => {
            loadVisits();
          }, 400);
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
          setTimeout(() => {
            loadVisits();
          }, 400);
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
          toast.error("No visit found for this reference number.");
          return;
        }
        // Check for past date
        const visitDate = new Date(visit.visitDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (visitDate < today) {
          toast.error("This visit is already past the scheduled date.");
          return;
        }
        // Check for condo mismatch (if available)
        if (user?.condo?.condoId && visit.condoId && visit.condoId !== user.condo.condoId) {
          toast.error("This visit does not belong to your condominium.");
          return;
        }
        if (visit.status !== "SCHEDULED") {
          toast.error("This visit cannot be verified right now.");
          return;
        }
        if (typeof onSuccess === "function") {
          onSuccess(visit);
        }
      } catch (error) {
        console.error("[useAdminVisits] Error searching visit by reference", error);
        const msg = error?.response?.data?.message?.toLowerCase() || "";
        if (msg.includes("condo") || msg.includes("not allowed")) {
          toast.error("This visit does not belong to your condominium.");
        } else if (msg.includes("past")) {
          toast.error("This visit is already past the scheduled date.");
        } else if (msg.includes("not found")) {
          toast.error("No visit found for this reference number.");
        } else {
          toast.error("You cannot access this visit.");
        }
      }
    },
    [searchTerm, user]
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
