package com.elytradev.oops;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    private NBTTagCompound expectedTag = new NBTTagCompound();

    // True if passed check in break speed code.
    private boolean passedCheck = false;

    public PlayerBreakData(World world, BlockPos pos, ItemStack initialStack, IBlockState expectedState) {
        this.pos = pos;
        this.world = world;
        this.initialStack = initialStack.copy();
        this.initialStack.setCount(1);
        this.ticksRemaining = OopsConfig.recoveryTime;
        this.expectedState = expectedState;
        this.expectedTag = getExpectedTag();
    }

    public void tick() {
        this.ticksRemaining--;
    }

    public boolean isKill() {
        return this.ticksRemaining <= 0;
    }

    public boolean posMatches(BlockPos otherPos) {
        return Objects.equals(pos, otherPos);
    }

    public boolean worldMatches(World otherWorld) {
        return Objects.equals(world, otherWorld);
    }

    public boolean dataMatches(World world, BlockPos pos) {
        return posMatches(pos) && worldMatches(world) &&
                (this.passedCheck || Objects.equals(world.getBlockState(pos), this.expectedState));
    }

    public boolean tagChanged() {
        return !getExpectedTag().equals(this.expectedTag);
    }

    public ItemStack getInitialStack() {
        return this.initialStack;
    }

    public IBlockState getExpectedState() {
        return this.expectedState;
    }

    public boolean isPassedCheck() {
        return this.passedCheck;
    }

    public void setPassedCheck(boolean passedCheck) {
        this.passedCheck = passedCheck;
    }

    public NBTTagCompound getExpectedTag() {
        if (world.getTileEntity(pos) != null) {
            return world.getTileEntity(pos).serializeNBT();
        } else {
            return new NBTTagCompound();
        }
    }
}
