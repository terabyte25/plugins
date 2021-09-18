package com.aliucord.plugins

import android.content.Context
import android.view.View
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import kotlin.Throws
import com.discord.widgets.user.Badge
import com.aliucord.patcher.PineInsteadFn
import com.aliucord.patcher.PinePatchFn
import com.aliucord.plugins.ReflectionExtensions.binding
import com.discord.models.guild.Guild
import com.discord.widgets.emoji.WidgetEmojiSheet
import com.discord.widgets.stickers.WidgetGuildStickerSheet
import com.google.android.material.button.MaterialButton
import top.canyie.pine.Pine.CallFrame

// This class is never used so your IDE will likely complain. Let's make it shut up!
@AliucordPlugin
class FuckOffDiscord : Plugin() {
    private val log = Logger()

    @Throws(NoSuchMethodException::class)  // Called when your plugin is started. This is the place to register command, add patches, etc
    override fun start(context: Context) {
        ReflectionExtensions.init();

        patcher.patch(Badge::class.java.getDeclaredMethod("getShowPremiumUpSell"), PineInsteadFn { false })

        patcher.patch(WidgetEmojiSheet::class.java.getDeclaredMethod("configureButtons", Boolean::class.java, Boolean::class.java, Guild::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetEmojiSheet).binding
            binding.root.findViewById<MaterialButton>(Utils.getResId("premium_btn", "id")).apply {
                visibility = View.GONE
            }
        })

        patcher.patch(WidgetGuildStickerSheet::class.java.getDeclaredMethod("configureButtons", Boolean::class.java, Boolean::class.java, Guild::class.java), PinePatchFn {
            val binding = (it.thisObject as WidgetGuildStickerSheet).binding
            binding.root.findViewById<MaterialButton>(Utils.getResId("guild_sticker_sheet_premium_btn", "id")).apply {
                visibility = View.GONE
            }
        })
    }

    // Called when your plugin is stopped
    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}