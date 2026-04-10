import React from "react";

export default function VisitorListSection({
  title,
  indicatorColorClass,
  loading,
  items,
  emptyMessage,
  renderItem,
}) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-8 shadow-sm">
      <div className="flex items-center gap-2.5 mb-6">
        <div className={`w-2.5 h-2.5 rounded-full ${indicatorColorClass}`}></div>
        <h4 className="text-[18px] font-bold text-gray-900">{title}</h4>
      </div>

      {loading ? (
        <p className="text-gray-500 text-[15px] py-4">Loading visits...</p>
      ) : !items || items.length === 0 ? (
        <p className="text-gray-500 text-[15px] py-4">{emptyMessage}</p>
      ) : (
        <div className="flex flex-col">
          {items.map((item) => (
            <React.Fragment key={item.visitId}>{renderItem(item)}</React.Fragment>
          ))}
        </div>
      )}
    </div>
  );
}
