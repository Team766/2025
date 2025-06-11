package com.team766.framework.test;

import com.team766.framework.MechanismWithRequests;
import com.team766.framework.Request;
import com.team766.framework.Status;

public class FakeMechanismWithRequests
        extends MechanismWithRequests<
                FakeMechanismWithRequests.FakeRequest, FakeMechanismWithRequests.FakeStatus> {
    public record FakeStatus(int currentState) implements Status {}

    public record FakeRequest(int targetState) implements Request<FakeStatus> {
        @Override
        public boolean isDone(FakeStatus status) {
            return status.currentState() == targetState;
        }
    }

    // Public for testing
    public static final FakeRequest NULL_REQUEST = new FakeRequest(-1);
    public static final FakeRequest INITIAL_REQUEST = new FakeRequest(10);
    public FakeRequest currentRequest = NULL_REQUEST;
    public Boolean wasRequestNew = null;

    @Override
    protected FakeRequest getInitialRequest() {
        return INITIAL_REQUEST;
    }

    @Override
    protected void run(FakeRequest request, boolean isRequestNew) {
        currentRequest = request;
        wasRequestNew = isRequestNew;
    }

    @Override
    protected FakeStatus updateStatus() {
        return new FakeStatus(currentRequest.targetState());
    }
}
