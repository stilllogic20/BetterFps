package guichaguri.betterfps.transformers.patcher;

import guichaguri.betterfps.ASMUtils;
import guichaguri.betterfps.BetterFpsHelper;
import guichaguri.betterfps.transformers.patcher.annotations.Copy;
import guichaguri.betterfps.transformers.patcher.annotations.Patcher;
import guichaguri.betterfps.transformers.patcher.annotations.Reference;
import java.util.HashMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Guilherme Chaguri
 */
public class Patch {

    private final ClassNode sourceClass;
    private final ClassNode targetClass;
    private final HashMap<String, MethodNode> refMethods = new HashMap<String, MethodNode>();
    private final HashMap<String, FieldNode> refFields = new HashMap<String, FieldNode>();

    Patch(ClassNode source, ClassNode target) {
        this.sourceClass = source;
        this.targetClass = target;
    }

    public ClassNode getSourceClass() {
        return sourceClass;
    }

    public ClassNode getTargetClass() {
        return targetClass;
    }

    public MethodNode getMethod(String refName) {
        return refMethods.get(refName);
    }

    public FieldNode getField(String refName) {
        return refFields.get(refName);
    }

    /**
     * Copies fields and methods with the {@link Copy} annotation and interfaces to target class
     */
    public void copy() {
        // Copy Methods
        for(MethodNode method : sourceClass.methods) {
            AnnotationNode copy = ASMUtils.getAnnotation(method.invisibleAnnotations, Copy.class);

            if(copy != null) {
                boolean replace = ASMUtils.getAnnotationValue(copy, "replace", boolean.class, false);
                ASMUtils.copyMethod(sourceClass, targetClass, method, replace);
            }
        }

        // Copy Fields
        for(FieldNode field : sourceClass.fields) {
            AnnotationNode copy = ASMUtils.getAnnotation(field.invisibleAnnotations, Copy.class);

            if(copy != null) {
                boolean replace = ASMUtils.getAnnotationValue(copy, "replace", boolean.class, false);
                ASMUtils.copyField(sourceClass, targetClass, field, replace);
            }
        }

        // Copy Interfaces
        for(String i : sourceClass.interfaces) {
            if(!targetClass.interfaces.contains(i)) {
                targetClass.interfaces.add(i);
            }
        }
    }

    /**
     * Patches methods using a custom {@link IClassPatcher}
     */
    public void patch() {
        AnnotationNode patcher = ASMUtils.getAnnotation(sourceClass.invisibleAnnotations, Patcher.class);

        if(patcher != null) {
            Type patcherClass = ASMUtils.getAnnotationValue(patcher, "value", Type.class);

            try {
                Class c = Class.forName(patcherClass.getClassName());
                IClassPatcher cp = (IClassPatcher)c.newInstance();

                // Find the references for the custom patcher
                findReferences();
                // Patch it!
                cp.patch(this);

            } catch(Exception ex) {
                BetterFpsHelper.LOG.error("Couldn't patch class {} with {}", targetClass.name, sourceClass.name);
                BetterFpsHelper.LOG.catching(ex);
            }
        }
    }

    /**
     * Finds methods and fields using the {@link Reference} annotation
     */
    private void findReferences() {
        // Find method references
        for(MethodNode method : sourceClass.methods) {
            AnnotationNode reference = ASMUtils.getAnnotation(method.invisibleAnnotations, Reference.class);

            if(reference != null) refMethods.put(ASMUtils.getAnnotationValue(reference, "value"), method);
        }

        // Find field references
        for(FieldNode field : sourceClass.fields) {
            AnnotationNode reference = ASMUtils.getAnnotation(field.invisibleAnnotations, Reference.class);

            if(reference != null) refFields.put(ASMUtils.getAnnotationValue(reference, "value"), field);
        }
    }

}