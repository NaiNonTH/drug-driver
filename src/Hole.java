public class Hole extends Obstacle implements Runnable {
    private boolean stepped = false;
    private Truck truck;

    public Hole(int slot, int sceneWidth, int roadWidth) {
        super("hole", 112, 35, slot, sceneWidth, roadWidth, false);
    }

    @Override
    public int onCollided(Object obj) {
        truck = (Truck) obj;
        
        if (stepped || truck.isFloating()) return 1;

        stepped = true;
        truck.time -= 15;
        truck.speed = (float) 0.5;
        
        new Thread(this).start();

        return 1;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        truck.speed = 1;
    }
}
