package net.dublin.bus.backend.data;

public class Route {
    private String number;
    private String inboundTowards;
    private String outboundTowards;
    private String code;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getInboundTowards() {
        return inboundTowards;
    }

    public void setInboundTowards(String inboundTowards) {
        this.inboundTowards = inboundTowards;
    }

    public String getOutboundTowards() {
        return outboundTowards;
    }

    public void setOutboundTowards(String outboundTowards) {
        this.outboundTowards = outboundTowards;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
