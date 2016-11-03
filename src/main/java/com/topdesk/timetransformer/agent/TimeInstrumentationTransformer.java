package com.topdesk.timetransformer.agent;

import static org.objectweb.asm.Opcodes.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class TimeInstrumentationTransformer implements ClassFileTransformer {
	private static final String TIME_TRANSFORMER_PACKAGE = "com/topdesk/timetransformer/";
	private static final String TIME_TRANSFORMER_CLASS = TIME_TRANSFORMER_PACKAGE + "TimeTransformer";
	private static final String DO_NOT_INSTRUMENT = "L" + TIME_TRANSFORMER_PACKAGE + "agent/DoNotInstrument;";
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader classReader = new ClassReader(classfileBuffer);
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassInstrumenter classVisitor = new ClassInstrumenter(classWriter);
		
		try {
			classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
		} catch (Throwable t) {
			// Try catch block, because ASM sometimes fails silently...
			t.printStackTrace();
		}
		return classWriter.toByteArray();
	}
	
	private static class ClassInstrumenter extends ClassVisitor {
		private boolean instrument = true;
		
		ClassInstrumenter(ClassVisitor classVisitor) {
			super(ASM5, classVisitor);
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
			return new MethodInstrumenter(methodVisitor);
		}
	}
	
	private static class MethodInstrumenter extends MethodVisitor {
		MethodInstrumenter(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
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
}
