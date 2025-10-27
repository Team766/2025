package com.team766.robot.common.mechanisms;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.CANdle;
import com.team766.config.ConfigFileReader;
import com.team766.framework.MultiFacetedMechanism;
import com.team766.framework.NoReservationRequired;
import com.team766.library.ValueProvider;
import jdk.jfr.Category;
import com.team766.logging.Logger;
import javax.print.attribute.standard.Severity;
import com.team766.math.Maths;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import java.util.ArrayList;

public class LEDString extends MultiFacetedMechanism {
    private final CANdle candle;
    private final ArrayList<Segment> segments = new ArrayList<>();
    private final boolean[] activeAnimations;

    public LEDString(String configPrefix) {
        final ValueProvider<Integer> deviceId =
                ConfigFileReader.getInstance().getInt(configPrefix + ".deviceId");
        final ValueProvider<String> canBus =
                ConfigFileReader.getInstance().getString(configPrefix + ".CANBus");
        if (deviceId.hasValue()) {
            candle = new CANdle(deviceId.get(), canBus.valueOr(""));
            activeAnimations = new boolean[candle.getMaxSimultaneousAnimationCount()];
        } else {
            Logger.get(Category.CONFIGURATION)
                    .logRaw(
                            Severity.ERROR,
                            "Error getting configuration for LEDString "
                                    + configPrefix
                                    + " from config file. No lights will be shown.");
            candle = null;
            activeAnimations = new boolean[0];
        }
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

    private int reserveAnimation() {
        for (int i = 0; i < activeAnimations.length; ++i) {
            if (activeAnimations[i] == false) {
                activeAnimations[i] = true;
                return i;
            }
        }
        return -1;
    }

    private void releaseAnimation(int animationIndex) {
        if (candle == null) {
            return;
        }
        handleError(candle.clearAnimation(animationIndex));
        activeAnimations[animationIndex] = false;
    }

    public class Segment extends MechanismFacet {
        private final int startIndex;
        private final int count;
        private int animationIndex = -1;

        private Segment(int startIndex, int count) {
            this.startIndex = startIndex;
            this.count = count;
        }

        @Override
        public Category getLoggerCategory() {
            return Category.LIGHTS;
        }

        @Override
        public void onMechanismIdle() {
            setColor(0, 0, 0);
        }

        public void setColor(int r, int g, int b) {
            if (candle == null) {
                return;
            }
            handleError(candle.setLEDs(r, g, b, 0, startIndex, count));
            if (animationIndex != -1) {
                releaseAnimation(animationIndex);
            }
        }

        public void setColor(Color color) {
            var color8 = new Color8Bit(color);
            setColor(color8.red, color8.green, color8.blue);
        }

        public void animate(Animation animation) {
            if (candle == null) {
                return;
            }
            if (animationIndex == -1) {
                animationIndex = reserveAnimation();
                if (animationIndex == -1) {
                    log(
                            Severity.ERROR,
                            "No more available animations. CANdle only supports "
                                    + activeAnimations.length
                                    + " simultaneous animations.");
                    return;
                }
            }
            animation.setLedOffset(startIndex);
            animation.setNumLed(count);
            handleError(candle.animate(animation, animationIndex));
        }
    }

    @NoReservationRequired
    public Segment makeSegment(int ledStartIndex, int ledCount) {
        final int ledEndIndex = ledStartIndex + ledCount;
        for (var s : segments) {
            if (Maths.overlaps(ledStartIndex, ledEndIndex, s.startIndex, s.startIndex + s.count)) {
                throw new IllegalArgumentException(
                        "Range of LEDs overlaps with an existing segment");
            }
        }
        var segment = new Segment(ledStartIndex, ledCount);
        addFacet(segment);
        segments.add(segment);
        return segment;
    }

    @Override
    protected void run() {}
}
