package com.team766.localization;

import com.team766.hal.GyroReader;
import com.team766.robot.common.mechanisms.SwerveModule;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/*
/*
 * Method which calculates the position of the robot based on wheel positions.
 */
public class Odometry {

    private GyroReader gyro;
    private SwerveModule[] moduleList;
    private int moduleCount;

    private Rotation2d[] prevWheelRotation;
    private Rotation2d[] currentWheelRotation;
    private Rotation2d[] wheelRotationChange;

    private double[] prevDriveDisplacement;
    private double[] driveDisplacementChange;

    // In meters
    private final double WHEEL_CIRCUMFERENCE;
    public final double GEAR_RATIO;
    public final int ENCODER_TO_REVOLUTION_CONSTANT;

    /**
     * Constructor for Odometry, taking in several defines for the robot.
     * @param gyro The gyro sensor used to determine heading, etc.
     * @param motors A list of every wheel-controlling motor on the robot.
     * @param CANCoders A list of the CANCoders corresponding to each wheel, in the same order as motors.
     * @param wheelLocations A list of the locations of each wheel, in the same order as motors.
     * @param wheelCircumference The circumfrence of the wheels, including treads.
     * @param gearRatio The gear ratio of the wheels.
     * @param encoderToRevolutionConstant The encoder to revolution constant of the wheels.
     */
    public Odometry(
            GyroReader gyro,
            SwerveModule[] moduleList,
            double wheelCircumference,
            double gearRatio,
            int encoderToRevolutionConstant) {

        this.gyro = gyro;
        this.moduleList = moduleList;
        moduleCount = moduleList.length;

        prevWheelRotation = new Rotation2d[moduleCount];
        currentWheelRotation = new Rotation2d[moduleCount];
        wheelRotationChange = new Rotation2d[moduleCount];

        prevDriveDisplacement = new double[moduleCount];
        driveDisplacementChange = new double[moduleCount];

        this.WHEEL_CIRCUMFERENCE = wheelCircumference;
        this.GEAR_RATIO = gearRatio;
        this.ENCODER_TO_REVOLUTION_CONSTANT = encoderToRevolutionConstant;

        for (int i = 0; i < moduleCount; i++) {
            prevWheelRotation[i] = new Rotation2d();
            currentWheelRotation[i] = new Rotation2d();
            wheelRotationChange[i] = new Rotation2d();

            prevDriveDisplacement[i] = 0;
            driveDisplacementChange[i] = 0;
        }
    }

    /**
     * Updates the odometry encoder values to the robot encoder values.
     */
    private void updateDisplacementAndRotation() {
        for (int i = 0; i < moduleCount; i++) {
            currentWheelRotation[i] =
                    Rotation2d.fromDegrees(gyro.getAngle()).plus(moduleList[i].getSteerAngle());
            wheelRotationChange[i] = currentWheelRotation[i].minus(prevWheelRotation[i]);
            prevWheelRotation[i] = currentWheelRotation[i];

            double currentDriveDisplacement = moduleList[i].getDriveDisplacement();
            driveDisplacementChange[i] = currentDriveDisplacement - prevDriveDisplacement[i];
            prevDriveDisplacement[i] = currentDriveDisplacement;
        }
    }

    /**
     * Calculates the position change of the robot since the last time method was run by assuming each wheel moved in an arc.
     * @return position change between previous time method was run and now
     */
    public Translation2d calculateCurrentPositionChange() {

        double sumX = 0;
        double sumY = 0;

        updateDisplacementAndRotation();

        for (int i = 0; i < moduleCount; i++) {

            // FOR SLOPES:
            // double yaw = Math.toRadians(gyro.getAngle());
            // double roll = Math.toRadians(gyro.getRoll());
            // double pitch = Math.toRadians(gyro.getPitch());

            // double w = moduleList[i].getSteerAngle().getRadians();
            // Vector2D u =
            //         new Vector2D(Math.cos(yaw) * Math.cos(pitch), Math.sin(yaw) *
            // Math.cos(pitch));
            // Vector2D v =
            //         new Vector2D(
            //                 Math.cos(yaw) * Math.sin(pitch) * Math.sin(roll)
            //                         - Math.sin(yaw) * Math.cos(roll),
            //                 Math.sin(yaw) * Math.sin(pitch) * Math.sin(roll)
            //                         + Math.cos(yaw) * Math.cos(roll));
            // Vector2D a = u.scalarMultiply(Math.cos(w)).add(v.scalarMultiply(Math.sin(w)));
            // Vector2D b = u.scalarMultiply(-Math.sin(w)).add(v.scalarMultiply(Math.cos(w)));
            // Vector2D wheelMotion;

            double deltaX, deltaY;

            if (Math.abs(wheelRotationChange[i].getDegrees()) != 0) {
                // estimates the bot moved in a circle to calculate new position
                double radius = driveDisplacementChange[i] / wheelRotationChange[i].getRadians();

                deltaX = radius * Math.sin(wheelRotationChange[i].getRadians());
                deltaY = radius * (1 - Math.cos(wheelRotationChange[i].getRadians()));

            } else {

                deltaX = driveDisplacementChange[i];
                deltaY = 0;
            }

            Translation2d wheelMotion =
                    new Translation2d(deltaX, deltaY).rotateBy(prevWheelRotation[i]);

            sumX += wheelMotion.getX();
            sumY += wheelMotion.getY();
        }

        return new Translation2d(sumX / moduleCount, sumY / moduleCount);
    }
}
