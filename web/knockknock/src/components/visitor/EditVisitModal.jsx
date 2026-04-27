import React, { useState, useEffect } from "react";
import { X, FileText } from "lucide-react";
import { toast } from "react-toastify";
import api from "@api/apiServices";
import { uploadFileToSupabase, saveFileMetadata } from "@services/fileService";

export default function EditVisitModal({ isOpen, onClose, visit, onVisitUpdated }) {
  const [unitNumber, setUnitNumber] = useState("");
  const [purpose, setPurpose] = useState("");
  const [visitDate, setVisitDate] = useState("");
  const [idFile, setIdFile] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [files, setFiles] = useState([]);

  useEffect(() => {
    const fetchVisitFiles = async (visitId) => {
      try {
        const response = await api.get(`/visits/${visitId}/files`);
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

  useEffect(() => {
    if (isOpen && visit) {
      setUnitNumber(visit.unitNumber || "");
      setPurpose(visit.purpose || visit.purposeOfVisit || "");
      setIdFile(null);

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

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!unitNumber.trim() || !purpose.trim() || !visitDate) {
      toast.error("Please fill in all required fields.");
      return;
    }

    setIsSubmitting(true);
    try {
      if (idFile) {
        try {
          const uploadResult = await uploadFileToSupabase(idFile, visit.visitId);
          await saveFileMetadata({
            visitId: visit.visitId,
            filePath: uploadResult.path,
            fileUrl: uploadResult.url,
            fileName: uploadResult.fileName,
            fileType: uploadResult.fileType,
          });
        } catch (error) {
          console.error("[EditVisitModal] Error uploading file:", error);
          toast.error("Could not upload your file. Please try again.");
          setIsSubmitting(false);
          return;
        }
      }

      await api.put(`/visits/${visit.visitId}`, {
        unitNumber: unitNumber.trim(),
        purpose: purpose.trim(),
        visitDate: visitDate,
      });

      toast.success("Visit updated successfully!");
      onVisitUpdated();
      onClose();
    } catch (error) {
      console.error("[EditVisitModal] Error updating visit:", error);
      const msg = error.response?.data?.message?.toLowerCase() || "";
      if (msg.includes("date")) {
        toast.error("The visit date must be today or later.");
      } else {
        toast.error("Something went wrong. Please try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen || !visit) return null;


  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[420px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col max-h-[90vh]">
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

        <form
          onSubmit={handleSubmit}
          className="px-6 pb-2 space-y-4 overflow-y-auto flex-1"
        >
          <div>
            <label className="text-sm font-medium text-gray-500 mb-1.5 block">
              Condominium
            </label>
            <input
              type="text"
              value={visit.condo?.name || visit.condominium || ""}
              disabled
              className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-500 cursor-not-allowed"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              Unit Number
            </label>
            <input
              type="text"
              value={unitNumber}
              onChange={(e) => setUnitNumber(e.target.value)}
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all placeholder:text-gray-400 disabled:bg-gray-50 disabled:text-gray-500"
              placeholder="Enter unit number"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              Purpose of Visit
            </label>
            <textarea
              value={purpose}
              onChange={(e) => setPurpose(e.target.value)}
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all placeholder:text-gray-400 disabled:bg-gray-50 disabled:text-gray-500 resize-none"
              rows="3"
              placeholder="Enter purpose of visit"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              Date of Visit
            </label>
            <input
              type="date"
              value={visitDate}
              onChange={(e) => setVisitDate(e.target.value)}
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all placeholder:text-gray-400 disabled:bg-gray-50 disabled:text-gray-500"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">
              ID Document
            </label>
            <div className="space-y-3">
              {files && files.length > 0 && (
                  <div className="space-y-2">
                    {files.map((file) => (
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
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              {(!files || files.length === 0) && (
                <div>
                  <input
                    type="file"
                    onChange={handleFileChange}
                    disabled={isSubmitting}
                    accept=".pdf,.jpg,.jpeg,.png"
                    className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all placeholder:text-gray-400 disabled:bg-gray-50 disabled:text-gray-500 cursor-pointer file:bg-gray-100 file:border-0 file:px-3 file:py-1.5 file:rounded file:text-xs file:font-medium file:text-gray-700 file:cursor-pointer hover:file:bg-gray-200"
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
          >
            {isSubmitting ? "Saving..." : "Save Changes"}
          </button>
        </div>
      </div>
    </div>
  );
}
