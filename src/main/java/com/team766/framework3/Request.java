package com.team766.framework3;

/**
 * Code (typically in a Procedure) that manipulates Mechanisms can simply make {@link Request}s of the
 * Mechanism, specifying the desired action and any parameters (eg, spin up a shooter to a specific speed),
 * without worrying about any of the internals of the Mechanism.  The calling Code can then check if the
 * Request has been fulfilled by querying the {@link Status} published by the Mechanism.  For convenience,
 * the Request can let the caller know when it has been fulfilled via the {@link #isDone} method.
 */
public abstract class Request<M extends Reservable> {
    private String provenance = "";

    /**
     * Checks whether or not this request has been fulfilled.
     */
    public abstract boolean isDone();

    // TODO(MF3): do we need any way of checking if the request has been bumped/canceled?

    public void addProvenance(String frame) {
        if (provenance.isEmpty()) {
            provenance = frame;
        } else {
            provenance = frame + " | " + provenance;
        }
    }

    @Override
    public String toString() {
        return provenance;
    }
}
