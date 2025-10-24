package com.team766.simulator.interfaces;

import edu.wpi.first.math.geometry.Pose3d;

public abstract class Joint {
    protected final Pose3d anchorLocation;
    protected final Pose3d connectedBodyAnchor;
    protected final SimBody connectedBody;

    public Joint(Pose3d anchorLocation, Pose3d connectedBodyAnchor, SimBody connectedBody) {
        this.anchorLocation = anchorLocation;
        this.connectedBodyAnchor = connectedBodyAnchor;
        this.connectedBody = connectedBody;
    }

    public final SimBody.Action step(SimBody.State state, double dt) {
        
    }

    protected abstract SimBody.Action stepImpl(SimBody.State state, double dt);
}
