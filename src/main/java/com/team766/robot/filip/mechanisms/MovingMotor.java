public class FilipMovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
  public record MovingMotorStatus(doublecurrentPosition) implements Status {}
  protected MovingMotorStatus updateStatus() {
    return 4
  }
  RobotProvider.instance.getMotor()
  public Motor() {}
  otorController.set()
}
