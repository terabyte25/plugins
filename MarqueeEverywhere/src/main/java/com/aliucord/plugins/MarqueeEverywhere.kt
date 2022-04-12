package com.aliucord.plugins

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.aliucord.PluginManager
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.plugins.ReflectionExtensions.binding
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting
import com.discord.views.UsernameView
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderHeader
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember

// This class is never used so your IDE will likely complain. Let's make it shut up!
@AliucordPlugin
class MarqueeEverywhere : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings)
    }

    @Throws(NoSuchMethodException::class)  // Called when your plugin is started. This is the place to register command, add patches, etc
    override fun start(context: Context) {
        ReflectionExtensions.init()

        if (settings.getBool("channelNames", true)) {
            patcher.after<WidgetChannelsListAdapter.ItemChannelText>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channels_item_channel_name", "id")))
            }

            patcher.after<WidgetChannelsListAdapter.ItemChannelVoice>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channels_item_voice_channel_name", "id")))
            }

            patcher.after<WidgetChannelsListAdapter.ItemChannelStageVoice>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("stage_channel_item_voice_channel_name", "id")))
            }

            patcher.after<WidgetChannelsListAdapter.ItemChannelCategory>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channels_item_category_name", "id")))
            }

            patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channels_list_item_private_name", "id")))
            }

            patcher.after<WidgetChannelsListAdapter.ItemChannelThread>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channels_item_thread_name", "id")))
            }
        }

        if (settings.getBool("memberNames", true)) {
            patcher.after<ChannelMembersListViewHolderHeader>("bind", ChannelMembersListAdapter.Item.Header::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channel_members_list_item_header_text", "id")))
            }

            patcher.after<ChannelMembersListViewHolderMember>("bind", ChannelMembersListAdapter.Item.Member::class.java, Function0::class.java) {
                setMarquee(binding.root.findViewById<UsernameView>(Utils.getResId("channel_members_list_item_name", "id")).findViewById(Utils.getResId("username_text", "id")))
            }
        }

        if (settings.getBool("userStatus", true)) {
            patcher.after<ChannelMembersListViewHolderMember>("bind", ChannelMembersListAdapter.Item.Member::class.java, Function0::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channel_members_list_item_game", "id")))
            }

            patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>("onConfigure", Int::class.java, ChannelListItem::class.java) {
                setMarquee(binding.root.findViewById(Utils.getResId("channels_list_item_private_desc", "id")))
            }
        }
    }

    private fun setMarquee(text: TextView?) {
        text?.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
        }
    }

    // Called when your plugin is stopped
    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    class PluginSettings(val settings: SettingsAPI) : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)

            context?.let {
                addView(createCheckedSetting(it, "Channel names", "Channel names (including names of DMs) should scroll", "channelNames"))
                addView(createCheckedSetting(it, "Member names", "Member names should scroll", "memberNames"))
                addView(createCheckedSetting(it, "User status", "User statuses should scroll", "userStatus"))
            }
        }

        private fun createCheckedSetting(ctx: Context, title: String, subtext: String, setting: String, default: Boolean = true): CheckedSetting {
            return Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtext).apply {
                isChecked = settings.getBool(setting, default)
                setOnCheckedListener {
                    settings.setBool(setting, it)
                    PluginManager.stopPlugin("MarqueeEverywhere")
                    PluginManager.startPlugin("MarqueeEverywhere")
                }
            }
        }
    }
}