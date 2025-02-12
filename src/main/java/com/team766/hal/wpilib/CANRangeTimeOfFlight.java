package com.team766.hal.wpilib;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.ToFParamsConfigs;
import com.ctre.phoenix6.hardware.CANrange;
import com.ctre.phoenix6.signals.MeasurementHealthValue;
import com.ctre.phoenix6.signals.UpdateModeValue;
import com.team766.hal.TimeOfFlightReader;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import edu.wpi.first.units.measure.Distance;

public class CANRangeTimeOfFlight implements TimeOfFlightReader {
    private CANrange sensor;

    public CANRangeTimeOfFlight(int canID) {
        sensor = new CANrange(canID);
    }

    public CANRangeTimeOfFlight(int canID, String canBus) {
        sensor = new CANrange(canID, canBus);
    }

    @Override
    public void setRange(TimeOfFlightReader.Range range) {
        ToFParamsConfigs config = new ToFParamsConfigs();
        config.UpdateFrequency = 100; // TODO: use a different value?
        switch (range) {
            case Short:
                config.UpdateMode = UpdateModeValue.ShortRangeUserFreq;
                break;
            case Long:
                config.UpdateMode = UpdateModeValue.LongRangeUserFreq;
                break;
        }
        StatusCode status = sensor.getConfigurator().apply(config);
        if (!status.isOK()) {
            Logger.get(Category.HAL)
                    .logData(Severity.ERROR, "Unable to set range: %s", status.toString());
        }
    }

    @Override
    public double getDistance() {
        StatusSignal<Distance> distance = sensor.getDistance();
        if (distance.getStatus().isOK()) {
            return distance.getValue().magnitude() * 1000.;
        }
        Logger.get(Category.HAL)
                .logData(Severity.ERROR, "Unable to get distance: %s", distance.toString());
        return 0.0;
    }

    @Override
    public boolean wasLastMeasurementValid() {
        StatusSignal<MeasurementHealthValue> health = sensor.getMeasurementHealth();
        if (health.getStatus().isOK()) {
            return health.getValue() == MeasurementHealthValue.Good;
        } else {
            Logger.get(Category.HAL)
                    .logData(
                            Severity.ERROR,
                            "Unable to get measurement health: %s",
                            health.toString());
            return false;
        }
    }
}
