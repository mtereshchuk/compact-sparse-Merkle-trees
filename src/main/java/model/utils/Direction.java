package model.utils;

public enum Direction {
    LEFT, RIGHT;

    public Direction reverse() {
        if (this == LEFT) return RIGHT;
        return LEFT;
    }
}
