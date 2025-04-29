package com.team766.robot.reva_2025.mechanisms;

/**
 * Utility class to convert between encoder units and physical units we use for different
 * mechanisms.
 */
public final class EncoderUtils {

    /**
     * Utility class.
     */
    private EncoderUtils() {}

    // Converts a target rotation (in degrees) to encoder units for the wrist motor.

    public static double coralWristDegreesToRotations(double angle) {
        // angle * net gear ratio * (rotations / degrees)
        return angle * (4. / 1.) * (9. / 1.) * (48. / 14.) * (1. / 360.);
    }

    // Converts the wrist motor's rotations to degrees.

    public static double coralWristRotationsToDegrees(double rotations) {
        // rotations * net gear ratio * (degrees / rotations)
        return rotations * (1. / 4.) * (1. / 9.) * (14. / 48.) * (360. / 1.);
    }

    public static double algaeArmDegreesToRotations(double angle) {
        // angle * net gear ratio * (rotations / degrees)
        return angle * (45. / 1.) * (3. / 1.) * (1. / 360.);
    }

    // Converts the wrist motor's rotations to degrees.

    public static double algaeArmRotationsToDegrees(double rotations) {
        // rotations * net gear ratio * (degrees / rotations)
        return rotations * (1. / 45.) * (1. / 3.) * (360. / 1.);
    }

    /**
     * Converts a desired height (in inches) to rotations for the elevator motors.
     *
     */
    public static double elevatorHeightToRotations(double height) {
        // height * net gear ratio * (rotations / height)
        return height * (9. / 1.) * (1. / (1.61 * Math.PI));
    }

    /**
     * Converts the elevator motor's rotations to a height (in inches).
     */
    public static double elevatorRotationsToHeight(double rotations) {
        // rotations * net gear ratio * (height / rotations)
        return rotations * (1. / 9.) * ((1.61 * Math.PI) / 1.);
    }

    public static final double ELEVATOR_ABSOLUTE_ENCODER_RANGE = 1.61 * Math.PI; // inches

    public static final double CORAL_WRIST_ABSOLUTE_ENCODER_RANGE = 14. / 48. * 360; // degrees
}
