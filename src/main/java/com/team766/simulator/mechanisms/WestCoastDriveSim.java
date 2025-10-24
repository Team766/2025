package com.team766.simulator.mechanisms;

import static com.team766.math.Transforms.add;
import static com.team766.math.Transforms.multiply;
import static com.team766.math.Transforms.rotateBy;

import com.team766.library.RateLimiter;
import com.team766.simulator.Parameters;
import com.team766.simulator.PhysicalConstants;
import com.team766.simulator.elements.WheelSim;
import com.team766.simulator.interfaces.MechanicalAngularDevice;
import com.team766.simulator.interfaces.MechanicalDevice;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist3d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;

public class WestCoastDriveSim extends DriveBaseSim {
    // TODO: Add traction limit/wheel slip (static vs kinetic friction)

    private static final int NUM_MODELED_WHEELS = 2;

    private final Dimensions dimensions;
    private final Friction friction;
    private final Translation2d leftWheelPosition;
    private final Translation2d rightWheelPosition;
    private final WheelSim leftWheels;
    private final WheelSim rightWheels;

    private final RateLimiter publishRate = new RateLimiter(Parameters.LOGGING_PERIOD);
    private final StructPublisher<Pose3d> posePublisher =
            NetworkTableInstance.getDefault().getStructTopic("Sim Drive Pose", Pose3d.struct).publish();
    private final StructPublisher<Twist3d> speedsPublisher =
            NetworkTableInstance.getDefault().getStructTopic("Sim Drive Speeds", Twist3d.struct).publish();

    private MechanicalDevice.State leftWheelState;
    private MechanicalDevice.State rightWheelState;

    private State state = new State(new Pose3d(), new Twist3d());

    public WestCoastDriveSim(
            MechanicalAngularDevice leftDrive,
            MechanicalAngularDevice rightDrive,
            Dimensions dimensions,
            Friction friction) {
        this.dimensions = dimensions;
        this.friction = friction;
        this.leftWheelPosition = new Translation2d(0., dimensions.wheelTrack() / 2.0);
        this.rightWheelPosition = new Translation2d(0., -dimensions.wheelTrack() / 2.0);
        this.leftWheels = new WheelSim(dimensions.wheelDiameter(), leftDrive);
        this.rightWheels = new WheelSim(dimensions.wheelDiameter(), rightDrive);
    }

    private static double softSignum(double x) {
        x /= 0.01;
        if (x > 1.0) {
            x = 1.0;
        } else if (x < -1.0) {
            x = -1.0;
        }
        return x;
    }

    private double tractionLimit(double appliedForce) {
        double maxFriction =
                friction.wheelCoefficientOfFriction()
                        * dimensions.mass()
                        * PhysicalConstants.GRAVITY_ACCELERATION
                        / NUM_MODELED_WHEELS;
        if (Math.abs(appliedForce) > maxFriction) {
            appliedForce = Math.signum(appliedForce) * maxFriction;
        }
        return appliedForce;
    }

    public void step(double dt) {
        final Twist3d egoVelocity =
                rotateBy(state.velocity(), state.position().getRotation().unaryMinus());

        double netForce = 0.0;
        double netTorque = 0.0;

        final double leftWheelVelocity;
        leftWheelState = new MechanicalDevice.State(leftWheelState.position(), leftWheelVelocity);
        double leftWheelForce = leftWheels.step(leftWheelState, dt).force() * -1.;
        leftWheelForce = tractionLimit(leftWheelForce);
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
        final var acceleration =
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

        if (publishRate.next()) {
            posePublisher.set(getPose());
            speedsPublisher.set(getVelocity());
        }
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
        return state.acceleration();
    }
}
