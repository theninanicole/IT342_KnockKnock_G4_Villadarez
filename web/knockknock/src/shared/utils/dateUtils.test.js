import { describe, expect, it } from "vitest";
import { formatDate, getVisitDateKey } from "./dateUtils";

describe("dateUtils", () => {
  it("normalizes visit date strings to yyyy-mm-dd keys", () => {
    expect(getVisitDateKey("2026-05-05T08:30:00Z")).toBe("2026-05-05");
  });

  it("normalizes array visit dates to yyyy-mm-dd keys", () => {
    expect(getVisitDateKey([2026, 5, 5])).toBe("2026-05-05");
  });

  it("returns null when a visit date cannot be read", () => {
    expect(getVisitDateKey(null)).toBeNull();
    expect(getVisitDateKey({ year: 2026 })).toBeNull();
  });

  it("formats valid dates and preserves invalid date values", () => {
    expect(formatDate("2026-05-05T00:00:00Z")).toMatch(/2026-05-0[45]/);
    expect(formatDate("not-a-date")).toBe("not-a-date");
    expect(formatDate(null)).toBe("-");
  });
});
