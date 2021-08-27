package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.discord.databinding.WidgetChannelsListItemChannelVoiceBinding;
import com.discord.widgets.channels.list.WidgetChannelsListAdapter;
import com.discord.widgets.channels.list.items.ChannelListItem;
import com.discord.widgets.channels.list.items.ChannelListItemVoiceChannel;


// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
public class VCID extends Plugin {
    @NonNull
    @Override
    // Plugin Manifest - Required
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{new Manifest.Author("HalalKing#1551", 261634919980204033L)};
        manifest.description = "Voice Channel ID";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/terabyte25/plugins/builds/updater.json";
        return manifest;
    }

    @SuppressLint("SetTextI18n")
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws Throwable {
        var itemClass = WidgetChannelsListAdapter.ItemChannelVoice.class.getDeclaredField("binding");

        // add the patch
        patcher.patch(WidgetChannelsListAdapter.ItemChannelVoice.class.getDeclaredMethod("onConfigure", int.class, ChannelListItem.class), new PinePatchFn(callFrame -> {
            var channel = (ChannelListItemVoiceChannel) callFrame.args[1];

            itemClass.setAccessible(true);
            try {
                var binding = (WidgetChannelsListItemChannelVoiceBinding) itemClass.get(callFrame.thisObject);
                binding.a.setOnLongClickListener(view -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied to clipboard.", String.valueOf(channel.getChannel().h()));
                    int duration = Toast.LENGTH_LONG;
                    Toast.makeText(context, "Copied to clipboard.", duration).show();
                    clipboard.setPrimaryClip(clip);
                    return true;
                });

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return;
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Remove all patches
        patcher.unpatchAll();
    }
}
