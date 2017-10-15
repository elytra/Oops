package com.elytradev.oops;

import com.elytradev.concrete.config.ConcreteConfig;
import com.elytradev.concrete.config.ConfigValue;
import net.minecraftforge.common.config.Property;

import java.io.File;

// basic concrete config implementation
public class OopsConfig extends ConcreteConfig {
    public static ConcreteConfig config;
    @ConfigValue(type = Property.Type.INTEGER, comment = "The amount of ticks you have to break an accidental placement.")
    public static int recoveryTime = 60;
    @ConfigValue(type = Property.Type.DOUBLE, comment = "The speed of the block breaking, 150 is instant, tweak as needed.")
    public static double breakSpeed = 150F;
    @ConfigValue(type = Property.Type.BOOLEAN, comment = "If false blocks with tile entities will be ignored.")
    public static boolean dropTileEntities = true;

    protected OopsConfig(File configFile) {
        super(configFile, OopsMod.MOD_ID);
        config = this;
    }
}
