package com.groupbankmemory;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneLiteRunner
{
    public static void main(String[] args) throws Exception
	{
        ExternalPluginManager.loadBuiltin(GroupBankMemoryPlugin.class);
        RuneLite.main(args);
    }
}
