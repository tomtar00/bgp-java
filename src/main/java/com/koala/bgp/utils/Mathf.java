package com.koala.bgp.utils;

import java.util.*;
import java.util.Map.Entry;

public class Mathf {
    public static Vector2 lerp(Vector2 startPoint, Vector2 endPoint, float progress) {
        float x = (int) ((endPoint.getX() - startPoint.getX()) * progress + startPoint.getX());
        float y = (int) ((endPoint.getY() - startPoint.getY()) * progress + startPoint.getY());
        return new Vector2(x, y);
    }

    public static Vector2 randomOneUnitCircle() {
        Random rand = new Random();
        return new Vector2(rand.nextFloat() * 2 - 1, rand.nextFloat() * 2 - 1);
    }

    public static <T> Tuple<T, Integer> mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Entry<T, Integer> max = null;

        for (java.util.Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return new Tuple<>(max.getKey(), max.getValue());
    }

    public static <T> Tuple<List<T>, Integer> mostCommons(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Entry<T, Integer> max = null;

        for (java.util.Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        List<T> result = new ArrayList<>();

        for (java.util.Map.Entry<T, Integer> e : map.entrySet()) {
            if (e.getValue() == max.getValue()) {
                result.add(e.getKey());
            }
        }

        return new Tuple<>(result, max.getValue());
    }

    public static Stack<Integer> randomUniqueIntStack(int from, int to) {
        Stack<Integer> stack = new Stack<>();
        for (int i = from; i < to; i++) {
            stack.push(i);
        }
        Collections.shuffle(stack);
        return stack;
    }

    public static Stack<Boolean> randomBoolStack(int from, int to, int numTrue) {
        Stack<Boolean> stack = new Stack<>();
        for (int i = from; i < to; i++) {
            if (i < numTrue)
                stack.push(true);
            else
                stack.push(false);
        }
        Collections.shuffle(stack);
        return stack;
    }

    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
