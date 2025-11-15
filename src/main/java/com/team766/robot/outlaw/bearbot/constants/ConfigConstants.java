package com.team766.robot.outlaw.bearbot.constants;

public final class ConfigConstants {

    // Intake
    public static final String INTAKE_ROLLER_MOTOR = "Intake.intakeRollerMotor";
    public static final String INTAKE_DEPLOYMENT_MOTOR = "Intake.deploymentMotor";
    public static final String INTAKE_ABSOLUTE_ENCODER = "Intake.absoluteEncoder";

    // Feeder
    public static final String FEEDER_FEEDER_MOTOR = "Feeder.feederMotor";

    // Shooter
    public static final String SHOOTER_SHOOTER_MOTOR = "Shooter.shooterMotor";

    // Turret
    public static final String TURRET_MOTOR = "Turret.turretMotor";
    public static final String TURRET_ABSOLUTE_ENCODER = "Turret.absoluteEncoder";

    // Gear Ratios
    public static final double INTAKE_GEAR_RATIO = 1;
    public static final double DEPLOYMENT_GEAR_RATIO = 60;
    public static final double FEEDER_GEAR_RATIO = 1;
    public static final double SHOOTER_GEAR_RATIO = 1;
    public static final double TURRET_GEAR_RATIO = 60 * 6.75;

    private ConfigConstants() {}
}
