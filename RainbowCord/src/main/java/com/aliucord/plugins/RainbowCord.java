package com.aliucord.plugins;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.patcher.Hook;
import com.aliucord.views.TextInput;
import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.utilities.view.text.TextWatcher;
import com.discord.views.CheckedSetting;
import com.discord.widgets.chat.input.MessageDraftsRepo;
import com.discord.widgets.chat.input.WidgetChatInputEditText;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.lytefast.flexinput.widget.FlexEditText;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class RainbowCord extends Plugin {
    public static class PluginSettings extends SettingsPage {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) { this.settings = settings; }

        public void onViewBound(View view) {
            super.onViewBound(view);
            setActionBarTitle("RainbowCord");

            TextInput textInput = new TextInput(view.getContext());
            //textInput.setHint("Animation Cycle Time");
            textInput.getEditText().setText(String.valueOf(settings.getLong("colorSpeed", RainbowCord.DEFAULT_SPEED)));
            textInput.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            textInput.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        if (Long.valueOf(editable.toString()) != 0)
                            settings.setLong("colorSpeed", Long.valueOf(editable.toString()));
                    } catch (Exception e) {
                        settings.setLong("colorSpeed", RainbowCord.DEFAULT_SPEED);
                    }
                }
            });

            addView(textInput);
            addView(createCheckedSetting(view.getContext(), "Rainbow Typing Box", "rainbowTyping", true));
            addView(createCheckedSetting(view.getContext(), "Rainbow Messages (may be laggy)", "rainbowMessages", false));
        }

        private CheckedSetting createCheckedSetting(Context ctx, String title, String setting, boolean checked) {
            CheckedSetting checkedSetting = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, null);

            checkedSetting.setChecked(settings.getBool(setting, checked));
            checkedSetting.setOnCheckedListener( check -> {
                settings.setBool(setting, check);
                PluginManager.stopPlugin("RainbowCord");
                PluginManager.startPlugin("RainbowCord");
            });

            return checkedSetting;
        }
    }

    private static final int[] animatedColors = {0xFFe5e5ea, 0xFFfea7b9, 0xFFcd9aec, 0xFFb5b8f8, 0xFF87beff, 0xFF97f2c3, 0xFFbbe061, 0xFFf9e560, 0xFFffb43f, 0xFFcfa075, 0xFFe5e5ea};
    private static final long DEFAULT_SPEED = 5000;

    public RainbowCord() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws NoSuchMethodException {
        if (settings.getBool("rainbowTyping", true)) {
            patcher.patch(WidgetChatInputEditText.class.getDeclaredConstructor(FlexEditText.class, MessageDraftsRepo.class), new Hook(callFrame -> {
                setValueAnimator((FlexEditText) callFrame.args[0]);
            }));
        }

        if (settings.getBool("rainbowMessages", false)) {
            patcher.patch(WidgetChatListAdapterItemMessage.class.getDeclaredMethod("processMessageText", SimpleDraweeSpanTextView.class, MessageEntry.class), new Hook(callFrame -> {
                setValueAnimator((SimpleDraweeSpanTextView) callFrame.args[0]);
            }));
        }
    }

    private void setValueAnimator(TextView textView) {
        ObjectAnimator colorAnim = ObjectAnimator.ofArgb(textView, "textColor", animatedColors);
        colorAnim.setRepeatMode(ObjectAnimator.REVERSE);
        colorAnim.setRepeatCount(ObjectAnimator.INFINITE);
        colorAnim.setDuration(settings.getLong("colorSpeed", RainbowCord.DEFAULT_SPEED));
        colorAnim.start();
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
