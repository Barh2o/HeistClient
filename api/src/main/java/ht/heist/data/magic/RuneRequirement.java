package ht.heist.data.magic;

import lombok.Value;

@Value
public class RuneRequirement
{
    int quantity;
    Rune rune;

    public boolean meetsRequirements()
    {
        return rune.getQuantity() >= quantity;
    }
}
