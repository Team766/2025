package com.team766.odometry;

import java.util.TreeMap;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.numbers.N4;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class KalmanFilter {
    private Matrix<N4, N1> curState;
    private Matrix<N4, N4> curCovariance; 
    private Matrix<N4, N4> noiseCovariance;
    private Matrix<N2, N2> odometryCovariancePerDist;
    private Matrix<N2, N2> visionCovariance;
    private TreeMap<Double, Translation2d> inputLog; 
    private double velocityInputDeletionTime; // in seconds

    private static final Matrix<N4, N1> CUR_STATE_DEFAULT = MatBuilder.fill(Nat.N4(), Nat.N1(), 0, 0, 0, 0);

    private static final Matrix<N4, N4> COVARIANCE_DEFAULT = Matrix.eye(Nat.N4());

    private static final Matrix<N4, N4> NOISE_COVARIANCE_DEFAULT = MatBuilder.fill(Nat.N4(), Nat.N4(), 
        0.03, 0, 0, 0,
                0, 0.03, 0, 0,
                0, 0, 0.01, 0,
                0, 0, 0, 0.01);

    private static final Matrix<N2, N2> ODOMETRY_COVARIANCE_DEFAULT = MatBuilder.fill(Nat.N2(), Nat.N2(), 
        0.2, 0, 
                0, 0.05);

    private static final Matrix<N2, N2> VISION_COVARIANCE_DEFAULT = MatBuilder.fill(Nat.N2(), Nat.N2(), 
        0.1, 0, 
                0, 0.1);
        
    private static final Matrix<N2, N4> OBSERVATION_MATRIX = MatBuilder.fill(Nat.N2(), Nat.N4(), 
        1, 0, 0, 0,
                0, 1, 0, 0);
    
    private static final double VELOCITY_INPUT_DELETION_TIME_DEFAULT = 1; // in seconds
    
    public KalmanFilter(Matrix<N4, N1> curState, Matrix<N4, N4> covariance, Matrix<N2, N2> odometryCovariancePerDist, Matrix<N2, N2> visionCovariance, Matrix<N4, N4> noiseCovariance, double velocityInputDeletionTime) {
        this.curState = curState;
        this.curCovariance = covariance;
        this.odometryCovariancePerDist = odometryCovariancePerDist;
        this.visionCovariance = visionCovariance;
        this.noiseCovariance = noiseCovariance;
        this.velocityInputDeletionTime = velocityInputDeletionTime;
        inputLog = new TreeMap<>();
    }

    public KalmanFilter(Matrix<N4, N1> curState, Matrix<N4, N4> covariance, Matrix<N2, N2> odometryCovariancePerDist, Matrix<N2, N2> visionCovariance) {
        this(curState, covariance, odometryCovariancePerDist, visionCovariance, NOISE_COVARIANCE_DEFAULT, VELOCITY_INPUT_DELETION_TIME_DEFAULT);
    }

    public KalmanFilter(Matrix<N2, N2> odometryCovariancePerDist, Matrix<N2, N2> visionCovariance) {
        this(CUR_STATE_DEFAULT, COVARIANCE_DEFAULT, odometryCovariancePerDist, visionCovariance);
    }

    public KalmanFilter() {
        this(ODOMETRY_COVARIANCE_DEFAULT, VISION_COVARIANCE_DEFAULT);
    }

    public void addVelocityInput(Translation2d velocityInput, double time) {
        inputLog.put(time, velocityInput);
        predictCurrentState(inputLog.lowerKey(time));

        if(time - inputLog.firstKey() > velocityInputDeletionTime) {
            inputLog.remove(inputLog.firstKey()); // delete old velocityInput values
        } 

        // SmartDashboard.putNumber("Cur input x velocity", velocityInput.getX());
        // SmartDashboard.putNumber("Cur State x velocity", curState.get(2, 0));
        // SmartDashboard.putNumber("Number of entries inputLog", inputLog.size());
        // Logger.get(Category.LOCALIZATION).logRaw(Severity.INFO, "pos cov: " + getCovariance().toString());
        // SmartDashboard.putString("Pos Covariance", "time: " + time + ", gain: " + getCovariance().toString());
        // SmartDashboard.putString("Full Covariance", "time: " + time + ", gain: " + curCovariance);
    }

    private void predict(double time, double nextStepTime, double dt) {
        Translation2d velocityChange; 
        if (inputLog.containsKey(time)) {
            velocityChange = inputLog.get(nextStepTime).minus(getVelocity()); 
        } else {
            velocityChange = inputLog.get(nextStepTime).minus(getVelocity()).times(dt/(nextStepTime - time)); // scalar multiplied to account for decreased velocity change if input targetTime is between two input entries
        }
        
        Matrix<N4, N4> transition = MatBuilder.fill(Nat.N4(), Nat.N4(), 
            1, 0, dt, 0,
                    0, 1, 0, dt,
                    0, 0, 1, 0,
                    0, 0, 0, 1);
                
        Matrix<N4, N1> input = MatBuilder.fill(Nat.N4(), Nat.N1(), 
            0, 
                    0, 
                    velocityChange.getX(), 
                    velocityChange.getY());

        curState = transition.times(curState).plus(input);
        curCovariance = transition.times(curCovariance.times(transition.transpose())).plus(noiseCovariance);
    }

    /**
     * changes curState and curCovariance to what it was at targetTime through backcalculation
     * @param targetTime in seconds
     */
    private void findPrevState(double targetTime) {
        double time = inputLog.lastKey();
        double prevTime;
        double dt; 

        while (time > targetTime) {
            try {
                prevTime = inputLog.lowerKey(time);
                dt = Math.max(prevTime, targetTime) - time; // will be negative

                predict(time, prevTime, dt);

                time += dt;
            } catch (Exception e) {
                Logger.get(Category.ODOMETRY).logRaw(Severity.ERROR, "inputLog does not go back far enough");
                break;
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
        double nextTime;
        double dt; 

        while (time < currentTime) {
            try {
                nextTime = inputLog.higherKey(time);
                
                // going forward, the target time (currentTime) will always be a key exactly since it is defined as the last key
                // that means that finding the minimum between current time and next time, similar to in findPrevState, is not necessary
                dt = nextTime - time;

                predict(time, nextTime, dt);

                time += dt;
            } catch (Exception e) {
                Logger.get(Category.ODOMETRY).logRaw(Severity.ERROR, "no higher key");
                break;
            }
        }
    }

    /**
     * Updates the esimated position using any sensor measurement returning an x and y position
     * @param measurement the position measurement
     * @param measurementCovariance the covariance matrix of this position measurement
     * @param time the time that the measurement took place, in seconds
     */
    private void updateWithPositionMeasurement(Translation2d measurement, Matrix<N2, N2> measurementCovariance, double time) {

        findPrevState(time);

        // SmartDashboard.putNumber("prev X value", getPos().getX());
        // SmartDashboard.putNumber("Prev state x velocity", curState.get(2, 0));
        // SmartDashboard.putString("prev covariance", getCovariance().toString());

        Matrix<N4, N2> kalmanGain = curCovariance.times(OBSERVATION_MATRIX.transpose().times(
            OBSERVATION_MATRIX.times(curCovariance.times(OBSERVATION_MATRIX.transpose())).plus(measurementCovariance).inv()));
        
        // SmartDashboard.putString("Kalman Gain", "time: " + time + ", gain: " + kalmanGain.toString());

        curState = kalmanGain.times(measurement.toVector().minus(OBSERVATION_MATRIX.times(curState))).plus(curState);
        curCovariance = Matrix.eye(Nat.N4()).minus(kalmanGain.times(OBSERVATION_MATRIX)).times(curCovariance.times(Matrix.eye(Nat.N4()).minus(kalmanGain.times(OBSERVATION_MATRIX)).transpose())).plus(
            kalmanGain.times(measurementCovariance.times(kalmanGain.transpose())));
        
        // SmartDashboard.putNumber("Updated prev state x velocity", curState.get(2, 0));

        predictCurrentState(time);

        // SmartDashboard.putNumber("Predicted Cur State x velocity", curState.get(2, 0));
    }

    /**
     * Updates the estimated position using a 
     * @param measurement
     * @param time in seconds
     */
    public void updateWithVisionMeasurement(Translation2d measurement, double time) {
        updateWithPositionMeasurement(measurement, visionCovariance, time);
    }

    /**
     * Updates the estimated position using a change in position since the last update
     * Assumes that this update is happening right after the previous velocity input is added and that all odometry calculations have negligible latency 
     * @param odometryInput change in position between the previous update and now
     */
    public void updateWithOdometry(Translation2d odometryInput) {
        double initialTime = inputLog.lowerKey(inputLog.lastKey());

        findPrevState(initialTime);
        Translation2d curPos = getPos().plus(odometryInput);
        predictCurrentState(initialTime);

        double angleRad = odometryInput.getAngle().getRadians();
        Matrix<N2, N2> track = MatBuilder.fill(Nat.N2(), Nat.N2(), Math.cos(angleRad), -Math.sin(angleRad), Math.sin(angleRad), Math.cos(angleRad));
        Matrix<N2, N2> odomCovariance = track.times(odometryInput.getNorm()).times(odometryCovariancePerDist.times(track.transpose())).plus(getCovariance());

        // Logger.get(Category.ODOMETRY).logRaw(Severity.INFO, "cov: " + getCovariance().toString());

        updateWithPositionMeasurement(curPos, odomCovariance, inputLog.lastKey());
    }

    public Translation2d getPos() {
        return new Translation2d(new Vector<N2>(curState.block(2, 1, 0, 0)));
    }

    public Translation2d getVelocity() {
        return new Translation2d(new Vector<N2>(curState.block(2, 1, 2, 0)));
    }

    public Matrix<N2, N2> getCovariance() {
        return curCovariance.block(2, 2, 0, 0);
    }

    public void setPos(Translation2d pos) {
        curState.assignBlock(0, 0, pos.toVector());
    }
}