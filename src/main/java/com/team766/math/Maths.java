package com.team766.math;

import java.util.function.Function;

public class Maths {
    public static double clamp(final double x, final double min, final double max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }

    public static double deadzone(double value, double deadzone) {
        return java.lang.Math.abs(value) >= deadzone ? value : 0;
    }

    public static boolean overlaps(int start1, int end1, int start2, int end2) {
        return start1 < end2 && start2 < end1;
    }

    public static boolean overlaps(double start1, double end1, double start2, double end2) {
        return start1 < end2 && start2 < end1;
    }

    public static <T extends Comparable<T>> boolean overlaps(T start1, T end1, T start2, T end2) {
        return start1.compareTo(end2) < 0 && start2.compareTo(end1) < 0;
    }

    /**
     * Returns the given angle, normalized to be within the range [-180, 180)
     */
    public static double normalizeAngleDegrees(double angle) {
        while (angle < -180) {
            angle += 360;
        }
        while (angle >= 180) {
            angle -= 360;
        }
        return angle;
    }

    /**
     * Performs simple linear interpolation (as described in
     * https://en.wikipedia.org/wiki/Linear_interpolation) of data in an array of type T.
     * NOTE: the data array must be sorted by x, from lowest to highest.
     *
     * The x values (eg measured data and target data point) should be available via a getter in T.
     * The y values (what this interpolates from measured data) should be available via a getter in T.
     *
     * Example usage:
     * <pre>
     *   public record Data(double x, double y);
     *   ...
     *   Data[] data = new Data[] { new Data(0.0, 1.0), new Data(1.0, 32.0), ... };
     *   double interpolatedY = Math.interpolate(data, 0.5, Data::x, Data::y);
     * </pre>
     *
     * @param <T> The class containing the x and y data.
     * @param data An array of data points, sorted by x from lowest to highest.
     * @param targetX The target x value.
     * @param xGetter A getter function that takes a T and returns the x value for that data point.
     * @param yGetter A getter function that takes a T and returns the y value for that data point.
     * @return The interpolated y value.  Returns the lowest y in data if x is below/at the low end of the range.
     * Returns the highest y if x is above.
     */
    public static <T> double interpolate(
            T[] data, double targetX, Function<T, Double> xGetter, Function<T, Double> yGetter) {
        if (data.length == 0) {
            throw new IndexOutOfBoundsException("No data to interpolate!");
        }
        // data must be sorted, lowest to highest by x
        if (targetX <= xGetter.apply(data[0])) {
            // if our target x is below / at the beginning of the data, return the first y
            return yGetter.apply(data[0]);
        } else if (targetX >= xGetter.apply(data[data.length - 1])) {
            // if our target x is at / beyond the end of the data, return the final y
            return yGetter.apply(data[data.length - 1]);
        }

        // search for the target x in the data range
        int index;
        double x1;

        for (index = 1; ; ++index) {
            x1 = xGetter.apply(data[index]);

            // found where our target x fits in our data range
            if (targetX < x1) {
                break;
            }
        }

        // interpolate
        final double x0 = xGetter.apply(data[index - 1]);
        final double y0 = yGetter.apply(data[index - 1]);
        final double y1 = yGetter.apply(data[index]);

        final double slope = (y1 - y0) / (x1 - x0);

        return y0 + (targetX - x0) * slope;
    }

    // Returns the smallest number
    // x such that both:
    // x % modulus1 = remainder1
    // x % modulus2 = remainder2
    // Assumption: Numbers in num[] are pairwise
    // coprime (gcd for every pair is 1)
    public static double chineseRemainder(
            double remainder1, int modulus1, double remainder2, int modulus2) {
        // https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Pseudocode

        if (modulus1 <= 1 || modulus2 <= 1) {
            throw new IllegalArgumentException("Moduli must be greater than 1");
        }

        int bezout1, bezout2;
        int greatest_common_divisor;
        {
            int s = 0, old_s = 1;
            int r = modulus2, old_r = modulus1;

            while (r != 0) {
                final int quotient = old_r / r;

                final int rp = r;
                r = old_r - quotient * r;
                old_r = rp;

                final int sp = s;
                s = old_s - quotient * s;
                old_s = sp;
            }

            bezout1 = old_s;
            bezout2 = (old_r - old_s * modulus1) / modulus2;
            greatest_common_divisor = old_r;
        }

        // https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Generalization_to_non-coprime_moduli

        return (remainder1 * bezout2 * modulus2 + remainder2 * bezout1 * modulus1)
                / greatest_common_divisor;
    }
}
