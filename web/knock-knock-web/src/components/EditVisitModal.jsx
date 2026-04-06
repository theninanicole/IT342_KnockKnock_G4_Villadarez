import React, { useState, useEffect } from "react";
import { X, FileText } from "lucide-react";
import { toast } from "react-toastify";
import api from "../services/apiServices";
import { uploadFileToSupabase, saveFileMetadata, deleteFileComplete } from "../services/fileUploadService";

/**
 * EditVisitModal - Edit existing visit details
 * Props:
 *   - isOpen: boolean - Controls modal visibility
 *   - onClose: function - Called when modal should close
 *   - visit: object - Visit data to pre-fill form
 *   - onVisitUpdated: function - Called after successful API update
 */
export default function EditVisitModal({ isOpen, onClose, visit, onVisitUpdated }) {
  const [unitNumber, setUnitNumber] = useState("");
  const [purpose, setPurpose] = useState("");
  const [visitDate, setVisitDate] = useState("");
  const [idFile, setIdFile] = useState(null);
  const [deletedFileIds, setDeletedFileIds] = useState([]);
  const [deletingFileIds, setDeletingFileIds] = useState(new Set());
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [files, setFiles] = useState([]);

  // Fetch files when modal opens
  useEffect(() => {
    const fetchVisitFiles = async (visitId) => {
      try {
        console.log("[EditVisitModal] Fetching files for visit:", visitId);
        const response = await api.get(`/visits/files/visit/${visitId}`);
        console.log("[EditVisitModal] Files response:", response.data);
        setFiles(response.data || []);
      } catch (error) {
        console.error("[EditVisitModal] Error fetching files:", error);
        setFiles([]);
      }
    };

    if (isOpen && visit?.visitId) {
      fetchVisitFiles(visit.visitId);
    } else {
      setFiles([]);
    }
  }, [isOpen, visit?.visitId]);

  // Pre-fill form when modal opens
  useEffect(() => {
    if (isOpen && visit) {
      setUnitNumber(visit.unitNumber || "");
      setPurpose(visit.purpose || visit.purposeOfVisit || "");
      setIdFile(null);
      setDeletedFileIds([]);
      setDeletingFileIds(new Set());

      // Format date for HTML date input (YYYY-MM-DD)
      if (visit.visitDate || visit.dateOfVisit) {
        const dateObj = new Date(visit.visitDate || visit.dateOfVisit);
        const formattedDate = dateObj.toISOString().split("T")[0];
        setVisitDate(formattedDate);
      }
    }
  }, [isOpen, visit]);

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setIdFile(file);
    }
  };

  const handleDeleteFile = async (file) => {
    // Confirm deletion
    if (!window.confirm(`Delete "${file.fileName}"? This cannot be undone.`)) {
      return;
    }

    const fileId = file.fileId;
    const filePath = file.filePath;

    try {
      // Mark as deleting
      setDeletingFileIds((prev) => new Set(prev).add(fileId));
      console.log("[EditVisitModal] Starting file deletion");
      console.log("[EditVisitModal] File ID:", fileId);
      console.log("[EditVisitModal] File path:", filePath);

      // Delete from Supabase Storage and backend database
      try {
        const result = await deleteFileComplete(filePath, fileId);
        console.log("[EditVisitModal] File deletion result:", result);
      } catch (deleteError) {
        console.error("[EditVisitModal] Error during file deletion:", deleteError);
        toast.error("Failed to delete file: " + deleteError.message);
        setDeletingFileIds((prev) => {
          const next = new Set(prev);
          next.delete(fileId);
          return next;
        });
        return;
      }

      // Mark as deleted in UI state (remove from display)
      setDeletedFileIds((prev) => [...prev, fileId]);
      setDeletingFileIds((prev) => {
        const next = new Set(prev);
        next.delete(fileId);
        return next;
      });

      toast.success("File deleted successfully");
    } catch (error) {
      console.error("[EditVisitModal] Unexpected error deleting file:", error);
      setDeletingFileIds((prev) => {
        const next = new Set(prev);
        next.delete(fileId);
        return next;
      });
      toast.error("Failed to delete file");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!unitNumber.trim()) {
      toast.error("Unit number is required");
      return;
    }

    if (!purpose.trim()) {
      toast.error("Purpose is required");
      return;
    }

    if (!visitDate) {
      toast.error("Visit date is required");
      return;
    }

    setIsSubmitting(true);
    try {
      // Note: Files are already deleted from Supabase & backend when user clicked delete button
      // No need for additional backend delete calls here
      
      // Upload new file to Supabase if provided
      if (idFile) {
        try {
          console.log("[EditVisitModal] Uploading new file to Supabase:", idFile.name);
          const uploadResult = await uploadFileToSupabase(idFile, visit.visitId);
          console.log("[EditVisitModal] File uploaded to Supabase:", uploadResult);

          // Save file metadata to backend
          await saveFileMetadata({
            visitId: visit.visitId,
            filePath: uploadResult.path,
            fileUrl: uploadResult.url,
            fileName: uploadResult.fileName,
            fileType: uploadResult.fileType,
          });
          console.log("[EditVisitModal] File metadata saved");
        } catch (error) {
          console.error("[EditVisitModal] Error uploading file:", error);
          toast.error("Failed to upload file: " + error.message);
          setIsSubmitting(false);
          return;
        }
      }

      // Update visit details
      console.log("[EditVisitModal] Updating visit details:", visit.visitId);
      await api.put(`/visits/${visit.visitId}`, {
        unitNumber: unitNumber.trim(),
        purpose: purpose.trim(),
        visitDate: visitDate,
      });

      console.log("[EditVisitModal] Visit updated successfully");
      toast.success("Visit updated successfully!");
      onVisitUpdated();
      onClose();
    } catch (error) {
      console.error("[EditVisitModal] Error updating visit:", error);
      console.error("[EditVisitModal] Error response:", error.response?.data);
      toast.error(
        error.response?.data?.message || "Failed to update visit"
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen || !visit) return null;

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[420px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="flex justify-between items-center px-6 pt-6 pb-4 flex-shrink-0">
          <h2 className="text-xl font-semibold" style={{ color: "#2663EB" }}>
            Edit Visit
          </h2>
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="text-gray-400 hover:text-gray-900 transition-colors p-1.5 rounded-full hover:bg-gray-100 disabled:opacity-50"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <form
          onSubmit={handleSubmit}
          className="px-6 pb-2 space-y-4 overflow-y-auto flex-1"
        >
          
          {/* Condominium (Read-Only) */}
          <div>
            <label className="text-sm font-medium text-gray-500 mb-1.5 block">
              Condominium
            </label>
            <input
              type="text"
              value={visit.condo?.name || visit.condominium || ""}
              disabled
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200/80 rounded-xl text-sm text-gray-500 cursor-not-allowed"
            />
          </div>

          {/* Unit Number */}
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              Unit Number
            </label>
            <input
              type="text"
              value={unitNumber}
              onChange={(e) => setUnitNumber(e.target.value)}
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 focus:outline-none focus:ring-4 transition-all disabled:bg-gray-50 disabled:text-gray-500"
              style={{
                "--tw-ring-color": "rgb(38, 99, 235, 0.1)",
              }}
              onFocus={(e) => {
                e.target.style.borderColor = "#2663EB";
                e.target.style.boxShadow = "0 0 0 4px rgba(38, 99, 235, 0.1)";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "";
                e.target.style.boxShadow = "";
              }}
              placeholder="Enter unit number"
            />
          </div>

          {/* Purpose */}
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              Purpose of Visit
            </label>
            <textarea
              value={purpose}
              onChange={(e) => setPurpose(e.target.value)}
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 focus:outline-none focus:ring-4 transition-all disabled:bg-gray-50 disabled:text-gray-500 resize-none"
              rows="3"
              onFocus={(e) => {
                e.target.style.borderColor = "#2663EB";
                e.target.style.boxShadow = "0 0 0 4px rgba(38, 99, 235, 0.1)";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "";
                e.target.style.boxShadow = "";
              }}
              placeholder="Enter purpose of visit"
            />
          </div>

          {/* Visit Date */}
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              Date of Visit
            </label>
            <input
              type="date"
              value={visitDate}
              onChange={(e) => setVisitDate(e.target.value)}
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 focus:outline-none focus:ring-4 transition-all disabled:bg-gray-50 disabled:text-gray-500"
              onFocus={(e) => {
                e.target.style.borderColor = "#2663EB";
                e.target.style.boxShadow = "0 0 0 4px rgba(38, 99, 235, 0.1)";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "";
                e.target.style.boxShadow = "";
              }}
            />
          </div>

          {/* ID Document (Editable) */}
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              ID Document
            </label>
            <div className="space-y-3">
              {/* Display existing files */}
              {files &&
                files.filter(
                  (file) => !deletedFileIds.includes(file.fileId)
                ).length > 0 && (
                  <div className="space-y-2">
                    {files
                      .filter((file) => !deletedFileIds.includes(file.fileId))
                      .map((file) => (
                        <div
                          key={file.fileId}
                          className="flex items-center justify-between p-4 bg-white border border-gray-200 rounded-2xl group hover:shadow-sm transition-shadow"
                        >
                          <div className="flex items-center gap-3 overflow-hidden flex-1">
                            <div className="p-2.5 bg-blue-100 text-blue-600 rounded-lg flex-shrink-0">
                              <FileText className="w-5 h-5" />
                            </div>
                            <span className="text-sm font-medium text-gray-900 truncate">
                              {file.fileName || "Document"}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 ml-3 flex-shrink-0">
                            <button
                              type="button"
                              onClick={() => window.open(file.fileUrl, "_blank")}
                              className="px-3 py-1.5 text-sm text-gray-500 hover:text-blue-600 font-medium transition-colors"
                            >
                              View
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteFile(file)}
                              disabled={deletingFileIds.has(file.fileId) || isSubmitting}
                              className="text-gray-300 hover:text-red-600 transition-colors p-1 rounded-full hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent"
                              title="Delete file"
                            >
                              {deletingFileIds.has(file.fileId) ? (
                                <div className="w-4 h-4 border-2 border-gray-300 border-t-red-600 rounded-full animate-spin" />
                              ) : (
                                <X className="w-4 h-4" />
                              )}
                            </button>
                          </div>
                        </div>
                      ))}
                  </div>
                )}

              {/* File upload input - only show if no existing files */}
              {!files || files.filter((file) => !deletedFileIds.includes(file.fileId)).length === 0 && (
                <div>
                  <input
                    type="file"
                    onChange={handleFileChange}
                    disabled={isSubmitting}
                    accept=".pdf,.jpg,.jpeg,.png"
                    className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 focus:outline-none focus:ring-4 transition-all disabled:bg-gray-50 disabled:text-gray-500 cursor-pointer file:bg-gray-100 file:border-0 file:px-3 file:py-1.5 file:rounded file:text-xs file:font-medium file:text-gray-700 file:cursor-pointer hover:file:bg-gray-200"
                    onFocus={(e) => {
                      e.target.style.borderColor = "#2663EB";
                      e.target.style.boxShadow = "0 0 0 4px rgba(38, 99, 235, 0.1)";
                    }}
                    onBlur={(e) => {
                      e.target.style.borderColor = "";
                      e.target.style.boxShadow = "";
                    }}
                  />
                  {idFile && (
                    <p className="text-xs text-green-600 px-4 py-2 bg-green-50 rounded-xl mt-2">
                      New file selected: <span className="font-medium">{idFile.name}</span>
                    </p>
                  )}
                </div>
              )}
            </div>
          </div>
        </form>

        {/* Footer */}
        <div className="flex flex-col-reverse sm:flex-row justify-between items-center gap-3 px-6 pb-6 pt-6 border-t border-gray-50 flex-shrink-0 bg-white rounded-b-[24px]">
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="flex-1 px-4 py-3 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition-all disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="flex-1 flex items-center justify-center gap-2 px-6 py-3 text-sm font-medium text-white rounded-xl transition-all disabled:opacity-75 disabled:cursor-not-allowed"
            style={{ backgroundColor: "#2663EB" }}
            onMouseEnter={(e) =>
              !isSubmitting && (e.currentTarget.style.backgroundColor = "#1e4bb8")
            }
            onMouseLeave={(e) =>
              (e.currentTarget.style.backgroundColor = "#2663EB")
            }
          >
            {isSubmitting ? (
              <>
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Updating...
              </>
            ) : (
              "Update Visit"
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
