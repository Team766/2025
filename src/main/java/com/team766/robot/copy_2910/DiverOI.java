package com.team766.robot.copy_2910;

import static com.team766.framework.RulePersistence.*;

import com.team766.hal.JoystickReader;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.OI.QueuedControl;
//import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight; TO DO make for 2910 or else no worky
//import com.team766.robot.reva_2025.constants.InputConstants;
//import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.copy_2910.mechanisms.Intake;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.mechanisms.Shoulder;

import java.util.Set;

public class DriverOI extends com.team766.robot.common.DriverOI {
    public DriverOI(
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            SwerveDrive drive,
            Elevator elevator,
            Wrist wrist,
            Intake intake,
            Shoulder shoulder,
            QueuedControl queuedControl) {
           // Climber climber) { soon
        super(leftJoystick, rightJoystick, drive);
        addRule(
                        "Outtake Coral",
                        leftJoystick.whenButton(InputConstants.BUTTON_CORAL_PLACE),
                        ONCE_AND_HOLD,
                        Set.of(intake, wrist),
                        () -> {
                            
                            coralIntake.out(queuedControl.scoreHeight);
                        })
                .withFinishedTriggeringProcedure(Intake, () -> Intake.stop());
       

        addRule(
                "Nudge Wrist Up",
                rightJoystick.whenPOV(0),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudge(-1);
                });

        addRule(
                "Nudge Wrist Down",
                rightJoystick.whenPOV(180),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudge(1);
                });
     //   addRule( soon
       //         "Winch Climber Down",
         //       leftJoystick.whenButton(InputConstants.BUTTON_WINCH_CLIMBER),
        //        ONCE_AND_HOLD,
          //      climber,
          //      () -> {
          //          climber.climb(1);
            //    });
    
            };
}
