public class Oil extends Entity {
    public Oil(int width, int height, int slot, int sceneWidth, int roadWidth) {
        super("oil", width, height, slot, sceneWidth, roadWidth);
    }

    @Override
    public int onCollided(Object obj) {
        Truck truck = (Truck) obj;
        truck.time += 5;

        return 1;
    }
}
