package com.team766.robot.reva_2025.mechanisms;

import java.util.ArrayList;
import com.team766.framework.Mechanism;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.orin.*;

public class Vision extends Mechanism{

    private GetOrinRawValue camera;
    
    public Vision(String ntCameraName){
        camera = new GetOrinRawValue(ntCameraName);
    }

    public KalamanApriltag getTagById(int tagId) throws NoTagFoundError, ValueNotFoundOnTableError{
        double[] poseData;
        poseData = camera.getRawPoseData();

        ArrayList<KalamanApriltag> tags = GetApriltagPoseData.getAllTags(poseData);

        for(KalamanApriltag tag : tags){
            if (tag.ID == tagId) return tag;
        }

        throw new NoTagFoundError(tagId);
    }

}
