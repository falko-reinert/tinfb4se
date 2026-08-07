package de.codecrunch.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.IdentityHashMap;
import java.util.Map;

import de.codecrunch.Model3DFactory;
import de.codecrunch.controller.C_Game;
import de.codecrunch.gameplay.PathUnit;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.render.RenderBatch;
import de.codecrunch.render.TileVisualFactory;

public class V_Game extends VA_Screen {

	private static final float CAMERA_FIELD_OF_VIEW = 60f;
	private static final float BACKGROUND_CHANNEL = 120f / 255f;
	private static final float MAP_BRIGHTNESS = 1f;
	private static final float TILE_DISTANCE = 10f;
	private final GameCameraLayout cameraLayout =
			new GameCameraLayout(M_Map.x_count, M_Map.y_count, TILE_DISTANCE);

	private C_Game controller;

	private PerspectiveCamera camera;
	private SpriteBatch hudBatch = new SpriteBatch();
	V_HUD v_hud = new V_HUD(hudBatch);
	private RenderBatch mapBatch = new RenderBatch();
	private RenderBatch towerBatch = new RenderBatch();
	private RenderBatch unitBatch = new RenderBatch();
	private TileVisualFactory tileVisualFactory = new TileVisualFactory();
	private Environment environment = new Environment();
	private PointLight mouseLight = new PointLight().set(1f, 1f, 1f, 30f, 1f, 40f, 100f);

	private float cameraMotion = 0f;
	private final GameInputProcessor inputProcessor = new GameInputProcessor();
	private final Map<PathUnit, ModelInstance> unitModels = new IdentityHashMap<>();

	public V_Game() {
		super();

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
		//environment.add(mouseLight);
	}

	public void setController(C_Game game) {
		controller = game;
	}

	public void loadMap(M_Map map) {
		map.foreachTile(tile -> mapBatch.addElement(tileVisualFactory.createGameModel(tile)));
	}

	public void addTower(MA_Tower tower) {
		ModelInstance model = Model3DFactory.instantiate(
				Model3DFactory.loadModel("towers/stub.g3db"));
		model.transform.setTranslation(tower.getX() * TILE_DISTANCE, 0.5f,
				tower.getY() * TILE_DISTANCE);
		towerBatch.addElement(model);
	}

	public void addUnit(PathUnit unit) {
		ModelInstance unitModel = Model3DFactory.instantiate(
				Model3DFactory.loadModel("towers/stub.g3db"));
		for (Material material : unitModel.materials) {
			material.set(ColorAttribute.createDiffuse(Color.RED));
		}
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
	}

	@Override
	public void dispose() {
		super.dispose();
		mapBatch.dispose();
		towerBatch.dispose();
		unitBatch.dispose();
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
		hudBatch.setProjectionMatrix(v_hud.stage.getCamera().combined);
		v_hud.stage.draw();

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
