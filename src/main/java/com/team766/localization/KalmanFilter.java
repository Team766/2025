package com.team766.localization;

import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class KalmanFilter {
    private Matrix<N2, N1> curState;
    private Matrix<N2, N2> curCovariance;
    private Matrix<N2, N2> odometryCovariancePerDist;
    private Matrix<N2, N2> visionCovariance;
    private TreeMap<Double, Translation2d> inputLog; // TODO: make circular buffer?
    private double velocityInputDeletionTime; // in seconds

    private static final Matrix<N2, N1> CUR_STATE_DEFAULT =
            MatBuilder.fill(Nat.N2(), Nat.N1(), 0, 0);

    private static final Matrix<N2, N2> INITIAL_COVARIANCE_DEFAULT =
            Matrix.eye(Nat.N2()).times(100000); // large so that it can quickly converge to robot

    private static final Matrix<N2, N2> SET_POS_COVARIANCE_DEFAULT =
            MatBuilder.fill(Nat.N2(), Nat.N2(), .5, 0, 0, .5);

    // FIXME: placeholder values
    private static final Matrix<N2, N2> ODOMETRY_COVARIANCE_PER_DIST_DEFAULT =
            MatBuilder.fill(Nat.N2(), Nat.N2(), 0.0025, 0, 0, 0.0001);

    // FIXME: placeholder values
    private static final Matrix<N2, N2> VISION_COVARIANCE_DEFAULT =
            MatBuilder.fill(Nat.N2(), Nat.N2(), 0.0009, 0, 0, 0.0009);

    private static final double VELOCITY_INPUT_DELETION_TIME_DEFAULT = 0.05; // in seconds

    public KalmanFilter(
            Matrix<N2, N1> curState,
            Matrix<N2, N2> covariance,
            Matrix<N2, N2> odometryCovariancePerDist,
            Matrix<N2, N2> visionCovariance,
            double velocityInputDeletionTime) {
        this.curState = curState;
        this.curCovariance = covariance;
        this.odometryCovariancePerDist = odometryCovariancePerDist;
        this.visionCovariance = visionCovariance;
        this.velocityInputDeletionTime = velocityInputDeletionTime;
        inputLog = new TreeMap<>();
    }

    public KalmanFilter(
            Matrix<N2, N2> odometryCovariancePerDist,
            Matrix<N2, N2> visionCovariance,
            double velocityInputDeletionTime) {
        this(
                CUR_STATE_DEFAULT,
                INITIAL_COVARIANCE_DEFAULT,
                odometryCovariancePerDist,
                visionCovariance,
                velocityInputDeletionTime);
    }

    public KalmanFilter(Matrix<N2, N2> odometryCovariancePerDist, Matrix<N2, N2> visionCovariance) {
        this(odometryCovariancePerDist, visionCovariance, VELOCITY_INPUT_DELETION_TIME_DEFAULT);
    }

    public KalmanFilter() {
        this(ODOMETRY_COVARIANCE_PER_DIST_DEFAULT, VISION_COVARIANCE_DEFAULT);
    }

    /**
     * Adds odometry input into log of recent position updates
     * @param odometryInput the change in position between the last timestamp input and the current time
     * @param time the current time
     */
    public void addOdometryInput(Translation2d odometryInput, double time) {

        // short circuits if inputting a value with the wrong time
        if (!inputLog.isEmpty() && time <= inputLog.lastKey()) {
            Logger.get(Category.ODOMETRY)
                    .logRaw(Severity.ERROR, "tried to input an old odometry value");
            return;
        }

        inputLog.put(time, odometryInput);
        if (inputLog.size() > 1) {
            predictCurrentState(inputLog.lowerKey(time));
        }

        if (time - inputLog.firstKey() > velocityInputDeletionTime) {
            inputLog.remove(inputLog.firstKey()); // delete old velocityInput values
        }
    }

    /**
     * Helper method to change the current state to a previous or future state
     * @param time the time of the current state
     * @param nextStepTime the time of the next state which is in inputLog
     * @param dt the change in time between time and the target time. The target time can be in between inputLog times.
     */
    private void predict(double time, double nextStepTime, double dt) {
        Translation2d positionChange;

        // scalar multiplied to account for decreased velocity change if input targetTime is between
        // two input entries
        positionChange = inputLog.get(nextStepTime).times(dt / (nextStepTime - time));

        curState = curState.plus(positionChange.toVector());

        double angleRad =
                positionChange.equals(Translation2d.kZero)
                        ? 0
                        : positionChange.getAngle().getRadians();
        Matrix<N2, N2> track =
                MatBuilder.fill(
                        Nat.N2(),
                        Nat.N2(),
                        Math.cos(angleRad),
                        -Math.sin(angleRad),
                        Math.sin(angleRad),
                        Math.cos(angleRad));
        curCovariance =
                track.times(odometryCovariancePerDist.times(track.transpose()))
                        .times(positionChange.getNorm())
                        .plus(getCovariance());
    }

    /**
     * changes curState and curCovariance to what it was at targetTime through backcalculation
     * @param targetTime in seconds
     * @throws IndexOutOfBoundsException if the target time is further back than the saved input values
     */
    private void resetToPrevState(double targetTime) throws IndexOutOfBoundsException {
        if (targetTime < inputLog.firstKey()) {
            throw new IndexOutOfBoundsException();
        } else {
            double time = inputLog.lastKey();
            while (time > targetTime) {
                double prevTime = inputLog.lowerKey(time);
                double dt = Math.max(prevTime, targetTime) - time; // will be negative

                predict(prevTime, time, dt);

                time = prevTime;
            }
        }
    }

    /**
     * predicts the current state based on the input time of a previous state
     * @param initialTime in seconds
     */
    private void predictCurrentState(double initialTime) {
        double time = initialTime;
        double currentTime = inputLog.lastKey();

        while (time < currentTime) {
            double nextTime = inputLog.higherKey(time);

            // going forward, the target time (currentTime) will always be a key exactly since it is
            // defined as the last key
            // that means that finding the minimum between current time and next time, similar to in
            // findPrevState, is not necessary
            double dt = nextTime - time;

            predict(time, nextTime, dt);

            time = nextTime;
        }
    }

    /**
     * Updates the esimated position using any sensor measurement returning an x and y position
     * @param measurement the position measurement
     * @param measurementCovariance the covariance matrix of this position measurement
     * @param time the time that the measurement took place, in seconds
     */
    private void updateWithPositionMeasurement(
            Translation2d measurement, Matrix<N2, N2> measurementCovariance, double time) {

        try {
            resetToPrevState(time);

            Matrix<N2, N2> kalmanGain =
                    curCovariance.times(curCovariance.plus(measurementCovariance).inv());

            curState = kalmanGain.times(measurement.toVector().minus(curState)).plus(curState);
            curCovariance = Matrix.eye(Nat.N2()).minus(kalmanGain).times(curCovariance);

            predictCurrentState(time);
        } catch (IndexOutOfBoundsException e) {
            Logger.get(Category.ODOMETRY)
                    .logRaw(
                            Severity.ERROR,
                            "inputLog does not go back far enough:"
                                    + LoggerExceptionUtils.exceptionToString(e));
        }
    }

    /**
     * Updates the esimated position using sensor measurements mapped to their covariance
     * @param measurements map key is the position measurements (x, y), value is covariance matrix of that measurement
     * @param time the time that the measurement took place, in seconds
     */
    private void updateWithPositionMeasurement(
            Map<Translation2d, Matrix<N2, N2>> measurements, double time) {

        try {
            resetToPrevState(time);
            for (var entry : measurements.entrySet()) {
                Matrix<N2, N2> kalmanGain =
                        curCovariance.times(curCovariance.plus(entry.getValue()).inv());
                curState =
                        kalmanGain.times(entry.getKey().toVector().minus(curState)).plus(curState);
                curCovariance = Matrix.eye(Nat.N2()).minus(kalmanGain).times(curCovariance);
            }

            predictCurrentState(time);
        } catch (IndexOutOfBoundsException e) {
            Logger.get(Category.ODOMETRY)
                    .logRaw(
                            Severity.ERROR,
                            "inputLog does not go back far enough: "
                                    + LoggerExceptionUtils.exceptionToString(e));
        }
    }

    /**
     * Updates the estimated position using a vision measurement from a given time, assuming default/input covariance matrix
     * @param measurement measured position on the field
     * @param time timestamp from vision measurement, in seconds
     */
    public void updateWithVisionMeasurement(Translation2d measurement, double time) {
        updateWithPositionMeasurement(measurement, visionCovariance, time);
    }

    /**
     * Updates the esimated position using vision measurements mapped to their covariance, allowing for a unique covariance for each measurement
     * @param measurements map key is the vision measurements (x, y), value is covariance matrix of that measurement
     * @param time the time that the measurement took place, in seconds
     */
    public void updateWithVisionMeasurement(
            Map<Translation2d, Matrix<N2, N2>> measurements, double time) {
        updateWithPositionMeasurement(measurements, time);
    }

    /**
     * Updates the esimated position using vision measurements mapped to the distance to tag, scaling the covariance input based on distance
     * @param measurements map key is the vision-based robot position (x, y), value is the distance from the tag to the robot
     * @param covariance the covariance of this camera
     * @param time the time that the measurement took place, in seconds
     */
    public void updateWithVisionMeasurement(
            Map<Translation2d, Double> measurements, double covariance, double time) {
        Map<Translation2d, Matrix<N2, N2>> measurementTreeMap = new HashMap<>();
        for (Map.Entry<Translation2d, Double> measurement : measurements.entrySet()) {
            // TODO: do this logic earlier in the covariance field
            // TODO: probably don't need the >3 check (throwing away data from tags >3m away), but
            // needs to be tested
            measurementTreeMap.put(
                    measurement.getKey(),
                    measurement.getValue() > 3
                            ? Matrix.eye(Nat.N2())
                            : MatBuilder.fill(Nat.N2(), Nat.N2(), covariance, 0, 0, covariance)
                                    .times(Math.pow(measurement.getValue(), 2)));
        }
        updateWithPositionMeasurement(measurementTreeMap, time);
    }

    /**
     * @return Translation2d of current position
     */
    public Translation2d getPos() {
        return new Translation2d(new Vector<N2>(curState));
    }

    /**
     * @return 2x2 Matrix of current covariance
     */
    public Matrix<N2, N2> getCovariance() {
        return curCovariance;
    }

    /**
     * set current state, both positon and covariance
     * @param pos Translation2d of pos
     * @param covariance 2x2 Matrix of covariance
     */
    public void setPos(Translation2d pos, Matrix<N2, N2> covariance) {
        curState = pos.toVector();
        curCovariance = covariance;
    }

    /**
     * set current position with a default covariance
     * @param pos Translation2d of pos
     */
    public void setPos(Translation2d pos) {
        setPos(pos, SET_POS_COVARIANCE_DEFAULT);
    }

    /**
     * resets state to a position of (0,0) and high covariance, meaning state will jump to next landmark measurement
     */
    public void resetPos() {
        setPos(new Translation2d(), INITIAL_COVARIANCE_DEFAULT);
    }
}
