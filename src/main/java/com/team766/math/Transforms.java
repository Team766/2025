package com.team766.math;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.geometry.Twist3d;

public class Transforms {
    public static Twist2d rotateBy(Twist2d in, Rotation2d xf) {
        var d = new Translation2d(in.dx, in.dy).rotateBy(xf);
        return new Twist2d(d.getX(), d.getY(), in.dtheta);
    }

    public static Twist3d rotateBy(Twist3d in, Rotation3d xf) {
        var d = new Translation3d(in.dx, in.dy, in.dz).rotateBy(xf);
        var r = new Translation3d(in.rx, in.ry, in.rz).rotateBy(xf);
        return new Twist3d(d.getX(), d.getY(), d.getZ(), r.getX(), r.getY(), r.getZ());
    }

    public static Twist2d add(Twist2d a, Twist2d b) {
        return new Twist2d(a.dx + b.dx, a.dy + b.dy, a.dtheta + b.dtheta);
    }

    public static Twist3d add(Twist3d a, Twist3d b) {
        return new Twist3d(
                a.dx + b.dx, a.dy + b.dy, a.dz + b.dz, a.rx + b.rx, a.ry + b.ry, a.rz + b.rz);
    }

    public static Twist2d multiply(Twist2d in, double scalar) {
        return new Twist2d(in.dx * scalar, in.dy * scalar, in.dtheta * scalar);
    }

    public static Twist3d multiply(Twist3d in, double scalar) {
        return new Twist3d(
                in.dx * scalar,
                in.dy * scalar,
                in.dz * scalar,
                in.rx * scalar,
                in.ry * scalar,
                in.rz * scalar);
    }

    public static Twist2d toTwist2d(Twist3d in) {
        return new Twist2d(in.dx, in.dy, in.rz);
    }
}
