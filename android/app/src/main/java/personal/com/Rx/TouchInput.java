package personal.com.Rx;

/**
 * Created by mahendra.chouhan on 7/30/18.
 */

public class TouchInput {

    public enum EventType {UP, DOWN, MOVE, SWIPE_UP, SWIPE_DOWN, DOUBLE_TAP}

    public EventType type;

    public float x, y;
    public final int pointer;
    public final long time;

    public TouchInput(float x, float y, int pointer, EventType type, long time) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.pointer = pointer;
        this.time = time;
    }

    //required for distinct until changed to work
    @Override
    public boolean equals(Object o) {
        if (o instanceof TouchInput) {
            TouchInput event = (TouchInput) o;
            return Math.abs(x - event.x) <= 0.5 && Math.abs(y - event.y) <= 0.5
                    && type == event.type && pointer == event.pointer;
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + x + ":" + y + ":" + pointer + "]  " + type.toString();
    }
}
