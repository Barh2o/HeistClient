package ht.heist.rlmixins;

import ht.heist.injector.annotations.*;
import ht.heist.util.asm.BytecodeBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Mixin("net/runelite/client/ui/ClientUI")
public class ClientUIAddNavigationMixin {

    @Insert(method = "addNavigation", at = @At(value = AtTarget.INVOKE, owner = "javax/swing/JTabbedPane", target = "insertTab"), raw = true)
    public static void sanitizeInsertTab(MethodNode method, AbstractInsnNode insertionPoint) {
        // insertionPoint is the INVOKEVIRTUAL insertTab call
        // The stack before the call has: [this (JTabbedPane), title, icon, component,
        // tip, index]
        // We want to sanitize 'index' (the last argument)

        // We'll insert logic BEFORE the call to cap the index:
        // index = Math.min(index, this.getTabCount())

        InsnList list = new InsnList();

        // Stack at entry: [..., component, tip, index]
        // Duplicate index and the JTabbedPane reference (it's at stack -5 from the top)
        // This is getting complicated with pure ASM if we don't know the exact stack
        // state.

        // Simpler way: Replace the entire method or use a Redirect-like approach.
        // But let's try to just pop the index and push a safe one.

        // Actually, the easiest way to fix the IndexOutOfBoundsException in
        // JTabbedPane.insertTab
        // is to wrap the call in a try-catch OR just ensure index <= size.

        /*
         * Code to insert:
         * DUP // duplicate index
         * ALOAD 0 // load JTabbedPane (assuming it's on the stack or we can get it)
         * // Wait, 'this' in ClientUI is NOT JTabbedPane.
         * // We need to find the JTabbedPane field.
         */
    }
}
