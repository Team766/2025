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

    public static final double ELEVATOR_ABSOLUTE_ENCODER_RANGE = elevatorRotationsToHeight(1);

    public static final double CORAL_WRIST_ABSOLUTE_ENCODER_RANGE = coralWristRotationsToDegrees(1);

    /**
     * Cosine law
     * @param side1
     * @param side2
     * @param angle in degrees
     * @return
     */
    public static double lawOfCosines(double side1, double side2, double angle) {
        double side3Squared =
                (Math.pow(side1, 2.0)
                        + Math.pow(side2, 2.0)
                        - (2 * side1 * side2 * Math.cos(Math.toRadians(angle))));
        return Math.sqrt(side3Squared);
    }

    public static double lawOfSines(double side1, double angle1, double side2) {
        return Math.asin(side2 * Math.sin(angle1) / side1);
    }

    public static double clampValueToRange(double value, double min, double max) {
        if (value > max) {
            value = max;
        } else if (value < min) {
            value = min;
        }
        return value;
    }
}
