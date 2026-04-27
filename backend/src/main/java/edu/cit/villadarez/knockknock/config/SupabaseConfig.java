package edu.cit.villadarez.knockknock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage-bucket:kk_files}")
    private String storageBucket;

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    public String getStorageBucket() {
        return storageBucket;
    }
}
