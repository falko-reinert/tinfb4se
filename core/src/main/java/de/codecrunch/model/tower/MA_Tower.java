package de.codecrunch.model.tower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.codecrunch.model.M_Bullet;

public abstract class MA_Tower {
	
	protected M_Bullet m_bullet = new M_Bullet();
	protected int x_pos,y_pos;

	public static List<MA_Tower> getAllTowers() {
		List<MA_Tower> list = new ArrayList<>();
		list.addAll(Arrays.asList(new M_SmallTower(), new M_MediumTower(), new M_BigTower()));
		return list;
	}
	
	public void setPos(int x, int y) {
		x_pos = x;
		y_pos = y;
	}

	public int getX() {
		return x_pos;
	}

	public int getY() {
		return y_pos;
	}
	
	public abstract int getPrice();
	public abstract int getRange();
}
