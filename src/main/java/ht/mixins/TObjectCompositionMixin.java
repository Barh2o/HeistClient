package ht.heist.mixins;

import ht.heist.api.TBuffer;
import ht.heist.api.TObjectComposition;
import ht.heist.injector.annotations.Disable;

import ht.heist.injector.annotations.Inject;
import ht.heist.injector.annotations.Mixin;
import lombok.Getter;

@Getter
@Mixin("ObjectComposition")
public class TObjectCompositionMixin implements TObjectComposition
{
    @Inject
    private static int blockAccessFlags;

    @Disable("decodeNext")
    public static boolean decodeNext(TBuffer buffer, int opcode)
    {
        if(opcode == 69)
        {
            byte[] array = buffer.getArray();
            int offset = buffer.getOffset();
            blockAccessFlags = array[offset] & 0xFF;
        }

        return true;
    }

    @Override
    public int getBlockAccessFlags() {
        return blockAccessFlags;
    }
}
