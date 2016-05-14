package org.drools.minecraft.util;

import java.util.ArrayList;
import org.drools.minecraft.model.Player;
import net.minecraft.block.Block;

import net.minecraft.block.state.BlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.drools.minecraft.adapter.Adapter;
import org.drools.minecraft.model.Door;
import org.drools.minecraft.model.Item;
import org.drools.minecraft.model.Location;
import org.drools.minecraft.model.Room;

/**
 *
 * Stub class. Used by drools to request actions from minecraft.
 *
 * @author Samuel
 *
 */
public class DroolsCommandInterface
{

    //I'd like to protect the user from minecraft entirely. This may be the way,
    //but will require a crapload of bookkeeping, which will not be done until I
    //run out of more important tasks.
    public static enum Blocks
    {
        Air(net.minecraft.init.Blocks.air),
        Planks(net.minecraft.init.Blocks.planks);

        private final Block block; // in meters

        Blocks(Block block)
        {
            this.block = block;
        }
    }

    //TODO: complete list for rule-writer reference
    public static final int potionSpeed = Potion.moveSpeed.id;
    public static final int potionSlow = Potion.moveSlowdown.id;
    public static final int potionRegen = Potion.regeneration.id;
    public static final int potionSaturation = Potion.saturation.id;

    //TODO: make a complete block list for rule-writer reference, so that they don't have to go through minecraft's blocks.
    /**
     * Stub function.
     *
     * @param player
     * @param x
     * @param y
     * @param z
     */
    public static void movePlayer(Player player, int x, int y, int z)
    {

    }
    //TODO: NOT IMPLEMENTED!

    public static void sendChat(String chat)
    {
        //TODO: NOT IMPLEMENTED
        /*if(RulesDriver.world.playerEntities.size() > 0)
		{
			RulesDriver.world.playerEntities.get(0).addChatMessage(new ChatComponentText(chat));
		}*/
    }

    public static void spawnEntity(String entityid, int x, int y, int z)
    {
        //TODO: NOT IMPLEMENTED
        /*Entity entity = EntityList.createEntityByName(entityid, RulesDriver.world);
		entity.setLocationAndAngles(x, y, z,0.0F, 0.0F);
		RulesDriver.world.spawnEntityInWorld(entity);*/
    }

    public static void enchantEntity(EntityLiving entity, int potion, int duration, int strength)
    {
        //TODO: NOT IMPLEMENTED
        //entity.addPotionEffect(new PotionEffect(potion, duration, strength));
    }

    public static void enchantEntity(EntityPlayer entity, int potion, int duration, int strength)
    {
        //TODO: NOT IMPLEMENTED
        //entity.addPotionEffect(new PotionEffect(potion, duration, strength));
    }

    public static boolean blockMatches(int x, int y, int z, BlockState match)
    {
        //TODO: NOT IMPLEMENTED
        return false;
        //IBlockState state = RulesDriver.world.getBlockState(new BlockPos(x, y, z));
        //System.out.println(state);
        //return match.getBlock().isAssociatedBlock(state.getBlock());
        //entity.addPotionEffect(new PotionEffect(potion, duration, strength));
    }

    public static void modifyBlocks(int x, int y, int z, int fx, int fy, int fz, int dimension, Blocks match)
    {
        for (int dx = Math.min(x, fx); dx <= Math.max(x, fx); dx++)
        {
            for (int dz = Math.min(z, fz); dz <= Math.max(z, fz); dz++)
            {
                for (int dy = Math.min(y, fy); dy <= Math.max(y, fy); dy++)
                {
                    Adapter.getInstance().getDimensions().get(dimension).setBlockState(new BlockPos(dx, dy, dz), match.block.getDefaultState());
                }
            }
        }
    }

    public static void modifyBlocks(Location firstBound, Location secondBound, int dimension, Blocks match)
    {
        modifyBlocks(firstBound.getX(), firstBound.getY(), firstBound.getZ(), secondBound.getX(), secondBound.getY(), secondBound.getZ(), dimension, match);
    }

    public static void openDoor(Door door)
    {
        modifyBlocks(door.getLowerBound(), door.getUpperBound(), door.getRoom().getDimension(), Blocks.Air);
    }

    public static void closeDoor(Door door)
    {
        modifyBlocks(door.getLowerBound(), door.getUpperBound(), door.getRoom().getDimension(), Blocks.Planks);
    }

    /**
     * Adds a door to a room.
     *
     * @param room
     * @param door
     */
    public static void addDoorToRoom(Room room, Door door)
    {
        room.getDoors().add(door);
        door.setRoom(room);
    }

    /**
     * Creates a door and adds it to a room. Coordinates are relative to the
     * room. So, if the room is stationed at 5, 5, 7, and you call with xyz
     * params 2 2 2 to 4 8 4, a door will be created from 7 7 9 to 9 13 11.
     *
     * @param room
     * @param x
     * @param y
     * @param z
     * @param fx
     * @param fy
     * @param fz
     * @param id
     * @return
     */
    public static Door addDoorToRoom(Room room, int x, int y, int z, int fx, int fy, int fz, String id)
    {
        int xR = Math.min(x, fx);
        int yR = Math.min(y, fy);
        int zR = Math.min(z, fz);
        int fxR = Math.max(x, fx);
        int fyR = Math.max(y, fy);
        int fzR = Math.max(z, fz);
        Location roomLoc = room.getLowerBound();
        Door door = new Door(xR + roomLoc.getX(), yR + roomLoc.getY(), zR + roomLoc.getZ(), fxR + roomLoc.getX(), fyR + roomLoc.getY(), fzR + roomLoc.getZ(), id);
        room.getDoors().add(door);
        door.setRoom(room);
        return door;
    }

    /**
     * places a chest within the world using absolute coordinates, but does not
     * associate it with a room.
     *
     * @param x
     * @param y
     * @param z
     * @param world
     * @param contents
     */
    public static void placeChest(int x, int y, int z, int dimension, ArrayList<Item> contents)
    {
        BlockPos placeLocation = new BlockPos(x, y, z);

        Adapter.getInstance().getDimensions().get(dimension).setBlockState(placeLocation, net.minecraft.init.Blocks.chest.getDefaultState(), 2);
        TileEntity chestEntity = Adapter.getInstance().getDimensions().get(dimension).getTileEntity(placeLocation);

        if (chestEntity instanceof TileEntityChest)
        {
            for (int i = 0; i < contents.size() && i < ((TileEntityChest) chestEntity).getSizeInventory(); i++)
            {
                ItemStack stack = droolsItemToItemStack(contents.get(i));
                if(stack != null)
                {
                    ((TileEntityChest) chestEntity).setInventorySlotContents(i, stack);
                }
            }
        }
    }
    
    /**
     * places a chest within a room, relative to the room coordinates
     *
     * @param x
     * @param y
     * @param z
     * @param world
     * @param contents
     */
    public static void placeChest(int x, int y, int z, Room room, ArrayList<Item> contents)
    {
        Location roomLocation = room.getLowerBound();
        BlockPos placeLocation = new BlockPos(roomLocation.getX() + x, roomLocation.getY() + y, roomLocation.getZ() + z);
        room.getChests().add(new Location(placeLocation.getX(), placeLocation.getY(), placeLocation.getZ()));

        Adapter.getInstance().getDimensions().get(room.getDimension()).setBlockState(placeLocation, net.minecraft.init.Blocks.chest.getDefaultState(), 2);
        TileEntity chestEntity = Adapter.getInstance().getDimensions().get(room.getDimension()).getTileEntity(placeLocation);

        if (chestEntity instanceof TileEntityChest)
        {
            for (int i = 0; i < contents.size() && i < ((TileEntityChest) chestEntity).getSizeInventory(); i++)
            {
                ItemStack stack = droolsItemToItemStack(contents.get(i));
                if(stack != null)
                {
                    ((TileEntityChest) chestEntity).setInventorySlotContents(i, stack);
                }
            }
        }
    }

    public static ItemStack droolsItemToItemStack(Item item)
    {
        net.minecraft.item.Item stackItem = GameRegistry.findItem("minecraft", item.getType());
        if(stackItem == null)
        {
            stackItem = GameRegistry.findItem("examplemod", item.getType());
        }
        if(stackItem == null)
        {
            System.err.println("The item " + item.getType() + " could not be found. Try whacking the box a couple times--that usually helps.");
            return null;
        }
        
        
        ItemStack returnable = new ItemStack(stackItem, item.getCount(), item.getDurability());
        if(item.getName() != null)
        {
            returnable.setStackDisplayName(item.getName());
        }
        return returnable;
    }

}
