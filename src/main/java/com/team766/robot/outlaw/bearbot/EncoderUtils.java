package com.team766.robot.outlaw.bearbot;

public final class EncoderUtils {
    private EncoderUtils() {}

    public static double intakeDeployerDegreesToRotations(double degrees) {
        return degrees * 1. * 60. /* gear ratio */ / 360.;
    }

    public static double intakeDeployerRotationsToDegrees(double rotations) {
        return rotations * 360. / 1. / 60. /* gear ratio */;
    }

    public static double turretDegreesToRotations(double degrees) {
        return degrees * 1. * 347.2 /* gear ratio */ / 360.;
    }

    public static double turretRotationsToDegrees(double rotations) {
        return rotations * 360. / 1. / 347.2 /* gear ratio */;
    }
}
