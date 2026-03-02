package ht.heist.model;

import ht.heist.services.ConfigManager;
import java.util.Base64;

public class RandomDat {
    private static final ConfigManager cachedDataProperties = new ConfigManager("CachedRandomDat");

    public static synchronized byte[] getCachedRandomDatData(String username)
    {
        byte[] data = null;
        String property = cachedDataProperties.getString(username);
        if (property != null)
        {
            data = Base64.getDecoder().decode(property);
        }
        return data;
    }

    public static void writeCachedRandomDatData(String username, byte[] data)
    {
        cachedDataProperties.setProperty(username, Base64.getEncoder().encodeToString(data));
    }
}
