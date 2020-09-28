package com.team766.robot.tutorial.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.hal.RobotProvider;
import com.team766.hal.SolenoidController;

public class Launcher extends Mechanism {
    private SolenoidController plungerSolenoid;

    public Launcher() {
        plungerSolenoid = RobotProvider.instance.getSolenoid("launch");
    }

    public void setPlunger(boolean state) {
        plungerSolenoid.set(state);
    }
}
