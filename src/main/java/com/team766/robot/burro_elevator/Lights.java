package com.team766.robot.burro_elevator;

import static com.team766.framework3.Conditions.checkForStatusWith;
import static com.team766.framework3.RulePersistence.*;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework3.Rule;
import com.team766.framework3.RuleEngine;
import com.team766.robot.burro_elevator.mechanisms.Elevator;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.Set;

public class Lights extends RuleEngine {
    private static final int CANID = 5;
    private static final int LED_COUNT = 90;
    private static final Animation rainbowAnimation = new RainbowAnimation(1, 1.5, LED_COUNT);
    private final CANdle candle = new CANdle(CANID);

    public Lights() {
        addRule(
                Rule.create(
                                "Endgame",
                                () ->
                                        DriverStation.getMatchTime() > 0
                                                && DriverStation.getMatchTime() < 17)
                        .withOnTriggeringProcedure(ONCE_AND_HOLD, Set.of(), () -> rainbow()));

        addRule(
                Rule.create(
                                "Elevator top",
                                () ->
                                        checkForStatusWith(
                                                Elevator.ElevatorStatus.class,
                                                s -> s.position() >= Elevator.TOP_POSITION))
                        .withOnTriggeringProcedure(ONCE_AND_HOLD, Set.of(), () -> elevatorAtTop()));

        addRule(
                Rule.create(
                                "Elevator bottom",
                                () ->
                                        checkForStatusWith(
                                                Elevator.ElevatorStatus.class,
                                                s -> s.position() <= Elevator.BOTTOM_POSITION))
                        .withOnTriggeringProcedure(
                                ONCE_AND_HOLD, Set.of(), () -> elevatorAtBottom()));
    }

    public void elevatorAtBottom() {
        candle.setLEDs(255, 0, 0);
    }

    public void elevatorAtTop() {
        candle.setLEDs(0, 255, 0);
    }

    public void rainbow() {
        candle.animate(rainbowAnimation);
    }
}
