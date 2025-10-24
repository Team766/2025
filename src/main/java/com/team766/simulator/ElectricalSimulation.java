package com.team766.simulator;

import com.team766.library.RateLimiter;
import com.team766.simulator.interfaces.ElectricalDevice;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.simulation.PDPSim;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ElectricalSimulation {
    private final PDPSim pdpSim = new PDPSim();

    private double nominalVoltage = Parameters.BATTERY_VOLTAGE;
    private double primaryResistance = Parameters.PRIMARY_ELECTRICAL_RESISTANCE;

    public class BranchInfo {
        public final ElectricalDevice device;
        public ElectricalDevice.Action flow;

        public final DoublePublisher branchVoltagePublisher;

        public BranchInfo(ElectricalDevice device) {
            this.device = device;
            this.flow = new ElectricalDevice.Action(0);
            this.branchVoltagePublisher =
                    NetworkTableInstance.getDefault().getDoubleTopic("Sim " + device.name() + " Current").publish();
        }
    }

    private final RateLimiter publishRate = new RateLimiter(Parameters.LOGGING_PERIOD);
    private final DoublePublisher systemVoltagePublisher =
            NetworkTableInstance.getDefault().getDoubleTopic("Sim Electrical System Voltage").publish();

    private ArrayList<BranchInfo> branchCircuits = new ArrayList<BranchInfo>();

    private ElectricalDevice.State systemState;

    public ElectricalSimulation() {
        systemState = new ElectricalDevice.State(nominalVoltage);
    }

    public void addDevice(ElectricalDevice device) {
        branchCircuits.add(new BranchInfo(device));
    }

    public void step(double dt) {
        int channel = 0;
        double current = 0.0;
        for (BranchInfo branch : branchCircuits) {
            branch.flow = branch.device.step(systemState, dt);
            current += branch.flow.current();
            pdpSim.setCurrent(channel, branch.flow.current());
            channel++;
        }
        systemState =
                new ElectricalDevice.State(
                        Math.max(0, nominalVoltage - primaryResistance * current));
        pdpSim.setVoltage(systemState.voltage());

        if (publishRate.next()) {
            systemVoltagePublisher.set(getSystemVoltage());
            for (BranchInfo branch : branchCircuits) {
                branch.branchVoltagePublisher.set(branch.flow.current());
            }
        }
    }

    public double getSystemVoltage() {
        return systemState.voltage();
    }

    public LinkedHashMap<String, Double> getBranchCurrents() {
        var currents = new LinkedHashMap<String, Double>();
        for (BranchInfo branch : branchCircuits) {
            currents.put(branch.device.name(), branch.flow.current());
        }
        return currents;
    }
}
