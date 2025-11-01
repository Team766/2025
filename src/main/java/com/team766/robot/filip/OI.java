package com.team766.robot.filip;

import java.util.Set;
import static com.team766.framework.RulePersistence.*;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotProvider;
import com.team766.robot.filip.mechanisms.Drive;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.InputConstants;

public class OI extends RuleEngine {
    public OI(Drive drive) {
        final JoystickReader joystick = RobotProvider.instance.getJoystick(0);

        addRule("RUN_LEFT_MOTOR",
                joystick.whenAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_YAXIS),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_left(joystick.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS));
                }
        );

        addRule("RUN_RIGHT_MOTOR",
                joystick.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_right(joystick.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS));
                }
        );
    
    }
}