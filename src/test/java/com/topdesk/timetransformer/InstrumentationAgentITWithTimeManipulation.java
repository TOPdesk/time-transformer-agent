package com.topdesk.timetransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstrumentationAgentITWithTimeManipulation {
	private static final long time = 1_000_000_000l;
	
	@BeforeEach
	public void before() {
		TimeTransformer.setTime(TransformingTime.INSTANCE);
		TransformingTime.INSTANCE.apply(TransformingTime.change().at(time).stop());
	}
	
	@AfterEach
	public void after() {
		TimeTransformer.setTime(DefaultTime.INSTANCE);
	}
	
	@Test
	public void testSystemCurrentTimeMillis() {
		assertEquals(time, System.currentTimeMillis());
	}
	
	@Test
	public void testSystemNanoTime() {
		assertEquals(TimeUnit.MILLISECONDS.toNanos(time), System.nanoTime());
	}
	
	@Test
	public void testClockSystemDefaultZoneMillis() {
		assertEquals(time, Clock.systemDefaultZone().millis());
	}
	
	@Test
	public void testClockSystemDefaultZoneInstant() {
		assertEquals(time, Clock.systemDefaultZone().instant().toEpochMilli());
	}
	
	@Test
	public void testDate() {
		assertEquals(time, new Date().toInstant().toEpochMilli());
	}
}
