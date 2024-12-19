package com.team766.hal;

import com.team766.library.SetValueProvider;
import com.team766.library.ValueProvider;

public class PIDConfig {
    private ValueProvider<Double> p = new SetValueProvider<>();
    private ValueProvider<Double> i = new SetValueProvider<>();
    private ValueProvider<Double> d = new SetValueProvider<>();
    private ValueProvider<Double> ff = new SetValueProvider<>();
    private ValueProvider<Double> outputMaxLow = new SetValueProvider<>();
    private ValueProvider<Double> outputMaxHigh = new SetValueProvider<>();

    public void setP(ValueProvider<Double> value) {
        p = value;
    }

    public void setI(ValueProvider<Double> value) {
        i = value;
    }

    public void setD(ValueProvider<Double> value) {
        d = value;
    }

    public void setFF(ValueProvider<Double> value) {
        ff = value;
    }

    public void setOutputRange(ValueProvider<Double> minOutput, ValueProvider<Double> maxOutput) {
        outputMaxLow = minOutput;
        outputMaxHigh = maxOutput;
    }

    public void apply(MotorController controller) {
        controller.setP(p.valueOr(0.0));
        controller.setI(i.valueOr(0.0));
        controller.setD(d.valueOr(0.0));
        controller.setFF(ff.valueOr(0.0));
        controller.setOutputRange(outputMaxLow.valueOr(-1.0), outputMaxHigh.valueOr(1.0));
    }
}
