package com.aliucord.plugins

import com.discord.databinding.WidgetEmojiSheetBinding
import com.discord.databinding.WidgetGuildStickerSheetBinding
import com.discord.widgets.emoji.WidgetEmojiSheet
import com.discord.widgets.stickers.WidgetGuildStickerSheet
import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectionExtensions {
    private lateinit var guildStickerBinding: Method
    private lateinit var emojiBinding: Method

    fun init() {
        guildStickerBinding = WidgetGuildStickerSheet::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }
        emojiBinding = WidgetEmojiSheet::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }
    }

    val WidgetGuildStickerSheet.binding: WidgetGuildStickerSheetBinding
        get() = guildStickerBinding.invoke(this) as WidgetGuildStickerSheetBinding

    val WidgetEmojiSheet.binding: WidgetEmojiSheetBinding
        get() = emojiBinding.invoke(this) as WidgetEmojiSheetBinding
}