public class Obstacle implements Collidable {
    private RoadSlot slot;
    private boolean isTall;
    
    public Obstacle(RoadSlot slot, boolean isTall) {
        this.slot = slot;
        this.isTall = isTall;
    }

    public RoadSlot getSlot() {
        return slot;
    }

    public boolean isTall() {
        return isTall;
    }

    @Override
    public boolean isCollidedWith(Truck truck) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onceCollideWith(Truck truck) {
        // TODO Auto-generated method stub
        return;
    }
}
