package com.team766.simulator;

import com.team766.library.RateLimiter;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import com.team766.simulator.interfaces.PneumaticDevice;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.ArrayList;

public class PneumaticsSimulation {
    public static final double PSI_TO_PASCALS = 6894.75729;

    private static class BranchCircuit {
        public PneumaticDevice device;
        public double regulatedPressure;
    }

    private final RateLimiter publishRate = new RateLimiter(Parameters.LOGGING_PERIOD);
    private final DoublePublisher systemPressurePublisher =
            NetworkTableInstance.getDefault().getDoubleTopic("Sim Pneumatic System Pressure").publish();

    private ArrayList<BranchCircuit> branchCircuits = new ArrayList<BranchCircuit>();

    private double systemPressure = Parameters.STARTING_PRESSURE;
    private double compressedAirVolume = 0.0;
    private boolean initialized = false;

    public void addDevice(final PneumaticDevice device, final double regulatedPressure) {
        BranchCircuit circuit = new BranchCircuit();
        circuit.device = device;
        circuit.regulatedPressure = regulatedPressure;
        branchCircuits.add(circuit);
    }

    public void step(double dt) {
        double flowVolume = 0.0;
        double systemVolume = 0.0;
        for (BranchCircuit circuit : branchCircuits) {
            double devicePressure = Math.min(circuit.regulatedPressure, systemPressure);
            PneumaticDevice.State inputState = new PneumaticDevice.State(devicePressure);
            PneumaticDevice.Action deviceAction = circuit.device.step(inputState, dt);
            // TODO: implement relieving pressure regulator (make sure device pressure doesn't
            // exceed circuit.regulatedPressure, even when including flow volume)
            flowVolume += deviceAction.flowVolume();
            systemVolume += deviceAction.deviceVolume();
        }
        compressedAirVolume += flowVolume;
        if (!initialized) {
            compressedAirVolume =
                    systemVolume
                            * (systemPressure + PhysicalConstants.ATMOSPHERIC_PRESSURE)
                            / PhysicalConstants.ATMOSPHERIC_PRESSURE;
            if (systemVolume == 0.) {
                Logger.get(Category.HAL).logRaw(Severity.WARNING, "Your pneumatics system has no storage volume");
            }
            initialized = true;
        }
        if (systemVolume == 0) {
            systemPressure = compressedAirVolume > 0 ? Double.POSITIVE_INFINITY : 0;
        } else {
            // TODO: This doesn't return the expected value when there is volume at less than 120psi
            systemPressure =
                    compressedAirVolume / systemVolume * PhysicalConstants.ATMOSPHERIC_PRESSURE
                            - PhysicalConstants.ATMOSPHERIC_PRESSURE;
        }

        if (publishRate.next()) {
            systemPressurePublisher.set(getSystemPressure() / PneumaticsSimulation.PSI_TO_PASCALS);
        }
    }

    public double getSystemPressure() {
        return systemPressure;
    }

    // Simulate the system venting all of its compressed air (e.g. someone opened the release valve;
    // to simulate the pneumatics system becoming compromised, call this method on every simulation
    // tick)
    public void ventPressure() {
        systemPressure = 0;
        compressedAirVolume = 0;
    }
}
