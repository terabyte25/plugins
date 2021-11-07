package com.aliucord.plugins.userbg

import android.content.Context
import com.aliucord.Logger
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.plugins.userbg.model.APFP
import com.aliucord.plugins.userbg.model.AbstractDatabase
import com.aliucord.plugins.userbg.model.USRBG
import kotlin.Throws

@AliucordPlugin
class UserBG : Plugin() {
    init {
        USRBG = com.aliucord.plugins.userbg.model.USRBG
        APFP = com.aliucord.plugins.userbg.model.APFP
    }
    @Throws(NoSuchMethodException::class)
    override fun start(ctx: Context) {
        USRBG.init(ctx, settings, patcher)
        APFP.init(ctx, settings, patcher)
    }

    override fun stop(ctx: Context) {
        patcher.unpatchAll()
    }

    companion object {
        lateinit var USRBG: USRBG
        lateinit var APFP: APFP

        val log: Logger = Logger("UserBG")
        const val REFRESH_CACHE_TIME = (6 * 60).toLong()
    }

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }
}