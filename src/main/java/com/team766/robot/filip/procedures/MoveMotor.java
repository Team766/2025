public class MoveMotor extends Procedure {
  private FilipMovingMotor motor;
  public MoveMotor(MovingMotor myMotor){
    motor = reserve(myMotor);
  }

}
