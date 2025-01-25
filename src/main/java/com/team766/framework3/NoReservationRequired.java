package com.team766.framework3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default, {@link MechanismsAspect} ensures that all public methods of a {@link Mechanism} class
 * can only be called by {@link Rule}s or {@link Procedure}s that have reserved that Mechanism.
 * If a public method may be called without reserving the Mechanism, apply this annotation to that
 * method to bypass the automatic call to {@link Mechanism#checkContextReservation()}.
 * Unless you know what you're doing, this annotation should only be applied to methods that don't
 * change any of the Mechanism's state (programmatic or physical).
 * This is an advanced feature; you probably shouldn't use it. Most uses of non-mutating methods
 * should be solved using {@link Status}es.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NoReservationRequired {}
