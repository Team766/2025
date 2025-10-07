package com.team766.logging;

import java.util.Date;

public record LogEntry(Date time, Severity severity, Category category, String message) {}
