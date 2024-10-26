public class Cone extends Obstacle {
    private boolean collided = false;

    public Cone(int slot, int sceneWidth, int roadWidth) {
        super("cone", 70, 77, true, slot, sceneWidth, roadWidth);
    }

    @Override
    public int onCollided(Object obj) {
        Truck truck = (Truck) obj;

        if (collided) return 1;

        collided = true;
        truck.speed = (float) Math.max(1, truck.speed - 0.5);

        return 1;
    }
}
