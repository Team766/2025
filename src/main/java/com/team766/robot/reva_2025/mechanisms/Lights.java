package com.team766.robot.reva_2025.mechanisms;
import com.team766.robot.common.mechanisms.LEDString;
import edu.wpi.first.wpilibj.util.Color;

public class Lights extends LEDString {
    public Lights(String configPrefix) {
        super(configPrefix);
    }
    
    public void signalFrontRightCameraConnected() {
        setColorIndividual(Color.kDarkGreen, 1);
    }

    public void signalFrontLeftCameraConnected() {
        setColorIndividual(Color.kDarkGreen, 0);
    }

    public void signalBackRightCameraConnected() {
        setColorIndividual(Color.kDarkGreen, 6);
    }

    public void signalBackLeftCameraConnected() {
        setColorIndividual(Color.kDarkGreen, 7);
    }

    public void setup(){
        for (int i = 0; i < 8; i++) {
            setColorIndividual(Color.kRed, i);
        }
    }

}
