package com.koala.bgp.byzantine;

import java.util.Random;
import java.util.Stack;

import com.koala.bgp.utils.Mathf;

public enum Decision {
    ATTACK_AT_DAWN,
    ATTACK_AT_NOON,
    ATTACK_IN_THE_EVENING,
    RETREAT;

    public static Decision randomDecision() {
        return Decision.values()[new Random().nextInt(Decision.values().length)];
    }

    public static Decision randomDecision(Decision excDecision) {
        Stack<Integer> stack = Mathf.randomUniqueIntStack(0, Decision.values().length);
        int decisionIndex;
        while(Decision.values()[decisionIndex = stack.pop()].equals(excDecision));
        return Decision.values()[decisionIndex];
    }
}
