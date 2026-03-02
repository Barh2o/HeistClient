package ht.heist.mixins;

import ht.heist.api.TItemComposition;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import ht.heist.util.TextUtil;
import lombok.Getter;

@Mixin("ItemComposition")
public abstract class TItemCompositionMixin implements TItemComposition
{
    @Shadow("groundActions")
    public String[] groundActions;

    public String[] getGroundActions()
    {
        String[] cleaned = new String[groundActions.length];
        for(int i = 0; i < groundActions.length; i++)
        {
            if(groundActions[i] != null)
            {
                cleaned[i] = TextUtil.sanitize(groundActions[i]);
            }
        }
        return cleaned;
    }
}
