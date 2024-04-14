package com.team766.hal;

import com.team766.config.ConfigFileReader;
import com.team766.framework.AutonomousMode;
import com.team766.framework.LaunchedContext;
import com.team766.framework.Procedure;
import com.team766.framework.Scheduler;
import com.team766.hal.simulator.VrConnector;
import com.team766.hal.wpilib.WPIRobotProvider;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import com.team766.simulator.ProgramInterface;
import com.team766.simulator.SimulationResetException;
import com.team766.simulator.SimulatorInterface;
import com.team766.web.AutonomousSelector;
import com.team766.web.ConfigUI;
import com.team766.web.Dashboard;
import com.team766.web.DriverInterface;
import com.team766.web.LogViewer;
import com.team766.web.ReadLogs;
import com.team766.web.WebServer;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.RobotBase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

public class RobotMain extends LoggedRobot {
    private static final String USB_CONFIG_FILE = "/U/config/robotConfig.txt";
    private static final String INTERNAL_CONFIG_FILE = "/home/lvuser/robotConfig.txt";

    private RobotConfigurator configurator;
    private Procedure m_oi;

    private WebServer m_webServer;
    private AutonomousSelector m_autonSelector;
    private AutonomousMode m_autonMode = null;
    private LaunchedContext m_autonomous = null;
    private LaunchedContext m_oiContext = null;

    private SimulatorInterface simulator;

    // Reset the autonomous routine if the robot is disabled for more than this
    // number of seconds.
    private static final double RESET_IN_DISABLED_PERIOD = 10.0;
    private double m_disabledModeStartTime;

    private boolean faultInRobotInit = false;
    private boolean faultInAutoInit = false;
    private boolean faultInTeleopInit = false;

    public static void main(final String... args) {
        Supplier<RobotMain> supplier =
                new Supplier<RobotMain>() {
                    RobotMain instance;

                    @Override
                    public RobotMain get() {
                        if (instance == null) {
                            instance = new RobotMain();
                        }
                        return instance;
                    }
                };

        // periodically poll "caniv" in the background, if present
        CanivPoller canivPoller = null;

        // UPDATE 1/21/2024: temporarily disable this poller
        // Prior to 2024, Phoenix Tuner only installed the "caniv" binary on the RoboRio when
        // a CANivore was configured.  Now, "caniv" is part of the 2024 RoboRio system image
        // even if a CANivore is never configured or used.
        // Thus, we will need to find a different way to condition when we poll.
        // eg, instead of conditioning on whether or not the caniv binary is present,
        // via the presence of a value in the config file, via an invocation of caniv to
        // see if any CAN buses are present, etc.  Until we update this logic, we'll
        // temporarily disable this altogether with a short-circuit AND.
        if (false && new File(CanivPoller.CANIV_BIN).exists()) {
            canivPoller = new CanivPoller(10 * 1000 /* millis */);
            new Thread(canivPoller, "caniv poller").start();
        }

        try {
            RobotBase.startRobot(supplier);
        } catch (Throwable ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        }

        if (canivPoller != null) {
            canivPoller.setDone(true);
        }
    }

    public RobotMain() {
        super(0.005);
    }

    private static String checkForAndReturnPathToConfigFile(final String file) {
        Path configPath = Filesystem.getDeployDirectory().toPath().resolve(file);
        File configFile = configPath.toFile();
        if (configFile.exists()) {
            return configFile.getPath();
        }
        return null;
    }

    @Override
    public void robotInit() {
        try {
            boolean configFromUSB = true;
            String filename = null;
            filename = checkForAndReturnPathToConfigFile(USB_CONFIG_FILE);

            if (isSimulation()) {
                // TODO: update this to come from deploy directory?
                filename = "simConfig.txt";
                configFromUSB = false;
            } else if (filename == null) {
                filename = INTERNAL_CONFIG_FILE;
                configFromUSB = false;
            }

            ConfigFileReader.instance =
                    new ConfigFileReader(filename, configFromUSB ? INTERNAL_CONFIG_FILE : null);
            RobotProvider.instance = new WPIRobotProvider();
            
            Scheduler.getInstance().reset();

            configurator = RobotSelector.createConfigurator();
            m_autonSelector = new AutonomousSelector(configurator.getAutonomousModes());
            m_webServer = new WebServer();
            m_webServer.addHandler(new Dashboard());
            m_webServer.addHandler(new DriverInterface(m_autonSelector));
            m_webServer.addHandler(new ConfigUI());
            m_webServer.addHandler(new LogViewer());
            m_webServer.addHandler(new ReadLogs());
            m_webServer.addHandler(m_autonSelector);
            m_webServer.start();

            DriverStation.startDataLog(DataLogManager.getLog());

            if (isReal()) {
                // enable dual-logging
                Logger.enableLoggingToDataLog(true);

                // set up AdvantageKit logging
                DataLogManager.log("Initializing logging.");
                org.littletonrobotics.junction.Logger.addDataReceiver(new WPILOGWriter("/U/logs")); // Log to sdcard
                // org.littletonrobotics.junction.Logger.addDataReceiver(new NT4Publisher()); // Publish data to NetworkTables
                new PowerDistribution(1, ModuleType.kRev); // Enables power distribution logging

            } else {
                // TODO: add support for simulation logging/replay
            }

            org.littletonrobotics.junction.Logger.start();

            try {
                configurator.initializeMechanisms();
    
                m_oi = configurator.createOI();
            } catch (Throwable ex) {
                faultInRobotInit = true;
                throw ex;
            }
            faultInRobotInit = false;
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void disabledInit() {
        try {
            m_disabledModeStartTime = RobotProvider.instance.getClock().getTime();
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void autonomousInit() {
        try {
            faultInAutoInit = true;

            if (m_oiContext != null) {
                m_oiContext.stop();
                m_oiContext = null;
            }

            if (m_autonomous != null) {
                Logger.get(Category.AUTONOMOUS)
                        .logRaw(
                                Severity.INFO,
                                "Continuing previous autonomus procedure "
                                        + m_autonomous.getContextName());
            } else if (m_autonSelector.getSelectedAutonMode() == null) {
                Logger.get(Category.AUTONOMOUS).logRaw(Severity.WARNING, "No autonomous mode selected");
            }
            faultInAutoInit = false;
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void teleopInit() {
        try {
            faultInTeleopInit = true;

            if (m_autonomous != null) {
                m_autonomous.stop();
                m_autonomous = null;
                m_autonMode = null;
            }

            if (m_oiContext == null && m_oi != null) {
                m_oiContext = Scheduler.getInstance().startAsync(m_oi);
            }

            faultInTeleopInit = false;
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void disabledPeriodic() {
        try {
            if (faultInRobotInit) return;

            // The robot can enter disabled mode for two reasons:
            // - The field control system set the robots to disabled.
            // - The robot loses communication with the driver station.
            // In the former case, we want to reset the autonomous routine, as there
            // may have been a field fault, which would mean the match is going to
            // be replayed (and thus we would want to run the autonomous routine
            // from the beginning). In the latter case, we don't want to reset the
            // autonomous routine because the communication drop was likely caused
            // by some short-lived (less than a second long, or so) interference;
            // when the communications are restored, we want to continue executing
            // the routine that was interrupted, since it has knowledge of where the
            // robot is on the field, the state of the robot's mechanisms, etc.
            // Thus, we set a threshold on the amount of time spent in autonomous of
            // 10 seconds. It is almost certain that it will take longer than 10
            // seconds to reset the field if a match is to be replayed, but it is
            // also almost certain that a communication drop will be much shorter
            // than 10 seconds.
            double timeInState = RobotProvider.instance.getClock().getTime() - m_disabledModeStartTime;
            if (timeInState > RESET_IN_DISABLED_PERIOD) {
                resetAutonomousMode("time in disabled mode");
            }
            Scheduler.getInstance().run();
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void autonomousPeriodic() {
        try {
            if (faultInRobotInit || faultInAutoInit) return;

            final AutonomousMode autonomousMode = m_autonSelector.getSelectedAutonMode();
            if (autonomousMode != null && m_autonMode != autonomousMode) {
                final Procedure autonProcedure = autonomousMode.instantiate();
                m_autonomous = Scheduler.getInstance().startAsync(autonProcedure);
                m_autonMode = autonomousMode;
                Logger.get(Category.AUTONOMOUS)
                        .logRaw(
                                Severity.INFO,
                                "Starting new autonomus procedure " + autonProcedure.getName());
            }
            Scheduler.getInstance().run();
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void teleopPeriodic() {
        try {
            if (faultInRobotInit || faultInTeleopInit) return;

            if (m_oiContext != null && m_oiContext.isDone()) {
                m_oiContext = Scheduler.getInstance().startAsync(m_oi);
                Logger.get(Category.OPERATOR_INTERFACE)
                        .logRaw(Severity.WARNING, "Restarting OI context");
            }
            Scheduler.getInstance().run();
        } catch (Exception e) {
            e.printStackTrace();
            LoggerExceptionUtils.logException(e);
        }
    }

    @Override
    public void simulationInit() {
        try {
            simulator = new VrConnector();
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Error initializing communication with 3d Simulator", ex);
        }
    }

    @Override
    public void simulationPeriodic() {
        try {
            simulator.step();
        } catch (SimulationResetException e) {
            resetAutonomousMode("simulation reset");
        }

        switch (ProgramInterface.robotMode) {
            case AUTON:
                DriverStationSim.setAutonomous(true);
                DriverStationSim.setEnabled(true);
                break;
            case DISABLED:
                DriverStationSim.setEnabled(false);
                break;
            case TELEOP:
                DriverStationSim.setAutonomous(false);
                DriverStationSim.setEnabled(true);
                break;
        }
    }

    public void resetAutonomousMode(final String reason) {
        if (m_autonomous != null) {
            m_autonomous.stop();
            m_autonomous = null;
            m_autonMode = null;
            Logger.get(Category.AUTONOMOUS)
                    .logRaw(Severity.INFO, "Resetting autonomus procedure from " + reason);
        }
    }
}
