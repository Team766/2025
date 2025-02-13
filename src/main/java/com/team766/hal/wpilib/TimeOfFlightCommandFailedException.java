package com.team766.hal.wpilib;

public class TimeOfFlightCommandFailedException extends RuntimeException {
    public TimeOfFlightCommandFailedException(String message) {
        super(message);
    }

    public TimeOfFlightCommandFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
