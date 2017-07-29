package com.example.kevin.builder;

/**
 * Created by kevin on 2016/4/20.
 */
public class FingerPrint {

    private int x, y;
    int signals[];

    public FingerPrint(int AP_NUMBER) {
        this.signals = new int[AP_NUMBER];

        //initialization
        for (int i = 0; i < AP_NUMBER; i++) {
            signals[i] = 0;
        }
    }

    void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
