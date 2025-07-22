package com.team766.robot.copy_2910;

import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.*;
import com.team766.robot.copy_2910.procedures.IntakeCoral;
import com.team766.robot.copy_2910.procedures.OuttakeCoral;


public class OI extends RuleEngine {

    public OI(
        SwerveDrive swerveDrive,
        Intake intake,
        Wrist wrist,
        Elevator elevator,
        Shoulder shoulder,
        Climber climber,
        Vision vision) {

        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(1);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(2);
        final JoystickReader boxopGamepad =
                RobotProvider.instance.getJoystick(3);

        addRule("Intake Coral",
                leftJoystick.whenButton(1),
                ONCE_AND_HOLD,
                () -> new IntakeCoral());

        addRule("Outtake Coral",
                leftJoystick.whenButton(2),
                ONCE_AND_HOLD,
                () -> new OuttakeCoral());

        addRule("Climb",
                boxopGamepad.whenButton(1),
                climber,
                ONCE_AND_HOLD,
                () -> climber.setClimberSpeed(1));
        addRule("Stop Climb",
                boxopGamepad.whenButton(2),
                climber,
                ONCE,
                () -> climber.setClimberSpeed(0));
        addRule("Remove Climber",
                boxopGamepad.whenButton(3),
                climber,
                ONCE_AND_HOLD,
                () -> climber.setClimberSpeed(-1));


        
    }
    
}
