package com.topdesk.timetransformer;

import com.topdesk.timetransformer.agent.DoNotInstrument;

/**
 * Default implementation of Time which redirects all calls to {@link System#currentTimeMillis} and {@link System#nanoTime}.
 * 
 * <p>Calls from this class to {@code System.currentTimeMillis} and {@code System.nanoTime} will not be redirected by the agent.
 */
@DoNotInstrument
public enum DefaultTime implements Time {
	/**
	 * Singleton instance of DefaultTime
	 */
	INSTANCE;
	
	@Override
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}
	
	@Override
	public long nanoTime() {
		return System.nanoTime();
	}
	
	@Override
	public String toString() {
		return super.toString() + ", DefaultTime";
	}
}
