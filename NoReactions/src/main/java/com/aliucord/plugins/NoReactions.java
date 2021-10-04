package com.aliucord.plugins;

import java.util.Collection;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PineInsteadFn;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class NoReactions extends Plugin {
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {
        var className = "com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemReactions";
        var methodName = "displayReactions";
        var methodArguments = new Class<?>[] { Collection.class, long.class, boolean.class, boolean.class, boolean.class };

        // add the patch
        patcher.patch(className, methodName, methodArguments, new PineInsteadFn(callFrame -> {
            return null;
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
