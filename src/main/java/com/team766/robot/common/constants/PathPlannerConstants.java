package com.team766.robot.common.constants;

public final class PathPlannerConstants {

    private PathPlannerConstants() {}

    // PID constants for drive controller
    // TODO: change pathplanner constants
    public static final double TRANSLATION_P = 0.1;
    public static final double TRANSLATION_I = 0;
    public static final double TRANSLATION_D = 0.05;

    public static final double ROTATION_P = 4.00;
    public static final double ROTATION_I = 0;
    public static final double ROTATION_D = 0;

    // default values
    public static final double MAX_SPEED_MPS = 4.5;
    public static final double MASS_KG =
            (135.0 /* weight limit, with bumper, in lbs */ + 12.0) /* battery weight */ / 2.2;
    // TODO: get this from CAD or a few measurements, update default!
    public static final double MOMENT_OF_INTERTIA = 6 /* kg * m^2 */;
}
