package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.patcher.PreHook;
import com.aliucord.widgets.BottomSheet;
import com.discord.api.message.embed.EmbedType;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.simpleast.core.node.Node;
import com.discord.utilities.embed.EmbedResourceUtils;
import com.discord.utilities.intent.IntentUtils;
import com.discord.utilities.textprocessing.MessagePreprocessor;
import com.discord.utilities.textprocessing.MessageRenderContext;
import com.discord.utilities.textprocessing.node.UrlNode;
import com.discord.views.CheckedSetting;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import d0.t.u;


// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class VideoEmbedPatch extends Plugin {
    public static class PluginSettings extends BottomSheet {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) { this.settings = settings; }

        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            addView(createCheckedSetting(view.getContext(), "Strip video embed links", "stripLinks", true));
            addView(createCheckedSetting(view.getContext(), "Correct copied links", "correctCopy", true));
        }

        private CheckedSetting createCheckedSetting(Context ctx, String title, String setting, boolean checked) {
            CheckedSetting checkedSetting = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, null);

            checkedSetting.setChecked(settings.getBool(setting, checked));
            checkedSetting.setOnCheckedListener( check -> {
                settings.setBool(setting, check);
            });

            return checkedSetting;
        }
    }

    public VideoEmbedPatch() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    private final Pattern mediaRegex = Pattern.compile("(https:\\/\\/)media(\\.discordapp\\.)net(\\/attachments\\/\\d+\\/\\d+\\/.+(\\.mov|\\.mp4|\\.webm))");

    @SuppressLint("SetTextI18n")
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws Throwable {
        if (settings.getBool("correctCopy", true)) {
            // add the patch
            patcher.patch(IntentUtils.class.getDeclaredMethod("performChooserSendIntent", Context.class, String.class, CharSequence.class), new PreHook(callFrame -> {
                callFrame.args[1] = fixMediaUrl(callFrame.args[1].toString());
            }));
        }

        if (settings.getBool("stripLinks", true)) {
            var field = MessagePreprocessor.class.getDeclaredField("embeds");
            field.setAccessible(true);

            patcher.patch("com.discord.utilities.textprocessing.MessagePreprocessor", "stripSimpleEmbedLink", new Class<?>[]{Collection.class}, new InsteadHook(callFrame -> {
                var collection = (Collection<Node<MessageRenderContext>>) callFrame.args[0];

                // fucky reflection shit that should not be done
                try {
                    var embeds = (List<MessageEmbed>) field.get(callFrame.thisObject);

                    if (collection.size() == 1 && embeds != null && embeds.size() == 1) {
                        MessageEmbed messageEmbed = embeds.get(0);
                        if ((((Node) u.elementAt(collection, 0)) instanceof UrlNode) && (EmbedResourceUtils.INSTANCE.isSimpleEmbed(messageEmbed) || messageEmbed.k() == EmbedType.VIDEO)) {
                            collection.clear();
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.errorToast(e);
                }

                return null;
            }));
        }

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
