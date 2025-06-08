package com.team766.hal.wpilib;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.ToFParamsConfigs;
import com.ctre.phoenix6.hardware.CANrange;
import com.ctre.phoenix6.signals.MeasurementHealthValue;
import com.ctre.phoenix6.signals.UpdateModeValue;
import com.team766.hal.TimeOfFlightReader;
import com.team766.logging.LoggerExceptionUtils;
import edu.wpi.first.units.measure.Distance;
import java.util.Optional;

public class CANRangeTimeOfFlight implements TimeOfFlightReader {
    private final CANrange sensor;

    public CANRangeTimeOfFlight(int canID) {
        sensor = new CANrange(canID);
    }

    public CANRangeTimeOfFlight(int canID, String canBus) {
        sensor = new CANrange(canID, canBus);
    }

    private enum ExceptionTarget {
        THROW,
        LOG,
    }

    private static void statusCodeToException(
            final ExceptionTarget throwEx, final StatusCode code) {
        if (code.isOK()) {
            return;
        }
        var ex = new TimeOfFlightCommandFailedException(code.toString());
        switch (throwEx) {
            case THROW:
                throw ex;
            default:
            case LOG:
                LoggerExceptionUtils.logException(ex);
                break;
        }
    }

    @Override
    public void setRange(TimeOfFlightReader.Range range) {
        ToFParamsConfigs config = new ToFParamsConfigs();
        config.UpdateFrequency = 100.; // TODO: use a different value?
        config.UpdateMode =
                switch (range) {
                    case Short -> UpdateModeValue.ShortRangeUserFreq;
                    case Long -> UpdateModeValue.LongRangeUserFreq;
                };
        statusCodeToException(ExceptionTarget.LOG, sensor.getConfigurator().apply(config));
    }

    @Override
    public Optional<Double> getDistance() {
        StatusSignal<Distance> distance = sensor.getDistance();
        if (distance.getStatus().isOK()) {
            return Optional.of(distance.getValue().magnitude());
        }
        statusCodeToException(ExceptionTarget.LOG, distance.getStatus());
        return Optional.empty();
    }

    @Override
    public boolean wasLastMeasurementValid() {
        StatusSignal<MeasurementHealthValue> health = sensor.getMeasurementHealth();
        if (health.getStatus().isOK()) {
            return health.getValue() == MeasurementHealthValue.Good;
        } else {
            statusCodeToException(ExceptionTarget.LOG, health.getStatus());
            return false;
        }
    }

    @Override
    public Optional<Double> getAmbientSignal() {
        StatusSignal<Double> ambient = sensor.getAmbientSignal();
        if (ambient.getStatus().isOK()) {
            return Optional.of(ambient.getValue());
        }
        statusCodeToException(ExceptionTarget.LOG, ambient.getStatus());
        return Optional.empty();
    }
}
