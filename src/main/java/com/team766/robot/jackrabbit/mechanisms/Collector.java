package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.hal.wpilib.CTREPhoenix6Utils.statusCodeToException;

import com.ctre.phoenix6.configs.CANrangeConfiguration;
import com.ctre.phoenix6.hardware.CANrange;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.UpdateModeValue;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.wpilib.CTREPhoenix6Utils.ExceptionTarget;
import com.team766.robot.jackrabbit.HardwareConfig;

public class Collector extends MechanismWithStatus<Collector.CollectorStatus> {
    public record CollectorStatus(boolean ballInCollector) implements Status {}

    private final TalonFX motor;
    private final CANrange proximitySensor;

    public Collector() {
        motor =
                new TalonFX(
                        HardwareConfig.Motor.COLLECTOR.canId(),
                        HardwareConfig.Motor.COLLECTOR.canBus());
        proximitySensor =
                new CANrange(
                        HardwareConfig.CANrange.COLLECTOR.canId(),
                        HardwareConfig.CANrange.COLLECTOR.canBus());

        CANrangeConfiguration sensorConfig = new CANrangeConfiguration();
        sensorConfig.FovParams.FOVRangeX = 6.75;
        sensorConfig.FovParams.FOVRangeY = 6.75;
        sensorConfig.ToFParams.UpdateMode = UpdateModeValue.ShortRange100Hz;
        sensorConfig.ProximityParams.ProximityThreshold = 0.4;
        statusCodeToException(
                ExceptionTarget.THROW, proximitySensor.getConfigurator().apply(sensorConfig));
    }

    public void stop() {
        motor.stopMotor();
    }

    public void intake() {
        motor.setVoltage(10);
    }

    public void outtake() {
        motor.setVoltage(-10);
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected CollectorStatus updateStatus() {
        return new CollectorStatus(proximitySensor.getIsDetected(false).getValue());
    }
}
