public class Entity implements Collidable {
    public int y = 0;
    
    private int x;
    private int width;
    private int height;

    private String name;
    
    public Entity(String name, int width, int height, boolean centered, int slot, int sceneWidth, int roadWidth) {
        this.name = name;
        this.y = -height;
        this.width = width;
        this.height = height;

        if (slot == RoadSlot.LEFT || slot == RoadSlot.BOTH)
            x = sceneWidth / 2 + (centered ? -roadWidth + width / 2 : -width);
        else
            x = sceneWidth / 2 + (centered ? width / 2 : 0);
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

    @Override
    public int onCollided(Object obj) {
        return 0;
    }

    @Override
    public int onCollided() {
        return 0;
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

    public String getName() {
        return name;
    }
}
