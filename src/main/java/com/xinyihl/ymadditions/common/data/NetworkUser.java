package com.xinyihl.ymadditions.common.data;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class NetworkUser {

    private Perm perm;

    private UUID uuid;
    private String name;

    public NetworkUser(Perm perm, GameProfile profile) {
        this.perm = perm;
        this.uuid = profile.getId();
        this.name = profile.getName();
    }

    public static enum Perm {
        ROOT,
        ADMIN,
        USER
    }
}
