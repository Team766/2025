package com.team766.hal.mock;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.hal.MotorController;

public class MockMotorController implements MotorController {

    private ControlMode controlMode = ControlMode.PercentOutput;
    private double setpoint = 0.;
    private double arbitraryFeedForward = 0.;
    private boolean inverted = false;
    private double sensorPosition = 0.;
    private double sensorVelocity = 0.;

    public MockMotorController(final int index) {}

    @Override
    public double get() {
        if (controlMode != ControlMode.PercentOutput) {
            throw new UnsupportedOperationException();
        }
        return setpoint;
    }

    @Override
    public void restoreFactoryDefault() {
        // No-op
    }

    @Override
    public void set(ControlMode mode, double value, double arbitraryFeedForward) {
        this.controlMode = mode;
        this.setpoint = value;
        this.arbitraryFeedForward = arbitraryFeedForward;
    }

    @Override
    public void setInverted(boolean isInverted) {
        inverted = isInverted;
    }

    @Override
    public boolean getInverted() {
        return inverted;
    }

    @Override
    public double getSensorPosition() {
        return sensorPosition;
    }

    @Override
    public double getSensorVelocity() {
        return sensorVelocity;
    }

    @Override
    public void setSensorPosition(double position) {
        sensorPosition = position;
    }

    public void setSensorVelocity(double velocity) {
        sensorVelocity = velocity;
    }

    @Override
    public void follow(MotorController leader) {}

    @Override
    public void setNeutralMode(NeutralMode neutralMode) {}

    @Override
    public void setP(double value) {}

    @Override
    public void setI(double value) {}

    @Override
    public void setD(double value) {}

    @Override
    public void setFF(double value) {}

    @Override
    public void setSelectedFeedbackSensor(FeedbackDevice feedbackDevice) {}

    @Override
    public void setSensorInverted(boolean inverted) {}

    @Override
    public void setOutputRange(double minOutput, double maxOutput) {}

    @Override
    public void setCurrentLimit(double ampsLimit) {}

    @Override
    public void setOpenLoopRamp(double secondsFromNeutralToFull) {}

    @Override
    public void setClosedLoopRamp(double secondsFromNeutralToFull) {}
}
