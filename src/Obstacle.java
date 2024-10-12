public class Obstacle implements Collidable {
    public int y;

    private int x;
    private int width;
    private int height;

    private boolean isTall;
    
    public Obstacle(int y, int width, int height, RoadSlot slot, int sceneWidth, int roadWidth, boolean isTall) {
        this.y = y;
        this.width = width;
        this.height = height;
        this.isTall = isTall;

        if (slot == RoadSlot.LEFT || slot == RoadSlot.BOTH)
            x = sceneWidth / 2 - roadWidth;
        else
            y = sceneWidth / 2;
    }

    @Override
    public boolean isCollidedWith(Truck truck) {
        if (
            truck.x + truck.getWidth() >= x &&
            truck.x <= x + width &&
            truck.y + truck.getHeight() >= y &&
            truck.y <= y + height
        )
            return true;

        return false;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isTall() {
        return isTall;
    }
}
