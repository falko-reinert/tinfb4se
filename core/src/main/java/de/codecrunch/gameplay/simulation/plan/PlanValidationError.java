package de.codecrunch.gameplay.simulation.plan;

/** Stable error codes returned instead of partially executing an invalid plan. */
public enum PlanValidationError {
    EMPTY_PLAN,
    TOO_MANY_DEPLOYMENTS,
    MISSING_DEPLOYMENT_ID,
    DUPLICATE_DEPLOYMENT_ID,
    UNKNOWN_UNIT_TYPE,
    SPAWN_OUTSIDE_WINDOW,
    SPAWN_OFF_GRID,
    BUDGET_EXCEEDED
}
