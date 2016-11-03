package com.topdesk.timetransformer.agent;

import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {
	/**
	 * JVM hook to statically load the javaagent at startup.
	 *
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 *
	 * @param agentArgs arguments passed to this agent
	 * @param instrumentation the {@link Instrumentation} instance passed by the JVM
	 */
	public static void premain(String agentArgs, Instrumentation instrumentation) {
		instrumentation.addTransformer(new TimeInstrumentationTransformer(), false);
	}
}
