package com.aliucord.plugins

import com.discord.databinding.*
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderHeader
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import java.lang.reflect.Field

object ReflectionExtensions {
    private lateinit var textChannelBinding: Field
    private lateinit var privateChannelBinding: Field
    private lateinit var categoryChannelBinding: Field
    private lateinit var stageChannelBinding: Field
    private lateinit var threadChannelBinding: Field
    private lateinit var voiceChannelBinding: Field
    private lateinit var roleHeaderBinding: Field
    private lateinit var memberBinding: Field

    fun init() {
        textChannelBinding = WidgetChannelsListAdapter.ItemChannelText::class.java.getDeclaredField("binding").apply { isAccessible = true }
        privateChannelBinding = WidgetChannelsListAdapter.ItemChannelPrivate::class.java.getDeclaredField("binding").apply { isAccessible = true }
        categoryChannelBinding = WidgetChannelsListAdapter.ItemChannelCategory::class.java.getDeclaredField("binding").apply { isAccessible = true }
        stageChannelBinding = WidgetChannelsListAdapter.ItemChannelStageVoice::class.java.getDeclaredField("binding").apply { isAccessible = true }
        threadChannelBinding = WidgetChannelsListAdapter.ItemChannelThread::class.java.getDeclaredField("binding").apply { isAccessible = true }
        voiceChannelBinding = WidgetChannelsListAdapter.ItemChannelVoice::class.java.getDeclaredField("binding").apply { isAccessible = true }
        roleHeaderBinding = ChannelMembersListViewHolderHeader::class.java.getDeclaredField("binding").apply { isAccessible = true }
        memberBinding = ChannelMembersListViewHolderMember::class.java.getDeclaredField("binding").apply { isAccessible = true }
    }

    val WidgetChannelsListAdapter.ItemChannelText.binding: WidgetChannelsListItemChannelBinding
        get() = textChannelBinding[this] as WidgetChannelsListItemChannelBinding

    val WidgetChannelsListAdapter.ItemChannelStageVoice.binding: WidgetChannelsListItemChannelStageVoiceBinding
        get() = stageChannelBinding[this] as WidgetChannelsListItemChannelStageVoiceBinding

    val WidgetChannelsListAdapter.ItemChannelPrivate.binding: WidgetChannelsListItemChannelPrivateBinding
        get() = privateChannelBinding[this] as WidgetChannelsListItemChannelPrivateBinding

    val WidgetChannelsListAdapter.ItemChannelCategory.binding: WidgetChannelsListItemCategoryBinding
        get() = categoryChannelBinding[this] as WidgetChannelsListItemCategoryBinding

    val WidgetChannelsListAdapter.ItemChannelThread.binding: WidgetChannelsListItemThreadBinding
        get() = threadChannelBinding[this] as WidgetChannelsListItemThreadBinding

    val WidgetChannelsListAdapter.ItemChannelVoice.binding: WidgetChannelsListItemChannelVoiceBinding
        get() = voiceChannelBinding[this] as WidgetChannelsListItemChannelVoiceBinding

    val ChannelMembersListViewHolderHeader.binding: WidgetChannelMembersListItemHeaderBinding
        get() = roleHeaderBinding[this] as WidgetChannelMembersListItemHeaderBinding

    val ChannelMembersListViewHolderMember.binding: WidgetChannelMembersListItemUserBinding
        get() = memberBinding[this] as WidgetChannelMembersListItemUserBinding
}