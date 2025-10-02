import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.simulator.elements.MotorController;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
    MotorController motor = RobotProvider.instance.getMotor("LeftMotor"):
    public record MovingMotorStatus(double currentPosition) implements Status {}
}
