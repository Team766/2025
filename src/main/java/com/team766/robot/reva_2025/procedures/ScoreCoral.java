package com.team766.robot.reva_2025.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva_2025.constants.CoralConstants.CoralConstant;

public class ScoreCoral extends Procedure{

    private CoralConstant position;
    private double levelHeight;
    private double angle;
    public ScoreCoral(CoralConstant position, double levelHeight, double angle){
        this.position = position;
        this.levelHeight = levelHeight;
        this.angle = angle;
    }

    public void run(Context context) {
        
    }
    
}
