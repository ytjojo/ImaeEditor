package com.xingren.imaging.core.homing;

/**
 * Created by felix on 2017/11/28 下午4:14.
 */

public class IMGHoming {

    public float x, y;

    public float scale;

    public IMGHoming() {

    }

    public IMGHoming(float x, float y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public void set(float x, float y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public void concat(IMGHoming homing) {
        this.scale *= homing.scale;
        this.x += homing.x;
        this.y += homing.y;
    }

    public void ccat(IMGHoming homing) {
        this.scale *= homing.scale;
        this.x -= homing.x;
        this.y -= homing.y;
    }

    @Override
    public String toString() {
        return "IMGHoming{" +
                "x=" + x +
                ", y=" + y +
                ", scale=" + scale +
                '}';
    }
}
