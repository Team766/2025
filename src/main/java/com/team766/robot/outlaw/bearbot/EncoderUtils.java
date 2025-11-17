package com.team766.robot.outlaw.bearbot;

import com.team766.robot.outlaw.bearbot.constants.ConfigConstants;

public final class EncoderUtils {
    private EncoderUtils() {}

    public static double intakeDeployerDegreesToRotations(double degrees) {
        return degrees * 1. * ConfigConstants.DEPLOYMENT_GEAR_RATIO / 360.;
    }

    public static double intakeDeployerRotationsToDegrees(double rotations) {
        return rotations * 360. / 1. / ConfigConstants.DEPLOYMENT_GEAR_RATIO;
    }

    public static double turretDegreesToRotations(double degrees) {
        return degrees * 1. * ConfigConstants.TURRET_GEAR_RATIO / 360.;
    }

    public static double turretRotationsToDegrees(double rotations) {
        return rotations * 360. / 1. / ConfigConstants.TURRET_GEAR_RATIO;
    }
}
