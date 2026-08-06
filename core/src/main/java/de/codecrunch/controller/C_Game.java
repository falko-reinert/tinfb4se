package de.codecrunch.controller;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Game;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.view.V_Game;

public class C_Game {
	private M_Game model = new M_Game();
	private C_Computer computer = new C_Computer(this);
	private V_Game view;
	private M_Map map;
	private Task computerTask;

	public C_Game(M_Map map) {
		this.map = map;
	}

	public void setView(V_Game view) {
		this.view = view;
	}

	public void start() {
		view.loadMap(map);

		computerTask = new Task() {
			private int timerCount = 0;

			@Override
			public void run() {
				computer.addMoney(50);
				computer.tick(timerCount);
				timerCount++;
			}
		};
		Timer.instance().scheduleTask(computerTask, 0, 1);
		Timer.instance().start();

		computer.init(map);
		view.setup();
	}

	public void dispose() {
		if (computerTask != null) {
			computerTask.cancel();
			computerTask = null;
		}
	}
	
	public void placeTower(MA_Tower tower, int x, int y){
			tower.setPos(x, y);
			view.addTower(tower);
			computer.drawMoney(tower.getPrice());
			map.getTile(x, y).setTileState(ME_TileState.OCCUPIED);
			computer.updateDistancePathCount(map);

	}
}
