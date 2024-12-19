package com.team766.robot.gatorade.constants;

/** Constants used for reading values from the config file. */
public final class ConfigConstants {
    // utility class
    private ConfigConstants() {}

    // intake config values
    public static final String INTAKE_MOTOR = "intake.motor";

    // wrist config values
    public static final String WRIST_MOTOR = "wrist.motor";
    public static final String WRIST_FFGAIN = "wrist.sparkPID.ffGain";

    // elevator config values
    public static final String ELEVATOR_LEFT_MOTOR = "elevator.leftMotor";
    public static final String ELEVATOR_RIGHT_MOTOR = "elevator.rightMotor";
    public static final String ELEVATOR_FFGAIN = "elevator.sparkPID.ffGain";

    // shoulder config values
    public static final String SHOULDER_LEFT_MOTOR = "shoulder.leftMotor";
    public static final String SHOULDER_RIGHT_MOTOR = "shoulder.rightMotor";
    public static final String SHOULDER_FFGAIN = "shoulder.sparkPID.ffGain";

    // pathplanner config values
    public static final String PATH_FOLLOWING_MAX_MODULE_SPEED_MPS =
            "followpath.maxSpeedMetersPerSecond";

    public static final String PATH_FOLLOWING_TRANSLATION_P = "followpath.translationP";
    public static final String PATH_FOLLOWING_TRANSLATION_I = "followpath.translationI";
    public static final String PATH_FOLLOWING_TRANSLATION_D = "followpath.translationD";
    public static final String PATH_FOLLOWING_ROTATION_P = "followpath.rotationP";
    public static final String PATH_FOLLOWING_ROTATION_I = "followpath.rotationI";
    public static final String PATH_FOLLOWING_ROTATION_D = "followpath.rotationD";
}
