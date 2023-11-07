package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.DimenUtils;
import com.discord.databinding.WidgetChannelsListItemChannelVoiceBinding;
import com.discord.utilities.permissions.PermissionUtils;
import com.discord.widgets.channels.list.WidgetChannelsListAdapter;
import com.discord.widgets.channels.list.items.ChannelListItem;
import com.discord.widgets.channels.list.items.ChannelListItemVoiceChannel;
import com.discord.widgets.voice.settings.WidgetVoiceChannelSettings;
import com.lytefast.flexinput.R;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class VCID extends Plugin {
    @SuppressLint("SetTextI18n")
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws Throwable {
        var itemClass = WidgetChannelsListAdapter.ItemChannelVoice.class.getDeclaredField("binding");
        itemClass.setAccessible(true);
        // add the patch
        patcher.patch(WidgetChannelsListAdapter.ItemChannelVoice.class.getDeclaredMethod("onConfigure", int.class, ChannelListItem.class), new Hook(callFrame -> {
            var channel = (ChannelListItemVoiceChannel) callFrame.args[1];
            if (PermissionUtils.can(16, channel.component1().k())) return;

            try {
                var binding = (WidgetChannelsListItemChannelVoiceBinding) itemClass.get(callFrame.thisObject);
                binding.a.setOnLongClickListener(view -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("VCID", String.valueOf(channel.getChannel().k()));
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, "Copied to clipboard.", duration).show();
                    clipboard.setPrimaryClip(clip);
                    return true;
                });

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }));

        final int viewId = View.generateViewId();

        patcher.patch(WidgetVoiceChannelSettings.class.getDeclaredMethod("configureUI", WidgetVoiceChannelSettings.Model.class), new Hook(callFrame -> {
            var binding = WidgetVoiceChannelSettings.access$getBinding$p((WidgetVoiceChannelSettings) callFrame.thisObject);
            var model = (WidgetVoiceChannelSettings.Model) callFrame.args[0];

            var layout = (LinearLayout) binding.a.findViewById(Utils.getResId("channel_settings_section_user_management", "id"));
            if (layout.findViewById(viewId) != null) {
                return;
            }

            var textView = new TextView(context, null, 0, Utils.getResId("UiKit_Settings_Item_Icon", "style"));
            textView.setId(viewId);
            textView.setText("Copy ID");
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(16);
            textView.setCompoundDrawablePadding(DimenUtils.dpToPx(32));
            var padding = DimenUtils.dpToPx(16);
            textView.setPadding(padding, padding, padding, padding);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.e.ic_content_copy_white_a60_24dp, 0,0, 0);
            textView.setOnClickListener(view -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("VCID", String.valueOf(model.getChannel().k()));
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, "Copied to clipboard.", duration).show();
                clipboard.setPrimaryClip(clip);
            });

            layout.addView(textView, 2);
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Remove all patches
        patcher.unpatchAll();
    }
}
