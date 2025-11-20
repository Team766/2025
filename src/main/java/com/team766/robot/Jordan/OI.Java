package com.team766.robot.Jordan;

import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;
import static com.team766.framework.RulePersistence.REPEATEDLY;
import static com.team766.robot.burro_elevator.constants.InputConstants.AXIS_FORWARD_BACKWARD;
import java.util.Set;
import com.ctre.phoenix.ButtonMonitor;
import com.team766.framework.Context;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.Jordan.mechanisms.Drive;
import com.team766.robot.Jordan.procedures.MoveMotorProcedure;
import com.team766.robot.copy_2910.mechanisms.Shoulder;

public class OI extends RuleEngine {

    public OI (Drive drive, MoveMotorProcedure moveitmoveitMotor){
    
    JoystickReader joystickLeft = 
        RobotProvider.instance.getJoystick(0);
    JoystickReader joystickRight = 
        RobotProvider.instance.getJoystick(1);
    JoystickReader buttonIntake = 
        RobotProvider.instance.getJoystick(5);
    JoystickReader buttonLoad = 
        RobotProvider.instance.getJoystick(8);
    JoystickReader buttonFire = 
        RobotProvider.instance.getJoystick(6);

        addRule ("LEFT",
            joystickLeft.whenAxisMoved(AXIS_FORWARD_BACKWARD),
            REPEATEDLY,
            Set.of(drive),
            () -> {
                drive.setLeftMotorPower(joystickLeft.getAxis(1));
            }
        );

        addRule ("RIGHT",
            joystickRight.whenAxisMoved(AXIS_FORWARD_BACKWARD),
            REPEATEDLY,
            Set.of(drive),
            () -> {
                drive.setRightMotorPower(joystickRight.getAxis(1));
            }
        );

        addRule ("INTAKE",
            buttonIntake.whenButton(5),
            ONCE_AND_HOLD,
            Set.of(drive),
            () -> {
                drive.setIntakeMotorPower(1);
            }
        );

        addRule ("LOAD",
            buttonLoad.whenButton(8),
            ONCE,
            Set.of(drive),
            () -> {
                drive.setLoadMotorPower(1);
            }
        );

        addRule ("SHOOT",
            buttonFire.whenButton(6),
            ONCE,
            Set.of(moveitmoveitMotor),
            () -> {
                moveitmoveitMotor.motor.run(null);
            }
        );


    }
}