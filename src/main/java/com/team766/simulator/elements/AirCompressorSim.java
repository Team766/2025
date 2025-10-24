package com.team766.simulator.elements;

import com.team766.math.Maths;
import com.team766.simulator.PneumaticsSimulation;
import com.team766.simulator.interfaces.ElectricalDevice;
import com.team766.simulator.interfaces.PneumaticDevice;
import java.util.Arrays;

public class AirCompressorSim implements ElectricalDevice, PneumaticDevice {
    private record ControlPoint(double pressure, double flowRate, double current) {}

    private static final double CFM_TO_M3_PER_SEC = 0.000471947443;

    // Values for http://www.andymark.com/product-p/am-2005.htm
    private static final double NOMINAL_VOLTAGE = 12;
    // Units: PSI, CFM, Amps
    private static final ControlPoint[] CONTROL_POINTS_US = {
        new ControlPoint(0., 0.88, 7.),
        new ControlPoint(10., 0.50, 8.),
        new ControlPoint(20., 0.43, 8.),
        new ControlPoint(30., 0.36, 9.),
        new ControlPoint(40., 0.30, 9.),
        new ControlPoint(50., 0.27, 9.),
        new ControlPoint(60., 0.25, 10.),
        new ControlPoint(70., 0.24, 10.),
        new ControlPoint(80., 0.24, 10.),
        new ControlPoint(90., 0.23, 11.),
        new ControlPoint(100., 0.22, 11.),
        new ControlPoint(110., 0.22, 11.),
    };
    private static final ControlPoint[] CONTROL_POINTS_METRIC =
            Arrays.stream(CONTROL_POINTS_US)
                    .map(
                            p ->
                                    new ControlPoint(
                                            p.pressure * PneumaticsSimulation.PSI_TO_PASCALS,
                                            p.flowRate * CFM_TO_M3_PER_SEC,
                                            p.current))
                    .toArray(ControlPoint[]::new);

    private static double getCurrent(double pressure) {
        return Maths.interpolate(
                CONTROL_POINTS_METRIC, pressure, ControlPoint::pressure, ControlPoint::current);
    }

    private static double getFlowRate(double pressure) {
        return Maths.interpolate(
                CONTROL_POINTS_METRIC, pressure, ControlPoint::pressure, ControlPoint::flowRate);
    }

    private boolean isOn = true;

    private ElectricalDevice.State electricalState = new ElectricalDevice.State(0);
    private PneumaticDevice.State pneumaticState = new PneumaticDevice.State(0);

    public void setIsOn(final boolean on) {
        isOn = on;
    }

    public double getCurrent() {
        if (isOn) {
            double nominalCurrent = getCurrent(pneumaticState.pressure());
            double adjustedCurrent = nominalCurrent * electricalState.voltage() / NOMINAL_VOLTAGE;
            return adjustedCurrent;
        } else {
            return 0;
        }
    }

    @Override
    public ElectricalDevice.Action step(ElectricalDevice.State state, double dt) {
        electricalState = state;
        return new ElectricalDevice.Action(getCurrent());
    }

    @Override
    public PneumaticDevice.Action step(PneumaticDevice.State state, double dt) {
        pneumaticState = state;
        double nominalFlowRate = getFlowRate(pneumaticState.pressure());
        double adjustedFlowRate = nominalFlowRate * electricalState.voltage() / NOMINAL_VOLTAGE;
        double flowVolume = adjustedFlowRate * dt;
        return new PneumaticDevice.Action(flowVolume, 0);
    }

    @Override
    public String name() {
        return "AirCompressor";
    }
}
