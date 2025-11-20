package com.team766.robot.Rookie_Training.mechanisms;
/* package is where the file is
 * 
 * import is importing stuff from other places
 */
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus <MovingMotor.MovingMotorStatus> {

    public MovingMotor() {

    }
    
    public 
    MovingMotorStatus(double currentPosition) implements Status {
    }

/* void (in the thing below) means that it doesn't return anything 
 * unlike in the protected one it returns a new status
 * 
 * double is float
*/
    
    public void setMotorPower(double power) {
        motor.set(power)
    }

    protected MovingMotorStatus updateStatus(){
        return new MovingMotorStatus(0)
    }
}
