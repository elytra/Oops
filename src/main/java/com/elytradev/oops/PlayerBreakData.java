package com.elytradev.oops;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;

// Just a data storage class, nothing interesting.
public class PlayerBreakData {
    private int ticksRemaining;
    private World world;
    private BlockPos pos;
    private ItemStack initialStack = null;
    private boolean canSilkHarvest = true;

    public PlayerBreakData(World world, BlockPos pos, ItemStack initialStack) {
        this.pos = pos;
        this.world = world;
        this.initialStack = initialStack != null ? initialStack.copy() : null;
        this.ticksRemaining = OopsConfig.recoveryTime;

        if (this.initialStack != null) {
            this.initialStack.stackSize = 1;
        }
    }

    public boolean doSilkHarvest() {
        return canSilkHarvest;
    }

    public void setCanSilkHarvest(boolean canSilkHarvest) {
        this.canSilkHarvest = canSilkHarvest;
    }

    public void tick() {
        ticksRemaining--;
    }

    public boolean isKill() {
        return ticksRemaining <= 0;
    }

    public boolean posMatches(BlockPos otherPos) {
        return Objects.equals(pos, otherPos);
    }

    public boolean worldMatches(World otherWorld) {
        return Objects.equals(world, otherWorld);
    }

    public boolean dataMatches(World world, BlockPos pos) {
        return posMatches(pos) && worldMatches(world);
    }

    @Nullable
    public ItemStack getInitialStack() {
        return initialStack;
    }
}
