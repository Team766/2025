package com.team766.hal.simulator;

import com.team766.config.ConfigFileReader;
import com.team766.framework.Scheduler;
import com.team766.hal.GenericRobotMain;
import com.team766.hal.RobotProvider;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.simulator.Program;
import com.team766.simulator.ProgramInterface;

public class RobotMain {
    private GenericRobotMain robot;
    private Runnable simulator;

    public RobotMain() {
        try {
            // TODO: update this to come from deploy directory?
            ConfigFileReader.instance = new ConfigFileReader("simConfig.txt");
            RobotProvider.instance = new SimulationRobotProvider();

            Scheduler.getInstance().reset();

            robot = new GenericRobotMain();

            robot.robotInit();

            ProgramInterface.program =
                    new Program() {
                        ProgramInterface.RobotMode prevRobotMode = null;

                        @Override
                        public void step(double dt) {
                            switch (ProgramInterface.robotMode) {
                                case DISABLED:
                                    if (prevRobotMode != ProgramInterface.RobotMode.DISABLED) {
                                        robot.disabledInit();
                                        prevRobotMode = ProgramInterface.RobotMode.DISABLED;
                                    }
                                    robot.disabledPeriodic();
                                    break;
                                case AUTON:
                                    if (prevRobotMode != ProgramInterface.RobotMode.AUTON) {
                                        robot.autonomousInit();
                                        prevRobotMode = ProgramInterface.RobotMode.AUTON;
                                    }
                                    robot.autonomousPeriodic();
                                    break;
                                case TELEOP:
                                    if (prevRobotMode != ProgramInterface.RobotMode.TELEOP) {
                                        robot.teleopInit();
                                        prevRobotMode = ProgramInterface.RobotMode.TELEOP;
                                    }
                                    robot.teleopPeriodic();
                                    break;
                                default:
                                    LoggerExceptionUtils.logException(
                                            new IllegalArgumentException(
                                                    "Value of ProgramInterface.robotMode invalid. Provided value: "
                                                            + ProgramInterface.robotMode));
                                    break;
                            }
                        }

                        @Override
                        public void reset() {
                            robot.resetAutonomousMode("simulation reset");
                        }
                    };

            simulator = new VrConnector();
        } catch (Exception exc) {
            exc.printStackTrace();
            LoggerExceptionUtils.logException(exc);
        }
    }

    public void run() {
        try {
            simulator.run();
        } catch (Exception exc) {
            exc.printStackTrace();
            LoggerExceptionUtils.logException(exc);
        }
    }

    public static void main(final String[] args) {
        new RobotMain().run();
    }
}
