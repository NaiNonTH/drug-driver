public interface Collidable {
    public boolean isCollidedWith(Truck truck);

    /**
     * @param truck - Truck
     * @return 0 if a method wants to end the game, 1 if otherwise
     */
    public int onCollided(Truck truck);
}