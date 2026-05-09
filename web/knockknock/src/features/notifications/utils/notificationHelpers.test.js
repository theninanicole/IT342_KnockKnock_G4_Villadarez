import { afterEach, describe, expect, it, vi } from "vitest";
import { formatRelativeTime, groupNotificationsByTime } from "./notificationHelpers";

describe("notificationHelpers", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("groups notifications into Today, This Week, and Older buckets", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-05-05T12:00:00Z"));

    const groups = groupNotificationsByTime([
      { id: 1, createdAt: "2026-05-05T08:00:00Z" },
      { id: 2, createdAt: "2026-05-02T08:00:00Z" },
      { id: 3, createdAt: "2026-04-20T08:00:00Z" },
      { id: 4, createdAt: "invalid" },
    ]);

    expect(groups.Today.map((item) => item.id)).toEqual([1]);
    expect(groups["This Week"].map((item) => item.id)).toEqual([2]);
    expect(groups.Older.map((item) => item.id)).toEqual([3, 4]);
  });

  it("formats relative notification time", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-05-05T12:00:00Z"));

    expect(formatRelativeTime("2026-05-05T11:59:45Z")).toBe("· just now");
    expect(formatRelativeTime("2026-05-05T11:30:00Z")).toBe("· 30 min ago");
    expect(formatRelativeTime("2026-05-05T09:00:00Z")).toBe("· 3 h ago");
    expect(formatRelativeTime("2026-05-03T12:00:00Z")).toBe("· 2 d ago");
  });
});
