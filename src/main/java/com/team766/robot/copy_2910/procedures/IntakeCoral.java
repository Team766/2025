package com.team766.robot.copy_2910.procedures;

import java.util.Optional;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.copy_2910.mechanisms.Intake;

public class IntakeCoral extends Procedure {

    private Intake intake;
    
    public IntakeCoral() {
        intake = reserve(intake);
    }

    @Override
    public void run(Context context) {
        Optional<Intake.IntakeStatus> status = getStatus(Intake.IntakeStatus.class);
        if (status.isEmpty()){
            log("No intake status"); 
            return;
        } 

        while(!status.get().hasCoralInBackCenter()){

        }
        
    }
}
