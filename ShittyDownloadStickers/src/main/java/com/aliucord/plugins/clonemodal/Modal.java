/*
 * Ven's Aliucord Plugins
 * Copyright (C) 2021 Vendicated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
*/

package com.aliucord.plugins.clonemodal;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliucord.CollectionUtils;
import com.aliucord.Http;
import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.plugins.ShittyDownloadStickers;
import com.discord.api.permission.Permission;
import com.discord.models.guild.Guild;
import com.discord.stores.StoreGuilds;
import com.discord.stores.StoreStream;
import com.discord.utilities.permissions.PermissionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Modal extends SettingsPage {
    private static final Map<Integer, Integer> stickerLimits = new HashMap<>();
    static {
        stickerLimits.put(0, 0);
        stickerLimits.put(1, 15);
        stickerLimits.put(2, 30);
        stickerLimits.put(3, 60);
    }

    private final Map<Long, Long> guildPerms = StoreStream.getPermissions().getGuildPermissions();
    private final StoreGuilds guildStore = StoreStream.getGuilds();

    private final String name;
    private final String url;
    private final long id;
    private final String description;
    private final String tags;

    public Modal(String url, String name, long id, String description, String tags) {
        this.url = url;
        this.name = name;
        this.id = id;
        this.description = description;
        this.tags = tags;
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Clone Sticker");
        setActionBarSubtitle(name);

        var ctx = view.getContext();

        setPadding(0);

        var recycler = new RecyclerView(ctx);
        recycler.setLayoutManager(new LinearLayoutManager(ctx, RecyclerView.VERTICAL, false));
        var adapter = new Adapter(this, CollectionUtils.filter(guildStore.getGuilds().values(), this::isCandidate));
        recycler.setAdapter(adapter);

        addView(recycler);
    }

    private boolean isCandidate(Guild guild) {
        var perms = guildPerms.get(guild.getId());
        if (!PermissionUtils.can(Permission.MANAGE_EMOJIS_AND_STICKERS, perms)) return false;
        int usedSlots = 0;
        for (var sticker : guild.getStickers()) {
            if (sticker.getId() == id) return false;

            usedSlots++;
        }
        var slots = stickerLimits.get(guild.getPremiumTier());
        return slots != null && usedSlots < slots;
    }

    public void clone(Context ctx, Guild guild) {
        Utils.threadPool.execute(() -> {

            try {
                var request = Http.Request.newDiscordRequest("/guilds/"+guild.getId()+"/stickers", "POST");
                var tempFile = File.createTempFile("sticker", ".png");
                Http.simpleDownload(url, tempFile);

                request.executeWithMultipartForm(Map.of("file", tempFile, "name", name, "description", description != null ? description : "", "tags", tags));
            } catch (Exception e) {
                ShittyDownloadStickers.log.errorToast(e);
            }

            /**
            var api = RestAPI.getApi();
            var uri = imageToDataUri();
            if (uri == null) {
                //EmojiUtility.logger.error(ctx, "Something went wrong while preparing the image");
                return;
            }
            var obs = api.postGuildEmoji(guild.getId(), new RestAPIParams.PostGuildEmoji(name, uri));
            var res = RxUtils.getResultBlocking(obs);
            /**if (res.second == null)
                EmojiUtility.logger.info(ctx, String.format("Successfully cloned %s to %s", name, guild.getName()));
            else
                EmojiUtility.logger.error(ctx, "Something went wrong while cloning this emoji", res.second);*/


        });
    }
}
