package com.team766.math;

import edu.wpi.first.math.Vector;
import edu.wpi.first.math.numbers.N2;

public class Vectors {
    public static double cross2d(Vector<N2> a, Vector<N2> b) {
        return a.get(0) * b.get(1) - b.get(0) * a.get(1);
    }
}
