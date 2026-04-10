import React, { useState, useEffect, useRef } from "react";
import { X, Upload, FileCheck, AlertCircle } from "lucide-react";
import { fetchCondos, createVisit } from "@api/apiServices";
import { uploadFile } from "@services/fileService";
import { toast } from "react-toastify";

export default function NewVisitModal({ isOpen, onClose, onVisitCreated }) {
  const [formData, setFormData] = useState({
    condoId: "",
    unitNumber: "",
    dateOfVisit: "",
    purposeOfVisit: "",
  });

  const [condos, setCondos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fileLoading, setFileLoading] = useState(false);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [fileError, setFileError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({
    condoId: "",
    unitNumber: "",
    dateOfVisit: "",
    purposeOfVisit: "",
  });
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      fetchCondosList();
    }
  }, [isOpen]);

  const fetchCondosList = async () => {
    try {
      const data = await fetchCondos();
      setCondos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error fetching condos:", error);
      toast.error(error.response?.data?.message || "Failed to load condominiums");
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear field-specific error when user starts typing/selecting
    setFieldErrors((prev) => ({
      ...prev,
      [name]: "",
    }));
  };

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFileError(null);
    setUploadedFile({
      name: file.name,
      file,
      type: file.type,
    });

    toast.info("File selected. It will upload when you submit the visit.");
  };

  const handleClearFile = () => {
    setUploadedFile(null);
    setFileError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const errors = {
      condoId: !formData.condoId ? "Condominium is required" : "",
      unitNumber: !formData.unitNumber ? "Unit number is required" : "",
      dateOfVisit: !formData.dateOfVisit ? "Date of visit is required" : "",
      purposeOfVisit: !formData.purposeOfVisit ? "Purpose of visit is required" : "",
    };

    const hasFieldErrors = Object.values(errors).some((msg) => msg);

    if (hasFieldErrors) {
      setFieldErrors(errors);
      toast.error("Please fill in all required fields.");
      return;
    }

    if (!uploadedFile || !uploadedFile.file) {
      setFileError("ID document is required");
      toast.error("Please upload your ID document.");
      return;
    }

    setLoading(true);
    try {
      const submitData = {
        condoId: formData.condoId,
        unitNumber: formData.unitNumber,
        dateOfVisit: formData.dateOfVisit,
        purposeOfVisit: formData.purposeOfVisit,
      };

      const visitResponse = await createVisit(submitData);
      const realVisitId = visitResponse.visitId;

      toast.success("Visit created!");

      setFileLoading(true);
      await uploadFile(uploadedFile.file, realVisitId);

      toast.success("ID document uploaded successfully!");

      setFileLoading(false);
      handleClose();
      onVisitCreated();
    } catch (error) {
      console.error("Error in visit/file creation:", error);
      toast.error(error.response?.data?.message || error.message || "Failed to create visit");
    } finally {
      setLoading(false);
      setFileLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      condoId: "",
      unitNumber: "",
      dateOfVisit: "",
      purposeOfVisit: "",
    });
    setUploadedFile(null);
    setFileError(null);
    setFieldErrors({
      condoId: "",
      unitNumber: "",
      dateOfVisit: "",
      purposeOfVisit: "",
    });
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    onClose();
  };

  if (!isOpen) return null;

  const isSubmitDisabled = loading || fileLoading;

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[420px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col max-h-[90vh]">
        <div className="flex justify-between items-center px-6 pt-6 pb-4 flex-shrink-0">
          <h2 className="text-xl font-semibold text-blue-600">New Visit</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-900 transition-colors p-1.5 rounded-full hover:bg-gray-100"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="px-6 pb-2 overflow-y-auto flex-1">
          <form id="new-visit-form" onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1.5 block">
                Condominium <span className="text-red-500">*</span>
              </label>
              <select
                name="condoId"
                value={formData.condoId}
                onChange={handleInputChange}
                className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all appearance-none cursor-pointer"
                required
              >
                <option value="" disabled>
                  Select a condominium
                </option>
                {condos.map((condo) => (
                  <option key={condo.condoId} value={condo.condoId}>
                    {condo.name}
                  </option>
                ))}
              </select>
              {fieldErrors.condoId && (
                <p className="mt-1 text-xs text-red-600 flex items-center gap-1">
                  <AlertCircle size={14} className="flex-shrink-0" />
                  {fieldErrors.condoId}
                </p>
              )}
            </div>

            <div>
              <label className="text-sm font-medium text-gray-700 mb-1.5 block">
                Unit Number <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="unitNumber"
                value={formData.unitNumber}
                onChange={handleInputChange}
                placeholder="Enter unit number"
                className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all placeholder:text-gray-400"
                required
              />
              {fieldErrors.unitNumber && (
                <p className="mt-1 text-xs text-red-600 flex items-center gap-1">
                  <AlertCircle size={14} className="flex-shrink-0" />
                  {fieldErrors.unitNumber}
                </p>
              )}
            </div>

            <div>
              <label className="text-sm font-medium text-gray-700 mb-1.5 block">
                Date of Visit <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="dateOfVisit"
                value={formData.dateOfVisit}
                onChange={handleInputChange}
                className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all"
                required
              />
              {fieldErrors.dateOfVisit && (
                <p className="mt-1 text-xs text-red-600 flex items-center gap-1">
                  <AlertCircle size={14} className="flex-shrink-0" />
                  {fieldErrors.dateOfVisit}
                </p>
              )}
            </div>

            <div>
              <label className="text-sm font-medium text-gray-700 mb-1.5 block">
                Purpose of Visit <span className="text-red-500">*</span>
              </label>
              <textarea
                name="purposeOfVisit"
                value={formData.purposeOfVisit}
                onChange={handleInputChange}
                placeholder="Enter purpose of visit"
                className="w-full px-4 py-2.5 bg-gray-50/50 border border-gray-200/80 rounded-xl text-sm text-gray-900 focus:outline-none focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 transition-all placeholder:text-gray-400 resize-none"
                rows="3"
                required
              />
              {fieldErrors.purposeOfVisit && (
                <p className="mt-1 text-xs text-red-600 flex items-center gap-1">
                  <AlertCircle size={14} className="flex-shrink-0" />
                  {fieldErrors.purposeOfVisit}
                </p>
              )}
            </div>

            <div className="pt-2">
              <label className="text-sm font-medium text-gray-700 mb-2 block">
                ID Document <span className="text-red-500">*</span>
              </label>

              {/* Hidden input always mounted so we can trigger it in all states */}
              <input
                ref={fileInputRef}
                type="file"
                onChange={handleFileSelect}
                accept="image/*,.pdf"
                className="hidden"
              />

              {fileError && (
                <p className="mb-2 text-xs text-red-600 flex items-center gap-1">
                  <AlertCircle size={14} className="flex-shrink-0" />
                  {fileError}
                </p>
              )}

              {!uploadedFile && (
                <div className="relative">
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="w-full flex flex-col items-center justify-center px-4 py-7 border-2 border-dashed border-gray-200/80 rounded-2xl bg-gray-50/50 cursor-pointer hover:bg-blue-50/50 hover:border-blue-300 transition-all group"
                  >
                    <div className="p-3 bg-white rounded-full shadow-sm ring-1 ring-gray-900/5 mb-3 group-hover:scale-105 transition-transform">
                      <Upload size={20} className="text-blue-500" />
                    </div>
                    <span className="text-sm font-medium text-gray-700 mb-1">
                      Click to upload an ID
                    </span>
                    <span className="text-xs text-gray-400">
                      JPEG, PNG, or PDF (Max 10MB)
                    </span>
                  </button>
                </div>
              )}

              {uploadedFile && !fileError && (
                <div className="flex items-center justify-between p-3.5 bg-blue-50/50 border border-blue-200/80 rounded-2xl group">
                  <div className="flex items-center gap-3.5 overflow-hidden">
                    <div className="p-2 bg-blue-100 text-blue-600 rounded-xl">
                      <FileCheck className="w-4 h-4" />
                    </div>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium text-gray-900 truncate max-w-[200px]">
                        {uploadedFile.name}
                      </span>
                      <span className="text-xs font-medium text-blue-600 mt-0.5">
                        Ready to submit
                      </span>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={handleClearFile}
                    className="p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                  >
                    <X size={18} />
                  </button>
                </div>
              )}
            </div>
          </form>
        </div>

        <div className="px-6 pb-6 pt-5 border-t border-gray-50 flex-shrink-0 bg-white rounded-b-[24px]">
          <button
            type="submit"
            form="new-visit-form"
            disabled={isSubmitDisabled}
            className="w-full flex items-center justify-center gap-2 px-6 py-3 text-sm font-medium text-white bg-blue-600 rounded-xl hover:bg-blue-700 transition-all shadow-sm disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {loading || fileLoading ? (
              <>
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Submitting...
              </>
            ) : (
              "Submit Visit"
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
