package com.topdesk.timetransformer.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Java Agent class to startup the TimeInstrumentationTransformer agent
 */
public class InstrumentationAgent {
	// Visible for testing
	static ClassFileTransformer timeTransformer = new TimeInstrumentationTransformer();
	
	/**
	 * JVM hook to statically load the javaagent at startup.
	 *
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 *
	 * @param agentArgs arguments passed to this agent
	 * @param instrumentation the {@link Instrumentation} instance passed by the JVM
	 * @throws UnmodifiableClassException  if a specified class cannot be modified(isModifiableClass would return false)
	 */
	public static void premain(@SuppressWarnings("unused") String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
		instrumentation.addTransformer(timeTransformer, true);
		
		if (instrumentation.isRetransformClassesSupported()) {
			Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();
			for (int i = 0; i < allLoadedClasses.length; i++) {
				Class<?> clazz = allLoadedClasses[i];
				if (instrumentation.isModifiableClass(clazz)) {
					instrumentation.retransformClasses(clazz);
				}
			}
		}
	}
}
