package de.codecrunch.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import de.codecrunch.TowerAttackGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Tower Attack");
		config.setWindowedMode(1024, 576);
		new Lwjgl3Application(new TowerAttackGame(), config);
	}
}
