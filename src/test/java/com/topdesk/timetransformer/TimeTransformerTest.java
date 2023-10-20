package com.topdesk.timetransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TimeTransformerTest {
	private static final Time ALWAYS_10_SYSTEM_TIME = () -> 10;
	private static final Time EXCEPTION_THROWING_SYSTEM_TIME = () -> {throw new RuntimeException();};
	
	@Test
	public void testSetTime() {
		TimeTransformer.setTime(ALWAYS_10_SYSTEM_TIME);
		assertTrue(TimeTransformer.isActiveTime(ALWAYS_10_SYSTEM_TIME));
		assertFalse(TimeTransformer.isActiveTime(EXCEPTION_THROWING_SYSTEM_TIME));
		
		TimeTransformer.setTime(EXCEPTION_THROWING_SYSTEM_TIME);
		assertFalse(TimeTransformer.isActiveTime(ALWAYS_10_SYSTEM_TIME));
		assertTrue(TimeTransformer.isActiveTime(EXCEPTION_THROWING_SYSTEM_TIME));
	}
	
	@Test
	public void testSetTime_RestoreWithNull() {
		TimeTransformer.setTime(ALWAYS_10_SYSTEM_TIME);
		assertFalse(TimeTransformer.isActiveTime(DefaultTime.INSTANCE));
		TimeTransformer.setTime(null);
		assertTrue(TimeTransformer.isActiveTime(DefaultTime.INSTANCE));
	}
	
	@Test
	public void testCurrentTimeMillis_Always10() {
		TimeTransformer.setTime(ALWAYS_10_SYSTEM_TIME);
		assertEquals(10, TimeTransformer.currentTimeMillis());
	}
	
	@Test
	public void testCurrentTimeMillis_ExceptionThrownInCurrentTimeMillisIsCaught() {
		TimeTransformer.setTime(EXCEPTION_THROWING_SYSTEM_TIME);
		TimeTransformer.currentTimeMillis();
	}
	
	@Test
	public void testNanoTime_Always10() {
		TimeTransformer.setTime(ALWAYS_10_SYSTEM_TIME);
		assertEquals(10_000_000, TimeTransformer.nanoTime());
	}
	
	@Test
	public void testNanoTime_ExceptionThrownInNanoTimeIsCaught() {
		TimeTransformer.setTime(EXCEPTION_THROWING_SYSTEM_TIME);
		TimeTransformer.nanoTime();
	}
}
