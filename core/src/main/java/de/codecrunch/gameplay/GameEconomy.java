package de.codecrunch.gameplay;

public class GameEconomy {
    private int balance;

    public int getBalance() {
        return balance;
    }

    public void credit(int amount) {
        balance += amount;
    }

    public void spend(int amount) {
        balance -= amount;
    }

    public boolean canAfford(int amount) {
        return balance >= amount;
    }
}
