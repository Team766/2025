package com.team766.simulator.elements;

import com.team766.simulator.interfaces.ElectricalDevice;

public class ElectricalResistanceSim implements ElectricalDevice {
    // Wire AWG -> Ohms/m (copper)
    // http://www.daycounter.com/Calculators/AWG.phtml
    private static double[] WIRE_RESISTANCE_PER_M =
            new double[] {
                0.000323, 0.000407, 0.000513, 0.000647, 0.000815, 0.00103, 0.00130, 0.00163,
                0.00206, 0.00260, // 0-9
                0.00328, 0.00413, 0.00521, 0.00657, 0.00829, 0.0104, 0.0132, 0.0166, 0.0210,
                0.0264, // 10-19
                0.0333, 0.0420, 0.0530, 0.0668, 0.0842, 0.106, 0.134, 0.169, 0.213, 0.268, // 20-29
                0.339, 0.427, 0.538, 0.679, 0.856, 1.08, 1.36, 1.72, 2.16, 2.73, 3.44, // 30-40
            };

    public static ElectricalResistanceSim makeWires(
            final int awg, final double length, final ElectricalDevice downstream) {
        return new ElectricalResistanceSim(WIRE_RESISTANCE_PER_M[awg] * length / 1000., downstream);
    }

    private final double resistance;

    private ElectricalDevice downstream;

    private ElectricalDevice.Action state;

    public ElectricalResistanceSim(final double resistance_, final ElectricalDevice downstream_) {
        this.resistance = resistance_;
        this.downstream = downstream_;
    }

    @Override
    public ElectricalDevice.Action step(ElectricalDevice.State input, double dt) {
        ElectricalDevice.State downstreamInput =
                new ElectricalDevice.State(input.voltage() - resistance * state.current());
        state = downstream.step(downstreamInput, dt);
        return state;
    }

    @Override
    public String name() {
        return downstream.name() + "+resistance";
    }
}
