package ht.heist.mixins;

import ht.heist.api.TClientPacket;
import ht.heist.api.TPacketBuffer;
import ht.heist.api.TPacketBufferNode;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import lombok.Getter;

@Mixin("PacketBufferNode")
@Getter
public class TPacketBufferNodeMixin implements TPacketBufferNode
{
    @Shadow("packetBuffer")
    private TPacketBuffer packetBuffer;

    @Shadow("clientPacket")
    private TClientPacket clientPacket;
}
