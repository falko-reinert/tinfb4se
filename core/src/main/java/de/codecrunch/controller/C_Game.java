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
import de.codecrunch.gameplay.WaveResult;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Game;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.model.unit.MA_Unit;
import de.codecrunch.model.unit.M_FastUnit;
import de.codecrunch.model.unit.M_SmallUnit;
import de.codecrunch.view.V_Game;

public class C_Game {
	private static final int WAVE_SIZE = 3;
	public static final int SMALL_UNIT_COST = 1;
	public static final int FAST_UNIT_COST = 3;
	private static final int INITIAL_PLAYER_BUDGET = 5;
	private static final int INITIAL_ENEMY_BASE_INTEGRITY = 3;
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
	private int playerBudget = INITIAL_PLAYER_BUDGET;
	private int enemyBaseIntegrity = INITIAL_ENEMY_BASE_INTEGRITY;
	private UnitOutcome lastUnitOutcome;
	private WaveResult waveResult = WaveResult.IN_PROGRESS;
	private boolean computerEnabled = true;
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
	}

	public void setView(V_Game view) {
		this.view = view;
	}

	public void disableComputer() {
		computerEnabled = false;
	}

	public void start() {
		view.loadMap(map);

		computer.init(map);
		view.setup();
		for (MA_Tower tower : defendingTowers) view.addTower(tower);
		for (PathUnit unit : units) view.addUnit(unit);
		view.setEnemyBaseIntegrity(enemyBaseIntegrity);
		view.setWaveResult(waveResult);
		updateDeploymentOptions();
		clock.start(progression::advance);
	}

	public void update(float deltaSeconds) {
		if (waveResult != WaveResult.IN_PROGRESS) return;
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

	int getEnemyBaseIntegrity() {
		return enemyBaseIntegrity;
	}

	UnitOutcome getLastUnitOutcome() {
		return lastUnitOutcome;
	}

	public WaveResult getWaveResult() {
		return waveResult;
	}

	public int getDeploymentsRemaining() {
		return WAVE_SIZE - spawnedUnitCount;
	}

	public int getPlayerBudget() {
		return playerBudget;
	}

	public boolean deploySmallUnit() {
		return deployUnit(new M_SmallUnit(), SMALL_UNIT_COST);
	}

	public boolean deployFastUnit() {
		return deployUnit(new M_FastUnit(), FAST_UNIT_COST);
	}

	private boolean deployUnit(MA_Unit unit, int cost) {
		if (playerBudget < cost) return false;
		if (waveResult != WaveResult.IN_PROGRESS || !spawnUnit(unit)) return false;
		playerBudget -= cost;
		updateDeploymentOptions();
		return true;
	}

	private void updateDeploymentOptions() {
		if (view != null) view.setDeploymentOptions(getDeploymentsRemaining(), playerBudget,
				SMALL_UNIT_COST, FAST_UNIT_COST);
	}

	void tick(int timerCount) {
		if (waveResult != WaveResult.IN_PROGRESS) return;
		if (computerEnabled) computer.tick(timerCount);
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

	private boolean spawnUnit(MA_Unit unit) {
		if (unitRoute == null || spawnedUnitCount >= WAVE_SIZE) return false;
		PathUnit pathUnit = new PathUnit(unit, unitRoute);
		units.add(pathUnit);
		spawnedUnitCount++;
		if (view != null) view.addUnit(pathUnit);
		return true;
	}

	private void removeResolvedUnits() {
		Iterator<PathUnit> iterator = units.iterator();
		while (iterator.hasNext()) {
			PathUnit unit = iterator.next();
			if (unit.isRunning()) continue;
			if (unit.isFinished()) {
				enemyBaseIntegrity--;
				if (view != null) view.setEnemyBaseIntegrity(enemyBaseIntegrity);
				lastUnitOutcome = UnitOutcome.ESCAPED;
			} else {
				lastUnitOutcome = UnitOutcome.DEFEATED;
			}
			if (view != null) view.setUnitOutcome(lastUnitOutcome);
			if (view != null) view.removeUnit(unit);
			iterator.remove();
		}
		finishWaveIfNeeded();
	}

	private void finishWaveIfNeeded() {
		if (enemyBaseIntegrity <= 0) {
			finishWave(WaveResult.VICTORY);
		} else if (spawnedUnitCount == WAVE_SIZE && units.isEmpty()) {
			finishWave(WaveResult.DEFEAT);
		}
	}

	private void finishWave(WaveResult result) {
		if (waveResult != WaveResult.IN_PROGRESS) return;
		waveResult = result;
		clock.stop();
		if (view != null) view.setWaveResult(result);
	}

	void addDefendingTower(MA_Tower tower) {
		defendingTowers.add(tower);
	}

	public void addStartingTower(MA_Tower tower, int x, int y) {
		tower.setPos(x, y);
		addDefendingTower(tower);
		map.getTile(x, y).setTileState(ME_TileState.OCCUPIED);
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
