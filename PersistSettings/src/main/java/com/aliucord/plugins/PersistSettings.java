package com.aliucord.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.widgets.BottomSheet;
import com.discord.stores.StoreAuthentication;
import com.discord.stores.StoreEmoji;
import com.discord.stores.StoreNux;
import com.discord.stores.StoreStickers;
import com.discord.utilities.persister.Persister;
import com.discord.views.CheckedSetting;
import com.discord.models.authentication.AuthState;
import com.discord.stores.authentication.AuthStateCache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import d0.z.d.m;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class PersistSettings extends Plugin {
    public static class PluginSettings extends BottomSheet {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) { this.settings = settings; }

        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            addView(createCheckedSetting(view.getContext(), "Save settings", "persistSetting", true));
        }

        private CheckedSetting createCheckedSetting(Context ctx, String title, String setting, boolean checked) {
            CheckedSetting checkedSetting = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, null);

            checkedSetting.setChecked(settings.getBool(setting, checked));
            checkedSetting.setOnCheckedListener( check -> {
                settings.setBool(setting, check);
            });

            return checkedSetting;
        }
    }

    public PersistSettings() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws NoSuchMethodException, NoSuchFieldException {
        Field fAuthState = StoreAuthentication.class.getDeclaredField("authState");
        fAuthState.setAccessible(true);

        Field fAuthStateCache = StoreAuthentication.class.getDeclaredField("authStateCache");
        fAuthStateCache.setAccessible(true);

        Method mCacheAuthState = AuthStateCache.class.getDeclaredMethod("cacheAuthState", AuthState.class);
        mCacheAuthState.setAccessible(true);

        try {patcher.patch(StoreAuthentication.class.getDeclaredMethod("handleAuthState$app_productionGoogleRelease", AuthState.class), new InsteadHook(callFrame -> {
            var storeAuth = (StoreAuthentication) callFrame.thisObject;
            var authState = (AuthState) callFrame.args[0];
 
            try {
                fAuthState.set(storeAuth, authState);
                var authStateCache = (AuthStateCache) fAuthStateCache.get(storeAuth);
                mCacheAuthState.invoke(authStateCache, authState);
            } catch (Exception e) {
                logger.error(e);
            }

            if (authState == null&& !settings.getBool("persistSetting", true)) {
                Persister.Companion.reset();
                SharedPreferences.Editor editorEdit = storeAuth.getPrefs().edit();
                m.checkNotNullExpressionValue(editorEdit, "editor");
                editorEdit.clear();
                editorEdit.apply();
            }

            return null;
        })); } catch (NoSuchMethodException e) {};

        patcher.patch(StoreEmoji.class.getDeclaredMethod("handlePreLogout"), InsteadHook.DO_NOTHING);

        patcher.patch(StoreStickers.class.getDeclaredMethod("handlePreLogout"), InsteadHook.DO_NOTHING);

        patcher.patch(StoreNux.class.getDeclaredMethod("setFirstOpen", boolean.class), InsteadHook.DO_NOTHING);
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
