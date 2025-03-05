package com.team766.framework3;

import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import java.util.StringJoiner;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * MechanismsAspect ensures that all public methods of a {@link Mechanism} class can only be called
 * by {@link Rule}s or {@link Procedure}s that have reserved that Mechanism. It also automatically
 * logs all calls to any method that it checks, including the value of any arguments that are passed
 * to the method.
 *
 * If a public method may be called without reserving the Mechanism, apply the
 * {@link NoReservationRequired} annotation to that method, but you probably won't need to do that.
 *
 * This uses AspectJ to insert some additional code at the beginning of each public method of every
 * Mechanism class while the class is being compiled. Google for AspectJ or
 * "aspect-oriented programming" for more information.
 */
@Aspect
public class MechanismsAspect {
    // Insert this code at the beginning of all public methods of subclasses of Reservable that are
    // not also methods of the Mechanism base class, or any other base class contained in the
    // framework3 package. Also ignore any methods that are annotated with NoReservationRequired.
    @Before(
            "execution(public * com.team766.framework3.Reservable+.*(..))"
                    + " && !within(com.team766.framework3.*)"
                    + " && !@annotation(com.team766.framework3.NoReservationRequired)")
    public void mechanismCheckedPublicMethods(JoinPoint joinPoint) {
        final Reservable mechanism = (Reservable) joinPoint.getTarget();

        mechanism.checkContextReservation();

        var nameBuilder = new StringBuilder();
        nameBuilder.append(mechanism.getName());
        nameBuilder.append(".");
        nameBuilder.append(joinPoint.getSignature().getName()); // Name of the method that is called
        nameBuilder.append("(");
        var argsJoiner = new StringJoiner(", ", nameBuilder, ")");
        for (var arg : joinPoint.getArgs()) {
            argsJoiner.add(String.valueOf(arg));
        }
        // TODO: should this log to mechanism's logger category?
        Logger.get(Category.MECHANISMS).logRaw(Severity.DEBUG, argsJoiner.toString());
    }
}
