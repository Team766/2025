package com.team766.robot.Geovanni_P;

import com.team766.hal.JoystickReader; 
import com.team766.framework.RuleEngine;
import static com.team766.framework.RulePersistence.*;
import com.team766.hal.RobotProvider;
import com.team766.robot.Geovanni_P.Mechanisms.*;
import java.util.Set;

public class OI extends RuleEngine {
    public OI(MovingMotor Rightmotor, MovingMotor Leftmotor) {
       JoystickReader joystick1 = RobotProvider.instance.getJoystick(0);
       JoystickReader joystick2 = RobotProvider.instance.getJoystick(1);
       addRule("Name",
        joystick1.whenButton(5),
        ONCE,
        Set.of(Rightmotor),
        () -> {Rightmotor.setMotorPower(1);});



    }

    
}
