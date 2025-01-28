package com.team766.robot.reva_2025.constants;

import com.team766.math.Math;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class AlgaeConstants {
    public static final double SHOOTER_RPM = 0; //Todo: Update me

    public static final Pose2d RED_ALGAE_POSE = new Pose2d(0,0,new Rotation2d());
    public static final Pose2d BLUE_ALGAE_POSE = new Pose2d(0,0,new Rotation2d());
    public record AlgaeShooterData(double distance, double angle){};

    //Ensure distance is sorted lowest to highest when testing!
    public static AlgaeShooterData[] data = new AlgaeShooterData[] { new AlgaeShooterData(0.0, 1.0), new AlgaeShooterData(1.0, 32.0)};

    double interpolatedY = Math.interpolate(data, 0.5, AlgaeShooterData::distance, AlgaeShooterData::angle);

    public static double calculateArmAngle(double distance){
        return Math.interpolate(data, distance, AlgaeShooterData::distance, AlgaeShooterData::angle);
    }

}
