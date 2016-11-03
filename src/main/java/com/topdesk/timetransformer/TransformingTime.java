package com.topdesk.timetransformer;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.topdesk.timetransformer.agent.DoNotInstrument;

/**
 * An implementation of {@link Time} with an internal clock that can be manipulated.
 */
@DoNotInstrument
public enum TransformingTime implements Time {
	INSTANCE;
	
	private final AtomicReference<Clock> clock = new AtomicReference<>(Clock.DEFAULT);
	
	@Override
	public long currentTimeMillis() {
		return clock.get().time();
	}
	
	/**
	 * @return {@code true} if this is the currently active Time.
	 */
	public static boolean active() {
		return TimeTransformer.isActiveTime(TransformingTime.INSTANCE);
	}
	
	/**
	 * Returns a {@link Changer} object to manipulate the clock.
	 * @return a {@code Changer} object to manipulate the clock
	 */
	public static Changer change() {
		return new Changer();
	}
	
	/**
	 * Apply the {@link Changer} to the current clock.
	 * @param changer containing the intended changes to the clock
	 */
	public void apply(Changer changer) {
		clock.set(changer.apply(clock.get()));
	}
	
	/**
	 * Restores the internal clock such that it runs at normal speed, with the actual time.
	 */
	public void restoreTime() {
		clock.set(Clock.DEFAULT);
	}
	
	/**
	 * Returns a descriptive, human-readable status of the internal clock.
	 * @return status of the internal clock
	 */
	public String status() {
		return clock.get().status();
	}
	
	/**
	 * An object to manipulate the clock.
	 * 
	 * <p>The {@code Changer} collects the intended changes to the clock. These changes are only applied when {@link TransformingTime#apply(Changer)} is called.
	 *
	 * <p>Example:<br>
	 * {@code TransformingTime.INSTANCE.apply(TransformingTime.change().move(1000).slowdown(10).start())}
	 */
	@DoNotInstrument
	public static class Changer {
		private int numerator;
		private int divisor;
		private long timestamp;
		private boolean timestampSet;
		private long move;
		private boolean moveSet;
		private boolean running;
		private boolean runningSet;
		
		private Changer() {
			// use change()
		}
		
		/**
		 * Sets the internal clock at the specified milliseconds since epoch.
		 * 
		 * @param time milliseconds since epoch
		 * @return this
		 */
		public Changer at(long time) {
			this.timestamp = time;
			timestampSet = true;
			return this;
		}
		
		/**
		 * Sets the internal clock at the specified date time.
		 * 
		 * @param time date time
		 * @return this
		 */
		public Changer at(ZonedDateTime time) {
			return at(time.toInstant().toEpochMilli());
		}
		
		/**
		 * Speeds up the internal clock by {@code factor}, so real-life second will advance the internal clock {@code factor} seconds.
		 * 
		 * @param factor factor to speed up time
		 * @return this
		 */
		public Changer speedup(int factor) {
			numerator = factor;
			divisor = 1;
			return this;
		}
		
		/**
		 * Slows down the internal clock by {@code factor}, so every internal second will take {@code factor} real-life seconds.
		 * 
		 * @param factor factor to slow down time
		 * @return this
		 */
		public Changer slowdown(int factor) {
			numerator = 1;
			divisor = factor;
			return this;
		}
		
		/**
		 * Moves the time by the specified amount. A negative amount will set the clock back by that amount.
		 * 
		 * @param delta the amount to move the clock, may be negative
		 * @param timeunit the unit of delta
		 * @return this
		 * @throws NullPointerException when timeunit is {@code null}
		 */
		public Changer move(long delta, TimeUnit timeunit) {
			this.move = timeunit.toMillis(delta);
			moveSet = true;
			return this;
		}
		
		/**
		 * Starts the clock.
		 * @return this
		 */
		public Changer start() {
			running = true;
			runningSet = true;
			return this;
		} 
		
		/**
		 * Stops the clock.
		 * 
		 * <p>Note: some applications can't handle a fully stopped clock. Consider slowing down the time with a factor.
		 * @return this
		 * @see #slowdown
		 */
		public Changer stop() {
			running = false;
			runningSet = true;
			return this;
		}
		
		private Clock apply(Clock previous) {
			long now = System.currentTimeMillis();
			long newTime = previous.time();
			if (timestampSet) {
				newTime = timestamp;
			}
			if (moveSet) {
				newTime += move;
			}
			int newNumerator = numerator == 0 ? previous.speedNumerator : numerator;
			int newDivisor = divisor == 0 ? previous.speedDivisor : divisor;
			boolean newRunning = runningSet ? running : previous.running;
			return new Clock(newRunning, newTime, now, newNumerator, newDivisor);
		}
	}
	
	@DoNotInstrument
	private static final class Clock {
		private static final Clock DEFAULT = new Clock(true, 0, 0, 1, 1);
		
		private final boolean running;
		private final long value;
		private final long offset;
		private final int speedNumerator;
		private final int speedDivisor;
		private final long timestampLastChange;
		
		private Clock(boolean running, long value, long when, int speedNumerator, int speedDivisor) {
			this.running = running;
			this.value = value;
			this.offset = when - value;
			this.speedNumerator = speedNumerator;
			this.speedDivisor = speedDivisor;
			this.timestampLastChange = when;
		}
		
		private long time() {
			if (running) {
				long delta = System.currentTimeMillis() - timestampLastChange;
				return timestampLastChange - offset + delta * speedNumerator / speedDivisor;
			}
			return value;
		}
		
		private String status() {
			if (!TransformingTime.active()) {
				return "Custom time implementation TransformingTime is not in use.";
			}
			return "Replaced currentTimeMillis: current time: " + time() + " and the clock " + (running ? "runs with factor: " + speedNumerator / speedDivisor : "is stopped");
		}
	}
}
