package com.team766.robot.Rookie_Training.mechanisms;

import java.util.jar.Attributes.Name;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;


public class OI extends RuleEngine {
    public OI(MovingMotor power)
        JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        JoystickReader rightJoystick = RobotProvider.instance.getJoystick(1);
        JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        addRule( "",
                    leftJoystick.whenButton(0))
            ,
            
        );
]}