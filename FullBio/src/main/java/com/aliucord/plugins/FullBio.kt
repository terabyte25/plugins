package com.aliucord.plugins

import android.content.Context
import android.widget.TextView
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.plugins.ReflectionExtensions.binding
import com.discord.utilities.view.text.LinkifiedTextView
import com.discord.widgets.settings.profile.SettingsUserProfileViewModel
import com.discord.widgets.settings.profile.WidgetEditUserOrGuildMemberProfile
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel

// This class is never used so your IDE will likely complain. Let's make it shut up!
@AliucordPlugin
class FullBio : Plugin() {
    private val log = Logger()

    @Throws(NoSuchMethodException::class)  // Called when your plugin is started. This is the place to register command, add patches, etc
    override fun start(context: Context) {
        ReflectionExtensions.init()

        patcher.patch(WidgetEditUserOrGuildMemberProfile::class.java.getDeclaredMethod("configureBio", SettingsUserProfileViewModel.ViewState.Loaded::class.java), Hook {
            val binding = (it.thisObject as WidgetEditUserOrGuildMemberProfile).binding
            setMax(binding.root.findViewById<LinkifiedTextView>(Utils.getResId("bio_preview_text", "id")))
            setMax(binding.root.findViewById<LinkifiedTextView>(Utils.getResId("bio_editor_text_input_field", "id")))
        })

        patcher.patch(WidgetUserSheet::class.java.getDeclaredMethod("configureAboutMe", WidgetUserSheetViewModel.ViewState.Loaded::class.java), Hook {
            val binding = (it.thisObject as WidgetUserSheet).binding
            setMax(binding.root.findViewById<LinkifiedTextView>(Utils.getResId("about_me_text", "id")))
        })
    }

    private fun setMax(text: TextView?) {
        text?.apply {
            maxLines = Integer.MAX_VALUE
        }
    }

    // Called when your plugin is stopped
    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}