public class Obstacle extends Entity {

    private boolean isTall;
    
    public Obstacle(String name, int width, int height, int slot, int sceneWidth, int roadWidth, boolean isTall) {
        super(name, width, height, false, slot, sceneWidth, roadWidth);
        this.isTall = isTall;
    }

    public boolean isTall() {
        return isTall;
    }
}
