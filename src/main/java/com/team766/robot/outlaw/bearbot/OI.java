package com.team766.robot.outlaw.bearbot;

import static com.team766.framework.RulePersistence.*;

import java.util.Set;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.outlaw.bearbot.mechanisms.*;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.gatorade.GamePieceType;
import com.team766.robot.outlaw.bearbot.constants.InputConstants;
import com.team766.robot.outlaw.bearbot.constants.SetPointConstants;

public class OI extends RuleEngine {
    public OI(SwerveDrive drive, Intake intake, Feeder feeder, Shooter shooter, Turret turret) {
        final JoystickReader driverController =
                RobotProvider.instance.getJoystick(InputConstants.DRIVER_CONTROLLER);

        // Add driver control rules here.
        addRules(new DriverOI(driverController, drive));

        // Button controls (deploy, intake, feeder, shooter)
        addRule( //toggle between up and down positions
            "Deploy",
            driverController.whenButton(InputConstants.BUTTON_DEPLOY),
            intake,
            () -> intake.deploy(SetPointConstants.Deployment_Deployed));

        addRule( // toggle between In power and Stop
            "Intake In",
            driverController.whenButton(InputConstants.BUTTON_INTAKE_IN),
            intake,
            () -> intake.in());
    
        addRule( // toggle between Out power and Stop
            "Intake Out",
            driverController.whenButton(InputConstants.BUTTON_INTAKE_OUT),
            intake,
            () -> intake.out());

        addRule( // toggle between In power and Stop
            "Feeder In",
            driverController.whenButton(InputConstants.BUTTON_FEEDER_IN),
            feeder,
            () -> feeder.feedIn());

        addRule( // toggle between Out power and Stop
            "Feeder Out",
            driverController.whenButton(InputConstants.BUTTON_FEEDER_OUT),
            feeder,
            () -> feeder.feedOut());

        addRule( // apply shooter power until released, then Stop
            "Shoot",
            driverController.whenButton(InputConstants.BUTTON_SHOOT),
            shooter,
            () -> shooter.shoot())
        .withFinishedTriggeringProcedure(shooter, () -> shooter.stopShooter());
    
        // POV controls ()
        addRule(
            "Select Turret Left",
            () -> driverController.getPOV() == SetPointConstants.Turret_Left,
            ONCE,
            Set.of(),
            () -> {
            });
        addRule(
            "Select Turret Center",
            () -> driverController.getPOV() == SetPointConstants.Turret_Center,
            ONCE,
            Set.of(),
            () -> {
            });
        addRule(
            "Select Turret Right",
            () -> driverController.getPOV() == SetPointConstants.Turret_Right,
            ONCE,
            Set.of(),
            () -> {
            });
          
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}
