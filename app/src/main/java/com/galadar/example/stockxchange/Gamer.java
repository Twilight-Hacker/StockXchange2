package com.galadar.example.stockxchange;

/**
 * Created by Galadar on 29/9/2015.
 * Gamer Object holding the player data.
 *
 * See each variable declaration for what it does.
 */
public class Gamer {

    long money;         //The amount of cash the player has
    int assets;         //The amount of full assets the player has
    int level;          //The player's level. This affects the options available to the player, as well as the amount of taxes the
    // player pays and the validity of rumors they hear (see MainActivity). Player levels up on their own volition, but cannot level down.
    private int fame;   //This will be used in the future for player generated events.



    public Gamer(MemoryDB DBHandler) {
        this.money = DBHandler.getPlayerMoney();
        this.assets = DBHandler.getAssets();
        this.level = DBHandler.getLevel();
        this.fame = DBHandler.getFame();
    }

    public Gamer(long money, int level, int assets, int fame) {
        this.money = money;
        this.level = level;
        this.assets = assets;
        this.fame = fame;
    }

    public long getMoney() {
        return this.money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public void alterMoney(long amount){
        this.money -= amount;
    }

    public int getAssets() {
        return assets;
    }

    public void setAssets(int assets) {
        this.assets = assets;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

/*
    public int getFame() {
        return fame;
    }

    public void alterFame(int alteration) {
        this.fame += fame;
    }
*/

}
