package com.aliucord.plugins;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.Button;
import com.discord.api.sticker.BaseSticker;
import com.discord.databinding.WidgetGuildStickerSheetBinding;
import com.discord.models.guild.Guild;
import com.discord.widgets.stickers.WidgetGuildStickerSheet;

import java.io.File;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class ShittyDownloadStickers extends Plugin {
    private final Logger log = new Logger();
    private File downloadDirectory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws NoSuchMethodException {
        final int layoutId = View.generateViewId();
        var method = WidgetGuildStickerSheet.class.getDeclaredMethod("getBinding");
        method.setAccessible(true);
        patcher.patch(WidgetGuildStickerSheet.class, "configureButtons", new Class<?>[] { boolean.class, boolean.class, Guild.class}, new PinePatchFn(callFrame -> {
            try {
                var binding = (WidgetGuildStickerSheetBinding) method.invoke(callFrame.thisObject);
                var ctx = binding.getRoot().getContext();
                FrameLayout layout = (FrameLayout) ((ViewGroup)((ViewGroup) binding.getRoot()).getChildAt(0)).getChildAt(1);
                layout.setVisibility(View.VISIBLE);

                var downloadButton = new Button(ctx);
                downloadButton.setText("Download Sticker");
                downloadButton.setPadding(DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18));
                downloadButton.setOnClickListener( (view) -> {
                    String url = getCDNAssetUrl(binding.m.j);
                        new Thread(() -> {
                            try {
                                File stickerSave = new File(downloadDirectory, binding.m.j.d()+binding.m.j.b());
                                Http.simpleDownload(url, stickerSave);
                                Utils.showToast(context, String.format("Saved sticker to %s", stickerSave.getAbsolutePath()));
                            } catch (Throwable e) { log.error(context, "Failed to download sticker."); }
                        }).start();
                });
                /**
                var copyUrl = new Button(ctx);
                copyUrl.setText("Copy URL");
                copyUrl.setPadding(DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18));
                copyUrl.setOnClickListener( (view) -> {
                    String url = getCDNAssetUrl(binding.m.j);
                    Utils.setClipboard("URL", url);
                });

                buttonLayout.addView(downloadButton);
                buttonLayout.addView(copyUrl);
                 */
                layout.addView(downloadButton,2 );
            } catch (Exception e) {
                log.error(context, e);
            }
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private final String getCDNAssetUrl(BaseSticker baseSticker) {
        int ordinal = baseSticker.a().ordinal();
        if (ordinal == 1 || ordinal == 2) {
            return "https://media.discordapp.net/stickers/" + baseSticker.d() + baseSticker.b();
        } else {
            return "https://discord.com/stickers/" + baseSticker.d() + baseSticker.b();
        }
    }

}
