package com.team766.robot.mayhem_shooter;

import com.team766.framework.RuleEngine;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.LEDString;

public class Lights extends RuleEngine {
    private LEDString leds = new LEDString("leds");

    public Lights() {
        // Add lights rules here.
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }
}
