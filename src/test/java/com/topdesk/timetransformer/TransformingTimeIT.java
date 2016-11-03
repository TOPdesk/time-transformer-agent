package com.topdesk.timetransformer;

import static org.junit.Assert.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.topdesk.timetransformer.TransformingTime;

// NOTE: This class is system dependent. It makes extensive use of Thread.sleep.
public class TransformingTimeIT {
	private static final ZonedDateTime INITIAL_DATE = ZonedDateTime.of(1970, 1, 1, 12, 34, 56, 789, ZoneId.of("UTC"));
	private static final long INITAL_TIME = INITIAL_DATE.toInstant().toEpochMilli();
	
	@Before
	public void before() {
		TransformingTime.INSTANCE.restoreTime();
	}
	
	@Test
	public void setTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE));
		assertEquals(INITAL_TIME, TransformingTime.INSTANCE.currentTimeMillis());
	}
	
	@Test
	public void restoreTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE));
		TransformingTime.INSTANCE.restoreTime();
		assertEquals(System.currentTimeMillis(), TransformingTime.INSTANCE.currentTimeMillis());
	}
	
	@Test
	public void stopTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().stop());
		long now = TransformingTime.INSTANCE.currentTimeMillis();
		
		pause(200L);
		assertTrue(TransformingTime.INSTANCE.currentTimeMillis() <= now);
	}
	
	@Test
	public void startTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().stop());
		
		long now = TransformingTime.INSTANCE.currentTimeMillis();
		TransformingTime.INSTANCE.apply(TransformingTime.change().start());
		
		pause(200L);
		assertTrue(TransformingTime.INSTANCE.currentTimeMillis() >= now + 150L);
	}
	
	@Test
	public void startTimeFromTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE));
		
		pause(200L);
		assertTrue(TransformingTime.INSTANCE.currentTimeMillis() >= INITAL_TIME + 200L);
	}
	
	@Test
	public void stopTimeAtTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE).stop());
		
		pause(200L);
		assertEquals(INITAL_TIME, TransformingTime.INSTANCE.currentTimeMillis());
	}
	
	@Test
	public void forwardTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE).move(2L, TimeUnit.SECONDS).stop());
		
		pause(200L);
		assertEquals(INITAL_TIME + 2000L, TransformingTime.INSTANCE.currentTimeMillis());
	}
	
	@Test
	public void reverseTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE).move(-3L, TimeUnit.SECONDS).stop());
		
		pause(200L);
		assertEquals(INITAL_TIME - 3000L, TransformingTime.INSTANCE.currentTimeMillis());
	}
	
	@Test
	public void slowTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE).slowdown(4));
		
		pause(1000L);
		long elapsedTime = TransformingTime.INSTANCE.currentTimeMillis() - INITAL_TIME;
		assertTrue(240L < elapsedTime && elapsedTime < 300L);
	}
	
	@Test
	public void speedupTimeTest() {
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(INITIAL_DATE).speedup(4));
		
		pause(250L);
		long elapsedTime = TransformingTime.INSTANCE.currentTimeMillis() - INITAL_TIME;
		assertTrue(960L < elapsedTime && elapsedTime < 1100L);
	}
	
	private void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
