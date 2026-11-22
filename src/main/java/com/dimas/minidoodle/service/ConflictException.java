package com.dimas.minidoodle.service;

/** Thrown when a request violates a business invariant (overlap, double booking, ...). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
