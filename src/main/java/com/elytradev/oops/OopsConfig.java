package com.elytradev.oops;

import com.elytradev.concrete.config.ConcreteConfig;
import com.elytradev.concrete.config.ConfigValue;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.List;

// basic concrete config implementation
public class OopsConfig extends ConcreteConfig {
    public static ConcreteConfig config;

    @ConfigValue(type = Property.Type.INTEGER, comment = "The amount of ticks you have to break an accidental placement.")
    public static int recoveryTime = 60;
    @ConfigValue(type = Property.Type.DOUBLE, comment = "The speed of the block breaking, multiplies block hardness by this number.")
    public static double breakMultiplier = 30;
    @ConfigValue(type = Property.Type.BOOLEAN, comment = "If false blocks with tile entities will be ignored.")
    public static boolean dropTileEntities = true;
    @ConfigValue(type = Property.Type.BOOLEAN, comment = "Enable for legacy block drop behaviour.")
    public static boolean legacyDrop = false;
    @ConfigValue(type = Property.Type.STRING, comment = "A list of blocks and resource domains to exclude.")
    private static String[] blacklist = new String[]{"modid", "othermodid:modblock"};

    private static List<String> blacklistedDomains;
    private static List<ResourceLocation> blacklistedBlocks;

    protected OopsConfig(File configFile) {
        super(configFile, OopsMod.MOD_ID);
        config = this;
    }

    @Override
    public void loadConfig() {
        super.loadConfig();

        this.blacklistedDomains = Lists.newArrayList();
        this.blacklistedBlocks = Lists.newArrayList();

        for (String blacklistEntry : blacklist) {
            if (!blacklistEntry.contains(":")) {
                this.blacklistedDomains.add(blacklistEntry);
            } else {
                this.blacklistedBlocks.add(new ResourceLocation(blacklistEntry));
            }
        }
    }

    /**
     * Check if a blockstate should be tracked for instant removal.
     *
     * @param state the state to check
     * @return true if the block should be tracked, false otherwise.
     */
    public static boolean trackBlock(IBlockState state) {
        ResourceLocation blockName = state.getBlock().getRegistryName();
        boolean skip = !dropTileEntities && state.getBlock().hasTileEntity(state);
        skip = skip || blacklistedDomains.contains(blockName.getResourceDomain());
        skip = skip || blacklistedBlocks.contains(blockName);
        return !skip;
    }
}
