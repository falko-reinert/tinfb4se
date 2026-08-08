package de.codecrunch.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;

import de.codecrunch.gameplay.GameClock;
import de.codecrunch.gameplay.GameEconomy;
import de.codecrunch.gameplay.GameProgression;
import de.codecrunch.gameplay.GdxGameClock;
import de.codecrunch.gameplay.PathUnit;
import de.codecrunch.gameplay.TowerCombat;
import de.codecrunch.gameplay.TowerShot;
import de.codecrunch.gameplay.UnitOutcome;
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
	private static final int INITIAL_BASE_INTEGRITY = 3;
	private static final float TURRET_TURN_DEGREES_PER_SECOND = 720f;
	private static final float FIRE_ALIGNMENT_DEGREES = 2f;

	private M_Game model = new M_Game();
	private final GameEconomy economy;
	private final C_Computer computer;
	private final GameProgression progression;
	private final GameClock clock;
	private final UnitRoute unitRoute;
	private final List<PathUnit> units = new ArrayList<>();
	private final TowerCombat towerCombat = new TowerCombat();
	private final List<MA_Tower> defendingTowers = new ArrayList<>();
	private final List<TowerShot> pendingShots = new ArrayList<>();
	private final Map<MA_Tower, Float> towerYaws = new IdentityHashMap<>();
	private int spawnedUnitCount;
	private int baseIntegrity = INITIAL_BASE_INTEGRITY;
	private UnitOutcome lastUnitOutcome;
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
		view.setBaseIntegrity(baseIntegrity);
		clock.start(progression::advance);
	}

	public void update(float deltaSeconds) {
		for (PathUnit unit : units) {
			unit.update(deltaSeconds);
			if (view != null) view.updateUnit(unit);
		}
		updatePendingShots(deltaSeconds);
		removeResolvedUnits();
	}

	PathUnit getUnit() {
		return units.isEmpty() ? null : units.get(0);
	}

	public List<PathUnit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	int getBaseIntegrity() {
		return baseIntegrity;
	}

	UnitOutcome getLastUnitOutcome() {
		return lastUnitOutcome;
	}

	void tick(int timerCount) {
		computer.tick(timerCount);
		if (timerCount > 0 && timerCount % SPAWN_INTERVAL_TICKS == 0) spawnUnit();
		for (MA_Tower tower : defendingTowers) {
			PathUnit target = towerCombat.selectTarget(tower, units);
			if (target != null) {
				pendingShots.add(new TowerShot(tower, target));
			}
		}
		removeResolvedUnits();
	}

	private void updatePendingShots(float deltaSeconds) {
		Iterator<TowerShot> iterator = pendingShots.iterator();
		while (iterator.hasNext()) {
			TowerShot shot = iterator.next();
			if (!shot.getTarget().isRunning()) {
				iterator.remove();
				continue;
			}
			float currentYaw = towerYaws.containsKey(shot.getTower()) ? towerYaws.get(shot.getTower()) : 0f;
			float targetYaw = targetYaw(shot.getTower(), shot.getTarget());
			float difference = normalizeAngle(targetYaw - currentYaw);
			float maximumTurn = TURRET_TURN_DEGREES_PER_SECOND * deltaSeconds;
			float nextYaw = normalizeAngle(currentYaw + Math.max(-maximumTurn, Math.min(maximumTurn, difference)));
			towerYaws.put(shot.getTower(), nextYaw);
			if (view != null) view.setTowerYaw(shot.getTower(), nextYaw);
			if (Math.abs(normalizeAngle(targetYaw - nextYaw)) <= FIRE_ALIGNMENT_DEGREES) {
				shot.getTarget().getUnit().takeDamage(TowerCombat.DAMAGE_PER_ATTACK);
				if (view != null) view.showTowerShot(shot.getTower());
				iterator.remove();
			}
		}
	}

	private float targetYaw(MA_Tower tower, PathUnit target) {
		return (float) Math.toDegrees(Math.atan2(target.getX() - tower.getX(), target.getY() - tower.getY()));
	}

	private float normalizeAngle(float angle) {
		float normalized = angle % 360f;
		if (normalized > 180f) return normalized - 360f;
		if (normalized < -180f) return normalized + 360f;
		return normalized;
	}

	private void spawnUnit() {
		if (unitRoute == null || spawnedUnitCount >= WAVE_SIZE) return;
		PathUnit unit = new PathUnit(new M_SmallUnit(), unitRoute);
		units.add(unit);
		spawnedUnitCount++;
		if (view != null) view.addUnit(unit);
	}

	private void removeResolvedUnits() {
		Iterator<PathUnit> iterator = units.iterator();
		while (iterator.hasNext()) {
			PathUnit unit = iterator.next();
			if (unit.isRunning()) continue;
			if (unit.isFinished()) {
				baseIntegrity--;
				if (view != null) view.setBaseIntegrity(baseIntegrity);
				lastUnitOutcome = UnitOutcome.ESCAPED;
			} else {
				lastUnitOutcome = UnitOutcome.DEFEATED;
			}
			if (view != null) view.setUnitOutcome(lastUnitOutcome);
			if (view != null) view.removeUnit(unit);
			iterator.remove();
		}
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
