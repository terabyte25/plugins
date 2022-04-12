package com.aliucord.plugins

import android.content.Context
import android.view.View
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import kotlin.Throws
import com.discord.widgets.user.Badge
import com.aliucord.patcher.after
import com.aliucord.patcher.instead
import com.aliucord.plugins.ReflectionExtensions.binding
import com.discord.models.guild.Guild
import com.discord.widgets.emoji.WidgetEmojiSheet
import com.discord.widgets.stickers.WidgetGuildStickerSheet
import com.google.android.material.button.MaterialButton

@AliucordPlugin
class FuckOffDiscord : Plugin() {
    @Throws(NoSuchMethodException::class)
    override fun start(context: Context) {
        ReflectionExtensions.init();

        patcher.instead<Badge>("getShowPremiumUpSell") { false }

        patcher.after<WidgetEmojiSheet>("configureButtons", Boolean::class.java, Boolean::class.java, Guild::class.java) {
            binding.root.findViewById<MaterialButton>(Utils.getResId("premium_btn", "id")).apply {
                visibility = View.GONE
            }
        }

        patcher.after<WidgetGuildStickerSheet>("configureButtons", Boolean::class.java, Boolean::class.java, Guild::class.java) {
            binding.root.findViewById<MaterialButton>(Utils.getResId("guild_sticker_sheet_premium_btn", "id")).apply {
                visibility = View.GONE
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}