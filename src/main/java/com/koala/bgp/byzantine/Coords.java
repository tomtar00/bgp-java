package com.koala.bgp.byzantine;

public class Coords 
{
    private int x, y;
    
    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Coords(Coords coords) {
        this.x = coords.x;
        this.y = coords.y;
    }

    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }


    @Override
    public String toString() {
        return "{" +
            " x='" + getX() + "'" +
            ", y='" + getY() + "'" +
            "}";
    }

}
