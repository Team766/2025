import com.team766.framework.Status;
import com.team766.simulator.elements.MotorController;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
    MotorController motor = RobotProvider.instance.getMotor("LeftMotor");
    public MovingMotor() {
    }
    public void MoveMotor(double motorPower) {
        motor.set(motorPower);
    }
    public record MovingMotorStatus(double currentPosition) implements Status {
        protected MovingMotorStatus updateStatus() {
            return new MovingMotorStatus(motor.getPosition());
        }
    }
}