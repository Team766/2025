package com.team766.robot.outlaw.bearbot;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.Conditions;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.outlaw.bearbot.constants.InputConstants;
import com.team766.robot.outlaw.bearbot.constants.SetPointConstants;
import com.team766.robot.outlaw.bearbot.mechanisms.*;

public class OI extends RuleEngine {
    public OI(SwerveDrive drive, Intake intake, Feeder feeder, Shooter shooter, Turret turret) {
        final JoystickReader driverController =
                RobotProvider.instance.getJoystick(InputConstants.DRIVER_CONTROLLER);

        // Add driver control rules here.
        addRules(new DriverOI(driverController, drive));

        // Button controls (deploy, intake, feeder, shooter)
        addRule( // toggle between up and down positions
                        "Deploy",
                        new Conditions.Toggle(
                                driverController.whenButton(InputConstants.BUTTON_DEPLOY)))
                .withOnTriggeringProcedure(
                        ONCE_AND_HOLD,
                        intake,
                        () -> intake.deploy(SetPointConstants.DEPLOYMENT_DEPLOYED))
                .withFinishedTriggeringProcedure(
                        intake, () -> intake.deploy(SetPointConstants.DEPLOYMENT_RETRACTED));

        addRule( // toggle between In power and Stop
                        "Intake In",
                        new Conditions.Toggle(
                                driverController.whenButton(InputConstants.BUTTON_INTAKE_IN)))
                .withOnTriggeringProcedure(ONCE_AND_HOLD, intake, () -> intake.in())
                .withFinishedTriggeringProcedure(intake, () -> intake.stop());

        addRule( // toggle between Out power and Stop
                        "Intake Out",
                        new Conditions.Toggle(
                                driverController.whenButton(InputConstants.BUTTON_INTAKE_OUT)))
                .withOnTriggeringProcedure(ONCE_AND_HOLD, intake, () -> intake.out())
                .withFinishedTriggeringProcedure(intake, () -> intake.stop());

        addRule( // toggle between In power and Stop
                        "Feeder In",
                        new Conditions.Toggle(
                                driverController.whenButton(InputConstants.BUTTON_FEEDER_IN)))
                .withOnTriggeringProcedure(ONCE_AND_HOLD, feeder, () -> feeder.in())
                .withFinishedTriggeringProcedure(intake, () -> feeder.stop());

        addRule( // toggle between Out power and Stop
                        "Feeder Out",
                        new Conditions.Toggle(
                                driverController.whenButton(InputConstants.BUTTON_FEEDER_OUT)))
                .withOnTriggeringProcedure(ONCE_AND_HOLD, feeder, () -> feeder.out())
                .withFinishedTriggeringProcedure(intake, () -> feeder.stop());

        addRule( // apply shooter power until released, then Stop
                        "Shoot",
                        driverController.whenButton(InputConstants.BUTTON_SHOOT),
                        shooter,
                        () -> shooter.shoot())
                .withFinishedTriggeringProcedure(shooter, () -> shooter.stopShooter());

        // POV controls ()
        addRule(
                "Select Turret Left",
                () -> driverController.getPOV() == SetPointConstants.TURRET_LEFT,
                ONCE,
                turret,
                () -> turret.rotate(SetPointConstants.TURRET_LEFT));
        addRule(
                "Select Turret Center",
                () -> driverController.getPOV() == SetPointConstants.TURRET_CENTER,
                ONCE,
                turret,
                () -> turret.rotate(SetPointConstants.TURRET_CENTER));
        addRule(
                "Select Turret Right",
                () -> driverController.getPOV() == SetPointConstants.TURRET_RIGHT,
                ONCE,
                turret,
                () -> turret.rotate(SetPointConstants.TURRET_RIGHT));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}
