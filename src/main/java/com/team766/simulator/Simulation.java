package com.team766.simulator;

import com.team766.simulator.elements.AirCompressorSim;
import com.team766.simulator.elements.AirReservoirSim;

import edu.wpi.first.wpilibj.simulation.CTREPCMSim;

public class Simulation {
    public final ElectricalSimulation electricalSystem = new ElectricalSimulation();
    public final PneumaticsSimulation pneumaticsSystem = new PneumaticsSimulation();

    private final AirCompressorSim compressor = new AirCompressorSim();
    private final CTREPCMSim pcmSim = new CTREPCMSim();

    public Simulation(double pneumaticsStorageVolume) {
        pneumaticsSystem.addDevice(
                new AirReservoirSim(pneumaticsStorageVolume), 120 * PneumaticsSimulation.PSI_TO_PASCALS);

        electricalSystem.addDevice(compressor);
        pneumaticsSystem.addDevice(compressor, 120 * PneumaticsSimulation.PSI_TO_PASCALS);

        pcmSim.setClosedLoopEnabled(true);
    }

    public void step(double dt) {
        pcmSim.setCompressorCurrent(compressor.getCurrent());
        // Model behavior of Nason Pressure Switch (am-2006)
        if (pneumaticsSystem.getSystemPressure() >= 120) {
            pcmSim.setPressureSwitch(false);
        } else if (pneumaticsSystem.getSystemPressure() <= 95) {
            pcmSim.setPressureSwitch(true);
        }
        compressor.setIsOn(pcmSim.getCompressorOn());

        electricalSystem.step(dt);
        pneumaticsSystem.step(dt);
    }
}
