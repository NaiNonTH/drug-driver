public interface Collidable {
    public boolean isCollidedWith(Truck truck);

    /**
     * @return 0 if a method wants to end the game, 1 if otherwise
     */
    public int onCollided();
    
    /**
     * @param obj - Object to modify
     * @return 0 if a method wants to end the game, 1 if otherwise
     */
    public int onCollided(Object obj);
}