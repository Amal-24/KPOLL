package com.kpollman.team3;

public class Session {
    public static String role;
    public static int userId;
    public static int boothId;
    public static int constituencyId;

    public static void login(int id, String r) {
        userId = id;
        role = r;
    }
}