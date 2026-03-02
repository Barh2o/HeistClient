package ht.heist.mixins;

import ht.heist.api.TClientPacket;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import lombok.Getter;

@Mixin("ClientPacket")
@Getter
public class TClientPacketMixin implements TClientPacket {
    @Shadow("id")
    public int id;

    @Shadow("length")
    public int length;
}
