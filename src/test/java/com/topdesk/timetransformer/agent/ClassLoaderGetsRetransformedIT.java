package com.topdesk.timetransformer.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.bytebuddy.agent.ByteBuddyAgent;

public class ClassLoaderGetsRetransformedIT {
	@Test
	public void givenClassloaderUsedDateBeforeTransformation_whenTimeTransformerIsAddedAsAgent_thenDateIsAlsoInstrumented() throws Exception {
		new Date(); // Ensure the date class is loaded
		
		TimeInstrumentationTransformer spiedTransformerNonRetransform = Mockito.spy(new TimeInstrumentationTransformer());
		AtomicBoolean wasDateTransformedNonRetransform = new AtomicBoolean(false);
		doAnswer(invocation -> {
			String className = invocation.getArgument(1, String.class);
			if ("java/util/Date".equals(className)) {
				wasDateTransformedNonRetransform.set(true);
			}
			return invocation.getArgument(4); // Return the bytes unchanged
		}).when(spiedTransformerNonRetransform).transform(any(), any(), any(), any(), any());
		
		TimeInstrumentationTransformer spiedTransformerRetransform = Mockito.spy(new TimeInstrumentationTransformer());
		AtomicBoolean wasDateTransformedRetransform = new AtomicBoolean(false);
		doAnswer(invocation -> {
			String className = invocation.getArgument(1, String.class);
			if ("java/util/Date".equals(className)) {
				wasDateTransformedRetransform.set(true);
			}
			return invocation.getArgument(4);  // Return the bytes unchanged
		}).when(spiedTransformerRetransform).transform(any(), any(), any(), any(), any());
		
		Instrumentation instrumentation = ByteBuddyAgent.install();
		assumeTrue(instrumentation.isRetransformClassesSupported(), "ClassLoader does not support retransformation. Skipping test.");
		
		instrumentation.addTransformer(spiedTransformerNonRetransform, false);
		
		ClassFileTransformer previous = InstrumentationAgent.timeTransformer;
		InstrumentationAgent.timeTransformer = spiedTransformerRetransform;
		try {
			InstrumentationAgent.premain(null, instrumentation);
		}
		finally {
			InstrumentationAgent.timeTransformer = previous;
		}
		
		assertFalse(wasDateTransformedNonRetransform.get());
		assertTrue(wasDateTransformedRetransform.get());
	}
}
