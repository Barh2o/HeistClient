package ht.heist.mixins;

import ch.qos.logback.classic.spi.PlatformInfo;
import ht.heist.Logger;
import ht.heist.Static;
import ht.heist.injector.annotations.Inject;
import ht.heist.injector.annotations.MethodOverride;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import ht.heist.model.DeviceID;
import ht.heist.util.ReflectBuilder;

import java.util.UUID;

@Mixin("PlatformInfo")
public abstract class TDeviceIDMixin
{
    @Shadow("JX_CHARACTER_ID")
    public static String characterId;

//    @MethodOverride("getDeviceId")
//    public static String getDeviceId(PlatformInfo info, int os)
//    {
//        return process(os);
//    }

    @MethodOverride("getDeviceId")
    public String getDeviceId(int os)
    {
        return process(os);
    }

    @Inject
    private static String process(int os)
    {
        if (!Static.getVitaConfig().shouldCacheDeviceId())
        {
            return DeviceID.vanillaGetDeviceID(os);
        }

        String username = ReflectBuilder.of(Static.getClient())
                .method("getUsername", null, null)
                .get();

        String identifier = username != null && !username.isEmpty() ? username : characterId;

        String cachedDeviceId = DeviceID.getCachedUUID(identifier);
        if (cachedDeviceId == null)
        {
            cachedDeviceId = UUID.randomUUID().toString();
            DeviceID.writeCachedUUID(identifier, cachedDeviceId);
            Logger.info("Generated new deviceId (UUID): " + cachedDeviceId + " for account: " + identifier);
        }
        Logger.info("Using cached deviceId (UUID): " + cachedDeviceId + " for account: " + identifier);
        return cachedDeviceId;
    }
}
