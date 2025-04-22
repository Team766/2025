package com.team766.orin;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.ArrayList;
import java.util.List;

public class GetApriltagPoseData {

    public static List<TimestampedApriltag> getAllTags(double[] ntArray, double covariance) {
        ArrayList<TimestampedApriltag> apriltags = new ArrayList<TimestampedApriltag>();

        double[] tagData;

        tagData = ntArray;

        if (tagData.length % 5 != 0 || tagData.length == 0) return apriltags;

        for (int i = 0; i < tagData.length; i += 5) {
            TimestampedApriltag tag =
                    new TimestampedApriltag(
                            tagData[i],
                            (int) tagData[i + 1],
                            new Pose3d(
                                    new Translation3d(
                                            tagData[i + 2], tagData[i + 3], tagData[i + 4]),
                                    new Rotation3d()),
                            covariance);
            apriltags.add(tag);
        }
        return apriltags;
    }
}
