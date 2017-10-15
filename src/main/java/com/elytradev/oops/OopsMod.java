package com.elytradev.oops;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.elytradev.oops.OopsMod.*;

// Fairly generic mod container, registers the event handler and config stuff.
@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION, guiFactory = GUI_FACTORY)
public class OopsMod {
    public static final String MOD_ID = "oops";
    public static final String MOD_NAME = "Oops";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String GUI_FACTORY = "com.elytradev.oops.OopsGuiFactory";

    @Mod.EventHandler
    public void onFMLPreInitialization(FMLPreInitializationEvent event) {
        // Create the config file then load it.
        OopsConfig oopsConfig = new OopsConfig(event.getSuggestedConfigurationFile());
        oopsConfig.loadConfig();
        // Register the event handler.
        MinecraftForge.EVENT_BUS.register(new OopsEventHandler());
    }
}
