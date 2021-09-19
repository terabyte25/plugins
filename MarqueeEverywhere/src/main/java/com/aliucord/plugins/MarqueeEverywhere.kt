package com.aliucord.plugins

import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import com.aliucord.plugins.ReflectionExtensions.binding
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem

// This class is never used so your IDE will likely complain. Let's make it shut up!
@AliucordPlugin
class MarqueeEverywhere : Plugin() {
    private val log = Logger()

    @Throws(NoSuchMethodException::class)  // Called when your plugin is started. This is the place to register command, add patches, etc
    override fun start(context: Context) {
        ReflectionExtensions.init()

        patcher.patch(WidgetChannelsListAdapter.ItemChannelText::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChannelListItem::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetChannelsListAdapter.ItemChannelText).binding
            setMarquee(binding.root.findViewById<TextView>(Utils.getResId("channels_item_channel_name", "id")))
        })

        patcher.patch(WidgetChannelsListAdapter.ItemChannelVoice::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChannelListItem::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetChannelsListAdapter.ItemChannelVoice).binding
            setMarquee(binding.root.findViewById<TextView>(Utils.getResId("channels_item_voice_channel_name", "id")))
        })

        patcher.patch(WidgetChannelsListAdapter.ItemChannelStageVoice::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChannelListItem::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetChannelsListAdapter.ItemChannelStageVoice).binding
            setMarquee(binding.root.findViewById<TextView>(Utils.getResId("stage_channel_item_voice_channel_name", "id")))
        })

        patcher.patch(WidgetChannelsListAdapter.ItemChannelCategory::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChannelListItem::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetChannelsListAdapter.ItemChannelCategory).binding
            setMarquee(binding.root.findViewById<TextView>(Utils.getResId("channels_item_category_name", "id")))
        })

        patcher.patch(WidgetChannelsListAdapter.ItemChannelPrivate::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChannelListItem::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetChannelsListAdapter.ItemChannelPrivate).binding
            setMarquee(binding.root.findViewById<TextView>(Utils.getResId("channels_list_item_private_name", "id")))
        })

        patcher.patch(WidgetChannelsListAdapter.ItemChannelThread::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChannelListItem::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetChannelsListAdapter.ItemChannelThread).binding
            setMarquee(binding.root.findViewById<TextView>(Utils.getResId("channels_item_thread_name", "id")))
        })
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
}