package org.example;

import java.util.concurrent.Semaphore;

/**
 * EventRequest
 */
public record EventRequest(Thread th, Semaphore sem) {
}
