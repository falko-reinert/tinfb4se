package de.codecrunch.model;

import java.io.Serializable;

public class M_Tile implements Serializable {
	private static final long serialVersionUID = 1043533140197378968L;

	public final int x_pos, y_pos;

	private transient ME_TileState state;
	//for serialization
	private int tileStateOrdinal;
	private int tileRotation;

	public M_Tile(ME_TileState state, int x_pos, int y_pos, int rotation) {
		this.state = state;
		this.x_pos = x_pos;
		this.y_pos = y_pos;
		this.tileRotation = rotation;
	}

	public ME_TileState getTileState() {
		return state;
	}

	public M_Tile setTileState(ME_TileState state) {
		this.state = state;
		return this;
	}

	public int getTileRotation() {
		return tileRotation;
	}

	public void setTileRotation(int tileRotation) {
		this.tileRotation = tileRotation;
	}

	public String toString() {
		return "[" + x_pos + "," + y_pos + "]";
	}

	public void deflate() {
		tileStateOrdinal = state.ordinal();
	}

	public void inflate() {
		state = ME_TileState.values()[tileStateOrdinal];
	}
}
