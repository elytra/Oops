package com.elytradev.oops;

import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles events and player break data, most of the mod is in this class.
 */
public class OopsEventHandler {
    public static final HashMap<UUID, List<PlayerBreakData>> UNDO_DATA = Maps.newHashMap();
    public static Invoker getSilkTouchDrop = Invokers.findMethod(Block.class, "getSilkTouchDrop", "func_180643_i", IBlockState.class);

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent e) {
        // remove player break data on logout.
        if (UNDO_DATA.containsKey(e.player.getGameProfile().getId()))
            UNDO_DATA.remove(e.player.getGameProfile().getId());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent e) {
        // tick and remove any existing player break data
        if (e.player == null || e.player instanceof FakePlayer || e.side.isClient() || e.phase == Phase.START)
            return;

        UUID id = e.player.getGameProfile().getId();
        getBreakData(id).forEach(PlayerBreakData::tick);
        getBreakData(id).removeIf(PlayerBreakData::isKill);
    }

    @SubscribeEvent
    public void getBreakSpeed(BreakSpeed e) {
        // tweak the block breaking speed if its a position we're tracking
        if (e.getEntityPlayer() == null || e.getEntityPlayer().world.isRemote)
            return;

        // Adjusts the break speed of the block so it happens fast if it was recently placed.
        World world = e.getEntityPlayer().world;
        BlockPos pos = e.getPos();
        UUID playerID = e.getEntityPlayer().getGameProfile().getId();

        List<PlayerBreakData> breakDataList = getBreakData(playerID);
        List<PlayerBreakData> breakResults = breakDataList.stream()
                .filter(breakData -> breakData.dataMatches(world, pos))
                .collect(Collectors.toList());

        if (!breakResults.isEmpty()) {
            if (e.getState().getBlockHardness(world, pos) > 0)
                e.setNewSpeed((float) (e.getState().getBlockHardness(world, pos) * OopsConfig.breakMultiplier));
            for (PlayerBreakData breakResult : breakResults) {
                breakResult.setPassedCheck(true);
            }
        }
    }

    @SubscribeEvent
    public void onHarvestCheck(HarvestCheck e) {
        // force the harvest check to return true if the player is looking at a block that were tracking
        if (e.canHarvest() || e.getEntityPlayer().world.isRemote)
            return;

        EntityPlayer player = e.getEntityPlayer();
        RayTraceResult hit = rayTrace(player);
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            World world = player.world;
            BlockPos pos = hit.getBlockPos();

            UUID playerID = player.getGameProfile().getId();
            List<PlayerBreakData> breakDataList = UNDO_DATA.getOrDefault(playerID, Collections.emptyList());
            Optional<PlayerBreakData> foundBreakData =
                    breakDataList.stream().filter(breakData -> breakData.dataMatches(world, pos)).findFirst();
            e.setCanHarvest(foundBreakData.isPresent());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHarvestDrops(HarvestDropsEvent e) {
        // tweak any drops if needed, whether that be switching to silktouch or using a backup we stored.
        if (e.getHarvester() == null || e.getHarvester().world.isRemote)
            return;

        EntityPlayer player = e.getHarvester();
        World world = player.world;
        BlockPos pos = e.getPos();

        List<PlayerBreakData> breakDataList = getBreakData(player.getGameProfile().getId());
        Optional<PlayerBreakData> foundBreakData = breakDataList.stream()
                .filter(breakData -> breakData.dataMatches(world, pos)).findFirst();
        boolean reDrop = foundBreakData.isPresent();
        if (!reDrop) return;

        if (!OopsConfig.legacyDrop) {
            // New drop behaviour, a bit cleaner and makes a lot more sense.
            PlayerBreakData breakData = foundBreakData.get();
            ItemStack drop = breakData.getInitialStack();
            getBreakData(player.getGameProfile().getId()).remove(foundBreakData.get());

            if (drop.isEmpty()) {
                drop = (ItemStack) getSilkTouchDrop.invoke(e.getState().getBlock(), e.getState());
            }
            if (!drop.isEmpty()) {
                e.getDrops().clear();
                e.getDrops().add(drop);
            }
        } else {
            //Somewhat hectic legacy drop behaviour.
            boolean doSilkHarvest = e.getState().getBlock().canSilkHarvest(world, pos, e.getState(), player);
            if (doSilkHarvest) {
                if (e.getState().getBlock().hasTileEntity(e.getState())) {
                    e.getDrops().clear();
                    e.getDrops().add(foundBreakData.get().getInitialStack());
                } else if (!e.isSilkTouching()) {
                    ItemStack silkDrop = (ItemStack) getSilkTouchDrop.invoke(e.getState().getBlock(), e.getState());
                    e.getDrops().clear();
                    e.getDrops().add(silkDrop);
                }
                getBreakData(player.getGameProfile().getId()).remove(foundBreakData.get());
            }
        }
    }

    @SubscribeEvent
    public void onBreakEvent(BlockEvent.BreakEvent e) {
        if (e.getPlayer() == null || e.getPlayer().world.isRemote)
            return;
        EntityPlayer player = e.getPlayer();
        World world = player.world;
        BlockPos pos = e.getPos();

        List<PlayerBreakData> breakDataList = getBreakData(player.getGameProfile().getId());
        Optional<PlayerBreakData> foundBreakData = breakDataList.stream()
                .filter(breakData -> breakData.dataMatches(world, pos)).findFirst();
        if (foundBreakData.isPresent()) {
            e.setExpToDrop(0);
        }
    }

    @Nullable
    public RayTraceResult rayTrace(EntityPlayer player) {
        double reach = player.isCreative() ? 5 : 4.5;
        Vec3d eyes = player.getPositionEyes(1F);
        Vec3d look = player.getLook(1F);
        Vec3d end = eyes.addVector(look.x * reach, look.y * reach, look.z * reach);
        return player.world.rayTraceBlocks(eyes, end, false, false, true);
    }

    @SubscribeEvent
    public void onBlockPlace(PlaceEvent e) {
        trackPlacement(e.getWorld(), e.getPos(), e.getState(), e.getPlayer(), e.getHand());
    }

    private void trackPlacement(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand) {
        // Some checks whether this is a placement to be concerned about.
        if (player == null || player instanceof FakePlayer || world.isRemote || !OopsConfig.trackBlock(state))
            return;

        List<PlayerBreakData> playerBreakData = getBreakData(player.getGameProfile().getId());
        playerBreakData.add(new PlayerBreakData(world, pos, player.getHeldItem(hand), state));
    }

    @SubscribeEvent
    public void onBlockMultiPlace(MultiPlaceEvent e) {
        for (BlockSnapshot b : e.getReplacedBlockSnapshots()) {
            trackPlacement(b.getWorld(), b.getPos(), b.getCurrentBlock(), e.getPlayer(), e.getHand());
        }
    }

    /**
     * Get or create a list of break data for a player with the given id.
     *
     * @param player the players uuid from their gameprofile
     * @return a list of break data associated with the player id
     */
    private List<PlayerBreakData> getBreakData(UUID player) {
        UNDO_DATA.putIfAbsent(player, Lists.newArrayList());
        return UNDO_DATA.get(player);
    }
}
