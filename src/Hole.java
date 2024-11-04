public class Hole extends Entity implements Runnable {
    private boolean stepped = false;
    private Truck truck;

    public Hole(boolean slot, int sceneWidth, int roadWidth) {
        super("hole", 112, 84, false, slot, sceneWidth, roadWidth);
    }

    @Override
    public int onCollided(Truck truck) {
        if (stepped || truck.speed < 1) return 1;

        this.truck = truck;

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
