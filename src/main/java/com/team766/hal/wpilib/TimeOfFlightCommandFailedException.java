public class TimeOfFlightCommandFailedException extends Exception {
    public TimeOfFlightCommandFailedException(String message) {
        super(message);
    }

    public TimeOfFlightCommandFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
