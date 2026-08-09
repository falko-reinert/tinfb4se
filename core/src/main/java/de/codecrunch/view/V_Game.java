package de.codecrunch.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import de.codecrunch.Model3DFactory;
import de.codecrunch.controller.C_Game;
import de.codecrunch.gameplay.PathUnit;
import de.codecrunch.gameplay.UnitOutcome;
import de.codecrunch.gameplay.WaveResult;
import de.codecrunch.gameplay.planning.PlanningSnapshot;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.render.RenderBatch;
import de.codecrunch.render.TileVisualFactory;

public class V_Game extends VA_Screen {

	private static final float CAMERA_FIELD_OF_VIEW = 60f;
	private static final float BACKGROUND_CHANNEL = 0f;
	private static final float MAP_BRIGHTNESS = 1f;
	private static final float TILE_DISTANCE = 10f;
	private static final float TOWER_MODEL_FORWARD_OFFSET_DEGREES = -90f;
	private final GameCameraLayout cameraLayout =
			new GameCameraLayout(M_Map.x_count, M_Map.y_count, TILE_DISTANCE);

	private C_Game controller;

	private PerspectiveCamera camera;
	private SpriteBatch hudBatch = new SpriteBatch();
	private final V_HUD v_hud;
	private RenderBatch mapBatch = new RenderBatch();
	private RenderBatch towerBatch = new RenderBatch();
	private RenderBatch unitBatch = new RenderBatch();
	private RenderBatch muzzleFlashBatch = new RenderBatch();
	private TileVisualFactory tileVisualFactory = new TileVisualFactory();
	private Environment environment = new Environment();
	private PointLight mouseLight = new PointLight().set(1f, 1f, 1f, 30f, 1f, 40f, 100f);

	private float cameraMotion = 0f;
	private final GameInputProcessor gameInputProcessor = new GameInputProcessor();
	private final InputMultiplexer inputProcessor;
	private final Runnable exitGameAction;
	private final Map<PathUnit, ModelInstance> unitModels = new IdentityHashMap<>();
	private final Map<MA_Tower, TowerVisual> towerModels = new IdentityHashMap<>();
	private final Map<ModelInstance, Float> activeMuzzleFlashes = new IdentityHashMap<>();
	private Model muzzleFlashModel;

	public V_Game() {
		this(() -> { }, () -> { });
	}

	public V_Game(Runnable exitGameAction) {
		this(exitGameAction, () -> { });
	}

	public V_Game(Runnable exitGameAction, Runnable replayAction) {
		super();
		this.exitGameAction = exitGameAction;
		v_hud = new V_HUD(hudBatch, exitGameAction, replayAction,
				this::deploySmallUnit, this::deployFastUnit,
				this::cyclePlanningUnit, this::adjustPlanningTime,
				this::submitAttackPlan);
		inputProcessor = new InputMultiplexer(v_hud.stage, gameInputProcessor);
	}

	public void setup() {

		camera = new PerspectiveCamera(
				CAMERA_FIELD_OF_VIEW,
				Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		camera.direction.set(0f, -1f, 0f);
		camera.up.set(0f, 0f, -1f);
		camera.position.set(cameraLayout.centerX(),
				cameraLayout.cameraDistance(CAMERA_FIELD_OF_VIEW), cameraLayout.centerZ());
		camera.near = 0.1f;
		camera.far = 120.0f;
		camera.update();

		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, MAP_BRIGHTNESS, MAP_BRIGHTNESS, MAP_BRIGHTNESS, 1.0f));
		muzzleFlashModel = new ModelBuilder().createSphere(1f, 1f, 1f, 12, 12,
				new Material(ColorAttribute.createDiffuse(Color.YELLOW)), Usage.Position | Usage.Normal);
		//environment.add(mouseLight);
	}

	public void setController(C_Game game) {
		controller = game;
	}

	public void loadMap(M_Map map) {
		map.foreachTile(tile -> {
			if (tile.getTileState() != ME_TileState.EMPTY
					&& tile.getTileState() != ME_TileState.OCCUPIED) {
				mapBatch.addElement(tileVisualFactory.createGameModel(tile));
			}
		});
	}

	public void addTower(MA_Tower tower) {
		ModelInstance model = Model3DFactory.instantiate(
				Model3DFactory.loadModel("towers/stub.g3db"));
		model.transform.setTranslation(tower.getX() * TILE_DISTANCE, 0.5f,
				tower.getY() * TILE_DISTANCE);
		towerBatch.addElement(model);
		TowerVisual visual = new TowerVisual(model);
		towerModels.put(tower, visual);
		updateTowerTransform(tower, visual);
	}

	public void setTowerYaw(MA_Tower tower, float yaw) {
		TowerVisual visual = towerModels.get(tower);
		if (visual == null) return;
		visual.currentYaw = yaw;
		updateTowerTransform(tower, visual);
	}

	public void showTowerShot(MA_Tower tower) {
		TowerVisual visual = towerModels.get(tower);
		if (visual == null) return;
		float radians = (float) Math.toRadians(visual.currentYaw);
		float xDirection = (float) Math.sin(radians);
		float yDirection = (float) Math.cos(radians);
		ModelInstance flash = new ModelInstance(muzzleFlashModel);
		flash.transform.setToTranslation((tower.getX() + xDirection * 0.75f) * TILE_DISTANCE,
				1.65f, (tower.getY() + yDirection * 0.75f) * TILE_DISTANCE);
		flash.transform.scale(0.25f, 0.25f, 0.25f);
		muzzleFlashBatch.addElement(flash);
		activeMuzzleFlashes.put(flash, 0.12f);
	}

	public void addUnit(PathUnit unit) {
		ModelInstance unitModel = Model3DFactory.instantiate(
				Model3DFactory.loadModel("towers/stub.g3db"));
		unitBatch.addElement(unitModel);
		unitModels.put(unit, unitModel);
		updateUnit(unit);
	}

	public void updateUnit(PathUnit unit) {
		ModelInstance unitModel = unitModels.get(unit);
		if (unitModel == null) return;
		unitModel.transform.setToTranslation(
				unit.getX() * TILE_DISTANCE, 1f, unit.getY() * TILE_DISTANCE);
		unitModel.transform.scale(0.5f, 0.5f, 0.5f);
		float healthRatio = Math.max(0f, Math.min(1f,
				(float) unit.getUnit().getCurrentLife() / unit.getUnit().getMaxLife()));
		for (Material material : unitModel.materials) {
			ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
			if (diffuse == null) {
				material.set(ColorAttribute.createDiffuse(1f - healthRatio, healthRatio, 0f, 1f));
			} else {
				diffuse.color.set(1f - healthRatio, healthRatio, 0f, 1f);
			}
		}
	}

	public void removeUnit(PathUnit unit) {
		ModelInstance unitModel = unitModels.remove(unit);
		if (unitModel != null) unitBatch.removeElement(unitModel);
	}

	public void setEnemyBaseIntegrity(int enemyBaseIntegrity) {
		v_hud.setEnemyBaseIntegrity(enemyBaseIntegrity);
	}

	public void setUnitOutcome(UnitOutcome outcome) {
		v_hud.setUnitOutcome(outcome);
	}

	public void setWaveResult(WaveResult result) {
		v_hud.setWaveResult(result);
	}

	public void setDeploymentOptions(int remaining, int budget, int smallUnitCost, int fastUnitCost) {
		v_hud.setDeploymentOptions(remaining, budget, smallUnitCost, fastUnitCost);
	}

	public void setPlanningState(PlanningSnapshot planningSnapshot) {
		v_hud.setPlanningState(planningSnapshot);
	}

	private void deploySmallUnit() {
		if (controller != null) controller.deploySmallUnit();
	}

	private void deployFastUnit() {
		if (controller != null) controller.deployFastUnit();
	}

	private void cyclePlanningUnit(int slotIndex) {
		if (controller != null) controller.cyclePlanningUnit(slotIndex);
	}

	private void adjustPlanningTime(int slotIndex, int gridSteps) {
		if (controller != null) controller.adjustPlanningTime(slotIndex, gridSteps);
	}

	private void submitAttackPlan() {
		if (controller != null) controller.submitAttackPlan();
	}

	@Override
	public void dispose() {
		super.dispose();
		mapBatch.dispose();
		towerBatch.dispose();
		unitBatch.dispose();
		muzzleFlashBatch.dispose();
		if (muzzleFlashModel != null) muzzleFlashModel.dispose();
		tileVisualFactory.dispose();
		v_hud.dispose();
		hudBatch.dispose();
	}

	@Override
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}

	@Override
	public void render(float delta) {
		if (controller != null) controller.update(delta);
		HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(BACKGROUND_CHANNEL, BACKGROUND_CHANNEL, BACKGROUND_CHANNEL, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camera.translate(cameraMotion, 0, 0);
		limitCamera();
		if (cameraMotion != 0)
			cameraMotion *= 0.95f;
		camera.update();
		mapBatch.begin(camera);
		mapBatch.renderAll(environment);
		mapBatch.end();
		towerBatch.begin(camera);
		towerBatch.renderAll(environment);
		towerBatch.end();
		unitBatch.begin(camera);
		unitBatch.renderAll(environment);
		unitBatch.end();
		updateMuzzleFlashes(delta);
		muzzleFlashBatch.begin(camera);
		muzzleFlashBatch.renderAll(environment);
		muzzleFlashBatch.end();
		hudBatch.setProjectionMatrix(v_hud.stage.getCamera().combined);
		v_hud.stage.draw();
		if (controller != null) v_hud.drawUnitHealth(camera, controller.getUnits());

	}

	private void updateMuzzleFlashes(float deltaSeconds) {
		Iterator<Map.Entry<ModelInstance, Float>> iterator = activeMuzzleFlashes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<ModelInstance, Float> flash = iterator.next();
			float remainingSeconds = flash.getValue() - deltaSeconds;
			if (remainingSeconds > 0f) {
				flash.setValue(remainingSeconds);
			} else {
				muzzleFlashBatch.removeElement(flash.getKey());
				iterator.remove();
			}
		}
	}

	private void updateTowerTransform(MA_Tower tower, TowerVisual visual) {
		visual.base.transform.setToTranslation(tower.getX() * TILE_DISTANCE, 0.5f,
				tower.getY() * TILE_DISTANCE);
		visual.base.transform.rotate(Vector3.Y,
				visual.currentYaw + TOWER_MODEL_FORWARD_OFFSET_DEGREES);
	}

	private static final class TowerVisual {
		private final ModelInstance base;
		private float currentYaw;

		private TowerVisual(ModelInstance base) {
			this.base = base;
		}
	}

	private boolean limitCamera() {
		float aspectRatio = camera.viewportWidth / camera.viewportHeight;
		float cameraUpper = cameraLayout.maximumCameraX(CAMERA_FIELD_OF_VIEW, aspectRatio);
		float cameraLower = cameraLayout.minimumCameraX(CAMERA_FIELD_OF_VIEW, aspectRatio);
		if (camera.position.x > cameraUpper) {
			camera.position.set(cameraUpper, camera.position.y, camera.position.z);
			cameraMotion = 0f;
			return false;
		}
		if (camera.position.x < cameraLower) {
			camera.position.set(cameraLower, camera.position.y, camera.position.z);
			cameraMotion = 0f;
			return false;
		}
		return true;
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		v_hud.stage.getViewport().update(width, height, true);
		if (camera == null || height == 0) return;
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		limitCamera();
		camera.update();
	}

	public class GameInputProcessor implements InputProcessor {

		@Override
		public boolean keyDown(int keycode) {
			if (keycode == Input.Keys.ESCAPE) {
				exitGameAction.run();
				return true;
			}
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			cameraMotion = Gdx.input.getDeltaY() * 0.088f;
			return true;
		}

		@Override
		public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			Vector3 tmpVector = new Vector3();
			Ray ray = camera.getPickRay(screenX, screenY);
			final float distance = -ray.origin.y / ray.direction.y - 1f;
			tmpVector.set(ray.direction).scl(distance).add(ray.origin);
			mouseLight.setPosition(tmpVector);
			return true;
		}

		@Override
		public boolean scrolled(float amountX, float amountY) {
			cameraMotion -= amountY * 0.5f;
			return true;
		}

	}
}
