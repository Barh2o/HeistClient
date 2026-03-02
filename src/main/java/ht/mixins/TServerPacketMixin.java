package ht.heist.mixins;

import ht.heist.api.TServerPacket;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import lombok.Getter;

@Mixin("ServerPacket")
@Getter
public class TServerPacketMixin implements TServerPacket
{
    @Shadow("id")
    public int id;

    @Shadow("length")
    public int length;
}
