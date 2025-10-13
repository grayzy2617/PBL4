package Model;

public class Link {
    private String tx;          // id của node phát
    public String rx;          // id của node thu
    public double distanceKm;
    public double frequencyMHz;
    public double B_alloc;
    private int priority;

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public String getRx() {
        return rx;
    }

    public void setRx(String rx) {
        this.rx = rx;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getFrequencyMHz() {
        return frequencyMHz;
    }

    public void setFrequencyMHz(double frequencyMHz) {
        this.frequencyMHz = frequencyMHz;
    }

    public double getB_alloc() {
        return B_alloc;
    }

    public void setB_alloc(double B_alloc) {
        this.B_alloc = B_alloc;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return tx + " -> " + rx + " (" + distanceKm + " km)";
    }
}
