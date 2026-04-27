export const parseTransition = (transition) => {
  if (!transition || typeof transition !== "string") {
    return { from: null, to: null };
  }

  let parts = transition.split("→");
  if (parts.length !== 2) {
    parts = transition.split("->");
  }
  if (parts.length !== 2) {
    return { from: transition, to: null };
  }

  return { from: parts[0].trim(), to: parts[1].trim() };
};
