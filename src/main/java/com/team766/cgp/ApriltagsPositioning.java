package com.team766.cgp;

import com.team766.robot.reva_2025.constants.FieldConstants;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

public class ApriltagsPositioning {

    // Converts a vector from tag to robot into a field-relative position
    private static Translation3d toField(Translation3d vRobotToTag, Pose3d tagPose) {
 
        // Multiply by -1.0 to get vector from tag to tobot.
        Translation3d result = vRobotToTag.times(-1.0).rotateBy(tagPose.getRotation())
                          .plus(tagPose.getTranslation());
        return result;
    };

    private static void assertPositionsEqual(Translation3d computed,
                                            Translation3d actual) {
    double dx = computed.getX() - actual.getX();
    double dy = computed.getY() - actual.getY();
    double dz = computed.getZ() - actual.getZ();
    double norm = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (norm > 1e-5) {
        throw new IllegalStateException(String.format(
            "Field position mismatch: |computed-actual|=%.4f m > tol=%.4f m. computed=%s, actual=%s",
            norm, 1e-5, computed, actual));
    }
}

    public static void main(String[] args) {
        var layout = FieldConstants.APRIL_TAG_REEFSCAPE_LAYOUT;

        // Example queries (adjust to actual API)
        System.out.println("Total tags: " + layout.getTags().size());
        layout.getTags()
                .forEach(tag -> System.out.println("Tag ID: " + tag.ID + " Pose: " + tag.pose));

        System.out.println("Field origin: " + layout.getOrigin());

        // Test case 1: tag with no rotation, robot at origin of tag.
        Pose3d fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d() // no rotation
        );

        var robotToTag1 = new Translation3d(0.0, 0.0, 0.0);
        var fieldPos = toField(robotToTag1, fakeTagPose);
        var actualFieldPos = robotToTag1.rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 1 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 2: tag with no rotation, robot 1 meter along tag X axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d()
        );

        robotToTag1 = new Translation3d(1.0, 0.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 2 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 3: tag with no rotation, robot 1 meter along tag -X axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d()
        );

        robotToTag1 = new Translation3d(-1.0, 0.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 3 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 4: tag with no rotation, robot 1 meter along tag Y axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d()
        );

        robotToTag1 = new Translation3d(0.0, 1.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 4 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 5: tag with no rotation, robot 1 meter along tag -Y axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d()
        );

        robotToTag1 = new Translation3d(0.0, -1.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 5 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 6: tag rotated 180 degrees around Z axis, robot 1 meter along tag X axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d(0.0, 0.0, Math.PI)
        );

        robotToTag1 = new Translation3d(1.0, 0.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 6 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 7: tag rotated 180 degrees around Z axis, robot 1 meter along tag -X axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d(0.0, 0.0, Math.PI)
        );

        robotToTag1 = new Translation3d(-1.0, 0.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 7 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 8: tag rotated 180 degrees around Z axis, robot 1 meter along tag Y axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d(0.0, 0.0, Math.PI)
        );

        robotToTag1 = new Translation3d(0.0, 1.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 8 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 9: tag rotated 180 degrees around Z axis, robot 1 meter along tag -Y axis.
        fakeTagPose = new Pose3d(
            new Translation3d(3.0, 2.0, 1.0),
            new Rotation3d(0.0, 0.0, Math.PI)
        );

        robotToTag1 = new Translation3d(0.0, -1.0, 0.0);
        fieldPos = toField(robotToTag1, fakeTagPose);
        actualFieldPos = robotToTag1.times(-1.0).rotateBy(fakeTagPose.getRotation())
                                      .plus(fakeTagPose.getTranslation());
        System.out.println("---- Test Case 9 ----");
        System.out.println("Computed field position: " + fieldPos);
        System.out.println("Actual field position: " + actualFieldPos);
        assertPositionsEqual(fieldPos, actualFieldPos);

        // Test case 10: get tags from the field and pretend the robot is at the center of the field.
        var tagList = layout.getTags();
        tagList.forEach(tag -> {
            var optionalTagPose = layout.getTagPose(tag.ID);
            var testTagPose = optionalTagPose.get();
            var length = layout.getFieldLength();
            var width = layout.getFieldWidth();

            Translation3d robotLocation = new Translation3d(length / 2.0, width / 2.0, 0.0);

            // Vector (robot -> tag) expressed in TAG frame: v_T = R_TF * (p_F_T - p_F_R)
            Translation3d vRobotToTag = testTagPose.getTranslation()
                    .minus(robotLocation)
                    .rotateBy(testTagPose.getRotation().unaryMinus());

            Translation3d computedFieldPos = toField(vRobotToTag, testTagPose);
            assertPositionsEqual(computedFieldPos, robotLocation);

        });
        
    }
}
