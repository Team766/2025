package com.team766.robot.Geovanni_P;

public class OI extends RuleEngine {
    Public OI(BurroDrive Rightmotor, BurroDrive Leftmotor) {
       JoystickReader joystick1 = robotProvider.instance.getjoystick(0);
       JoystickReader joystick2 = robotProvider.instance.getjoystick(1);
       AddRule("Name",
        Rightjoystick.whenButton(5),
        ONCE,
        Set.of(Rightmotor),
        () -> {Rightmotor.});



    }

    
}
