package com.topdesk.timetransformer;

/**
 * Interceptor class to redirect all calls to {@code System.currentTimeMillis()} and {@code System.nanoTime()} to.
 */
public final class TimeTransformer {
	private static volatile Time delegate = DefaultTime.INSTANCE;
	
	/**
	 * Returns the current time in milliseconds since epoch.
	 * 
	 * <p>If the underlying implementation throws an exception, fall back to the {@link DefaultTime}
	 * 
	 * @return the current time in milliseconds since epoch
	 * @see System#currentTimeMillis()
	 */
	public static long currentTimeMillis() {
		try {
			return delegate.currentTimeMillis();
		}
		catch (Throwable t) {
			return DefaultTime.INSTANCE.currentTimeMillis();
		}
	}
	
	/**
	 * Returns the current time in nanoseconds since epoch.
	 * 
	 * <p>If the underlying implementation throws an exception, fall back to the {@link DefaultTime}
	 * 
	 * @return the current time in nanoseconds since epoch
	 * @see System#nanoTime()
	 */
	public static long nanoTime() {
		try {
			return delegate.nanoTime();
		}
		catch (Throwable t) {
			return DefaultTime.INSTANCE.nanoTime();
		}
	}
	
	/**
	 * Set the Time to {@code delegate}.
	 * 
	 * <p>If {@code delegate} is {@code null}, the {@link DefaultTime} is restored.
	 * 
	 * @param delegate the {@code Time} implementation to delegate to
	 */
	public static void setTime(Time delegate) {
		TimeTransformer.delegate = delegate == null ? DefaultTime.INSTANCE : delegate;
	}
	
	/**
	 * Check if the specified {@code Time} instance is the currently active one.
	 * @param systemTime the {@code Time} instance to check
	 * @return true if the currently active {@code Time} equals to {@code systemTime}
	 */
	public static boolean isActiveTime(Time systemTime) {
		return delegate.equals(systemTime);
	}
}
