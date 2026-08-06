package de.codecrunch.controller;

import de.codecrunch.gameplay.GameClock;
import de.codecrunch.gameplay.GameEconomy;
import de.codecrunch.gameplay.GameProgression;
import de.codecrunch.gameplay.GdxGameClock;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Game;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.view.V_Game;

public class C_Game {
	private M_Game model = new M_Game();
	private final GameEconomy economy;
	private final C_Computer computer;
	private final GameProgression progression;
	private final GameClock clock;
	private V_Game view;
	private M_Map map;

	public C_Game(M_Map map) {
		this(map, new GdxGameClock());
	}

	public C_Game(M_Map map, GameClock clock) {
		this.map = map;
		this.clock = clock;
		economy = new GameEconomy();
		computer = new C_Computer(this, economy);
		progression = new GameProgression(economy, computer::tick);
	}

	public void setView(V_Game view) {
		this.view = view;
	}

	public void start() {
		view.loadMap(map);

		clock.start(progression::advance);

		computer.init(map);
		view.setup();
	}

	public void dispose() {
		clock.stop();
	}
	
	public void placeTower(MA_Tower tower, int x, int y){
			tower.setPos(x, y);
			view.addTower(tower);
			economy.spend(tower.getPrice());
			map.getTile(x, y).setTileState(ME_TileState.OCCUPIED);
			computer.updateDistancePathCount(map);

	}
}
