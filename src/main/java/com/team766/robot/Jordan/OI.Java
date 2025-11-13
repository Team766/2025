package com.team766.robot.Jordan;


public class OI extends RuleEngine {
    
    JoystickReader joystickLeft = RobotProvider.instance.getJoystick(index:0);
    JoystickReader joystickRight = RobotProvider.instance.getJoystick(index:0);

    public OI (Drive){

        addRule ("LEFT",
            joystickLeft.whenAxis(getAxis),
            ONCE_AND_HOLD,
            Set.of(motor1),
            () -> {motor1.setPower(getAxis);});

        addRule ("RIGHT",
            joystickRight.whenAxis(getAxis),
            ONCE_AND_HOLD,
            Set.of(motor2),
            () -> {motor2.setPower(getAxis);});

    }
}