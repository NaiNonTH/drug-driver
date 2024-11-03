public class Oil extends Entity {
    public boolean isCollected = false;

    public Oil(int slot, int sceneWidth, int roadWidth) {
        super("oil", 70, 112, true, slot, sceneWidth, roadWidth);
    }

    @Override
    public int onCollided(Truck truck) {
        if (isCollected || truck.isFloating()) return 1;

        isCollected = true;

        truck.time += 10;
        truck.speed = Math.min(2, truck.speed + 1 / (truck.speed + 5));

        return 1;
    }
}
