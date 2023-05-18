package ru.feytox.feyfriends.client;

public enum NotificationType {
    OFF("OFF"),
    ON_JOIN("ON_JOIN"),
    ON_LEAVE("ON_LEAVE"),
    BOTH("BOTH");

    private final String notifName;
    NotificationType(String notifName) {
        this.notifName = notifName;
    }

    public String getNotifName() { return notifName; }
    }
