package com.elytradev.oops;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

// Just a data storage class, nothing interesting.
public class PlayerBreakData {
    private int ticksRemaining;
    private World world;
    private BlockPos pos;
    private ItemStack initialStack = ItemStack.EMPTY;
    private boolean canSilkHarvest = true;

    public PlayerBreakData(World world, BlockPos pos, ItemStack initialStack) {
        this.pos = pos;
        this.world = world;
        this.initialStack = initialStack.copy();
        this.ticksRemaining = OopsConfig.recoveryTime;
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

    public ItemStack getInitialStack() {
        return initialStack;
    }
}
