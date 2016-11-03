package com.topdesk.timetransformer;

import java.util.concurrent.TimeUnit;

/**
 * Default implementation of Time which redirects to {@link System#currentTimeMillis} and {@link System#nanoTime}.
 * 
 * <p>Calls to {@code System.currentTimeMillis} and {@code System.nanoTime} in implementations of this class will not be redirected by the agent.
 */
public interface Time {
	/**
	 * Returns the current time in milliseconds since epoch.
	 * @return the current time in milliseconds since epoch
	 * @see System#currentTimeMillis()
	 */
	long currentTimeMillis();
	
	/**
	 * Returns the current time in nanoseconds since epoch.
	 * 
	 * If not implemented, returns {@link #currentTimeMillis} in nanoseconds.
	 * @return the current time in nanoseconds since epoch
	 * @see System#nanoTime()
	 */
	default long nanoTime() {
		return TimeUnit.MILLISECONDS.toNanos(currentTimeMillis());
	}
}
