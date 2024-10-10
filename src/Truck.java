public class Truck {
    public int x;
    private boolean floating = false;
    
    public final static int baseSize = 96;
    public final static int baseOffset = -24;
    
    private int size = floating ? baseSize * 2 : baseSize;
    private int offset = floating ? baseOffset * 2 : baseOffset;

    public boolean isFloating() {
        return floating;
    }

    public void setFloatingTo(boolean f) {
        floating = f;

        size = floating ? baseSize * 2 : baseSize;
        offset = floating ? baseOffset * 2 : baseOffset;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }
}
