package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.databinding.WidgetManageReactionsEmojiBinding;
import com.discord.widgets.chat.managereactions.ManageReactionsEmojisAdapter;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class ReactionEmojiName extends Plugin {
    private final Logger log = new Logger();

    @SuppressLint("SetTextI18n")
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws Throwable {
        var field = ManageReactionsEmojisAdapter.ReactionEmojiViewHolder.class.getDeclaredField("binding");
        field.setAccessible(true);
        patcher.patch(ManageReactionsEmojisAdapter.ReactionEmojiViewHolder.class.getDeclaredMethod("onConfigure", new Class<?>[] {int.class, ManageReactionsEmojisAdapter.ReactionEmojiItem.class}), new PinePrePatchFn(callFrame -> {
            try {
                WidgetManageReactionsEmojiBinding binding = (WidgetManageReactionsEmojiBinding) field.get(callFrame.thisObject);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.a.setTooltipText(((ManageReactionsEmojisAdapter.ReactionEmojiItem)callFrame.args[1]).getReaction().b().d());
                }
            } catch (Exception e) {
                log.error(e);
            }
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Remove all patches
        patcher.unpatchAll();
    }
}
