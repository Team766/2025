package com.team766.robot.filip;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.hal.MotorController;

public class ShooterOI extends RuleGroup{
    JoystickReader controller = RobotProvider.instance.getJoystick(0);
    MotorController shooter = RobotProvider.instance.getMotor("shooter");

    
}
