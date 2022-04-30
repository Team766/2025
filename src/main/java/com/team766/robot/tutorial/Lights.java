package com.team766.robot.tutorial;

import com.team766.framework.RuleEngine;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.LEDString;

public class Lights extends RuleEngine {
    private LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);

    public Lights() {
        // Add lights rules here.
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }
}
