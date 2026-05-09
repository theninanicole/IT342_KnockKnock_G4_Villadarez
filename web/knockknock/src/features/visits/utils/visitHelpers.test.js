import { describe, expect, it } from "vitest";
import { normalizeStatus } from "./visitHelpers";

describe("visitHelpers", () => {
  it("normalizes visit status values for filtering and comparisons", () => {
    expect(normalizeStatus(" scheduled ")).toBe("SCHEDULED");
    expect(normalizeStatus("checked-in")).toBe("CHECKED-IN");
  });

  it("returns an empty string for missing statuses", () => {
    expect(normalizeStatus(null)).toBe("");
    expect(normalizeStatus(undefined)).toBe("");
    expect(normalizeStatus("")).toBe("");
  });
});
