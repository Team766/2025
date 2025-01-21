package com.team766.robot.reva.procedures;

import com.team766.ViSIONbase.AprilTagGeneralCheckedException;
import com.team766.framework3.Context;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import com.team766.orin.TimestampedApriltag;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.VisionUtil.VisionPIDProcedure;
import com.team766.robot.reva.constants.VisionConstants;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Orin;
import com.team766.robot.reva.mechanisms.Shoulder;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.Optional;

public class DriverShootNow extends VisionPIDProcedure {

    private final SwerveDrive drive;
    private final Shoulder shoulder;
    private final Intake intake;
    private int tagId;

    public DriverShootNow(SwerveDrive drive, Shoulder shoulder, Intake intake) {
        this.drive = reserve(drive);
        this.shoulder = reserve(shoulder);
        this.intake = reserve(intake);
    }

    // TODO: ADD LED COMMANDS BASED ON EXCEPTIONS
    public void run(Context context) {

        Optional<Alliance> alliance = DriverStation.getAlliance();

        if (alliance.isPresent()) {
            if (alliance.get().equals(Alliance.Blue)) {
                tagId = VisionConstants.MAIN_BLUE_SPEAKER_TAG;
            } else if (alliance.get().equals(Alliance.Red)) {
                tagId = VisionConstants.MAIN_RED_SPEAKER_TAG;
            }
        } else {
            tagId = -1;
        }

        publishStatus(new ShootingProcedureStatus(ShootingProcedureStatus.Status.RUNNING));
        drive.stopDrive();

        /* Interchange the following two lines for Orin vs. Orange Pi! */
        // Optional<Transform3d> maybeToUse = getTransform3dOfRobotToTag();
        Optional<Transform3d> maybeToUse = getTransform3dOfRobotToTagOrin();
        if (!maybeToUse.isPresent()) {
            log(Severity.ERROR, "No tag info available");
            return;
        }
        Transform3d toUse = maybeToUse.orElseThrow();

        double x = toUse.getX();
        double z = toUse.getZ();

        anglePID.setSetpoint(0);

        double distanceOfRobotToTag =
                Math.sqrt(Math.pow(toUse.getX(), 2) + Math.pow(toUse.getZ(), 2));

        log("DIST: " + distanceOfRobotToTag);
        if (distanceOfRobotToTag
                > VisionPIDProcedure.scoringPositions
                        .get(VisionPIDProcedure.scoringPositions.size() - 1)
                        .distanceFromCenterApriltag()) {
            publishStatus(new ShootingProcedureStatus(ShootingProcedureStatus.Status.OUT_OF_RANGE));
            return;
        }
        // double power;
        double armAngle;
        try {
            // power = VisionPIDProcedure.getBestPowerToUse(distanceOfRobotToTag);
            armAngle = VisionPIDProcedure.getBestArmAngleToUse(distanceOfRobotToTag);
        } catch (AprilTagGeneralCheckedException e) {
            LoggerExceptionUtils.logException(e);
            return;
        }

        // shooter.shoot(power);

        shoulder.rotate(armAngle);
        log("ArmAngle: " + armAngle);

        double angle = Math.atan2(x, z);

        log("ROBOT ANGLE: " + angle);

        anglePID.calculate(angle);

        log("ANGLE PID: " + anglePID.getOutput());

        while (Math.abs(anglePID.getOutput()) > 0.075) {
            context.yield();

            // SmartDashboard.putNumber("[ANGLE PID OUTPUT]", anglePID.getOutput());
            // SmartDashboard.putNumber("[ANGLE PID ROTATION]", angle);
            maybeToUse = getTransform3dOfRobotToTagOrin();
            if (!maybeToUse.isPresent()) {
                continue;
            }
            toUse = maybeToUse.orElseThrow();

            z = toUse.getZ();
            x = toUse.getX();

            angle = Math.atan2(x, z);

            anglePID.calculate(angle);

            drive.controlRobotOriented(0, 0, anglePID.getOutput());
        }

        drive.stopDrive();

        // SmartDashboard.putNumber("[ANGLE PID OUTPUT]", anglePID.getOutput());
        // SmartDashboard.putNumber("[ANGLE PID ROTATION]", angle);

        waitForStatusMatchingOrTimeout(
                context, Shoulder.ShoulderStatus.class, s -> s.isNearTo(armAngle), 1);

        publishStatus(new ShootingProcedureStatus(ShootingProcedureStatus.Status.FINISHED));
        context.runSync(new DriverShootVelocityAndIntake(intake));
    }

    private Optional<Transform3d> getTransform3dOfRobotToTag() {
        return getStatusOrThrow(ForwardApriltagCamera.ApriltagCameraStatus.class)
                .speakerTagTransform();
    }

    private Optional<Transform3d> getTransform3dOfRobotToTagOrin() {
        Optional<TimestampedApriltag> tag = getStatusOrThrow(Orin.OrinStatus.class).getTagById(tagId);

        if (tag.isEmpty()) {
            return Optional.empty();
        }

        Pose3d pose = tag.orElseThrow().pose;

        Transform3d poseNew =
                new Transform3d(pose.getX(), pose.getY(), pose.getZ(), new Rotation3d());
        return Optional.of(poseNew);
    }
}
