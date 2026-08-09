package de.codecrunch.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import de.codecrunch.gameplay.PathUnit;
import de.codecrunch.gameplay.UnitOutcome;
import de.codecrunch.gameplay.WaveResult;
import de.codecrunch.gameplay.planning.PlanningSlotView;
import de.codecrunch.gameplay.planning.PlanningSnapshot;
import de.codecrunch.gameplay.simulation.ReferenceScenario;
import de.codecrunch.gameplay.simulation.plan.PlanValidationError;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.TowerSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

public class V_HUD implements Disposable {
    public Stage stage;
    private Viewport viewport;
    private final BitmapFont font;
    private final Label baseIntegrityLabel;
    private final Label unitOutcomeLabel;
    private final Label waveResultLabel;
	private final Label playerBudgetLabel;
	private final TextButton deploySmallUnit;
	private final TextButton deployFastUnit;
	private final Table planningPanel;
	private final Label planningBudgetLabel;
	private final Label planningDefenseLabel;
	private final Label planningValidationLabel;
	private final TextButton[] planningUnitButtons = new TextButton[3];
	private final Label[] planningCostLabels = new Label[3];
	private final Label[] planningTimeLabels = new Label[3];
	private final TextButton[] planningMinusButtons = new TextButton[3];
	private final TextButton[] planningPlusButtons = new TextButton[3];
	private final TextButton startAttackButton;
	private int deploymentsRemaining = 3;
	private int playerBudget = 5;
	private int smallUnitCost = 1;
	private int fastUnitCost = 3;
    private final Vector3 projectedUnitPosition = new Vector3();

    public V_HUD(SpriteBatch sb, final Runnable exitGameAction, final Runnable replayAction,
            final Runnable deploySmallUnitAction, final Runnable deployFastUnitAction,
			final IntConsumer cyclePlanningUnitAction,
			final BiConsumer<Integer, Integer> adjustPlanningTimeAction,
			final Runnable submitPlanningAction){
        viewport = new FitViewport(1024,576, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        font = new BitmapFont();
        baseIntegrityLabel = new Label("Enemy base integrity: 3", new Label.LabelStyle(font, Color.WHITE));
        baseIntegrityLabel.setPosition(20f, viewport.getWorldHeight() - 35f);
        stage.addActor(baseIntegrityLabel);
        unitOutcomeLabel = new Label("Last unit: -", new Label.LabelStyle(font, Color.WHITE));
        unitOutcomeLabel.setPosition(20f, viewport.getWorldHeight() - 65f);
        stage.addActor(unitOutcomeLabel);
        waveResultLabel = new Label(WaveResult.IN_PROGRESS.getDisplayText(),
                new Label.LabelStyle(font, Color.WHITE));
        waveResultLabel.setPosition(20f, viewport.getWorldHeight() - 95f);
        stage.addActor(waveResultLabel);
		playerBudgetLabel = new Label("Command points: 5", new Label.LabelStyle(font, Color.WHITE));
		playerBudgetLabel.setPosition(20f, viewport.getWorldHeight() - 125f);
		stage.addActor(playerBudgetLabel);
        TextButton exitGame = new TextButton("Exit game", VA_Screen.uiSkin);
        exitGame.setPosition(20f, 20f);
        exitGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exitGameAction.run();
            }
        });
        stage.addActor(exitGame);
        TextButton replay = new TextButton("Replay", VA_Screen.uiSkin);
        replay.setPosition(160f, 20f);
        replay.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                replayAction.run();
            }
        });
        stage.addActor(replay);
		deploySmallUnit = new TextButton("Send small: 100 HP, speed 2 (cost 1)", VA_Screen.uiSkin);
		deploySmallUnit.setPosition(300f, 20f);
		deploySmallUnit.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				deploySmallUnitAction.run();
			}
		});
		stage.addActor(deploySmallUnit);
		deployFastUnit = new TextButton("Send fast: 200 HP, speed 4 (cost 3)", VA_Screen.uiSkin);
		deployFastUnit.setPosition(560f, 20f);
		deployFastUnit.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				deployFastUnitAction.run();
			}
		});
		stage.addActor(deployFastUnit);

		Label.LabelStyle planningLabelStyle = new Label.LabelStyle(font, Color.WHITE);
		planningPanel = new Table();
		planningPanel.setBounds(20f, 75f, 940f, 465f);
		planningPanel.setBackground(VA_Screen.uiSkin.getDrawable("window-c"));
		planningPanel.pad(14f);
		Label planningTitle = new Label("ANGRIFF PLANEN", planningLabelStyle);
		planningTitle.setFontScale(1.35f);
		planningPanel.add(planningTitle).colspan(6).left().padBottom(8f).row();
		planningBudgetLabel = new Label("", planningLabelStyle);
		planningPanel.add(planningBudgetLabel).colspan(6).left().row();
		planningDefenseLabel = new Label("", planningLabelStyle);
		planningPanel.add(planningDefenseLabel).colspan(6).left().padBottom(8f).row();
		planningPanel.add(new Label("Slot", planningLabelStyle)).width(55f).left();
		planningPanel.add(new Label("Einheit", planningLabelStyle)).width(220f).left();
		planningPanel.add(new Label("Werte", planningLabelStyle)).width(200f).left();
		planningPanel.add(new Label("Startzeit", planningLabelStyle)).colspan(3).left().row();
		for (int index = 0; index < planningUnitButtons.length; index++) {
			final int slotIndex = index;
			planningPanel.add(new Label(Integer.toString(index + 1), planningLabelStyle))
					.width(55f).left();
			planningUnitButtons[index] = new TextButton("Leer", VA_Screen.uiSkin);
			planningUnitButtons[index].addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					cyclePlanningUnitAction.accept(slotIndex);
				}
			});
			planningPanel.add(planningUnitButtons[index]).width(220f).height(45f);
			planningCostLabels[index] = new Label("0", planningLabelStyle);
			planningPanel.add(planningCostLabels[index]).width(200f).left();
			planningMinusButtons[index] = new TextButton("-0,25", VA_Screen.uiSkin);
			planningMinusButtons[index].addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					adjustPlanningTimeAction.accept(slotIndex, -1);
				}
			});
			planningPanel.add(planningMinusButtons[index]).width(80f).height(42f);
			planningTimeLabels[index] = new Label("0,00 s", planningLabelStyle);
			planningPanel.add(planningTimeLabels[index]).width(75f).center();
			planningPlusButtons[index] = new TextButton("+0,25", VA_Screen.uiSkin);
			planningPlusButtons[index].addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					adjustPlanningTimeAction.accept(slotIndex, 1);
				}
			});
			planningPanel.add(planningPlusButtons[index]).width(80f).height(42f).row();
		}
		planningValidationLabel = new Label("", planningLabelStyle);
		planningValidationLabel.setWrap(true);
			planningPanel.add(planningValidationLabel).colspan(6).width(890f).left()
				.padTop(8f).padBottom(6f).row();
		startAttackButton = new TextButton("Angriff starten", VA_Screen.uiSkin);
		startAttackButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				submitPlanningAction.run();
			}
		});
		planningPanel.add(startAttackButton).colspan(6).width(260f).height(48f).center();
		planningPanel.setVisible(false);
		stage.addActor(planningPanel);
    }

    public void setEnemyBaseIntegrity(int enemyBaseIntegrity) {
        baseIntegrityLabel.setText("Enemy base integrity: " + enemyBaseIntegrity);
    }

    public void setUnitOutcome(UnitOutcome outcome) {
        unitOutcomeLabel.setText("Last unit: " + outcome.getDisplayText());
    }

    public void setWaveResult(WaveResult result) {
        waveResultLabel.setText(result.getDisplayText());
        waveResultLabel.setColor(result == WaveResult.DEFEAT ? Color.RED
                : result == WaveResult.VICTORY ? Color.GREEN : Color.WHITE);
    }

	public void setDeploymentOptions(int remaining, int budget, int smallCost, int fastCost) {
		deploymentsRemaining = remaining;
		playerBudget = budget;
		smallUnitCost = smallCost;
		fastUnitCost = fastCost;
		playerBudgetLabel.setText("Command points: " + budget + " | Deployments: " + remaining);
		deploySmallUnit.setText("Send small: 100 HP, speed 2 (cost " + smallCost + ")");
		deployFastUnit.setText("Send fast: 200 HP, speed 4 (cost " + fastCost + ")");
		deploySmallUnit.setDisabled(remaining == 0 || budget < smallCost);
		deployFastUnit.setDisabled(remaining == 0 || budget < fastCost);
	}

	public void setPlanningState(PlanningSnapshot snapshot) {
		planningPanel.setVisible(true);
		baseIntegrityLabel.setVisible(false);
		unitOutcomeLabel.setVisible(false);
		waveResultLabel.setVisible(false);
		playerBudgetLabel.setVisible(false);
		deploySmallUnit.setVisible(false);
		deployFastUnit.setVisible(false);

		ScenarioSpec scenario = snapshot.getScenario();
		planningBudgetLabel.setText("Command Points: " + snapshot.getSpentBudget() + " / "
				+ scenario.getBudget() + "   |   Frei: " + snapshot.getRemainingBudget()
				+ "   |   Slots: " + scenario.getMaxDeployments());
		planningDefenseLabel.setText(defenseText(scenario));
		List<PlanningSlotView> slots = snapshot.getSlots();
		for (int index = 0; index < planningUnitButtons.length; index++) {
			PlanningSlotView slot = slots.get(index);
			planningUnitButtons[index].setText(unitText(scenario, slot));
			planningCostLabels[index].setText(unitValues(scenario, slot));
			planningTimeLabels[index].setText(formatTime(slot.getSpawnTick()));
			boolean editingDisabled = snapshot.isSubmitted();
			planningUnitButtons[index].setDisabled(editingDisabled);
			planningMinusButtons[index].setDisabled(editingDisabled || slot.isEmpty()
					|| slot.getSpawnTick() == 0);
			planningPlusButtons[index].setDisabled(editingDisabled || slot.isEmpty()
					|| slot.getSpawnTick() == scenario.getMaxSpawnTick());
		}

		if (snapshot.isSubmitted()) {
			planningValidationLabel.setText(
					"Plan übernommen. Die visuelle Ausführung folgt in Meilenstein 4.");
			planningValidationLabel.setColor(Color.GREEN);
			startAttackButton.setText("Plan übernommen");
			startAttackButton.setDisabled(true);
		} else if (snapshot.isValid()) {
			planningValidationLabel.setText(
					"Plan gültig. Angriff kann gestartet und an die Simulation übergeben werden.");
			planningValidationLabel.setColor(Color.GREEN);
			startAttackButton.setText("Angriff starten");
			startAttackButton.setDisabled(false);
		} else {
			planningValidationLabel.setText(validationText(snapshot.getValidationErrors()));
			planningValidationLabel.setColor(Color.RED);
			startAttackButton.setText("Plan ungültig");
			startAttackButton.setDisabled(true);
		}
	}

	private String unitText(ScenarioSpec scenario, PlanningSlotView slot) {
		if (slot.isEmpty()) return "Leer";
		return ReferenceScenario.BULWARK.equals(slot.getUnitTypeId())
				? "Bollwerk" : "Läufer";
	}

	private String unitValues(ScenarioSpec scenario, PlanningSlotView slot) {
		if (slot.isEmpty()) return "–";
		UnitSpec spec = scenario.getUnitSpec(slot.getUnitTypeId());
		long speedHundredths = spec.getSpeedUnitsPerTick()
				* ScenarioSpec.TICKS_PER_SECOND * 100L
				/ ScenarioSpec.ROUTE_UNITS_PER_PROGRESS;
		return spec.getCost() + " CP | " + spec.getMaxHealth() + " LP | "
				+ formatHundredths(speedHundredths) + "/s";
	}

	private String defenseText(ScenarioSpec scenario) {
		if (scenario.getTowerSpecs().isEmpty()) {
			return "Basisintegrität: " + scenario.getBaseIntegrity() + " | Keine Türme";
		}
		TowerSpec tower = scenario.getTowerSpecs().get(0);
		long rangeHundredths = tower.getRangeUnits() * 100L
				/ ScenarioSpec.ROUTE_UNITS_PER_PROGRESS;
		long cooldownHundredths = tower.getCooldownTicks() * 100L
				/ ScenarioSpec.TICKS_PER_SECOND;
		return "Basisintegrität: " + scenario.getBaseIntegrity()
				+ " | Wächter: Schaden " + tower.getDamage()
				+ ", Cooldown " + formatHundredths(cooldownHundredths)
				+ " s, Reichweite " + formatHundredths(rangeHundredths)
				+ ", Ziel: vorderste Unit";
	}

	private String validationText(List<PlanValidationError> errors) {
		StringBuilder text = new StringBuilder("Plan ungültig: ");
		for (PlanValidationError error : errors) {
			if (text.length() > 16) text.append("; ");
			switch (error) {
				case EMPTY_PLAN: text.append("mindestens eine Unit wählen"); break;
				case TOO_MANY_DEPLOYMENTS: text.append("zu viele Deployments"); break;
				case MISSING_DEPLOYMENT_ID: text.append("Slot-ID fehlt"); break;
				case DUPLICATE_DEPLOYMENT_ID: text.append("Slot-ID doppelt"); break;
				case UNKNOWN_UNIT_TYPE: text.append("unbekannte Unit"); break;
				case SPAWN_OUTSIDE_WINDOW: text.append("Startzeit außerhalb 0–6 s"); break;
				case SPAWN_OFF_GRID: text.append("Startzeit nicht im 0,25-s-Raster"); break;
				case BUDGET_EXCEEDED: text.append("Budget überschritten"); break;
				default: text.append(error.name());
			}
		}
		return text.toString();
	}

	private String formatTime(int tick) {
		return formatHundredths((long) tick * 100L / ScenarioSpec.TICKS_PER_SECOND) + " s";
	}

	private String formatHundredths(long hundredths) {
		return (hundredths / 100L) + "," + (hundredths % 100L < 10L ? "0" : "")
				+ hundredths % 100L;
	}

    public void drawUnitHealth(PerspectiveCamera camera, Iterable<PathUnit> units) {
        SpriteBatch batch = (SpriteBatch) stage.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (PathUnit unit : units) {
            projectedUnitPosition.set(unit.getX() * 10f, 2f, unit.getY() * 10f);
            camera.project(projectedUnitPosition);
            viewport.unproject(projectedUnitPosition);
            font.draw(batch, "HP: " + unit.getUnit().getCurrentLife(),
                    projectedUnitPosition.x - 20f, projectedUnitPosition.y + 15f);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
}
