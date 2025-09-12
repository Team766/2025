package com.team766.robot.jackrabbit;

import com.ctre.phoenix6.CANBus;

public class HardwareConfig {
    private static final CANBus RIO_CAN_BUS = new CANBus("rio");
    private static final CANBus CANIVORE1_CAN_BUS = new CANBus("canivore1");

    private static final CANBus DRIVE_CAN_BUS = CANIVORE1_CAN_BUS;
    private static final CANBus COLLECTOR_CAN_BUS = CANIVORE1_CAN_BUS;
    private static final CANBus SPINDEXER_CAN_BUS = CANIVORE1_CAN_BUS;
    private static final CANBus LEFT_FEEDER_CAN_BUS = CANIVORE1_CAN_BUS;
    private static final CANBus RIGHT_FEEDER_CAN_BUS = CANIVORE1_CAN_BUS;
    private static final CANBus TURRET_CAN_BUS = CANIVORE1_CAN_BUS;
    private static final CANBus SHOOTER_CAN_BUS = CANIVORE1_CAN_BUS;

    private interface DeviceConfig {
        // Extends java.lang.Enum
        int ordinal();

        CANBus canBus();

        default int canId() {
            return ordinal() + 1;
        }
    }

    public enum Motor implements DeviceConfig {
        DRIVE_LEFT(DRIVE_CAN_BUS),
        DRIVE_RIGHT(DRIVE_CAN_BUS),
        COLLECTOR(COLLECTOR_CAN_BUS),
        SPINDEXER(SPINDEXER_CAN_BUS),
        FEEDER_LEFT(LEFT_FEEDER_CAN_BUS),
        FEEDER_RIGHT(RIGHT_FEEDER_CAN_BUS),
        TURRET(TURRET_CAN_BUS),
        HOOD(SHOOTER_CAN_BUS),
        SHOOTER_LEFT(SHOOTER_CAN_BUS),
        SHOOTER_RIGHT(SHOOTER_CAN_BUS),
        ;

        private final CANBus canBus;

        Motor(CANBus bus) {
            this.canBus = bus;
        }

        public CANBus canBus() {
            return canBus;
        }
    }

    public enum CANcoder implements DeviceConfig {
        HOOD_MAJOR(SHOOTER_CAN_BUS),
        HOOD_MINOR(SHOOTER_CAN_BUS),
        ;

        private final CANBus canBus;

        CANcoder(CANBus bus) {
            this.canBus = bus;
        }

        public CANBus canBus() {
            return canBus;
        }
    }

    public enum CANrange implements DeviceConfig {
        COLLECTOR(COLLECTOR_CAN_BUS),
        ;

        private final CANBus canBus;

        CANrange(CANBus bus) {
            this.canBus = bus;
        }

        public CANBus canBus() {
            return canBus;
        }
    }

    public enum Pigeon implements DeviceConfig {
        DRIVE(DRIVE_CAN_BUS),
        TURRET(SHOOTER_CAN_BUS),
        ;

        private final CANBus canBus;

        Pigeon(CANBus bus) {
            this.canBus = bus;
        }

        public CANBus canBus() {
            return canBus;
        }
    }
}
