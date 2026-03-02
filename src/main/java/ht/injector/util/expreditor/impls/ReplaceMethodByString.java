package ht.heist.injector.util.expreditor.impls;

import ht.heist.util.asm.BytecodeBuilder;
import ht.heist.injector.util.expreditor.ExprEditor;
import ht.heist.injector.util.expreditor.LiteralValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ReplaceMethodByString extends ExprEditor
{
    private final String target;
    public ReplaceMethodByString(String target)
    {
        this.target = target;
    }
    @Override
    public void edit(LiteralValue literal) {
        if (literal.isString() && literal.getStringValue().equals(target)) {
            MethodNode method = literal.getMethod();
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables.clear();
            method.exceptions.clear();

            InsnList insns = BytecodeBuilder.create()
                    .pushThis()
                    .pushInt(0)
                    .appendInsn(new InsnNode(Opcodes.IRETURN))
                    .build();
            method.instructions.add(insns);
            method.maxStack = 1;
            method.maxLocals = 1;
        }
    }
}
