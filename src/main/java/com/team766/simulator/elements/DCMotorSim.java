package com.team766.simulator.elements;

import com.team766.simulator.interfaces.ElectricalDevice;
import com.team766.simulator.interfaces.MechanicalAngularDevice;

public class DCMotorSim implements ElectricalDevice, MechanicalAngularDevice {
    // TODO: Add rotor inertia
    // TODO: Add thermal effects

    public static DCMotorSim makeCIM(String name) {
        return new DCMotorSim(name, edu.wpi.first.math.system.plant.DCMotor.getCIM(1));
    }

    public static DCMotorSim makeKrakenX60(String name) {
        return new DCMotorSim(name, edu.wpi.first.math.system.plant.DCMotor.getKrakenX60(1));
    }

    public static DCMotorSim makeKrakenX60Foc(String name) {
        return new DCMotorSim(name, edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1));
    }

    public static DCMotorSim make775Pro(String name) {
        return new DCMotorSim(name, edu.wpi.first.math.system.plant.DCMotor.getVex775Pro(1));
    }

    public static DCMotorSim makeNeo(String name) {
        return new DCMotorSim(name, edu.wpi.first.math.system.plant.DCMotor.getNEO(1));
    }

    public static DCMotorSim makeNeo550(String name) {
        return new DCMotorSim(name, edu.wpi.first.math.system.plant.DCMotor.getNeo550(1));
    }

    private ElectricalDevice.Action electricalAction = new ElectricalDevice.Action(0);
    private MechanicalAngularDevice.State mechanicalState = new MechanicalAngularDevice.State(0, 0);

    private final String m_name;

    private final edu.wpi.first.math.system.plant.DCMotor motor;

    public DCMotorSim(String name, edu.wpi.first.math.system.plant.DCMotor motor) {
        m_name = name;
        this.motor = motor;
    }

    @Override
    public MechanicalAngularDevice.Action step(MechanicalAngularDevice.State state, double dt) {
        mechanicalState = state;

        return new MechanicalAngularDevice.Action(motor.getTorque(electricalAction.current()));
    }

    @Override
    public ElectricalDevice.Action step(ElectricalDevice.State state, double dt) {
        electricalAction =
                new ElectricalDevice.Action(
                        motor.getCurrent(mechanicalState.angularVelocity(), state.voltage()));
        return electricalAction;
    }

    /*package*/ MechanicalAngularDevice.State getMechanicalState() {
        return mechanicalState;
    }

    @Override
    public String name() {
        return m_name;
    }
}
