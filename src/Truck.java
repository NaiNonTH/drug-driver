public class Truck {
    public int x;
    public int y;
    private boolean floating = false;

    public int time = 60;
    public float speed = 1;
    public float stamina = 10;
    
    public final static int baseSize = 96;
    public final static int baseOffset = -24;

    private int width = floating ? (baseSize * 8 / 15) * 2 : baseSize * 8 / 15;
    private int height = floating ? baseSize * 2 : baseSize;
    private int offset = floating ? baseOffset * 2 : baseOffset;

    public boolean isFloating() {
        return floating;
    }

    public void setFloatingTo(boolean f) {
        floating = f;

        width = floating ? (baseSize * 8 / 15) * 2 : baseSize * 8 / 15;
        height = floating ? baseSize * 2 : baseSize;
        offset = floating ? baseOffset * 2 : baseOffset;
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getOffset() {
        return offset;
    }
}