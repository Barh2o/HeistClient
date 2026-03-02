package ht.heist.mixins;

import ht.heist.Static;
import ht.heist.api.TClient;
import ht.heist.api.TPacketBuffer;
import ht.heist.api.TPacketWriter;
import ht.heist.api.TServerPacket;
import ht.heist.events.PacketReceived;
import ht.heist.injector.annotations.*;
import ht.heist.util.asm.BytecodeBuilder;
import ht.heist.util.MappingProvider;
import ht.heist.model.ui.HeistClientOptionsPanel;
import ht.heist.util.dto.JClass;
import ht.heist.util.dto.JField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
@Mixin("Client")
public class TServerPacketLoggerMixin
{
    @Insert(
            method = "processServerPacket",
            at = @At(value = AtTarget.RETURN),
            raw = true
    )
    public static void process(MethodNode method, AbstractInsnNode insertionPoint)
    {
        JClass clazz = MappingProvider.getClass("PacketWriter");
        if(clazz == null)
            return;
        JField field = MappingProvider.getField(clazz, "serverPacket");

        AbstractInsnNode target = null;
        for(AbstractInsnNode node : method.instructions)
        {
            if(node.getOpcode() != Opcodes.GETFIELD)
                continue;

            FieldInsnNode fin = (FieldInsnNode) node;
            if(!fin.name.equals(field.getObfuscatedName()) || !fin.owner.equals(clazz.getObfuscatedName()))
                continue;

            if(fin.getNext().getOpcode() != Opcodes.GETSTATIC)
                continue;

            if(fin.getNext().getNext().getOpcode() != Opcodes.IF_ACMPNE)
                continue;

            target = fin;
            break;
        }

        if(target == null)
            return;

        InsnList code = BytecodeBuilder.create()
                .invokeStatic(
                        "client",
                        "process",
                        "()V"
                ).build();
        method.instructions.insertBefore(target, code);
    }

    @Inject
    public static void process()
    {
        TClient client = Static.getClient();
        TPacketWriter writer = client.getPacketWriter();
        TServerPacket packet = writer.getServerPacket();
        TPacketBuffer buffer = writer.getServerPacketBuffer();
        byte[] bytes = new byte[writer.getServerPacketLength()];
        System.arraycopy(buffer.getArray(), 0, bytes, 0, writer.getServerPacketLength());
        int id = packet.getId();
        int length = writer.getServerPacketLength();
        PacketReceived packetReceived = PacketReceived.of(id, length, bytes);
        Static.post(packetReceived);
        HeistClientOptionsPanel.getInstance().onPacketReceived(packetReceived);
    }
}
