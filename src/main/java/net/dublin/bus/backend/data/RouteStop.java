package net.dublin.bus.backend.data;

public class RouteStop {
    private String r;
    private String s;

    public RouteStop(String r, String s) {
        this.r = r;
        this.s = s;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }
}
