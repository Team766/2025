package com.team766.robot.sim_example.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.framework.MechanismSimulation;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.simulator.Simulation;
import com.team766.simulator.elements.CanMotorControllerSim;
import com.team766.simulator.elements.DCMotorSim;
import com.team766.simulator.elements.GearsSim;
import com.team766.simulator.mechanisms.DoubleJointedArmSim;

public class DoubleJointedArm extends Mechanism {
    private MotorController j1Motor;
	private MotorController j2Motor;

	public DoubleJointedArm() {
		j1Motor = RobotProvider.instance.getMotor("arm.j1Motor");
		j2Motor = RobotProvider.instance.getMotor("arm.j2Motor");
	}

	public void setMotorPower(double j1Power, double j2Power){
		checkContextOwnership();
		j1Motor.set(j1Power);
		j2Motor.set(j2Power);
	}

    @Override
    protected MechanismSimulation createSimulation(Simulation sim) {
        return new MechanismSimulation() {
            private static final int J1_CAN_CHANNEL = 98;
            private static final int J2_CAN_CHANNEL = 99;
            private static final double J1_GEAR_RATIO = 4. * 4. * 3. * (58. / 14.);
            private static final double J2_GEAR_RATIO = 4. * 4. * 3. * (58. / 14.);
            private DCMotorSim j1Motor = DCMotorSim.makeNeo("ArmJoint1");
            private DCMotorSim j2Motor = DCMotorSim.makeNeo("ArmJoint2");
            private DoubleJointedArmSim armSim =
                    new DoubleJointedArmSim(
                            new GearsSim(J1_GEAR_RATIO, j1Motor), new GearsSim(J2_GEAR_RATIO, j2Motor));

            {
                sim.electricalSystem.addDevice(new CanMotorControllerSim(J1_CAN_CHANNEL, j1Motor));
                sim.electricalSystem.addDevice(new CanMotorControllerSim(J2_CAN_CHANNEL, j2Motor));
            }
            
            @Override
            public void step(double dt) {
                armSim.step(dt);
            }
        };
    }
}
