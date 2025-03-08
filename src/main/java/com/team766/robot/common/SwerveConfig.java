package com.team766.robot.common;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.system.plant.DCMotor;

/**
 * Configuration for the Swerve Drive motors on this robot.
 */
// TODO: switch from Vector2D to WPILib's Translation2D.
public class SwerveConfig {
    public static final String DEFAULT_CAN_BUS = "swerve";
    public static final String RIO_CAN_BUS = "";
    // defines where the wheels are in relation to the center of the robot
    // allows swerve drive code to calculate how to turn
    public static final double DEFAULT_FL_X = 1;
    public static final double DEFAULT_FL_Y = 1;
    public static final double DEFAULT_FR_X = 1;
    public static final double DEFAULT_FR_Y = -1;
    public static final double DEFAULT_BL_X = -1;
    public static final double DEFAULT_BL_Y = 1;
    public static final double DEFAULT_BR_X = -1;
    public static final double DEFAULT_BR_Y = -1;

    // Radius of the wheels. The circumference was measured to be 30.5cm, then experimentally this
    // value had an error of 2.888%. This was then converted to meters, and then the radius.
    public static final double DEFAULT_WHEEL_RADIUS = 30.5 * 1.02888 / 100 / (2 * Math.PI);

    // Circumference of the wheels. It was measured to be 30.5cm, then experimentally this value had
    // an error of 2.888%. This was then converted to meters.
    // TODO: compute this from the wheel radius!
    public static final double DEFAULT_WHEEL_CIRCUMFERENCE = 30.5 * 1.0 / 100;

    // Unique to the type of swerve module we have. This is the gear ratio for the drive motors.
    public static final double DEFAULT_DRIVE_GEAR_RATIO = 6.75;

    // Unique to the type of swerve module we have. This is the gear ratio for the steer motors.
    public static final double DEFAULT_STEER_GEAR_RATIO = 150.0 / 7.0;

    // The distance between the center of a wheel to the center of an adjacent wheel, assuming the
    // robot is square. This was measured as 20.5 inches, then converted to meters.
    public static final double DEFAULT_DISTANCE_BETWEEN_WHEELS = 20.5 * 2.54 / 100;

    // Unique to the type of swerve module we have. For every revolution of the wheel, the encoders
    // will increase by 1.
    public static final int DEFAULT_ENCODER_TO_REVOLUTION_CONSTANT = 1;

    // FIXME: check/tune these
    public static final double WHEEL_COEFF_FRICTION_STATIC = 1.1;
    public static final double WHEEL_COEFF_FRICTION_DYNAMIC = 0.8;
    public static final double DEFAULT_DRIVE_CURRENT_LIMIT = 35;
    public static final double DEFAULT_STEER_CURRENT_LIMIT = 30;
    public static final double DRIVE_STATOR_CURRENT_LIMIT = 80.0;
    public static final double STEER_STATOR_CURRENT_LIMIT = 80.0;

    public static final DCMotor DEFAULT_DRIVE_MOTOR = DCMotor.getKrakenX60(1 /* motors */);

    private String canBus = DEFAULT_CAN_BUS;
    // TODO: can we combine Drive's wheel locations and odometry's wheel locations?
    private Translation2d frontLeftLocation = new Translation2d(DEFAULT_FL_X, DEFAULT_FL_Y);
    private Translation2d frontRightLocation = new Translation2d(DEFAULT_FR_X, DEFAULT_FR_Y);
    private Translation2d backLeftLocation = new Translation2d(DEFAULT_BL_X, DEFAULT_BL_Y);
    private Translation2d backRightLocation = new Translation2d(DEFAULT_BR_X, DEFAULT_BR_Y);
    private double wheelRadius = DEFAULT_WHEEL_RADIUS;
    private double wheelCircumference = DEFAULT_WHEEL_CIRCUMFERENCE;
    private double driveGearRatio = DEFAULT_DRIVE_GEAR_RATIO;
    private double steerGearRatio = DEFAULT_STEER_GEAR_RATIO;
    private double distanceBetweenWheels = DEFAULT_DISTANCE_BETWEEN_WHEELS;
    private double wheelDistanceFromCenter = Math.sqrt(2) * DEFAULT_DISTANCE_BETWEEN_WHEELS / 2;
    private int encoderToRevolutionConstant = DEFAULT_ENCODER_TO_REVOLUTION_CONSTANT;
    private double wheelCoeffFrictionStatic = WHEEL_COEFF_FRICTION_STATIC;
    private double wheelCoeffFrictionDynamic = WHEEL_COEFF_FRICTION_DYNAMIC;
    private double driveMotorCurrentLimit = DEFAULT_DRIVE_CURRENT_LIMIT;
    private double steerMotorCurrentLimit = DEFAULT_STEER_CURRENT_LIMIT;
    private double driveMotorStatorCurrentLimit = DRIVE_STATOR_CURRENT_LIMIT;
    private double steerMotorStatorCurrentLimit = STEER_STATOR_CURRENT_LIMIT;
    private DCMotor driveMotor = DEFAULT_DRIVE_MOTOR;

    public SwerveConfig() {}

    public String canBus() {
        return canBus;
    }

    public Translation2d frontLeftLocation() {
        return frontLeftLocation;
    }

    public Translation2d frontRightLocation() {
        return frontRightLocation;
    }

    public Translation2d backLeftLocation() {
        return backLeftLocation;
    }

    public Translation2d backRightLocation() {
        return backRightLocation;
    }

    public double wheelRadius() {
        return wheelRadius;
    }

    public double wheelCircumference() {
        return wheelCircumference;
    }

    public double driveGearRatio() {
        return driveGearRatio;
    }

    public double steerGearRatio() {
        return steerGearRatio;
    }

    public double distanceBetweenWheels() {
        return distanceBetweenWheels;
    }

    public double wheelDistanceFromCenter() {
        return wheelDistanceFromCenter;
    }

    public int encoderToRevolutionConstant() {
        return encoderToRevolutionConstant;
    }

    public double wheelCoeffFrictionStatic() {
        return wheelCoeffFrictionStatic;
    }

    public double wheelCoeffFrictionDynamic() {
        return wheelCoeffFrictionDynamic;
    }

    public double driveMotorCurrentLimit() {
        return driveMotorCurrentLimit;
    }

    public double steerMotorCurrentLimit() {
        return steerMotorCurrentLimit;
    }

    public double driveMotorStatorCurrentLimit() {
        return driveMotorStatorCurrentLimit;
    }

    public double steerMotorStatorCurrentLimit() {
        return steerMotorStatorCurrentLimit;
    }

    public DCMotor driveMotor() {
        return driveMotor;
    }

    public SwerveConfig withCanBus(String canBus) {
        this.canBus = canBus;
        return this;
    }

    public SwerveConfig withFrontLeftLocation(double x, double y) {
        this.frontLeftLocation = new Translation2d(x, y);
        return this;
    }

    public SwerveConfig withFrontRightLocation(double x, double y) {
        this.frontRightLocation = new Translation2d(x, y);
        return this;
    }

    public SwerveConfig withBackLeftLocation(double x, double y) {
        this.backLeftLocation = new Translation2d(x, y);
        return this;
    }

    public SwerveConfig withBackRightLocation(double x, double y) {
        this.backRightLocation = new Translation2d(x, y);
        return this;
    }

    public SwerveConfig withWheelRadius(double wheelRadius) {
        this.wheelRadius = wheelRadius;
        return this;
    }

    public SwerveConfig withWheelCircumference(double wheelCircumference) {
        this.wheelCircumference = wheelCircumference;
        return this;
    }

    public SwerveConfig withDriveGearRatio(double driveGearRatio) {
        this.driveGearRatio = driveGearRatio;
        return this;
    }

    public SwerveConfig withSteerGearRatio(double steerGearRatio) {
        this.steerGearRatio = steerGearRatio;
        return this;
    }

    public SwerveConfig withEncoderToRevolutionConstant(int encoderToRevolutionConstant) {
        this.encoderToRevolutionConstant = encoderToRevolutionConstant;
        return this;
    }

    public SwerveConfig withDistanceBetweenWheels(double distanceBetweenWheels) {
        this.distanceBetweenWheels = distanceBetweenWheels;
        this.wheelDistanceFromCenter = Math.sqrt(2) * distanceBetweenWheels / 2;
        return this;
    }

    public SwerveConfig withWheelCoeffFrictionStatic(double coefficient) {
        this.wheelCoeffFrictionStatic = coefficient;
        return this;
    }

    public SwerveConfig withWheelCoeffFrictionDynamic(double coefficient) {
        this.wheelCoeffFrictionDynamic = coefficient;
        return this;
    }

    public SwerveConfig withDriveMotorCurrentLimit(double limit) {
        this.driveMotorCurrentLimit = limit;
        return this;
    }

    public SwerveConfig withSteerMotorCurrentLimit(double limit) {
        this.steerMotorCurrentLimit = limit;
        return this;
    }

    public SwerveConfig withDriveMotorStatorCurrentLimit(double limit) {
        this.driveMotorStatorCurrentLimit = limit;
        return this;
    }

    public SwerveConfig withSteerMotorStatorCurrentLimit(double limit) {
        this.steerMotorStatorCurrentLimit = limit;
        return this;
    }

    public SwerveConfig withDriveMotor(DCMotor motor) {
        this.driveMotor = motor;
        return this;
    }
}
