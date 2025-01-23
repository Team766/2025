package com.team766.orin;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.ArrayList;

public class GetApriltagPoseData {

    public static ArrayList<TimestampedApriltag> getAllTags(ArrayList<Double> ntList) {
        ArrayList<TimestampedApriltag> apriltags = new ArrayList<TimestampedApriltag>();

        ArrayList<Double> tagData;

        tagData = ntList;

        if (tagData.size() % 5 != 0 || tagData.size() == 0) return apriltags;

        for (int i = 0; i < tagData.size(); i += 5) {
            TimestampedApriltag tag =
                    new TimestampedApriltag(
                            tagData.get(i),
                            tagData.get(i + 1).intValue(),
                            new Pose3d(
                                    new Translation3d(
                                            tagData.get(i + 2),
                                            tagData.get(i + 3),
                                            tagData.get(i + 4)),
                                    new Rotation3d()));
            apriltags.add(tag);
        }
        return apriltags;
    }
}
