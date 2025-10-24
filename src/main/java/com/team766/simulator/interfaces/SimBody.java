package com.team766.simulator.interfaces;

import static com.team766.math.Transforms.add;
import static com.team766.math.Transforms.multiply;
import static com.team766.math.Transforms.rotateBy;

import java.util.ArrayList;
import java.util.List;

import com.team766.simulator.PhysicalConstants;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Twist3d;
import edu.wpi.first.math.numbers.N3;

public final class SimBody {
    public record State(Pose3d position, Twist3d velocity) {
        public State(State other) {
            this(other.position, other.velocity);
        }
    }

    public record Action(Twist3d wrench, Matrix<N3, N3> inertia) {}

    private final List<Joint> joints = new ArrayList<>();

    private State state = new State(new Pose3d(), new Twist3d());
    private Twist3d acceleration = new Twist3d();

    public void step(Twist3d externalForce, double dt) {
        Twist3d netWrench = new Twist3d();

        final double leftWheelVelocity;
        leftWheelState = new MechanicalDevice.State(leftWheelState.position(), leftWheelVelocity);
        double leftWheelForce = leftWheels.step(leftWheelState, dt).force() * -1.;
        netForce += leftWheelForce;
        netTorque +=
                -leftWheelPosition.getNorm()
                        * leftWheelForce; // FIXME: this is not correct if wheel isn't on the
        // centerline

        final double rightWheelVelocity;
        rightWheelState = new MechanicalDevice.State(rightWheelState.position(), rightWheelVelocity);
        double rightWheelForce = rightWheels.step(rightWheelState, dt).force() * -1.;
        rightWheelForce = tractionLimit(rightWheelForce);
        netForce += rightWheelForce;
        netTorque +=
                rightWheelPosition.getNorm()
                        * rightWheelForce; // FIXME: this is not correct if wheel isn't on the
        // centerline

        final double rollingResistance =
                -softSignum(egoVelocity.dx)
                        * dimensions.mass()
                        * PhysicalConstants.GRAVITY_ACCELERATION
                        * friction.rollingResistance();
        netForce += rollingResistance;

        final double transverseFriction =
                -softSignum(egoVelocity.dy)
                        * friction.wheelCoefficientOfFriction()
                        * dimensions.mass()
                        * PhysicalConstants.GRAVITY_ACCELERATION;

        final double maxFriction =
                friction.wheelCoefficientOfFriction()
                        * dimensions.mass()
                        * PhysicalConstants.GRAVITY_ACCELERATION
                        * friction.turningResistanceFactor();
        netTorque += -softSignum(state.velocity().rz) * maxFriction * dimensions.wheelBase() / 2;

        // TODO: Use integration algorithm from edu.wpi.first.math.system.NumericalIntegration
        acceleration =
                rotateBy(
                        new Twist3d(
                                netForce / dimensions.mass(),
                                transverseFriction,
                                0,
                                0,
                                0,
                                netTorque / dimensions.momentOfInertia()),
                        state.position().getRotation());
        final var velocity = add(state.velocity(), multiply(acceleration, dt));
        final var position = state.position().exp(multiply(velocity, dt));
        state = new State(position, velocity, acceleration);
    }

    public State getState() {
        return state;
    }

    public Pose3d getPose() {
        return state.position();
    }

    public Twist3d getVelocity() {
        return state.velocity();
    }

    public Twist3d getAcceleration() {
        return acceleration;
    }
}
