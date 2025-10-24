package com.team766.simulator;

import com.team766.hal.BeaconReader;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProgramInterface {
    public interface SensorCallback {
        void run(double deltaTime);
    }

    static {
        resetSimulationTime();
    }

    public static void resetSimulationTime() {
        if (!Parameters.REALTIME_ROBOT_CLOCK) {
            SimHooks.pauseTiming();
        }
        SimHooks.restartTiming();
    }

    public static void stepSimulationTime(double deltaSeconds) {
        if (!Parameters.REALTIME_ROBOT_CLOCK) {
            SimHooks.stepTiming(deltaSeconds);
        }
    }

    public static ArrayList<Runnable> motorUpdates = new ArrayList<>();
    public static ArrayList<SensorCallback> sensorUpdates = new ArrayList<>();

    public static final double[] pwmChannels = new double[20];

    public static class CANMotorControllerCommand {
        public double percentOutput;
    }

    public static class CANMotorControllerStatus {
        public double sensorPosition;
        public double sensorVelocity;
    }

    public static class CANMotorControllerCommunication {
        public final CANMotorControllerCommand command = new CANMotorControllerCommand();
        public final CANMotorControllerStatus status = new CANMotorControllerStatus();
    }

    public static final CANMotorControllerCommunication[] canMotorControllerChannels =
            initializeArray(256, CANMotorControllerCommunication.class);

    public static final double[] analogChannels = new double[20];

    public static final boolean[] digitalChannels = new boolean[20];

    public static final int[] relayChannels = new int[20];

    public static final boolean[] solenoidChannels = new boolean[20];

    public static class EncoderChannel {
        public long distance = 0;
        public double rate = 0;
    }

    public static final EncoderChannel[] encoderChannels =
            initializeArray(20, EncoderChannel.class);

    public static class GyroCommunication {
        public double angle; // Yaw angle (accumulative)
        public double rate; // Yaw rate
        public double pitch;
        public double roll;
    }

    public static final GyroCommunication gyro = new GyroCommunication();

    public static class RobotPosition {
        public double x;
        public double y;
        public double heading;
    }

    public static final RobotPosition robotPosition = new RobotPosition();

    public static final int NUM_BEACONS = 8;
    public static BeaconReader.BeaconPose[] beacons =
            initializeArray(NUM_BEACONS, BeaconReader.BeaconPose.class);

    private static <E> E[] initializeArray(final int size, final Class<E> clazz) {
        @SuppressWarnings("unchecked")
        E[] array = (E[]) Array.newInstance(clazz, size);
        for (int i = 0; i < size; ++i) {
            try {
                array[i] = clazz.getConstructor().newInstance();
            } catch (Throwable e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        return array;
    }
}
