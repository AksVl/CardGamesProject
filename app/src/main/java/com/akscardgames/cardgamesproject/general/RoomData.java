package com.akscardgames.cardgamesproject.general;

public class RoomData {
    private final String name;
    private final int size;
    private final int playerCount;
    private final String access;

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public String getAccess() {
        return access;
    }

    public RoomData(String roomName, int size, int playerCount, String mode) {
        this.name = roomName;
        this.size = size;
        this.playerCount = playerCount;
        this.access = mode;
    }
}
