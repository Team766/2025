package com.team766.robot.Jordan;

import static com.team766.framework.RulePersistence.REPEATEDLY;
import static com.team766.robot.burro_elevator.constants.InputConstants.AXIS_FORWARD_BACKWARD;
import java.util.Set;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.Jordan.mechanisms.Drive;

public class OI extends RuleEngine {

    public OI (Drive drive){
    
    JoystickReader joystickLeft = 
        RobotProvider.instance.getJoystick(0);
    JoystickReader joystickRight = 
        RobotProvider.instance.getJoystick(1);

        addRule ("LEFT",
            joystickLeft.whenAxisMoved(AXIS_FORWARD_BACKWARD),
            REPEATEDLY,
            Set.of(drive),
            () -> {drive.setLeftMotorPower(joystickLeft.getAxis(1));});

        addRule ("RIGHT",
            joystickRight.whenAxisMoved(AXIS_FORWARD_BACKWARD),
            REPEATEDLY,
            Set.of(drive),
            () -> {drive.setRightMotorPower(joystickRight.getAxis(1));});

    }
}