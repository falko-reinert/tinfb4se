package de.codecrunch.view;

final class GameCameraLayout {
    private static final float MAP_MARGIN = 1.08f;

    private final float mapWidth;
    private final float mapDepth;
    private final float halfTileSize;

    GameCameraLayout(int columns, int rows, float tileSize) {
        mapWidth = columns * tileSize;
        mapDepth = rows * tileSize;
        halfTileSize = tileSize / 2f;
    }

    float centerX() {
        return (mapWidth / 2f) - halfTileSize;
    }

    float centerZ() {
        return (mapDepth / 2f) - halfTileSize;
    }

    float cameraDistance(float verticalFieldOfView) {
        float halfFieldOfView = (float) Math.toRadians(verticalFieldOfView / 2f);
        return (mapDepth * MAP_MARGIN / 2f) / (float) Math.tan(halfFieldOfView);
    }

    float minimumCameraX(float verticalFieldOfView, float aspectRatio) {
        return Math.min(centerX(), leftEdge() + horizontalHalfSpan(verticalFieldOfView, aspectRatio));
    }

    float maximumCameraX(float verticalFieldOfView, float aspectRatio) {
        return Math.max(centerX(), rightEdge() - horizontalHalfSpan(verticalFieldOfView, aspectRatio));
    }

    private float horizontalHalfSpan(float verticalFieldOfView, float aspectRatio) {
        float halfFieldOfView = (float) Math.toRadians(verticalFieldOfView / 2f);
        return cameraDistance(verticalFieldOfView) * (float) Math.tan(halfFieldOfView) * aspectRatio;
    }

    private float leftEdge() {
        return -halfTileSize;
    }

    private float rightEdge() {
        return mapWidth - halfTileSize;
    }
}
