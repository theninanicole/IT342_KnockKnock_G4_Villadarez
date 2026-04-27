import { fetchVisits } from "@api/apiServices";

// Layer 1: Pure data fetching for visits
// This keeps API details in one place and reusable
export const getAllVisits = async () => {
  return await fetchVisits();
};
