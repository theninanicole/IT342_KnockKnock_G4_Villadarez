import React from "react";
import { Search } from "lucide-react";

export default function SearchInput({
  value,
  onChange,
  placeholder,
  className = "",
}) {
  const handleChange = (e) => {
    if (typeof onChange === "function") {
      onChange(e.target.value);
    }
  };

  return (
    <div className={`flex items-center ${className}`}>
      <Search className="text-gray-400 w-5 h-5 mr-3 flex-shrink-0" />
      <input
        type="text"
        value={value}
        onChange={handleChange}
        placeholder={placeholder}
        className="bg-transparent border-none outline-none focus:outline-none focus:ring-0 focus-visible:outline-none w-full text-sm text-gray-900 placeholder:text-gray-400"
      />
    </div>
  );
}
