package com.team766.robot.common.mechanisms;

import static com.team766.robot.common.constants.ConfigConstants.*;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.team766.framework.Mechanism;
import com.team766.framework.MechanismSimulation;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.simulator.Parameters;
import com.team766.simulator.Simulation;
import com.team766.simulator.elements.DCMotorSim;
import com.team766.simulator.elements.GearsSim;
import com.team766.simulator.elements.Pigeon2Sim;
import com.team766.simulator.elements.PwmMotorControllerSim;
import com.team766.simulator.mechanisms.DriveBaseSim;
import com.team766.simulator.mechanisms.WestCoastDriveSim;

public class BurroDrive extends Mechanism {

    private final MotorController leftMotor;
    private final MotorController rightMotor;
    private final Pigeon2 gyro;

    public BurroDrive() {
        loggerCategory = Category.DRIVE;

        leftMotor = RobotProvider.instance.getMotor(DRIVE_LEFT);
        rightMotor = RobotProvider.instance.getMotor(DRIVE_RIGHT);
        gyro = (Pigeon2) RobotProvider.instance.getGyro(DRIVE_GYRO);
    }

    /**
     * @param forward how much power to apply to moving the robot (positive being forward)
     * @param turn how much power to apply to turning the robot (positive being CCW)
     */
    public void drive(double forward, double turn) {
        checkContextOwnership();
        leftMotor.set(forward - turn);
        rightMotor.set(forward + turn);
    }

    /*
     * Stops each drive motor
     */
    public void stopDrive() {
        checkContextOwnership();
        leftMotor.stopMotor();
        rightMotor.stopMotor();
    }

    @Override
    protected MechanismSimulation createSimulation(Simulation sim) {
        return new MechanismSimulation() {
            private static final double DRIVE_WHEEL_DIAMETER = 0.1524; // 6 inches in meters
            private static final double DRIVE_GEAR_RATIO = 8.0;
            private static final double WHEEL_BASE = 0.6604;
            private static final double WHEEL_TRACK = 0.6604;
            private DCMotorSim leftMotorSim = DCMotorSim.makeCIM("DriveLeftMotor");
            private DCMotorSim rightMotorSim = DCMotorSim.makeCIM("DriveRightMotor");
            private WestCoastDriveSim driveSim =
                    new WestCoastDriveSim(
                            new GearsSim(DRIVE_GEAR_RATIO, leftMotorSim),
                            new GearsSim(DRIVE_GEAR_RATIO, rightMotorSim),
                            new DriveBaseSim.Dimensions(
                                    WHEEL_BASE,
                                    WHEEL_TRACK,
                                    DRIVE_WHEEL_DIAMETER,
                                    Parameters.FULL_ROBOT_MASS),
                            DriveBaseSim.DEFAULT_FRICTION);
            private Pigeon2Sim gyroSim = new Pigeon2Sim(gyro);

            {
                sim.electricalSystem.addDevice(new PwmMotorControllerSim(6, leftMotorSim));
                sim.electricalSystem.addDevice(new PwmMotorControllerSim(4, rightMotorSim));
            }

            @Override
            public void step(double dt) {
                driveSim.step(dt);
                gyroSim.step(driveSim.getState());
            }
        };
    }
}
