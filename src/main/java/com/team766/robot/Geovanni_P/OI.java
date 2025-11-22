package com.team766.robot.Geovanni_P;

import com.team766.hal.JoystickReader; 
import com.team766.framework.RuleEngine;
import static com.team766.framework.RulePersistence.*;
import com.team766.hal.RobotProvider;
import com.team766.robot.Geovanni_P.Mechanisms.*;
import java.util.Set;

public class OI extends RuleEngine {
    public OI(Drive drive) {
       JoystickReader joystick1 = RobotProvider.instance.getJoystick(0);
       
        addRule("Drive",
        joystick1.whenAxisMoved(0),
        REPEATEDLY,
        Set.of(drive),
        () -> {
            double UpDownAxis = joystick1.getAxis(0);
            double LeftRightAxis = joystick1.getAxis(1);
            drive.setMotorPower(UpDownAxis + LeftRightAxis, UpDownAxis - LeftRightAxis);});
    
    }

    
}
