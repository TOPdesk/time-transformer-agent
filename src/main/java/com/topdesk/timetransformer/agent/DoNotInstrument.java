package com.topdesk.timetransformer.agent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface to annotate classes that should not be instrumented with the TimeTransformer
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface DoNotInstrument {
	// Marker interface
}
