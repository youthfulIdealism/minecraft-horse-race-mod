/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.minecraft.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.drools.minecraft.adapter.listener.ModAgendaEventListener;
import org.drools.minecraft.adapter.listener.ModRuleRuntimeEventListener;
import org.drools.minecraft.model.Door;
import org.drools.minecraft.model.Event;
import org.drools.minecraft.model.Item;
import org.drools.minecraft.model.Location;
import org.drools.minecraft.model.Player;
import org.drools.minecraft.model.Room;
import org.drools.minecraft.model.Session;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 *
 * @author rs
 */
public class Adapter {

    private int throttle = 0;

    private KieSession kSession;
    private HashMap<String, Player> players;
    private HashMap<Integer, World> dimensions;
    private ArrayList<Room> rooms;

    private static final Adapter instance = new Adapter();

    //TODO: this has to change, if we want rules accesible from different dimensions.
    //public static World world;
    //ArrayList<DroolsPlayer> players;
    /**
     * The adapter provides a bridge from Minecraft to Drools. One is created
     * automatically on game bootup
     */
    private Adapter() {
        players = new HashMap<String, Player>();
        dimensions = new HashMap<Integer, World>();
        rooms = new ArrayList<Room>();

        bootstrapKieSession();
        constructWorld();

        kSession.insert(new Event("Setup"));
    }

    public static Adapter getInstance() {
        return instance;
    }

    private void bootstrapKieSession() {
        try {
            // load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            kSession = kContainer.newKieSession();
//            kSession.addEventListener(new ModAgendaEventListener());
//            kSession.addEventListener(new ModRuleRuntimeEventListener());
            kSession.fireAllRules();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void constructWorld() {
        if (kSession != null) {
            Room lightHouseInterior = new Room(-81, 76, 436, -88, 87, 429, "LighthouseInterior");
            rooms.add(lightHouseInterior);
            kSession.insert(lightHouseInterior);

            Door lighthouseDoor = new Door(-85, 76, 437, -84, 79, 437, "LighthouseDoor");
            lighthouseDoor.setRoom(lightHouseInterior);
            kSession.insert(lighthouseDoor);
        } else {
            throw new IllegalStateException("There is no KieSession available, the rules will not work");
        }
    }

    /**
     * Updates a particular dimension.
     *
     * @param world
     */
    private void update(World world) {
        for (EntityPlayer player : world.playerEntities) {
            Player droolsPlayer = players.get(player.getName());
            Location playerLoc = droolsPlayer.getLocation();
            playerLoc.setX(player.getPosition().getX());
            playerLoc.setY(player.getPosition().getY());
            playerLoc.setZ(player.getPosition().getZ());
            if (droolsPlayer.getInventoryDirty()) {
                rebuildInventory(player);
            }
            droolsPlayer.setInventoryDirty(false);

            droolsPlayer.getRoomsIn().clear();
            for (Room room : rooms) {
                if (playerWithinRoom(droolsPlayer, room)) {
                    droolsPlayer.getRoomsIn().add(room);
                    kSession.update(kSession.getFactHandle(room), room);
                }
            }
            kSession.update(kSession.getFactHandle(droolsPlayer), droolsPlayer);
        }
        kSession.fireAllRules();
    }

    /**
     * Execute any updates that occur when the game ticks.
     *
     * @param event
     */
    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            throttle++;
            
            if (throttle % 50 == 0) {
                System.out.println("Current Time Millis: "+ System.currentTimeMillis());
                //for simplicity's sake, this locks the adapter into only working
                //in the default dimension. Rules will not work in the nether or end.
                //We should change this at some point.
                if (event.world.provider.getDimensionId() == 0) {
                    if (!dimensions.containsKey(event.world.provider.getDimensionId())) {
                        dimensions.put(event.world.provider.getDimensionId(), event.world);
                    }
                    update(event.world);
                }
            }
        }
    }

    /**
     * Set up player session, inventory, etc.
     *
     * @param event
     */
    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (!event.world.isRemote) {
            if (event.entity instanceof EntityPlayer) {
                Player player = new Player();
                players.put(event.entity.getName(), player);
                player.setInventoryDirty(true);

                kSession.insert(new Session(player));
                kSession.insert(player);
                kSession.insert(player.getInventory());
            }
        }
    }

    /**
     * Whenever the player's inventory changes, we need to entirely rebuild the
     * model on the minecraft side. There's just too much data to track to keep
     * a synchronised model.
     *
     * @param entity
     */
    private void rebuildInventory(EntityPlayer entity) {
        Player player = players.get(entity.getName());
        player.getInventory().clear();
        for (int i = 0; i < entity.inventory.mainInventory.length; i++) {
            ItemStack stack = entity.inventory.mainInventory[i];
            if (stack != null) {
                player.getInventory().add(new Item(stack.getUnlocalizedName(), stack.stackSize));
            }
        }
        for (int i = 0; i < entity.inventory.armorInventory.length; i++) {
            ItemStack stack = entity.inventory.armorInventory[i];
            if (stack != null) {
                player.getInventory().add(new Item(stack.getUnlocalizedName(), stack.stackSize));
            }
        }
        kSession.update(kSession.getFactHandle(player.getInventory()), player.getInventory());
    }

    /**
     * Find out if an inventory needs rebuilding.
     *
     * @param event
     */
    @SubscribeEvent
    public void addInventoryItem(EntityItemPickupEvent event) {
        if (!event.entityLiving.worldObj.isRemote) {
            if (event.entityPlayer != null) {
                Player player = players.get(event.entityPlayer.getName());
                player.setInventoryDirty(true);
            }
        }
    }

    /**
     * Find out if an inventory needs rebuilding.
     *
     * @param event
     */
    @SubscribeEvent
    public void dropInventoryItem(ItemTossEvent event) {
        if (!event.entity.worldObj.isRemote) {
            if (event.player != null) {
                Player player = players.get(event.player.getName());
                player.setInventoryDirty(true);
            }
        }
    }

    public boolean playerWithinRoom(Player player, Room room) {
        Location playerLoc = player.getLocation();
        Location roomLowerLoc = room.getLowerBound();
        Location roomUpperLoc = room.getUpperBound();
        boolean xWithin = within(playerLoc.getX(), roomLowerLoc.getX(), roomUpperLoc.getX());
        boolean yWithin = within(playerLoc.getY(), roomLowerLoc.getY(), roomUpperLoc.getY());
        boolean zWithin = within(playerLoc.getZ(), roomLowerLoc.getZ(), roomUpperLoc.getZ());
        return xWithin && yWithin && zWithin;
    }

    public boolean within(int number, int first, int second) {
        int min = Math.min(first, second);
        int max = Math.max(first, second);
        return number >= min && number <= max;
    }

    public HashMap<Integer, World> getDimensions() {
        return dimensions;
    }

    public void setDimensions(HashMap<Integer, World> dimensions) {
        this.dimensions = dimensions;
    }

}
