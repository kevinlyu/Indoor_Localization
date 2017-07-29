package com.example.kevin.builder;

/**
 * Created by kevin on 2016/4/20.
 */
public class AP {

    static final int SIG_LIMIT = -200; //when signal is loss, use SIG_LIMIT to instead
    private String SSID;
    private String SSID_5G;
    private String MAC;
    private int AP_X, AP_Y; //record the location of the AP

    public AP() {
    }

    public AP(String SSID, String SSID_5G, String MAC) {
        this.SSID = SSID;
        this.SSID_5G = SSID_5G;
        this.MAC = MAC;
    }

    public AP(String SSID, String SSID_5G) {
        this.SSID = SSID;
        this.SSID_5G = SSID_5G;
    }

    public AP(String MAC) {
        this.MAC = MAC;
    }

    public String getSSID() {
        return SSID;
    }

    public String getSSID_5G() {
        return SSID_5G;
    }

    public String getMAC() {
        return MAC;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setSSID_5G(String SSID_5G) {
        this.SSID_5G = SSID_5G;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public void setAPLoc(int AP_X, int AP_Y) {
        this.AP_X = AP_X;
        this.AP_Y = AP_Y;
    }


}
