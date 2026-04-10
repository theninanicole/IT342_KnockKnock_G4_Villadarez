import { createClient } from "@supabase/supabase-js";

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabaseKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_DEFAULT_KEY;

if (!supabaseUrl || !supabaseKey) {
  console.error("Missing Supabase credentials in environment variables");
}

export const supabase = createClient(supabaseUrl, supabaseKey);

/**
 * Set the user's authentication JWT on the Supabase client
 * This is required for RLS policies to work with file deletions
 * The token will be sent in the Authorization header for all Supabase requests
 * @param {string} accessToken - JWT access token from your backend
 */
export const setSupabaseSession = async (_accessToken) => {
  void _accessToken;
  // You are not using Supabase Auth (only Storage with public policies),
  // so there is no real Supabase session to set. We keep this function
  // as a no-op to avoid runtime errors from supabase.auth.setSession.
  console.log("[setSupabaseSession] Skipping Supabase auth session setup (not used in this app)");
  return true;
};

/**
 * Alternative: Create authenticated Supabase client with explicit token
 * Use this if setSession doesn't work with your backend JWT
 */
export const createAuthenticatedSupabaseClient = (accessToken) => {
  if (!accessToken) {
    console.warn("[createAuthenticatedSupabaseClient] No token provided, returning default client");
    return supabase;
  }

  console.log("[createAuthenticatedSupabaseClient] Creating client with explicit token");
  
  // Create a new client instance that will include the token in all requests
  return createClient(supabaseUrl, supabaseKey, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
};

/**
 * Clear the user's authentication session
 * Call this on logout
 */
export const clearSupabaseSession = async () => {
  // No Supabase Auth session is used, so nothing to clear.
  console.log("[clearSupabaseSession] No Supabase auth session to clear");
};
