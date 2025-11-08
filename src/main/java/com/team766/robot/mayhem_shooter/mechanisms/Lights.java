package com.team766.robot.mayhem_shooter.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.led.Animation;

public class Lights extends MechanismWithStatus<Lights.LightsStatus> {

    private CANdle m_candle;

    public static record LightsStatus() implements Status {
        
    }

    public Lights() {
        m_candle = new CANdle(1, "");
    }

    public void setSolidColor(int red, int green, int blue) {
        m_candle.setLEDs(red, green, blue);
    }

    @Override
    protected LightsStatus updateStatus() {
        return new LightsStatus();
    }
    
}
