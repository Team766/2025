package com.team766.robot.ROBOT_NAME;

import com.team766.framework.RuleEngine;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.ROBOT_NAME.mechanisms.*;
import com.team766.robot.ROBOT_NAME.procedures.*;

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
