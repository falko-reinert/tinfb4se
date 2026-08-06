package de.codecrunch.model.unit;

public abstract class MA_Unit {
    private int speed;
    private final UnitHealth health;

    public MA_Unit(int speed, int maxLife){
        this.speed = speed;
        health = new UnitHealth(maxLife);
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int speed) {
        if (!(speed < 0)){
            this.speed = speed;
        }
    }

    public int getMaxLife() {
        return health.getMaxLife();
    }

    public void setMaxLife(int maxLife) {
        health.setMaxLife(maxLife);
    }

    public int getCurrentLife() {
        return health.getCurrentLife();
    }

    public void setCurrentLife(int currentLife) {
        health.setCurrentLife(currentLife);
    }

    public void takeDamage(int damage){
        health.takeDamage(damage);
    }

    public void heal(int healing){
        health.heal(healing);
    }
}
