package com.team766.simulator.mechanisms;

import com.team766.simulator.interfaces.SimBody;

public abstract class DriveBaseSim extends SimBody {
    public record Dimensions(
            double wheelBase,
            double wheelTrack,
            double wheelDiameter,
            double mass,
            double momentOfInertia) {
        /**
         * Default to a moment of inertial that distributes the mass uniformly.
         */
        public Dimensions(double wheelBase, double wheelTrack, double wheelDiameter, double mass) {
            this(
                    wheelBase,
                    wheelTrack,
                    wheelDiameter,
                    mass,
                    1.0 / 12.0 * mass * (wheelTrack * wheelTrack + wheelBase * wheelBase));
        }
    }

    public record Friction(
            double wheelCoefficientOfFriction,
            double rollingResistance,
            double turningResistanceFactor) {}

    public static final Friction DEFAULT_FRICTION = new Friction(1.1, 0.09, 0.15);

    public abstract State getState();
}
