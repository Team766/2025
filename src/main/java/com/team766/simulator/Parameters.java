package com.team766.simulator;

import edu.wpi.first.math.util.Units;

public class Parameters {
    public static final double TIME_STEP = 0.0001; // seconds
    public static final double DURATION = 10.0; // seconds

    public static final double LOGGING_PERIOD = 0.005; // seconds

    public static final double BATTERY_VOLTAGE = 12.4; // volts
    public static final double PRIMARY_ELECTRICAL_RESISTANCE = 0.018 + 0.01; // ohms

    public static final double STARTING_PRESSURE =
            120 * PneumaticsSimulation.PSI_TO_PASCALS; // pascals (relative)

    public static final double FULL_ROBOT_MASS = Units.lbsToKilograms(145);
}
