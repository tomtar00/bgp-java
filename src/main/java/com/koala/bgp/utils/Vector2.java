package com.koala.bgp.utils;

public class Vector2 {
    private float x, y;

    public Vector2() {
        this.x = 0f;
        this.y = 0f;
    }
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public Vector2 add(Vector2 vector2) {
        return new Vector2(this.getX() + vector2.getX(), this.getY() + vector2.getY());
    }

    public Vector2 multiply(float value) {
        return new Vector2(this.getX() * value, this.getY() * value);
    }


    @Override
    public String toString() {
        return "{" +
            " x='" + getX() + "'" +
            ", y='" + getY() + "'" +
            "}";
    }

}
