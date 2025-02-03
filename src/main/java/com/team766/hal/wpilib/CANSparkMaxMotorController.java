package com.team766.hal.wpilib;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.revrobotics.REVLibError;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkAnalogSensor;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.ClosedLoopConfig.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team766.hal.MotorController;
import com.team766.hal.MotorControllerCommandFailedException;
import com.team766.logging.LoggerExceptionUtils;
import java.util.function.Function;
import java.util.function.Supplier;

public class CANSparkMaxMotorController extends SparkMax implements MotorController {

    private Supplier<Double> sensorPositionSupplier;
    private Supplier<Double> sensorVelocitySupplier;
    private Function<Double, REVLibError> sensorPositionSetter;
    private Function<Boolean, REVLibError> sensorInvertedSetter;
    private boolean sensorInverted = false;

    public CANSparkMaxMotorController(final int deviceId) {
        super(deviceId, MotorType.kBrushless);

        SparkMaxConfig config = new SparkMaxConfig();
        config.smartCurrentLimit(10, 80, 200);
        configureAndCheckRevError(config);

        // Set default feedback device. This ensures that our implementations of
        // getSensorPosition/getSensorVelocity return values that match what the
        // device's PID controller is using.
        selectPrimaryFeedbackSensor();
    }

    private enum ExceptionTarget {
        THROW,
        LOG,
    }

    private static void revErrorToException(final ExceptionTarget throwEx, final REVLibError err) {
        if (err == REVLibError.kOk) {
            return;
        }
        var ex = new MotorControllerCommandFailedException(err.toString());
        switch (throwEx) {
            case THROW:
                throw ex;
            default:
            case LOG:
                LoggerExceptionUtils.logException(ex);
                break;
        }
    }

    private void configureAndCheckRevError(final SparkMaxConfig config) {
        revErrorToException(
                ExceptionTarget.LOG,
                configure(
                        config, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters));
    }

    @Override
    public double getSensorPosition() {
        return sensorPositionSupplier.get();
    }

    @Override
    public double getSensorVelocity() {
        return sensorVelocitySupplier.get();
    }

    @Override
    public void set(final ControlMode mode, final double value, double arbitraryFeedForward) {
        switch (mode) {
            case Disabled:
                disable();
                break;
            case PercentOutput:
                getClosedLoopController().setReference(value, SparkMax.ControlType.kDutyCycle);
                break;
            case Position:
                getClosedLoopController()
                        .setReference(
                                value,
                                SparkMax.ControlType.kPosition,
                                ClosedLoopSlot.kSlot0,
                                arbitraryFeedForward);
                break;
            case Velocity:
                getClosedLoopController()
                        .setReference(
                                value,
                                SparkMax.ControlType.kVelocity,
                                ClosedLoopSlot.kSlot0,
                                arbitraryFeedForward);
                break;
            case Voltage:
                getClosedLoopController().setReference(value, SparkMax.ControlType.kVoltage);
            default:
                throw new IllegalArgumentException("Unsupported control mode " + mode);
        }
    }

    @Override
    public void setSensorPosition(final double position) {
        revErrorToException(ExceptionTarget.THROW, sensorPositionSetter.apply(position));
    }

    @Override
    public void follow(final MotorController leader) {
        SparkMaxConfig config = new SparkMaxConfig();
        try {
            config.follow((SparkMax) leader);
        } catch (ClassCastException ex) {
            LoggerExceptionUtils.logException(
                    new IllegalArgumentException(
                            "Spark Max can only follow another Spark Max", ex));
            return;
        }
        configureAndCheckRevError(config);
    }

    @Override
    public void setNeutralMode(final NeutralMode neutralMode) {
        SparkMaxConfig config = new SparkMaxConfig();

        switch (neutralMode) {
            case Brake:
                config.idleMode(SparkBaseConfig.IdleMode.kBrake);
                break;
            case Coast:
                config.idleMode(SparkBaseConfig.IdleMode.kCoast);
                break;
            default:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("Unsupported neutral mode " + neutralMode));
                return;
        }
        configureAndCheckRevError(config);
    }

    @Override
    public void setP(final double value) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.p(value);
        configureAndCheckRevError(config);
    }

    @Override
    public void setI(final double value) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.i(value);
        configureAndCheckRevError(config);
    }

    @Override
    public void setD(final double value) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.d(value);
        configureAndCheckRevError(config);
    }

    @Override
    public void setFF(final double value) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.velocityFF(value);
        configureAndCheckRevError(config);
    }

    private void selectPrimaryFeedbackSensor() {
        RelativeEncoder encoder = getEncoder();
        sensorPositionSupplier = encoder::getPosition;
        sensorVelocitySupplier = encoder::getVelocity;
        sensorPositionSetter = encoder::setPosition;
        sensorInvertedSetter =
                (inverted) -> {
                    SparkMaxConfig config = new SparkMaxConfig();
                    config.encoder.inverted(inverted);
                    return configure(
                            config,
                            ResetMode.kNoResetSafeParameters,
                            PersistMode.kPersistParameters);
                };

        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.feedbackSensor(FeedbackSensor.kPrimaryEncoder);
        configureAndCheckRevError(config);
    }

    @Override
    public void setSelectedFeedbackSensor(final FeedbackDevice feedbackDevice) {
        switch (feedbackDevice) {
            case Analog:
                {
                    SparkAnalogSensor analog = getAnalog();
                    sensorPositionSupplier = analog::getPosition;
                    sensorVelocitySupplier = analog::getVelocity;
                    sensorPositionSetter = (pos) -> REVLibError.kOk;
                    sensorInvertedSetter =
                            (inverted) -> {
                                SparkMaxConfig config = new SparkMaxConfig();
                                config.analogSensor.inverted(inverted);
                                return configure(
                                        config,
                                        ResetMode.kNoResetSafeParameters,
                                        PersistMode.kPersistParameters);
                            };
                    SparkMaxConfig config = new SparkMaxConfig();
                    config.closedLoop.feedbackSensor(FeedbackSensor.kAnalogSensor);
                    configureAndCheckRevError(config);
                    return;
                }
            case CTRE_MagEncoder_Absolute:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support CTRE Mag Encoder"));
            case CTRE_MagEncoder_Relative:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support CTRE Mag Encoder"));
            case None:
                return;
            case PulseWidthEncodedPosition:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support PWM sensors"));
            case QuadEncoder:
                // TODO: should we pass a real counts-per-rev scale here?
                RelativeEncoder encoder = getAlternateEncoder();
                sensorPositionSupplier = encoder::getPosition;
                sensorVelocitySupplier = encoder::getVelocity;
                sensorPositionSetter = encoder::setPosition;
                sensorInvertedSetter =
                        (inverted) -> {
                            SparkMaxConfig config = new SparkMaxConfig();
                            config.alternateEncoder.inverted(inverted);
                            return configure(
                                    config,
                                    ResetMode.kNoResetSafeParameters,
                                    PersistMode.kPersistParameters);
                        };
                SparkMaxConfig config = new SparkMaxConfig();
                config.closedLoop.feedbackSensor(FeedbackSensor.kAlternateOrExternalEncoder);
                configureAndCheckRevError(config);
                return;
            case RemoteSensor0:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support remote sensors"));
            case RemoteSensor1:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support remote sensors"));
            case SensorDifference:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support SensorDifference"));
            case SensorSum:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support SensorSum"));
            case SoftwareEmulatedSensor:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException(
                                "SparkMax does not support SoftwareEmulatedSensor"));
            case Tachometer:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("SparkMax does not support Tachometer"));
            default:
                LoggerExceptionUtils.logException(
                        new IllegalArgumentException("Unsupported sensor type " + feedbackDevice));
        }
    }

    @Override
    public void setSensorInverted(final boolean inverted) {
        sensorInverted = inverted;
        revErrorToException(ExceptionTarget.LOG, sensorInvertedSetter.apply(inverted));
    }

    @Override
    public void setOutputRange(final double minOutput, final double maxOutput) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.outputRange(minOutput, maxOutput);
        configureAndCheckRevError(config);
    }

    public void setCurrentLimit(final double ampsLimit) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.smartCurrentLimit((int) (ampsLimit));
        configureAndCheckRevError(config);
    }

    @Override
    public void restoreFactoryDefault() {
        SparkMaxConfig config = new SparkMaxConfig();
        revErrorToException(
                ExceptionTarget.LOG,
                configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
        selectPrimaryFeedbackSensor();
    }

    @Override
    public void setOpenLoopRamp(final double secondsFromNeutralToFull) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.openLoopRampRate(secondsFromNeutralToFull);
        configureAndCheckRevError(config);
    }

    @Override
    public void setClosedLoopRamp(final double secondsFromNeutralToFull) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoopRampRate(secondsFromNeutralToFull);
        configureAndCheckRevError(config);
    }

    @Override
    public void setInverted(boolean isInverted) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.inverted(isInverted);
        configureAndCheckRevError(config);
    }

    @Override
    public boolean getInverted() {
        return configAccessor.getInverted();
    }
}
