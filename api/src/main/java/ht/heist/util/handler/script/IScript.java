package ht.heist.util.handler.script;

import ht.heist.Static;
import ht.heist.api.threaded.Delays;
import ht.heist.util.handler.StepHandler;
import net.runelite.api.Client;

/**
 * Interface for writing scripts that can be executed in the client.
 *
 * subroutines defined as non-static methods that take a ScriptBuilder parameter and return void.
 */
public interface IScript
{
    void main(ScriptBuilder script);

    static void execute(Class<? extends IScript> script)
    {
        Client client = Static.getClient();
        if(client.isClientThread())
        {
            throw new IllegalStateException("Cannot execute script on client thread");
        }
        StepHandler handler = build(script);
        while(handler.step())
        {
            Delays.tick();
        }
    }

    static StepHandler build(Class<? extends IScript> script)
    {
        return Script.build(script);
    }
}
