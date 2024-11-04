public class Cone extends Entity {
    private boolean collided = false;

    public Cone(boolean slot, int sceneWidth, int roadWidth) {
        super("cone", 70, 77, true, slot, sceneWidth, roadWidth);
    }

    @Override
    public int onCollided(Truck truck) {
        if (collided) return 1;

        setName("cone_broken");
        collided = true;
        truck.speed = (float) Math.max(1, truck.speed - 0.5);

        return 1;
    }
}
