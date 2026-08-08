package de.codecrunch.gameplay;

import de.codecrunch.model.unit.MA_Unit;

public final class PathUnit {
    private final MA_Unit unit;
    private final UnitRoute route;
    private float distance;
    private float x;
    private float y;
    private boolean finished;

    public PathUnit(MA_Unit unit, UnitRoute route) {
        if (unit == null || route == null) {
            throw new IllegalArgumentException("Unit and route are required");
        }
        this.unit = unit;
        this.route = route;
        x = route.getX(0);
        y = route.getY(0);
    }

    public void update(float deltaSeconds) {
        if (deltaSeconds <= 0 || !isRunning()) return;

        float routeLength = route.size() - 1;
        float exitDistance = Math.max(0f, routeLength - 0.5f);
        distance = Math.min(exitDistance, distance + unit.getSpeed() * deltaSeconds);
        if (distance >= exitDistance) {
            finished = true;
            distance = routeLength;
            x = route.getX(route.size() - 1);
            y = route.getY(route.size() - 1);
            return;
        }

        int segment = (int) distance;
        float segmentProgress = distance - segment;
        x = interpolate(route.getX(segment), route.getX(segment + 1), segmentProgress);
        y = interpolate(route.getY(segment), route.getY(segment + 1), segmentProgress);
    }

    public MA_Unit getUnit() {
        return unit;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return !finished && unit.getCurrentLife() > 0;
    }

    public float getRouteProgress() {
        return distance;
    }

    private float interpolate(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
}
