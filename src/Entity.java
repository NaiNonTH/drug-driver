public class Entity implements Collidable {
    public float y = 0;
    
    private int x;
    private int width;
    private int height;

    private String name;
    
    public Entity(String name, int width, int height, boolean centered, boolean useSlotRight, int sceneWidth, int roadWidth) {
        this.name = name;
        this.y = -height;
        this.width = width;
        this.height = height;

        if (useSlotRight)
            x = sceneWidth / 2 + (centered ? width / 2 : 0);
        else
            x = sceneWidth / 2 + (centered ? -roadWidth + width / 2 : -width);
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
    public int onCollided(Truck truck) {
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

    public void setName(String name) {
        this.name = name;
    }
}
