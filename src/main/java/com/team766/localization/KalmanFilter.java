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
            MatBuilder.fill(Nat.N2(), Nat.N2(), 0.2, 0, 0, 0.05);

    // FIXME: placeholder values
    private static final Matrix<N2, N2> VISION_COVARIANCE_DEFAULT =
            MatBuilder.fill(Nat.N2(), Nat.N2(), .5, 0, 0, .5);

    private static final double VELOCITY_INPUT_DELETION_TIME_DEFAULT = 1; // in seconds

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
            Matrix<N2, N1> curState,
            Matrix<N2, N2> covariance,
            Matrix<N2, N2> odometryCovariancePerDist,
            Matrix<N2, N2> visionCovariance) {
        this(
                curState,
                covariance,
                odometryCovariancePerDist,
                visionCovariance,
                VELOCITY_INPUT_DELETION_TIME_DEFAULT);
    }

    public KalmanFilter(Matrix<N2, N2> odometryCovariancePerDist, Matrix<N2, N2> visionCovariance) {
        this(
                CUR_STATE_DEFAULT,
                INITIAL_COVARIANCE_DEFAULT,
                odometryCovariancePerDist,
                visionCovariance);
    }

    public KalmanFilter() {
        this(ODOMETRY_COVARIANCE_PER_DIST_DEFAULT, VISION_COVARIANCE_DEFAULT);
    }

    public void addOdometryInput(Translation2d odometryInput, double time) {

        // short circuits if inputting a value with the wrong time
        if (time <= inputLog.lastKey()) {
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

    private void predict(double time, double nextStepTime, double dt) {
        Translation2d positionChange;

        // scalar multiplied to account for decreased velocity change if input targetTime is between
        // two input entries
        positionChange = inputLog.get(nextStepTime).times(dt / (nextStepTime - time));

        curState = curState.plus(positionChange.toVector());

        double angleRad = positionChange.getAngle().getRadians();
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
     * Updates the esimated position using vision measurements mapped to their covariance, allowing for a unique covariance for each measurement
     * @param measurements map key is the vision measurements (x, y), value is covariance matrix of that measurement
     * @param time the time that the measurement took place, in seconds
     */
    public void updateWithVisionMeasurement(Translation2d[] measurements, double time) {
        TreeMap<Translation2d, Matrix<N2, N2>> measurementTreeMap = new TreeMap<>();
        for (Translation2d measurement : measurements) {
            measurementTreeMap.put(measurement, visionCovariance);
        }
        updateWithPositionMeasurement(measurementTreeMap, time);
    }

    public Translation2d getPos() {
        return new Translation2d(new Vector<N2>(curState));
    }

    public Matrix<N2, N2> getCovariance() {
        return curCovariance;
    }

    public void setPos(Translation2d pos, Matrix<N2, N2> covariance) {
        curState = pos.toVector();
        curCovariance = covariance;
    }

    public void setPos(Translation2d pos) {
        setPos(pos, SET_POS_COVARIANCE_DEFAULT);
    }
}
