package ht.heist.rlmixins;

import ht.heist.injector.annotations.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import ht.heist.runelite.ClientUIUpdater;

import static org.objectweb.asm.Opcodes.*;

@Mixin("net/runelite/client/ui/ClientUI")
public class ClientUIMixin {
    @Insert(method = "lambda$init$6", at = @At(value = AtTarget.INVOKE, owner = "net/runelite/client/ui/ClientUI", target = "updateFrameConfig"), ordinal = -1)
    public static void initVita() {
        ClientUIUpdater.inject();
    }

    /**
     * Patch addNavigation(...insertTab...) to cap the tab index at the current tab
     * count.
     * RuneLite assigns high priority numbers (like 10) to Config/Wrench, but if the
     * tabs pane
     * has fewer than 10 tabs at startup, insertTab throws
     * IndexOutOfBoundsException.
     *
     * Strategy: Before insertTab is called, the stack is:
     * [..., JTabbedPane, title, icon, component, tip, index]
     * We want: index = Math.min(index, tabs.getTabCount())
     * But we have the JTabbedPane buried deep. Simpler: just POP and ICONST_0.
     * This puts everything at index 0 (prepend). All tabs still appear, just in
     * reverse
     * priority order (lowest priority number = index 0 = leftmost).
     * The Wrench icon is typically priority 10 so it ends up last (rightmost) =
     * correct.
     *
     * With the old Math.min(index, 5) cap, if any plugin had priority > 5 it would
     * crash again.
     * ICONST_0 is unconditionally safe.
     */
    @Insert(method = "addNavigation", at = @At(value = AtTarget.INVOKE, owner = "javax/swing/JTabbedPane", target = "insertTab"), raw = true)
    public static void sanitizeAddNavigation(MethodNode method, AbstractInsnNode insertionPoint) {
        InsnList list = new InsnList();
        // Stack before: [..., JTabbedPane, title, icon, component, tip, index]
        list.add(new InsnNode(Opcodes.POP)); // pop the original (potentially out-of-bounds) index
        list.add(new InsnNode(Opcodes.ICONST_0)); // push 0 — always safe, inserts at front
        method.instructions.insertBefore(insertionPoint, list);
        System.out.println("[Heist] Patched addNavigation (safe index 0)");
    }
}
