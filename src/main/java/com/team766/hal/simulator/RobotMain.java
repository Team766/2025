package com.team766.hal.simulator;

import com.team766.config.ConfigFileReader;
import com.team766.hal.GenericRobotMain;
import com.team766.hal.RobotProvider;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.simulator.ProgramInterface;
import com.team766.simulator.Simulator;
import java.io.IOException;

public class RobotMain {
    enum Mode {
        MaroonSim,
        VrConnector,
    }

    private GenericRobotMain robot;
    private SimulatorInterface simulator;

    @SuppressWarnings("StaticAssignmentInConstructor")
    public RobotMain(final Mode mode) {
        try {
            // TODO: update this to come from deploy directory?
            ConfigFileReader.instance = new ConfigFileReader("simConfig.txt");
            RobotProvider.instance = new SimulationRobotProvider();

            robot = new GenericRobotMain();

            robot.robotInit();
        } catch (Exception exc) {
            exc.printStackTrace();
            LoggerExceptionUtils.logException(exc);
        }

        switch (mode) {
            case MaroonSim -> {
                simulator = new Simulator();
            }
            case VrConnector -> {
                ProgramInterface.robotMode = ProgramInterface.RobotMode.DISABLED;
                try {
                    simulator = new VrConnector();
                } catch (IOException ex) {
                    throw new RuntimeException(
                            "Error initializing communication with 3d Simulator", ex);
                }
            }
        }

        simulator.setResetHandler(() -> robot.resetAutonomousMode("simulation reset"));
    }

    public void run() {
        try {
            ProgramInterface.RobotMode prevRobotMode = null;
            while (true) {
                simulator.prepareStep();

                switch (ProgramInterface.robotMode) {
                    case DISABLED -> {
                        if (prevRobotMode != ProgramInterface.RobotMode.DISABLED) {
                            robot.disabledInit();
                            prevRobotMode = ProgramInterface.RobotMode.DISABLED;
                        }
                        robot.disabledPeriodic();
                    }
                    case AUTON -> {
                        if (prevRobotMode != ProgramInterface.RobotMode.AUTON) {
                            robot.autonomousInit();
                            prevRobotMode = ProgramInterface.RobotMode.AUTON;
                        }
                        robot.autonomousPeriodic();
                    }
                    case TELEOP -> {
                        if (prevRobotMode != ProgramInterface.RobotMode.TELEOP) {
                            robot.teleopInit();
                            prevRobotMode = ProgramInterface.RobotMode.TELEOP;
                        }
                        robot.teleopPeriodic();
                    }
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            LoggerExceptionUtils.logException(exc);
        }
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Needs -maroon_sim or -vr_connector");
            System.exit(1);
        }
        Mode mode;
        switch (args[0]) {
            case "-maroon_sim":
                mode = Mode.MaroonSim;
                break;
            case "-vr_connector":
                mode = Mode.VrConnector;
                break;
            default:
                System.err.println("Needs -maroon_sim or -vr_connector");
                System.exit(1);
                return;
        }
        new RobotMain(mode).run();
    }
}
