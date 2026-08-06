package de.codecrunch.model.unit;

public class UnitHealth {
    private int maxLife;
    private int currentLife;

    public UnitHealth(int maxLife) {
        this.maxLife = maxLife;
        currentLife = maxLife;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(int maxLife) {
        if (maxLife > 0) this.maxLife = maxLife;
    }

    public int getCurrentLife() {
        return currentLife;
    }

    public void setCurrentLife(int currentLife) {
        if (maxLife < currentLife) this.currentLife = maxLife;
        else this.currentLife = currentLife;
    }

    public void takeDamage(int damage) {
        if (currentLife < damage) currentLife = 0;
        else currentLife -= damage;
    }

    public void heal(int healing) {
        if (currentLife == 0) currentLife = 0;
        else if (maxLife < currentLife + healing) currentLife = maxLife;
        else currentLife += healing;
    }
}
