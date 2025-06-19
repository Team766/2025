package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.quest.QuestNav;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.measure.Angle;

public class Quest extends MechanismWithStatus<Quest.QuestStatus> {

    private QuestNav questNav;
    /*The FRC coordinate system follows these standards:

    X (First value): Positive -> Forwards from robot center
    Y (Second value): Positive -> Left from robot center
    Rotation (Third value): Positive -> Counter Clockwise */

    // First, Declare our geometrical transform from the Quest to the robot center

    private static double x_offset = ConfigFileReader.instance.getDouble("quest.x_offset").get();
    private static double y_offset = ConfigFileReader.instance.getDouble("quest.y_offset").get();
    private static double rotation_x = ConfigFileReader.instance.getDouble("quest.rotation_x").get();
    private static double rotation_y = ConfigFileReader.instance.getDouble("quest.rotation_y").get();
    private static Transform2d QUEST_TO_ROBOT =
            new Transform2d(x_offset, y_offset, new Rotation2d(rotation_x, rotation_y));

    public record QuestStatus(Pose2d questPose) implements Status {
        public Pose2d getPose() {
            return questPose.transformBy(QUEST_TO_ROBOT.inverse());
        }
    }

    public Quest() {
        questNav = new QuestNav();
    }

    protected QuestStatus updateStatus() {
        Pose2d questPose = questNav.getPose();
        return new QuestStatus(questPose);
    }
}
