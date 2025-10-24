package com.team766.simulator.elements;

import com.team766.simulator.interfaces.MechanicalAngularDevice;
import com.team766.simulator.interfaces.MechanicalDevice;

public class Combiners {
    public static MechanicalAngularDevice combine(MechanicalAngularDevice... devices) {
        return new MechanicalAngularDevice() {
            @Override
            public Action step(State state, double dt) {
                double torque = 0.0;
                for (var d : devices) {
                    var output = d.step(state, dt);
                    torque += output.torque();
                }
                return new Action(torque);
            }
        };
    }

    public static MechanicalDevice combine(MechanicalDevice... devices) {
        return new MechanicalDevice() {
            @Override
            public Action step(State state, double dt) {
                double force = 0.0;
                for (var d : devices) {
                    var output = d.step(state, dt);
                    force += output.force();
                }
                return new Action(force);
            }
        };
    }

    private Combiners() {}
}
