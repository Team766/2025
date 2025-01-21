package com.team766.robot.common.mechanisms;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.CANdle;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.library.ValueProvider;
import com.team766.logging.Category;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

public class LEDString extends Mechanism {
    private final CANdle candle;

    public LEDString(String configPrefix) {
        final ValueProvider<Integer> deviceId =
                ConfigFileReader.getInstance().getInt(configPrefix + ".deviceId");
        final ValueProvider<String> canBus =
                ConfigFileReader.getInstance().getString(configPrefix + ".CANBus");
        candle = new CANdle(deviceId.get(), canBus.valueOr(""));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }

    private void handleError(ErrorCode e) {
        if (!e.equals(ErrorCode.OK)) {
            log(Severity.ERROR, "Error setting LED segment " + e);
        }
    }

    public void setColor(int r, int g, int b) {
        checkContextReservation();
        handleError(candle.setLEDs(r, g, b));
    }

    public void setColor(Color color) {
        var color8 = new Color8Bit(color);
        setColor(color8.red, color8.green, color8.blue);
    }

    public void animate(Animation animation) {
        handleError(candle.animate(animation));
    }
}
