package de.codecrunch.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.codecrunch.gameplay.GameClock;
import de.codecrunch.gameplay.GameEconomy;
import de.codecrunch.gameplay.GameProgression;
import de.codecrunch.gameplay.GdxGameClock;
import de.codecrunch.gameplay.PathUnit;
import de.codecrunch.gameplay.TowerCombat;
import de.codecrunch.gameplay.UnitRoute;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Game;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.model.unit.M_SmallUnit;
import de.codecrunch.view.V_Game;

public class C_Game {
	private static final int WAVE_SIZE = 3;
	private static final int SPAWN_INTERVAL_TICKS = 2;

	private M_Game model = new M_Game();
	private final GameEconomy economy;
	private final C_Computer computer;
	private final GameProgression progression;
	private final GameClock clock;
	private final UnitRoute unitRoute;
	private final List<PathUnit> units = new ArrayList<>();
	private final TowerCombat towerCombat = new TowerCombat();
	private final List<MA_Tower> defendingTowers = new ArrayList<>();
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
		progression = new GameProgression(economy, this::tick);
		unitRoute = UnitRoute.fromMap(map).orElse(null);
		spawnUnit();
	}

	public void setView(V_Game view) {
		this.view = view;
	}

	public void start() {
		view.loadMap(map);

		computer.init(map);
		view.setup();
		for (PathUnit unit : units) view.addUnit(unit);
		clock.start(progression::advance);
	}

	public void update(float deltaSeconds) {
		for (PathUnit unit : units) {
			unit.update(deltaSeconds);
			if (view != null) view.updateUnit(unit);
		}
	}

	PathUnit getUnit() {
		return units.isEmpty() ? null : units.get(0);
	}

	List<PathUnit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	void tick(int timerCount) {
		computer.tick(timerCount);
		if (timerCount > 0 && timerCount % SPAWN_INTERVAL_TICKS == 0) spawnUnit();
		towerCombat.attackAll(defendingTowers, units);
	}

	private void spawnUnit() {
		if (unitRoute == null || units.size() >= WAVE_SIZE) return;
		PathUnit unit = new PathUnit(new M_SmallUnit(), unitRoute);
		units.add(unit);
		if (view != null) view.addUnit(unit);
	}

	void addDefendingTower(MA_Tower tower) {
		defendingTowers.add(tower);
	}

	public void dispose() {
		clock.stop();
	}
	
	public void placeTower(MA_Tower tower, int x, int y){
			tower.setPos(x, y);
			addDefendingTower(tower);
			view.addTower(tower);
			economy.spend(tower.getPrice());
			map.getTile(x, y).setTileState(ME_TileState.OCCUPIED);
			computer.updateDistancePathCount(map);

	}
}
