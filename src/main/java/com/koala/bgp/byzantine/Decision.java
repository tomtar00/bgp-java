package com.koala.bgp.byzantine;

import java.util.Random;
import java.util.Stack;
import java.awt.*;

import com.koala.bgp.utils.Mathf;

public enum Decision {
    ATTACK_AT_DAWN,
    ATTACK_AT_NOON,
    ATTACK_IN_THE_EVENING,
    RETREAT;

    public static Color getColor(Decision decision) {
        int decIndex = Decision.valueOf(decision.toString()).ordinal();
        float value = (float)decIndex / (float)Decision.values().length;
        
        return Color.getHSBColor(value, 1f, 1f);
    }

    public static Decision randomDecision(int numDecisions) {
        if (numDecisions < 0)
            numDecisions = 0;
        else if (numDecisions > Decision.values().length)
            numDecisions = Decision.values().length;
        return Decision.values()[new Random().nextInt(numDecisions)];
    }

    public static Decision randomDecision(Decision excDecision, int numDecisions) {
        if (numDecisions < 0)
            numDecisions = 0;
        else if (numDecisions > Decision.values().length)
            numDecisions = Decision.values().length;
        Stack<Integer> stack = Mathf.randomUniqueIntStack(0, numDecisions);
        int decisionIndex;
        while(Decision.values()[decisionIndex = stack.pop()].equals(excDecision));
        return Decision.values()[decisionIndex];
    }
}
