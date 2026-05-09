import { describe, expect, it } from "vitest";
import { isStrongPassword, isValidEmail } from "./validators";

describe("validators", () => {
  it("accepts well-formed email addresses", () => {
    expect(isValidEmail("visitor@example.com")).toBe(true);
    expect(isValidEmail("condo.admin+test@example.co")).toBe(true);
  });

  it("rejects missing or malformed email addresses", () => {
    expect(isValidEmail("")).toBe(false);
    expect(isValidEmail("visitor")).toBe(false);
    expect(isValidEmail("visitor@example")).toBe(false);
    expect(isValidEmail("visitor example.com")).toBe(false);
  });

  it("requires strong passwords to include upper, lower, number, and minimum length", () => {
    expect(isStrongPassword("Password1")).toBe(true);
    expect(isStrongPassword("password1")).toBe(false);
    expect(isStrongPassword("PASSWORD1")).toBe(false);
    expect(isStrongPassword("Password")).toBe(false);
    expect(isStrongPassword("Pass1")).toBe(false);
  });
});
