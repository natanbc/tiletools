package com.github.natanbc.tiletools.util;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.TileTools;
import net.minecraft.tileentity.TileEntity;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class TileEntityDecelerator {
    private static final Map<Class<? extends TileEntity>, DeceleratedInfo>
        GENERATED = new ConcurrentHashMap<>();
    private static final Map<Class<? extends TileEntity>, String>
        UNSUPPORTED = new ConcurrentHashMap<>();
    private static final DefineClassLoader LOADER = new DefineClassLoader();
    private static final AtomicInteger CLASS_NUMBER = new AtomicInteger();
    
    public static Map<Class<? extends TileEntity>, String> unsupportedClasses() {
        return Collections.unmodifiableMap(UNSUPPORTED);
    }
    
    public static TileEntity decelerate(TileEntity owner, TileEntity target, int worldTicksPerTick) {
        if(target instanceof DeceleratableTileEntity) {
            ((DeceleratableTileEntity) target).$tiletools_setFactor(worldTicksPerTick);
            return target;
        }
        if(!canDecelerate(target.getClass())) {
            return target;
        }
        try {
            TileEntity te = getInfoFor(target.getClass()).decelerate(owner, target);
            ((DeceleratableTileEntity)te).$tiletools_setFactor(worldTicksPerTick);
            return te;
        } catch(Throwable e) {
            UNSUPPORTED.put(target.getClass(), "Error creating: " + e);
            TileTools.logger().error("Unable to create helper class at {} for {} (with type {})",
                    target.getPos(), target, target.getClass(), e);
            return target;
        }
    }
    
    public static TileEntity unbox(TileEntity owner, TileEntity boxed) {
        if(boxed instanceof DeceleratableTileEntity) {
            if(owner != ((DeceleratableTileEntity) boxed).$tiletools_getOwner()) {
                throw new IllegalStateException("Attempt to unbox tile with different owner");
            }
            return getInfoFor(
                    boxed.getClass().getSuperclass().asSubclass(TileEntity.class)
            ).disable(boxed);
        }
        return boxed;
    }
    
    public static boolean isBoxed(TileEntity te) {
        return te instanceof DeceleratableTileEntity;
    }
    
    public static boolean isBoxedBy(TileEntity owner, TileEntity te) {
        return (te instanceof DeceleratableTileEntity &&
                                ((DeceleratableTileEntity) te).$tiletools_getOwner() == owner);
    }
    
    private static void abort(Class<? extends TileEntity> clazz, String cause) {
        UNSUPPORTED.put(clazz, cause);
        if(Config.LOG_CODEGEN_ABORTS.get()) {
            TileTools.logger().error("Aborted codegen for {}: {}", clazz, cause);
        }
    }
    
    private static boolean canDecelerate(Class<? extends TileEntity> clazz) {
        if(GENERATED.containsKey(clazz)) return true;
        if(UNSUPPORTED.containsKey(clazz)) return false;
        
        if(!Modifier.isPublic(clazz.getModifiers())) {
            abort(clazz, "Class is not public");
            return false;
        }
        
        if(Modifier.isFinal(clazz.getModifiers())) {
            abort(clazz, "Class is final");
            return false;
        }
        try {
            clazz.getConstructor();
        } catch(NoSuchMethodException e) {
            abort(clazz, "no public zero args constructor");
            return false;
        }
        try {
            Method m = clazz.getMethod("tick");
            if(Modifier.isFinal(m.getModifiers())) {
                abort(clazz, "tick() is final");
                return false;
            }
        } catch(ReflectiveOperationException e) {
            //shouldn't ever happen, tick is public
            throw new AssertionError(e);
        }
        
        return true;
    }
    
    private static DeceleratedInfo getInfoFor(Class<? extends TileEntity> targetClass) {
        DeceleratedInfo info = GENERATED.get(targetClass);
        if(info == null) {
            Constructor<? extends TileEntity> decelerate = generateSubtype(targetClass);
            Constructor<? extends TileEntity> original;
            try {
                original = targetClass.getConstructor();
            } catch(NoSuchMethodException e) {
                throw new AssertionError(e);
            }
            info = new DeceleratedInfo(original, decelerate);
            GENERATED.put(targetClass, info);
        }
        return info;
    }
    
    private static Constructor<? extends TileEntity> generateSubtype(Class<?> parent) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String name = "com/github/natanbc/tiletools/" + CLASS_NUMBER.incrementAndGet() +
                              "decelerated_" + parent.getCanonicalName().replace('.', '_');
        String parentName = Type.getInternalName(parent);
        String teDesc = Type.getDescriptor(TileEntity.class);
        cw.visit(V1_8, ACC_SUPER | ACC_PUBLIC, name, null, parentName, new String[] {
                Type.getInternalName(DeceleratableTileEntity.class)
        });
        cw.visitSource(".dynamic", null);
    
        {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE | ACC_FINAL, "$tiletools_owner", teDesc, null, null);
            fv.visitEnd();
        }
        {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE, "$tiletools_frozen", "Z", null, null);
            fv.visitEnd();
        }
        {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE, "$tiletools_factor", "I", null, null);
            fv.visitEnd();
        }
        {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE, "$tiletools_ticks", "I", null, null);
            fv.visitEnd();
        }
        {
            String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TileEntity.class));
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", desc, null, null);
            mv.visitParameter("owner", 0);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, parentName, "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, name, "$tiletools_owner", teDesc);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            String desc = Type.getMethodDescriptor(Type.getType(TileEntity.class));
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "$tiletools_getOwner", desc, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "$tiletools_owner", teDesc);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "$tiletools_setFrozen", "(Z)V", null, null);
            mv.visitParameter("frozen", 0);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitFieldInsn(PUTFIELD, name, "$tiletools_frozen", "Z");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "$tiletools_setFactor", "(I)V", null, null);
            mv.visitParameter("factor", 0);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitFieldInsn(PUTFIELD, name, "$tiletools_factor", "I");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "tick", "()V", null, null);
            mv.visitCode();
            Label ret = new Label();
            Label tick = new Label();
            
            //if(frozen) goto ret
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "$tiletools_frozen", "Z");
            mv.visitJumpInsn(IFNE, ret);
            
            //int tmp = ticks + 1
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "$tiletools_ticks", "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitVarInsn(ISTORE, 1);
            
            //if(tmp >= factor) goto tick
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "$tiletools_factor", "I");
            mv.visitJumpInsn(IF_ICMPGE, tick);
            
            //ticks = tmp; return;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitFieldInsn(PUTFIELD, name, "$tiletools_ticks", "I");
            mv.visitInsn(RETURN);
            
            //tick: ticks = 0; super.tick()
            mv.visitLabel(tick);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ICONST_0);
            mv.visitFieldInsn(PUTFIELD, name, "$tiletools_ticks", "I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, parentName, "tick", "()V", false);
            //ret:
            mv.visitLabel(ret);
            mv.visitInsn(RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();
        byte[] code = cw.toByteArray();
        if(Config.DUMP_CODEGEN.get()) {
            Path dumpPath = Paths.get("tiletools_dumps",
                    "decelerated_" + parent.getCanonicalName().replace('.', '_') + ".class");
            TileTools.logger().info("Dumping codegen for {} at {}", parent, dumpPath);
            try {
                Files.createDirectories(dumpPath.getParent());
                Files.write(dumpPath, code);
            } catch(IOException e) {
                TileTools.logger().error("Unable to dump generated code", e);
            }
        }
        try {
            return LOADER.define(name, code).asSubclass(TileEntity.class)
                           .getConstructor(TileEntity.class);
        } catch(NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
    
    private static class DefineClassLoader extends ClassLoader {
        protected DefineClassLoader() {
            super(DefineClassLoader.class.getClassLoader());
        }
        
        public Class<?> define(String name, byte[] code) {
            return super.defineClass(name.replace('/', '.'), code, 0, code.length);
        }
    }
}
