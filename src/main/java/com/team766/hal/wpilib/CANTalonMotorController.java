package com.team766.hal.wpilib;

import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.team766.hal.MotorController;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import com.team766.simulator.ProgramInterface;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;

public class CANTalonMotorController extends BaseCTREMotorController implements MotorController {

    private final String deviceName;
    private WPI_TalonSRX m_device;
    private boolean shouldReportDefaultCurrentLimit;

    public CANTalonMotorController(String deviceName, final int deviceNumber) {
        m_device = new WPI_TalonSRX(deviceNumber);
        this.deviceName = deviceName;

        setCurrentLimit(15);
        shouldReportDefaultCurrentLimit = true;
    }

    private void reportDefaultCurrentLimit() {
        if (shouldReportDefaultCurrentLimit) {
            Logger.get(Category.HAL)
                    .logRaw(
                            Severity.ERROR,
                            deviceName
                                    + " is using the default current limit, which is probably"
                                    + " lower than you want. Call"
                                    + " yourMotor.setCurrentLimit(yourCurrentLimitValue); to"
                                    + " resolve this error.");
            shouldReportDefaultCurrentLimit = false;
        }
    }

    @Override
    public void set(final ControlMode mode, double value, double arbitraryFeedForward) {
        reportDefaultCurrentLimit();
        com.ctre.phoenix.motorcontrol.ControlMode ctre_mode = null;
        boolean useFourTermSet = true;
        switch (mode) {
            case PercentOutput:
                ctre_mode = com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput;
                useFourTermSet = false;
                break;
            case Position:
                ctre_mode = com.ctre.phoenix.motorcontrol.ControlMode.Position;
                break;
            case Velocity:
                // Sensor velocity is measured in units per 100ms.
                value /= 10.0;
                ctre_mode = com.ctre.phoenix.motorcontrol.ControlMode.Velocity;
                break;
            case Voltage:
                m_device.setVoltage(value);
                return;
            case Disabled:
                ctre_mode = com.ctre.phoenix.motorcontrol.ControlMode.Disabled;
                useFourTermSet = false;
                break;
            default:
                LoggerExceptionUtils.logException(
                        new UnsupportedOperationException(
                                "invalid mode provided. provided value: " + mode));
                break;
        }
        if (ctre_mode == null) {
            Logger.get(Category.HAL)
                    .logRaw(Severity.ERROR, "CAN ControlMode is not translatable: " + mode);
            ctre_mode = com.ctre.phoenix.motorcontrol.ControlMode.Disabled;
        }
        if (useFourTermSet) {
            m_device.set(ctre_mode, value, DemandType.ArbitraryFeedForward, arbitraryFeedForward);
        } else {
            m_device.set(ctre_mode, value);
        }
    }

    @Override
    public void stopMotor() {
        m_device.set(com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput, 0);
    }

    @Override
    public double getSensorPosition() {
        return m_device.getSelectedSensorPosition(0);
    }

    @Override
    public double getSensorVelocity() {
        // Sensor velocity is returned in units per 100ms.
        return m_device.getSelectedSensorVelocity(0) * 10.0;
    }

    @Override
    public void setSensorPosition(final double position) {
        errorCodeToException(
                ExceptionTarget.THROW, m_device.setSelectedSensorPosition(position, 0, 0));
    }

    @Override
    public void follow(final MotorController leader) {
        try {
            m_device.follow((IMotorController) leader);
        } catch (ClassCastException ex) {
            LoggerExceptionUtils.logException(
                    new IllegalArgumentException(
                            "Talon can only follow another CTRE motor controller", ex));
        }
    }

    @Override
    public void setOpenLoopRamp(final double secondsFromNeutralToFull) {
        errorCodeToException(
                ExceptionTarget.LOG,
                m_device.configOpenloopRamp(secondsFromNeutralToFull, TIMEOUT_MS));
    }

    @Override
    public void setClosedLoopRamp(final double secondsFromNeutralToFull) {
        errorCodeToException(
                ExceptionTarget.LOG,
                m_device.configClosedloopRamp(secondsFromNeutralToFull, TIMEOUT_MS));
    }

    @Override
    public void setFF(final double value) {
        errorCodeToException(ExceptionTarget.LOG, m_device.config_kF(0, value, TIMEOUT_MS));
    }

    @Override
    public void setP(final double value) {
        errorCodeToException(ExceptionTarget.LOG, m_device.config_kP(0, value));
    }

    @Override
    public void setI(final double value) {
        errorCodeToException(ExceptionTarget.LOG, m_device.config_kI(0, value));
    }

    @Override
    public void setD(final double value) {
        errorCodeToException(ExceptionTarget.LOG, m_device.config_kD(0, value));
    }

    @Override
    public void setSelectedFeedbackSensor(final FeedbackDevice feedbackDevice) {
        errorCodeToException(
                ExceptionTarget.LOG, m_device.configSelectedFeedbackSensor(feedbackDevice));
    }

    @Override
    public void setSensorInverted(final boolean inverted) {
        m_device.setSensorPhase(inverted);
    }

    @Override
    public void setOutputRange(final double minOutput, final double maxOutput) {
        errorCodeToException(ExceptionTarget.LOG, m_device.configPeakOutputReverse(minOutput));
        errorCodeToException(ExceptionTarget.LOG, m_device.configPeakOutputForward(maxOutput));
    }

    @Override
    public void setCurrentLimit(final double ampsLimit) {
        errorCodeToException(ExceptionTarget.LOG, m_device.configPeakCurrentLimit(0));
        errorCodeToException(ExceptionTarget.LOG, m_device.configPeakCurrentDuration(10));
        errorCodeToException(
                ExceptionTarget.LOG, m_device.configContinuousCurrentLimit((int) ampsLimit));
        m_device.enableCurrentLimit(true);
        shouldReportDefaultCurrentLimit = false;
    }

    @Override
    public void restoreFactoryDefault() {
        errorCodeToException(ExceptionTarget.LOG, m_device.configFactoryDefault());
    }

    @Override
    public double get() {
        return m_device.get();
    }

    @Override
    public void set(final double power) {
        reportDefaultCurrentLimit();
        m_device.set(power);
    }

    @Override
    public void setInverted(final boolean isInverted) {
        m_device.setInverted(isInverted);
    }

    @Override
    public boolean getInverted() {
        return m_device.getInverted();
    }

    @Override
    public void setNeutralMode(final NeutralMode neutralMode) {
        m_device.setNeutralMode(neutralMode);
    }

    @Override
    public void simulationMotorUpdate() {
        ProgramInterface.canMotorControllerChannels[m_device.getDeviceID()].command.percentOutput =
                m_device.getSimCollection().getMotorOutputLeadVoltage()
                        / RoboRioSim.getVInVoltage();
    }

    @Override
    public void simulationSensorUpdate(double deltaTime) {
        m_device.getSimCollection().setBusVoltage(RoboRioSim.getVInVoltage());
        m_device.getSimCollection()
                .setQuadratureRawPosition(
                        (int)
                                ProgramInterface.canMotorControllerChannels[m_device.getDeviceID()]
                                        .status
                                        .sensorPosition);
        m_device.getSimCollection()
                .setQuadratureVelocity(
                        (int)
                                (ProgramInterface.canMotorControllerChannels[m_device.getDeviceID()]
                                                .status
                                                .sensorVelocity
                                        / 0.1));
        // TODO:
        // m_device.getSimCollection().setStatorCurrent();
        // m_device.getSimCollection().setSupplyCurrent();
        // TODO: simulation of limit switch inputs
        // m_device.getSimCollection().setLimitFwd();
        // m_device.getSimCollection().setLimitRev();
    }
}
