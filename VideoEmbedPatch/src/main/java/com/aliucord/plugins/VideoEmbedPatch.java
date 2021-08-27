package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.utilities.intent.IntentUtils;

import java.util.regex.Pattern;


// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
public class VideoEmbedPatch extends Plugin {
    private final Pattern mediaRegex = Pattern.compile("(https:\\/\\/)media(\\.discordapp\\.)net(\\/attachments\\/\\d+\\/\\d+\\/.+(\\.mov|\\.mp4|\\.webm))");

    @NonNull
    @Override
    // Plugin Manifest - Required
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{new Manifest.Author("HalalKing#1551", 261634919980204033L)};
        manifest.description = "VideoEmbedPatch";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/terabyte25/plugins/builds/updater.json";
        return manifest;
    }

    @SuppressLint("SetTextI18n")
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws Throwable {
        // add the patch
        // TODO: not use a hacky way of getting the method
        patcher.patch(IntentUtils.class.getMethods()[11], new PinePrePatchFn(callFrame -> {
            callFrame.args[1] = fixMediaUrl(callFrame.args[1].toString());
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Remove all patches
        patcher.unpatchAll();
    }

    private String fixMediaUrl(String url) {
        return mediaRegex.matcher(url).replaceFirst("$1cdn$2com$3");
    }
}
