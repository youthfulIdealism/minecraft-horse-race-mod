/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.minecraft.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Samuel
 */
public class Room {

    private String id;

    private Location lowerBound;
    private Location upperBound;

    private int dimension;

    private List<Door> doors;

    private List<WorldItem> items;
    
    private List<String> playersInRoom  = new ArrayList<>();
    private HashMap<String, Mob> mobsInRoom;

    private Room(){
        this.dimension = 0;
        doors = new ArrayList<>();
        items = new ArrayList<>();
        mobsInRoom = new HashMap<String, Mob>();
    }
    
    public Room(String id) {
        this();
        this.id = id;
    }

    public Room(int x, int y, int z, int fx, int fy, int fz, String id) {
        this(id);
        lowerBound = new Location(Math.min(x, fx), Math.min(y, fy), Math.min(z, fz));
        upperBound = new Location(Math.max(x, fx), Math.max(y, fy), Math.max(z, fz));
    }

    public Location getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Location lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Location getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Location upperBound) {
        this.upperBound = upperBound;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public void setDoors(List<Door> doors) {
        this.doors = doors;
    }

    public void addDoor(Door door) {
        if (this.doors == null) {
            this.doors = new ArrayList<>();
        }
        this.doors.add(door);
    }


    public void addItem(WorldItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public List<WorldItem> getItems() {
        return items;
    }

    public List<String> getPlayersInRoom() {
        return playersInRoom;
    }

    public void setPlayersInRoom(List<String> playersInRoom) {
        this.playersInRoom = playersInRoom;
    }
    
    public void addPlayer(String player){
        this.playersInRoom.add(player);
    }
    
    public void removePlayer(String player){
        this.playersInRoom.remove(player);
    }

    public HashMap<String, Mob> getMobsInRoom()
    {
        return mobsInRoom;
    }

    public void setMobsInRoom(HashMap<String, Mob> mobsInRoom)
    {
        this.mobsInRoom = mobsInRoom;
    }
    
    @Override
    public String toString() {
        return "Room{" + "id=" + id + ", lowerBound=" + lowerBound + ", upperBound=" + upperBound + ", dimension=" + dimension + ", doors=" + doors + ", items=" + items + ", playersInRoom=" + playersInRoom + '}';
    }
    
    
   
    

}
