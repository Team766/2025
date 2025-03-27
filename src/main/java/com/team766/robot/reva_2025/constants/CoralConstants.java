package com.team766.robot.reva_2025.constants;

import com.pathplanner.lib.util.FlippingUtil;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

public class CoralConstants {
    private CoralConstants() {} // utility class

    public enum ReefPos {
        ReefA(3.16, 4.190, 0, RelativeReefPos.Left),
        ReefB(3.16, 3.860, 0, RelativeReefPos.Right),
        ReefC(3.679, 2.954, 60, RelativeReefPos.Left),
        ReefD(3.965, 2.792, 60, RelativeReefPos.Right),
        ReefE(5.010, 2.792, 120, RelativeReefPos.Right),
        ReefF(5.297, 2.954, 120, RelativeReefPos.Left),
        ReefG(5.815, 3.860, 180, RelativeReefPos.Right),
        ReefH(5.815, 4.190, 180, RelativeReefPos.Left),
        ReefI(5.297, 5.094, -120, RelativeReefPos.Right),
        ReefJ(5.010, 5.258, -120, RelativeReefPos.Left),
        ReefK(3.965, 5.258, -60, RelativeReefPos.Left),
        ReefL(3.679, 5.094, -60, RelativeReefPos.Right);

        private final Pose2d position;
        private final RelativeReefPos relativeReefPos;

        private ReefPos(double x, double y, double angleDeg, RelativeReefPos relativeReefPos) {
            this.position = new Pose2d(x, y, Rotation2d.fromDegrees(angleDeg));
            this.relativeReefPos = relativeReefPos;
        }

        public Pose2d getPosition(Alliance alliance, double distance) {
            Pose2d farPose =
                    new Pose2d(
                            position.getTranslation()
                                    .minus(new Translation2d(distance, position.getRotation())),
                            position.getRotation());
            return alliance.equals(Alliance.Blue) ? farPose : FlippingUtil.flipFieldPose(farPose);
        }

        public RelativeReefPos getRelativeReefPos(Alliance alliance) {
            return relativeReefPos;
        }
    }

    public enum RelativeReefPos {
        Left,
        Right;
    }

    public enum ScoreHeight {
        Intake(ElevatorPosition.ELEVATOR_INTAKE, WristPosition.CORAL_INTAKE),
        L1(ElevatorPosition.ELEVATOR_L1, WristPosition.CORAL_L1_PLACE),
        L2(ElevatorPosition.ELEVATOR_L2, WristPosition.CORAL_L2_PLACE),
        L3(ElevatorPosition.ELEVATOR_L3, WristPosition.CORAL_L3_PLACE),
        L4(ElevatorPosition.ELEVATOR_L4, WristPosition.CORAL_L4_PLACE),
        CLIMB(ElevatorPosition.ELEVATOR_CLIMB, WristPosition.CORAL_CLIMB);

        private final ElevatorPosition elevatorPosition;
        private final WristPosition wristPosition;

        private ScoreHeight(ElevatorPosition elevatorPosition, WristPosition wristPosition) {
            this.elevatorPosition = elevatorPosition;
            this.wristPosition = wristPosition;
        }

        public ElevatorPosition getElevatorPosition() {
            return elevatorPosition;
        }

        public WristPosition getWristPosition() {
            return wristPosition;
        }
    }
}
