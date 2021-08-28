package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.Logger;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PineInsteadFn;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.api.message.embed.EmbedType;
import com.discord.api.message.embed.EmbedVideo;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.models.message.Message;
import com.discord.simpleast.core.node.Node;
import com.discord.utilities.embed.EmbedResourceUtils;
import com.discord.utilities.intent.IntentUtils;
import com.discord.utilities.textprocessing.MessagePreprocessor;
import com.discord.utilities.textprocessing.MessageRenderContext;
import com.discord.utilities.textprocessing.node.UrlNode;
import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.MessageEntry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import d0.t.u;


// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
public class VideoEmbedPatch extends Plugin {
    private final Pattern mediaRegex = Pattern.compile("(https:\\/\\/)media(\\.discordapp\\.)net(\\/attachments\\/\\d+\\/\\d+\\/.+(\\.mov|\\.mp4|\\.webm))");
    private final Logger log = new Logger();
    
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

        patcher.patch("com.discord.utilities.textprocessing.MessagePreprocessor", "stripSimpleEmbedLink", new Class<?>[] {Collection.class}, new PineInsteadFn(callFrame -> {
            var collection = (Collection<Node<MessageRenderContext>>) callFrame.args[0];

            // fucky reflection shit that should not be done
            try {
                var field = MessagePreprocessor.class.getDeclaredField("embeds");
                field.setAccessible(true);
                var embeds = (List<MessageEmbed>) field.get(callFrame.thisObject);

                if (collection.size() == 1 && embeds != null && embeds.size() == 1) {
                    MessageEmbed messageEmbed = embeds.get(0);
                    if ((((Node) u.elementAt(collection, 0)) instanceof UrlNode) && (EmbedResourceUtils.INSTANCE.isSimpleEmbed(messageEmbed) || messageEmbed.k() == EmbedType.VIDEO)) {
                        collection.clear();
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(context, e);
            }

            return null;
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
