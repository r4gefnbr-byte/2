package dev.client.nebula.module;

public class Setting {
    private final String name;
    private final double min, max, step;
    private double value;

    public Setting(String name, double def, double min, double max, double step) {
        this.name = name;
        this.value = def;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public String getName() { return name; }
    public double get() { return value; }
    public float getF() { return (float) value; }

    public void set(double v) { value = Math.max(min, Math.min(max, v)); }

    public void nudge(int direction) {
        double v = value + direction * step;
        value = Math.round(Math.max(min, Math.min(max, v)) / step) * step;
    }
}
