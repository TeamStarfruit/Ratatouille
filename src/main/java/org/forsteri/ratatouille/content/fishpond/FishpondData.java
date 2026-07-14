package org.forsteri.ratatouille.content.fishpond;

public class FishpondData {
    public int sizeLevel;
    public int foodLevel;
    public int oxygenLevel;
    public int waterQualityLevel;
    public int sludgeLevel;
    public int spawnSpeedLevel;

    public int spawnTimer;
    public int updateRequired;

    public void tick(FishpondBlockEntity fishpondBlockEntity) {

    }

    public void clear() {
        sizeLevel = 0;
        foodLevel = 0;
        oxygenLevel = 0;
        waterQualityLevel = 0;
        sludgeLevel = 0;
        spawnSpeedLevel = 0;


        spawnTimer = 0;
    }

    public boolean evaluate(FishpondBlockEntity be) {
        sizeLevel = be.height * be.radius * be.radius / 4;
        return false;
    }
}