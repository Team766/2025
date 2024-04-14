package com.team766.hal;

import edu.wpi.first.wpilibj.Timer;

public class SystemClock implements com.team766.hal.Clock {

    public static final SystemClock instance = new SystemClock();

    @Override
    public double getTime() {
        return Timer.getFPGATimestamp();
    }
}
