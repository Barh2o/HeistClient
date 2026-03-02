package ht.heist.data.wrappers.abstractions;

import ht.heist.queries.combined.EntityQuery;

public interface Entity extends Locatable, Interactable, Identifiable
{
    static EntityQuery search()
    {
        return new EntityQuery();
    }
}
