package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.quest.QuestNav;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;

public class Quest extends MechanismWithStatus<Quest.QuestStatus> {

    private QuestNav questNav;
    /*The FRC coordinate system follows these standards:

    X (First value): Positive -> Forwards from robot center
    Y (Second value): Positive -> Left from robot center
    Rotation (Third value): Positive -> Counter Clockwise */

    // First, Declare our geometrical transform from the Quest to the robot center

    private static double x_offset =
            ConfigFileReader.instance.getDouble("quest.x_offset").valueOr(0d);
    private static double y_offset =
            ConfigFileReader.instance.getDouble("quest.y_offset").valueOr(0d);
    private static double rotation =
            ConfigFileReader.instance.getDouble("quest.rotation_degrees").valueOr(0d);

    private static double x_field_translation =
            ConfigFileReader.instance.getDouble("quest.x_translation").valueOr(0d);
    private static double y_field_translastion =
            ConfigFileReader.instance.getDouble("quest.y_translation").valueOr(0d);

    private static Transform2d QUEST_TO_ROBOT =
            new Transform2d(x_offset, y_offset, Rotation2d.fromDegrees(rotation));

    private static Pose2d robotPose =
            new Pose2d(x_field_translation, y_field_translastion, new Rotation2d());

    private static Pose2d questPose = robotPose.transformBy(QUEST_TO_ROBOT);

    public record QuestStatus(Pose2d currentPose) implements Status {
        public Pose2d getPose() {

            return currentPose;
        }
    }

    public Quest() {
        questNav = new QuestNav();
        questNav.setPose(questPose);
        // resetQuestPosition();
    }

    public void resetQuestPosition() {
        questNav.setPose(questPose);
    }

    protected QuestStatus updateStatus() {
        Pose2d currentPose = questNav.getPose();
        questNav.setPose(currentPose.transformBy(QUEST_TO_ROBOT));
        return new QuestStatus(currentPose);
    }
}
