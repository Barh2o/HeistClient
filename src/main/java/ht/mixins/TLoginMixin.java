package ht.heist.mixins;

import ht.heist.api.TAccountType;
import ht.heist.api.TClient;
import ht.heist.injector.annotations.Inject;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;

@Mixin("Client")
public abstract class TLoginMixin implements TClient {
    @Shadow("setLoginIndex")
    @Override
    public abstract void setLoginIndex(int index);

    @Shadow("accountTypeCheck")
    public static int accountType;

    @Shadow("legacyType")
    public static int legacyType;

    @Shadow("jagexType")
    public static int jagexType;

    @Inject
    @Override
    public void setAccountTypeLegacy() {
        accountType = legacyType;
    }

    @Inject
    @Override
    public void setAccountTypeJagex() {
        accountType = jagexType;
    }
}
