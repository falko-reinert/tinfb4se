package de.codecrunch.view;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameCameraLayoutTest {
    private final GameCameraLayout layout = new GameCameraLayout(20, 9, 10f);

    @Test
    public void centersCameraOnTheTileGrid() {
        assertEquals(95f, layout.centerX(), 0.001f);
        assertEquals(40f, layout.centerZ(), 0.001f);
    }

    @Test
    public void fitsMapDepthAndKeepsHorizontalPanningSymmetric() {
        float distance = layout.cameraDistance(60f);
        float lower = layout.minimumCameraX(60f, 16f / 9f);
        float upper = layout.maximumCameraX(60f, 16f / 9f);

        assertTrue(distance > 80f);
        assertTrue(distance < 90f);
        assertTrue(lower < layout.centerX());
        assertEquals(layout.centerX() - lower, upper - layout.centerX(), 0.001f);
    }
}
