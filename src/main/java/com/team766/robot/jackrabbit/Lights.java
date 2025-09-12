package com.team766.robot.jackrabbit;

import com.team766.framework.RuleEngine;
import com.team766.robot.common.mechanisms.LEDString;

public class Lights extends RuleEngine {
    private static final int LED_COUNT = 90;
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, LED_COUNT);
}
