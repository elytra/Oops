package com.elytradev.oops;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;

// Just an immutable data storage class, nothing interesting.
public class PlayerBreakData {
    private final int finalTickdown;
    private final World world;
    private final BlockPos pos;
    private final ItemStack initialStack;
    private final IBlockState expectedState;
    @Nullable private final NBTTagCompound expectedTEState;

    // True if passed check in break speed code.
    private boolean passedCheck = false;

    public PlayerBreakData(World world, BlockPos pos, ItemStack initialStack, IBlockState expectedState, int tick) {
        this.pos = pos;
        this.world = world;
        this.initialStack = initialStack.copy();
        this.initialStack.setCount(1);
        this.finalTickdown = tick + OopsConfig.recoveryTime;
        this.expectedState = expectedState;
        this.expectedTEState = dumpTEState(world, pos);
    }

    public boolean isKill(int oneTrueMasterTick) {
        return finalTickdown <= oneTrueMasterTick;
    }

    public boolean posMatches(BlockPos otherPos) {
        return Objects.equals(pos, otherPos);
    }

    public boolean worldMatches(World otherWorld) {
        return Objects.equals(world, otherWorld);
    }

    public boolean dataMatches(World world, BlockPos pos) {
        return posMatches(pos) && worldMatches(world) &&
                (passedCheck || (
                    Objects.equals(world.getBlockState(pos), expectedState)
                        &&
                    Objects.equals(dumpTEState(world, pos), expectedTEState)
                ));
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

    public void setPassedCheck(boolean passedCheck) {
        this.passedCheck = passedCheck;
    }

    @Nullable
    private static NBTTagCompound dumpTEState(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null)
            return te.serializeNBT();
        return null;
    }
}
