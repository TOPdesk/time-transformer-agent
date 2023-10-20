package com.topdesk.timetransformer.agent;

import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Bytecode transformer to 
 */
public class TimeInstrumentationTransformer implements ClassFileTransformer {
	private static final String TIME_TRANSFORMER_PACKAGE = "com/topdesk/timetransformer/";
	private static final String TIME_TRANSFORMER_CLASS = TIME_TRANSFORMER_PACKAGE + "TimeTransformer";
	private static final String DO_NOT_INSTRUMENT = "L" + TIME_TRANSFORMER_PACKAGE + "agent/DoNotInstrument;";
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader classReader = new ClassReader(classfileBuffer);
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassInstrumenter classVisitor = new ClassInstrumenter(classWriter, className);
		
		try {
			classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
		} catch (Throwable t) {
			// Try catch block, because ASM sometimes fails silently...
			t.printStackTrace();
		}
		return classWriter.toByteArray();
	}
	
	private static class ClassInstrumenter extends ClassVisitor {
		private final String className;
		private boolean instrument = true;
		
		ClassInstrumenter(ClassVisitor classVisitor, String className) {
			super(ASM9, classVisitor);
			this.className = className;
		}
		
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (DO_NOT_INSTRUMENT.equals(desc)) {
				instrument = false;
			}
			return super.visitAnnotation(desc, visible);
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
			if (!instrument || (access & ACC_NATIVE) != 0) {
				return methodVisitor;
			}
			if ("java/time/Clock$SystemClock".equals(className) && 
					"instant".equals(name) && 
					"()Ljava/time/Instant;".equals(desc)) {
				return new SystemClockMethodInstrumenter(methodVisitor);
			}
			return new MethodInstrumenter(methodVisitor);
		}
	}
	
	/**
	 * MethodVisitor that will rewrite any call to {@link java.lang.System#currentTimeMillis()} and {@link java.lang.System#nanoTime()} to use the TimeTransformer instead
	 */
	private static class MethodInstrumenter extends MethodVisitor {
		MethodInstrumenter(MethodVisitor methodVisitor) {
			super(ASM9, methodVisitor);
		}
		
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if ((owner.equals("java/lang/System") && name.equals("currentTimeMillis") && desc.equals("()J")) ||
					(owner.equals("java/lang/System") && name.equals("nanoTime") && desc.equals("()J"))) {
				mv.visitMethodInsn(opcode, TIME_TRANSFORMER_CLASS, name, desc, itf);
			}
			else {
				super.visitMethodInsn(opcode, owner, name, desc, itf);
			}
		}
	}
	
	/**
	 * MethodVisitor to transform the {@link java.time.Clock$SystemClock#instant()} method to its Java 8 implementation, such that its time is correctly transformed by the TimeTransformer.
	 * 
	 * Java 9 changed the implementation to achieve a higher resolution than System.currentTimeMillis. It calls another native method, so was not instrumented by the TimeTransformer.
	 */
	private static class SystemClockMethodInstrumenter extends MethodVisitor {
		private final MethodVisitor target;
		
		SystemClockMethodInstrumenter(MethodVisitor methodVisitor) {
			super(ASM9, null);
			target = methodVisitor;
		}
		
		@Override
		public void visitCode() {
			target.visitCode();
			Label label0 = new Label();
			target.visitLabel(label0);
			target.visitVarInsn(ALOAD, 0);
			target.visitMethodInsn(INVOKEVIRTUAL, "java/time/Clock$SystemClock", "millis", "()J", false);
			target.visitMethodInsn(INVOKESTATIC, "java/time/Instant", "ofEpochMilli", "(J)Ljava/time/Instant;", false);
			target.visitInsn(ARETURN);
			Label label1 = new Label();
			target.visitLabel(label1);
			target.visitLocalVariable("this", "Ljava/time/Clock$SystemClock;", null, label0, label1, 0);
			target.visitMaxs(2, 1);
			target.visitEnd();
		}
	}
}
