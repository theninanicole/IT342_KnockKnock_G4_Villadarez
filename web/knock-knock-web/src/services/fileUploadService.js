import { supabase } from "./supabaseClient";
import api from "./apiServices";

const BUCKET_NAME = "kk_files";
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
const ALLOWED_FILE_TYPES = [
  "image/jpeg",
  "image/png",
  "image/jpg",
  "application/pdf",
];

// Validate file before upload
export const validateFile = (file) => {
  if (!file) {
    throw new Error("No file selected");
  }

  if (file.size > MAX_FILE_SIZE) {
    throw new Error(`File size exceeds ${MAX_FILE_SIZE / 1024 / 1024}MB limit`);
  }

  if (!ALLOWED_FILE_TYPES.includes(file.type)) {
    throw new Error("File type not allowed. Use JPEG, PNG, or PDF");
  }

  return true;
};

const sanitizeFileName = (name) => {
  if (!name || typeof name !== "string") return "file";

  // Strip any path information (just in case)
  const justName = name.split("/").pop().split("\\").pop();

  const lastDotIndex = justName.lastIndexOf(".");
  const base = lastDotIndex > 0 ? justName.slice(0, lastDotIndex) : justName;
  const ext = lastDotIndex > 0 ? justName.slice(lastDotIndex) : "";

  // Replace anything that's not alphanumeric, dash or underscore with an underscore
  const safeBase = base.replace(/[^a-zA-Z0-9-_]/g, "_");

  return `${safeBase}${ext}` || "file";
};

// Upload file directly to Supabase Storage
// Returns: { path, url, fileName, fileType }
export const uploadFileToSupabase = async (file, visitId) => {
  try {
    validateFile(file);

    const timestamp = Date.now();
    const safeName = sanitizeFileName(file.name);
    const fileName = `${timestamp}_${safeName}`;
    const filePath = `visitors_id/${visitId}/${fileName}`;

    console.log("[uploadFileToSupabase] Uploading to path:", filePath);

    const { data, error } = await supabase.storage
      .from(BUCKET_NAME)
      .upload(filePath, file, {
        cacheControl: "3600",
        upsert: false,
      });

    if (error) {
      console.error("[uploadFileToSupabase] Upload error:", error);
      throw new Error(`Upload failed: ${error.message}`);
    }

    console.log("[uploadFileToSupabase] File uploaded successfully:", data);

    const { data: publicUrlData } = supabase.storage
      .from(BUCKET_NAME)
      .getPublicUrl(filePath);

    const publicUrl = publicUrlData.publicUrl;
    console.log("[uploadFileToSupabase] Public URL:", publicUrl);

    return {
      path: filePath,
      url: publicUrl,
      fileName: file.name,
      fileType: file.type,
    };
  } catch (error) {
    console.error("[uploadFileToSupabase] Error:", error);
    throw error;
  }
};

// Save file metadata to backend database (visit_files row)
export const saveFileMetadata = async (fileMetadata) => {
  try {
    console.log("[saveFileMetadata] Saving metadata:", fileMetadata);
    const response = await api.post("/visits/files", fileMetadata);
    console.log("[saveFileMetadata] Metadata saved:", response.data);
    return response.data;
  } catch (error) {
    console.error("[saveFileMetadata] Error:", error);
    throw error;
  }
};

// Complete upload workflow: upload to Supabase then save metadata
export const uploadFile = async (file, visitId) => {
  try {
    console.log("[uploadFile] Starting file upload for visit:", visitId);

    const supabaseData = await uploadFileToSupabase(file, visitId);

    const metadataPayload = {
      visitId,
      filePath: supabaseData.path,
      fileUrl: supabaseData.url,
      fileName: supabaseData.fileName,
      fileType: supabaseData.fileType,
    };

    const backendData = await saveFileMetadata(metadataPayload);

    console.log("[uploadFile] Upload complete");
    return {
      supabase: supabaseData,
      backend: backendData,
    };
  } catch (error) {
    console.error("[uploadFile] Upload failed:", error);
    throw error;
  }
};

// Low-level helper: delete directly from Supabase Storage (not used by UI)
export const deleteFileFromSupabase = async (filePath) => {
  try {
    if (!filePath || typeof filePath !== "string") {
      throw new Error("Invalid filePath: must be a non-empty string");
    }

    if (filePath.startsWith("/")) {
      console.warn("[deleteFileFromSupabase] filePath starts with '/':", filePath);
    }

    if (filePath.includes("http://") || filePath.includes("https://")) {
      throw new Error(
        "Invalid filePath: use the storage path only, not a full URL"
      );
    }

    console.log("[deleteFileFromSupabase] Deleting from bucket:", BUCKET_NAME);
    console.log("[deleteFileFromSupabase] File path:", filePath);

    const { data, error } = await supabase.storage
      .from(BUCKET_NAME)
      .remove([filePath]);

    console.log("[deleteFileFromSupabase] Supabase response:", { data, error });

    if (error) {
      console.error("[deleteFileFromSupabase] Delete error:", error);
      throw new Error(`Delete failed: ${error.message}`);
    }

    console.log(
      "[deleteFileFromSupabase] File deleted successfully from Supabase Storage"
    );
  } catch (error) {
    console.error("[deleteFileFromSupabase] Fatal error:", error);
    throw error;
  }
};

// High-level delete: mirror upload flow in frontend
// 1) Delete from Supabase Storage using supabase-js
// 2) Delete metadata row from visit_files via backend
export const deleteFileComplete = async (filePath, fileId) => {
  const result = {
    supabaseDeleted: false,
    backendDeleted: false,
  };

  console.log("[deleteFileComplete] Starting full file deletion workflow");
  console.log("[deleteFileComplete] FilePath:", filePath);
  console.log("[deleteFileComplete] FileID:", fileId);

  if (!filePath || typeof filePath !== "string") {
    throw new Error("Missing or invalid filePath for deletion");
  }
  if (!fileId) {
    throw new Error("Missing fileId for deletion");
  }

  // Step 1: delete from Supabase Storage
  try {
    await deleteFileFromSupabase(filePath);
    result.supabaseDeleted = true;
    console.log("[deleteFileComplete] Supabase deletion successful");
  } catch (error) {
    console.error("[deleteFileComplete] Supabase deletion failed:", error);
    throw new Error(`Failed to delete file from storage: ${error.message}`);
  }

  // Step 2: delete DB row via backend
  try {
    console.log("[deleteFileComplete] Calling DELETE /visits/files/" + fileId);
    const response = await api.delete(`/visits/files/${fileId}`);
    console.log("[deleteFileComplete] Backend status:", response.status);
    console.log("[deleteFileComplete] Backend data:", response.data);
    result.backendDeleted = true;
    console.log("[deleteFileComplete] Backend metadata deletion successful");
  } catch (error) {
    console.error("[deleteFileComplete] Backend deletion failed:", error);
    const backendMessage = error?.response?.data || error.message;
    throw new Error(`Failed to delete file metadata: ${backendMessage}`);
  }

  console.log("[deleteFileComplete] File deletion workflow complete", result);
  return result;
};

