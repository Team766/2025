package com.team766.robot.Jordan;

import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.robot.burro_elevator.constants.InputConstants.AXIS_FORWARD_BACKWARD;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;

public class OI extends RuleEngine {
    
    JoystickReader joystickLeft = 
        RobotProvider.instance.getJoystick(index:0);
    JoystickReader joystickRight = 
        RobotProvider.instance.getJoystick(index:0);

    public OI (){

        addRule ("LEFT",
            joystickLeft.getAxis(AXIS_FORWARD_BACKWARD),
            ONCE,
            Set.of(motor1),
            () -> {motor1.setPower(getAxis);});

        addRule ("RIGHT",
            joystickRight.getAxis(AXIS_FORWARD_BACKWARD),
            ONCE,
            Set.of(motor2),
            () -> {motor2.setPower(getAxis);});

    }
}