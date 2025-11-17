package com.team766.robot.outlaw.bearbot.constants;

public final class SetPointConstants {
    // Intake Rollers
    public static final double INTAKE_IN_POWER = 0.3;
    public static final double INTAKE_OUT_POWER = -0.3;

    // Intake Deployment (deg of deployment. 0 deg is resting on top bar. Positive deployment is
    // desired.)
    public static final double DEPLOYMENT_DEPLOYED = 133.0;
    public static final double DEPLOYMENT_RETRACTED = 8.0;

    // Feeder
    public static final double FEEDER_IN_POWER = 0.5;
    public static final double FEEDER_OUT_POWER = -0.5;

    // Shooter
    public static final double SHOOTER_POWER = 0.5;
    public static final double SHOOTER_TARGET_SPEED = 1.0;
    public static final double SHOOTER_LOW_SPEED = 0.9;
    public static final double SHOOTER_HIGH_SPEED = 1.1;

    // Turret
    public static final double TURRET_LEFT = -5.0;
    public static final double TURRET_CENTER = 0.0;
    public static final double TURRET_RIGHT = 5.0;
}
