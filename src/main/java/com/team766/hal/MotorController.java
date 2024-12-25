package com.team766.hal;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.Mechanism;
import com.team766.library.ValueProvider;
import java.util.function.Supplier;

/**
 * Interface for motor controlling devices.
 */
public interface MotorController extends BasicMotorController {
    // Almost all systems on our robot work on the scale of 0-12 V.
    // 0.1 V seems like a reasonable tolerance for that scale.
    static final double VOLTAGE_TOLERANCE = 0.1;

    enum Type {
        VictorSP,
        VictorSPX,
        TalonSRX,
        SparkMax,
        TalonFX,
    }

    enum ControlMode {
        PercentOutput,
        Position,
        Velocity,
        Voltage,
        Disabled,
    }

    /**
     * Common interface for setting the power outputu by a motor controller.
     *
     * @param power The power to set. Value should be between -1.0 and 1.0.
     */
    void set(double power);

    /**
     * Sets the appropriate output on the motor controller, depending on the mode.
     * @param mode The output mode to apply.
     * In PercentOutput, the output is between -1.0 and 1.0, with 0.0 as stopped.
     * In Current mode, output value is in amperes.
     * In Velocity mode, output value is in position change / 100ms.
     * In Position mode, output value is in encoder ticks or an analog value,
     * depending on the sensor.
     * In Follower mode, the output value is the integer device ID of the talon to
     * duplicate.
     *
     * @param value The setpoint value, as described above.
     */
    default void set(ControlMode mode, double value) {
        set(mode, value, 0.0);
    }

    void set(ControlMode mode, double value, double arbitraryFeedForward);

    /**
     * Common interface for inverting direction of a motor controller.
     *
     * This changes the direction of the motor and sensor together. To change the
     * direction of the sensor relative to the direction of the motor,
     * use setSensorInverted.
     *
     * @param isInverted The state of inversion true is inverted.
     */
    void setInverted(boolean isInverted);

    /**
     * Common interface for returning if a motor controller is in the inverted
     * state or not.
     *
     * @return isInverted The state of the inversion true is inverted.
     */
    boolean getInverted();

    /**
     * Stops motor movement. Motor can be moved again by calling set without having
     * to re-enable the motor.
     */
    void stopMotor();

    /**
     * Read the motor position from the sensor attached to the motor controller.
     */
    double getSensorPosition();

    /**
     * Read the motor velocity from the sensor attached to the motor controller.
     */
    double getSensorVelocity();

    /**
     * Sets the motors encoder value to the given position.
     *
     * @param position The desired set position
     */
    void setSensorPosition(double position);

    void follow(MotorController leader);

    void setNeutralMode(NeutralMode neutralMode);

    PIDConfig getPIDConfig();

    default void applyPIDConfig() {
        getPIDConfig().apply(this);
    }

    void setP(double value);

    default void setP(ValueProvider<Double> value) {
        getPIDConfig().setP(value);
    }

    void setI(double value);

    default void setI(ValueProvider<Double> value) {
        getPIDConfig().setI(value);
    }

    void setD(double value);

    default void setD(ValueProvider<Double> value) {
        getPIDConfig().setD(value);
    }

    void setFF(double value);

    default void setFF(ValueProvider<Double> value) {
        getPIDConfig().setFF(value);
    }

    void setSelectedFeedbackSensor(FeedbackDevice feedbackDevice);

    /**
     * Set whether to reverse the sensor relative to the direction of the motor.
     *
     * This is different from setInverted, which sets the direction of both the
     * motor and sensor together.
     *
     * @param inverted The state of inversion true is inverted.
     */
    void setSensorInverted(boolean inverted);

    void setOutputRange(double minOutput, double maxOutput);

    default void setOutputRange(ValueProvider<Double> minOutput, ValueProvider<Double> maxOutput) {
        getPIDConfig().setOutputRange(minOutput, maxOutput);
    }

    void setCurrentLimit(double ampsLimit);

    void restoreFactoryDefault();

    void setOpenLoopRamp(double secondsFromNeutralToFull);

    void setClosedLoopRamp(double secondsFromNeutralToFull);

    double getOutputVoltage();

    default Mechanism.Directive requestStop() {
        return requestPercentOutput(0.0);
    }

    default Mechanism.Directive requestPercentOutput(double percentOutput) {
        set(ControlMode.PercentOutput, percentOutput);
        return () -> get() == percentOutput;
    }

    default Mechanism.Directive requestPosition(
            double targetPosition, double positionErrorThreshold, double velocityThreshold) {
        set(ControlMode.Position, targetPosition);
        return () ->
                Math.abs(targetPosition - getSensorPosition()) <= positionErrorThreshold
                        && Math.abs(getSensorVelocity()) <= velocityThreshold;
    }

    default Mechanism.Directive requestPosition(
            double targetPosition,
            double positionErrorThreshold,
            double velocityThreshold,
            double arbitraryFeedForward) {
        set(ControlMode.Position, targetPosition, arbitraryFeedForward);
        return () ->
                Math.abs(targetPosition - getSensorPosition()) <= positionErrorThreshold
                        && Math.abs(getSensorVelocity()) <= velocityThreshold;
    }

    default Mechanism.Directive requestPosition(
            double targetPosition,
            double positionErrorThreshold,
            double velocityThreshold,
            Supplier<Double> arbitraryFeedForward) {
        return () -> {
            set(ControlMode.Position, targetPosition, arbitraryFeedForward.get());
            return Math.abs(targetPosition - getSensorPosition()) <= positionErrorThreshold
                    && Math.abs(getSensorVelocity()) <= velocityThreshold;
        };
    }

    default Mechanism.Directive requestVelocity(
            double targetVelocity, double velocityErrorThreshold) {
        set(MotorController.ControlMode.Velocity, targetVelocity);
        return () -> Math.abs(targetVelocity - getSensorVelocity()) <= velocityErrorThreshold;
    }

    default Mechanism.Directive requestVoltage(double targetVoltage) {
        set(MotorController.ControlMode.Voltage, targetVoltage);
        return () -> Math.abs(targetVoltage - getOutputVoltage()) <= VOLTAGE_TOLERANCE;
    }
}
