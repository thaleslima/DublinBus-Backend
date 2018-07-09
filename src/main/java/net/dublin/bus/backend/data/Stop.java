package net.dublin.bus.backend.data;

public class Stop {
    private String stopnumber;

    private Double lat;

    private Double lng;

    private String description;

    private String routes;

    public String getStopnumber() {
        return stopnumber;
    }

    public void setStopnumber(String stopnumber) {
        this.stopnumber = stopnumber;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoutes() {
        return routes;
    }

    public void setRoutes(String routes) {
        this.routes = routes;
    }
}
