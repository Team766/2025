package com.team766.robot.reva_2025.constants;

import com.pathplanner.lib.util.FlippingUtil;
import com.team766.robot.reva_2025.mechanisms.Elevator.Position;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

public class CoralConstants {

    public enum ReefPos {
        Pole1(6.15, 3.86, -180, RelativeReefPos.Left),
        Pole2(6.15, 4.19, -180, RelativeReefPos.Right),
        Pole3(5.465, 5.385, -120, RelativeReefPos.Left),
        Pole4(5.18, 5.55, -120, RelativeReefPos.Right),
        Pole5(3.8, 5.55, -60, RelativeReefPos.Left),
        Pole6(3.51, 5.385, -60, RelativeReefPos.Right),
        Pole7(2.82, 4.19, 0, RelativeReefPos.Left),
        Pole8(2.82, 3.86, 0, RelativeReefPos.Right),
        Pole9(3.51, 2.665, 60, RelativeReefPos.Left),
        Pole10(3.8, 2.5, 60, RelativeReefPos.Right),
        Pole11(5.18, 2.5, 120, RelativeReefPos.Left),
        Pole12(5.465, 2.665, 120, RelativeReefPos.Right);

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
                                    .minus(
                                            new Translation2d(
                                                    distance, position.getRotation())),
                            position.getRotation());
            return alliance.equals(Alliance.Blue) ? farPose : FlippingUtil.flipFieldPose(farPose);
        }

        public RelativeReefPos getRelativeReefPos() {
            return relativeReefPos;
        }
    }

    public enum RelativeReefPos {
        Left,
        Right;
    }

    public enum ScoreHeight {
        Intake(Position.ELEVATOR_INTAKE, WristPosition.CORAL_INTAKE),
        L1(Position.ELEVATOR_L1, WristPosition.CORAL_L1_PLACE),
        L2(Position.ELEVATOR_L2, WristPosition.CORAL_L2_PLACE),
        L3(Position.ELEVATOR_L3, WristPosition.CORAL_L3_PLACE),
        L4(Position.ELEVATOR_L4, WristPosition.CORAL_L4_PLACE);

        private final Position elevatorPosition;
        private final WristPosition wristPosition;

        private ScoreHeight(Position elevatorPosition, WristPosition wristPosition) {
            this.elevatorPosition = elevatorPosition;
            this.wristPosition = wristPosition;
        }

        public Position getElevatorPosition() {
            return elevatorPosition;
        }

        public WristPosition getWristPosition() {
            return wristPosition;
        }
    }
}
