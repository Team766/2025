public class OI extends RuleEngine {
    
    JoystickReader joystick = RobotProvider.instance.getJoystick(INDEX)

    public OI (MovingMotor){

        addRule ("MOTOR",
            joystick.WhenButton(0)
            ONCE_AND_HOLD,
            Set.of(motor),
            () -> {motor.setPower(1);});

        addRule ("MOTOR",
            joystick.WhenButton(1)
            ONCE_AND_HOLD,
            Set.of(motor),
            () -> {motor.setPower(1);});

    }
}