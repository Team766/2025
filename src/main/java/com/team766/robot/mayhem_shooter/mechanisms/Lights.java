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
        m_candle = new CANdle(11, "rio");
    }

    public void setSolidColor(int red, int green, int blue) {
        log("ERROR CODE:" + m_candle.setLEDs(red, green, blue));
    }

    @Override
    protected LightsStatus updateStatus() {
        return new LightsStatus();
    }

    public void setKrakenColor(double distanceFromKraken) {
        log("HERE!");
        if(Math.abs(1.4986 - distanceFromKraken) < 0.02){
            setSolidColor(0, 255, 0);
            return;
        } else if (Math.abs(1.4986 - distanceFromKraken) < 0.05) {
            setSolidColor(0, 255, 255);
            return;
        } else if (Math.abs(1.4986 - distanceFromKraken) < 0.15){
            setSolidColor(255, 0, 255);
            return;
        } else {
            log("RED");
            setSolidColor(255, 0, 0);
            return;
        }
    }
    
}
