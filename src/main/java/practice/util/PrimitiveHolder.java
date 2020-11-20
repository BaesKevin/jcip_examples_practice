package practice.util;

public class PrimitiveHolder<T> {
    T boxedPrimitive;

    public PrimitiveHolder(T boxedPrimitive) {
        this.boxedPrimitive = boxedPrimitive;
    }

    public synchronized T get() {
        return boxedPrimitive;
    }

    public synchronized void set(T boxedPrimitive) {
        this.boxedPrimitive = boxedPrimitive;
    }
}
