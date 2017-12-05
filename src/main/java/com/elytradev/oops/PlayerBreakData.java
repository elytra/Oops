package com.elytradev.oops;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
    private IBlockState expectedState = Blocks.AIR.getDefaultState();

    // True if passed check in break speed code.
    private boolean passedCheck = false;

    public PlayerBreakData(World world, BlockPos pos, ItemStack initialStack, IBlockState expectedState) {
        this.pos = pos;
        this.world = world;
        this.initialStack = initialStack.copy();
        this.initialStack.setCount(1);
        this.ticksRemaining = OopsConfig.recoveryTime;
        this.expectedState = expectedState;
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
        return posMatches(pos) && worldMatches(world) &&
                (passedCheck || Objects.equals(world.getBlockState(pos), expectedState));
    }

    public ItemStack getInitialStack() {
        return initialStack;
    }

    public IBlockState getExpectedState() {
        return expectedState;
    }

    public boolean isPassedCheck() {
        return passedCheck;
    }
    }

    public void setPassedCheck(boolean passedCheck) {
        this.passedCheck = passedCheck;
    }
}
