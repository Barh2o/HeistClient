package ht.heist.api.handlers;

import ht.heist.api.entities.NpcAPI;
import ht.heist.api.widgets.DialogueAPI;
import ht.heist.data.locatables.NpcLocations;
import ht.heist.data.wrappers.NpcEx;
import ht.heist.queries.NpcQuery;
import ht.heist.util.DialogueNode;
import ht.heist.util.handler.AbstractHandlerBuilder;
import net.runelite.api.NPC;

/**
 * A builder for creating NPC interaction handlers.
 */
public class NpcBuilder extends AbstractHandlerBuilder<NpcBuilder>
{
    /**
     * Creates a new instance of NpcBuilder.
     *
     * @return A new NpcBuilder instance.
     */
    public static NpcBuilder get()
    {
        return new NpcBuilder();
    }

    /**
     * Adds an interaction with an NPC to the handler.
     *
     * @param name   name
     * @param action action
     * @return NpcBuilder instance
     */
    public NpcBuilder interact(String name, String action)
    {
        add(() -> {
            NpcEx npc = new NpcQuery().withName(name).first();
            NpcAPI.interact(npc, action);
        });
        return this;
    }

    /**
     * Walks to and interacts with an NPC at a specified location.
     *
     * @param npcLocations npcLocations
     * @param action       action
     * @return NpcBuilder instance
     */
    public NpcBuilder visit(NpcLocations npcLocations, String action)
    {
        walkTo(npcLocations.getLocation());
        interact(npcLocations.getName(), action);
        return this;
    }

    /**
     * Walks to and Talks to an NPC at a specified location.
     *
     * @param npcLocations npcLocations
     * @return NpcBuilder instance
     */
    public NpcBuilder talkTo(NpcLocations npcLocations)
    {
        visit(npcLocations, "Talk-to");
        addDelayUntil(DialogueAPI::dialoguePresent);
        return this;
    }

    /**
     * Walks to and Talks to an NPC at a specified location and processes the dialogue
     *
     * @param npcLocations npcLocations
     * @param dialogueNode dialogueNode
     * @return NpcBuilder instance
     */
    public NpcBuilder talkTo(NpcLocations npcLocations, DialogueNode dialogueNode)
    {
        talkTo(npcLocations);
        addDelayUntil(() -> !dialogueNode.processStep());
        return this;
    }

    /**
     * Walks to and Talks to an NPC at a specified location and processes the dialogue
     *
     * @param npcLocations npcLocations
     * @param chatOptions  chatOptions
     * @return NpcBuilder instance
     */
    public NpcBuilder talkTo(NpcLocations npcLocations, String... chatOptions)
    {
        DialogueNode dialogueNode = DialogueNode.get(chatOptions);
        return talkTo(npcLocations, dialogueNode);
    }

    /**
     * Walks to and Attacks an NPC at a specified location.
     *
     * @param npcLocations npcLocations
     * @return NpcBuilder instance
     */
    public NpcBuilder attack(NpcLocations npcLocations)
    {
        visit(npcLocations, "Attack");
        return this;
    }

    /**
     * Walks to and Trades with an NPC at a specified location.
     *
     * @param npcLocations npcLocations
     * @return NpcBuilder instance
     */
    public NpcBuilder trade(NpcLocations npcLocations)
    {
        visit(npcLocations, "Trade");
        return this;
    }
}
