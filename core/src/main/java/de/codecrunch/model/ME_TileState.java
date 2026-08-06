package de.codecrunch.model;

public enum ME_TileState {
	EMPTY(ME_TileStateGroup.EMPTY),
	START(ME_TileStateGroup.PATH),
	END(ME_TileStateGroup.PATH),
	OCCUPIED(ME_TileStateGroup.OBSTACLE),
	PATH_STRAIGHT(ME_TileStateGroup.PATH),
	PATH_LEFT(ME_TileStateGroup.PATH),
	PATH_RIGHT(ME_TileStateGroup.PATH),
	PATH_CROSS(ME_TileStateGroup.PATH),
	PATH_SPLIT_RIGHT(ME_TileStateGroup.PATH),
	PATH_SPLIT_LEFT(ME_TileStateGroup.PATH);

	private final ME_TileStateGroup group;

	ME_TileState(ME_TileStateGroup group) {
		this.group = group;
	}

	public ME_TileStateGroup getGroup() {
		return group;
	}
	
	public enum ME_TileStateGroup {
		PATH,OBSTACLE,EMPTY;
	}
}
