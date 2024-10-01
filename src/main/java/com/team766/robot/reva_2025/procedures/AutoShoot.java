package com.team766.robot.reva_2025.procedures;

import com.pathplanner.lib.util.FlippingUtil;
import com.team766.ViSIONbase.AnywhereScoringPosition;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.framework.StatusBus;
import com.team766.logging.Severity;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.mechanisms.SwerveDrive.DriveStatus;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.ArrayList;
import java.util.Optional;

public class AutoShoot extends Procedure {
    private AlgaeIntake algaeIntake;
    private SwerveDrive drive;
    private Pose2d setPosition2D =
            new Pose2d(6.75, 0, new Rotation2d(-25)); // TODO: update with actual values
    private final boolean useSetPosition = true;
    protected static ArrayList<AnywhereScoringPosition> shootingPositions =
            new ArrayList<AnywhereScoringPosition>() {
                { // TODO: change temporary values
                    add(new AnywhereScoringPosition(1.9506, 4800, 23.516));
                    add(new AnywhereScoringPosition(2.072, 4800, 27.32));
                    add(new AnywhereScoringPosition(2.29161, 4800, 30.109));
                    add(new AnywhereScoringPosition(2.4616, 4800, 30.8987));
                    add(new AnywhereScoringPosition(2.6942, 4800, 32.699));
                }
            };

    public AutoShoot(AlgaeIntake algaeIntake, SwerveDrive drive) {
        this.algaeIntake = reserve(algaeIntake);
        this.drive = reserve(drive);

        Optional<Alliance> alliance = DriverStation.getAlliance();
        if (alliance.isPresent()) {
            if (alliance.get().equals(Alliance.Red)) {
                setPosition2D = FlippingUtil.flipFieldPose(setPosition2D);
            }
        }
    }

    @Override
    public void run(Context context) {
        if (useSetPosition) {
            shootFromSetPosition(context);
        } else {
            shootFromCurrentPosition(context);
        }
    }

    public void shootFromSetPosition(Context context) {

        final Optional<DriveStatus> driveStatus =
                StatusBus.getInstance().getStatus(SwerveDrive.DriveStatus.class);
        if (driveStatus.isEmpty()) {
            log(Severity.ERROR, "Cannot find drive status, aborting AutoShoot");
            return;
        }
        Pose2d curPose = driveStatus.get().currentPosition();
        Pose2d newPose = new Pose2d(curPose.getX(), setPosition2D.getY(), curPose.getRotation());
        algaeIntake.setArmAngle(Level.Shoot);
        algaeIntake.setState(State.Shoot);
        context.runSync(new AutoAlign(newPose, drive));
        waitForStatusMatchingOrTimeout(
                context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle(), 1);
        waitForStatusMatchingOrTimeout(
                context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtTargetSpeed(), 1);
        algaeIntake.setState(State.Feed);
    }

    // TODO: still need to interpolate between two nearest postions
    public void shootFromCurrentPosition(Context context) {
        drive.stopDrive();
        Optional<Transform3d> maybeToUse =
                context.waitForValueOrTimeout(this::getTransform3dOfRobotToTag, 1.0);
        if (!maybeToUse.isPresent()) {
            log(Severity.ERROR, "No tag info available");
            return;
        }
        Transform3d toUse = maybeToUse.orElseThrow();
        double distanceOfRobotToTag =
                Math.sqrt(Math.pow(toUse.getX(), 2) + Math.pow(toUse.getY(), 2));
        double bestAngle = getBestAngleToUse(distanceOfRobotToTag);
        if (bestAngle > -1) {
            algaeIntake.setArmAngle(bestAngle);
            waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle());
            algaeIntake.setState(State.Shoot);
            context.waitForSeconds(1);
            algaeIntake.setState(State.Idle);
        }
    }

    // TODO: confirm this is the right apriltag
    private Optional<Transform3d> getTransform3dOfRobotToTag() {
        return getStatusOrThrow(ForwardApriltagCamera.ApriltagCameraStatus.class)
                .speakerTagTransform();
    }

    public static double getBestAngleToUse(double distanceFromCenterApriltag) {
        for (int i = 0; i < shootingPositions.size(); i++) {
            if (distanceFromCenterApriltag
                    <= shootingPositions.get(i).distanceFromCenterApriltag()) {
                return shootingPositions.get(i).angleToSetArm();
            }
        }
        return -1;
    }
}
