package com.aliucord.plugins;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.plugins.clonemodal.Modal;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.Button;
import com.discord.api.sticker.BaseSticker;
import com.discord.api.sticker.Sticker;
import com.discord.databinding.WidgetGuildStickerSheetBinding;
import com.discord.widgets.stickers.GuildStickerSheetViewModel;
import com.discord.widgets.stickers.WidgetGuildStickerSheet;
import com.lytefast.flexinput.R;

import java.io.File;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class ShittyDownloadStickers extends Plugin {
    public static final Logger log = new Logger("ShittyDownloadStickers");
    private File downloadDirectory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws NoSuchMethodException {
        final int layoutId = View.generateViewId();
        var method = WidgetGuildStickerSheet.class.getDeclaredMethod("getBinding");
        method.setAccessible(true);
        patcher.patch(WidgetGuildStickerSheet.class, "configureUI", new Class<?>[] { GuildStickerSheetViewModel.ViewState.Loaded.class }, new Hook(callFrame -> {
            try {
                // i looked at ven's plugin for help since i have no idea what im doing
                var sticker = ( (GuildStickerSheetViewModel.ViewState.Loaded) callFrame.args[0]).component1();
                var nitro = ( (GuildStickerSheetViewModel.ViewState.Loaded) callFrame.args[0]).component2();
                var isKnown = ( (GuildStickerSheetViewModel.ViewState.Loaded) callFrame.args[0]).getGuildStickerGuildInfo() != null;
                var isUserInGuild = ( (GuildStickerSheetViewModel.ViewState.Loaded) callFrame.args[0]).getGuildStickerGuildInfo().isUserInGuild();

                var binding = (WidgetGuildStickerSheetBinding) method.invoke(callFrame.thisObject);
                if (binding == null) return;
                var root = (ViewGroup) binding.getRoot();
                var rootLayout = (android.widget.LinearLayout) root.getChildAt(0);

                if (rootLayout.findViewById(layoutId) != null) return;

                var ctx = rootLayout.getContext();
                if (ctx == null) return;

                int marginDpFour = DimenUtils.dpToPx(4);
                int marginDpEight = marginDpFour * 2;
                int marginDpSixteen = marginDpEight * 2;

                var downloadButton = new Button(ctx);
                configureDownload(context, sticker, downloadButton);

                var copyUrl = new Button(ctx);
                copyUrl.setText("Copy URL");
                copyUrl.setPadding(DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18));
                copyUrl.setOnClickListener( (view) -> {
                    String url = getCDNAssetUrl(sticker);
                    Utils.setClipboard("URL", url);
                    Utils.showToast("Copied to clipboard");
                });

                var cloneButton = new Button(ctx);
                cloneButton.setText("Clone to other server");
                cloneButton.setOnClickListener(v -> Utils.openPageWithProxy(ctx, new Modal(getCDNAssetUrl(sticker), sticker.h(), sticker.getId(), sticker.f(), sticker.j())));

                var buttonParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                buttonParams.setMargins(0, 0, 0, 0);
                downloadButton.setLayoutParams(buttonParams);
                copyUrl.setLayoutParams(buttonParams);
                cloneButton.setLayoutParams(buttonParams);

                var pluginButtonLayout = new com.aliucord.widgets.LinearLayout(ctx);
                pluginButtonLayout.setId(layoutId);
                pluginButtonLayout.addView(downloadButton);
                pluginButtonLayout.addView(copyUrl);
                pluginButtonLayout.addView(cloneButton);

                int idx = 2;
                if (
                        (!nitro /* need nitro */ ||
                                !isUserInGuild /* not on server */
                        ) && isKnown
                ) {
                    // Nitro or Join Button visible
                    pluginButtonLayout.setPadding(marginDpSixteen, marginDpFour, marginDpSixteen, marginDpEight);

                    // Adjust nitro and join button
                    var params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 0);
                    binding.k.setLayoutParams(params); // Nitro
                    binding.l.setLayoutParams(params); // Join

                    // Adjust nitro/join container
                    var joinContainer = (FrameLayout) binding.k.getParent();
                    var containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    containerParams.setMargins(marginDpSixteen, marginDpEight, marginDpSixteen, 0);
                    joinContainer.setLayoutParams(containerParams);
                } else {
                    // No buttons
                    pluginButtonLayout.setPadding(marginDpSixteen, marginDpEight, marginDpSixteen, marginDpEight);
                }

                rootLayout.addView(pluginButtonLayout, idx);
            } catch (Exception e) {
                log.error(context, e);
            }
        }));
    }

    private void configureDownload(Context context, Sticker sticker, Button downloadButton) {
        Runnable callSelf = () -> configureDownload(context, sticker, downloadButton);

        File stickerSave = new File(downloadDirectory, sticker.d() + sticker.b());
        downloadButton.setPadding(DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18), DimenUtils.dpToPx(18));
        if (!stickerSave.exists()) {
            downloadButton.setText("Save Sticker");
            downloadButton.setBackgroundColor(context.getColor(R.c.uikit_btn_bg_color_selector_brand));
            downloadButton.setOnClickListener((view) -> {
                Utils.threadPool.execute(() -> {
                    try {
                        String url = getCDNAssetUrl(sticker);

                        Http.simpleDownload(url, stickerSave);
                        Utils.showToast(String.format("Saved sticker to %s", stickerSave.getAbsolutePath()));
                        callSelf.run();
                    } catch (Throwable e) {
                        log.error(context, "Failed to download sticker.");
                    }
                });
            });
        } else {
            downloadButton.setText("Remove Saved Sticker");
            downloadButton.setBackgroundColor(context.getColor(R.c.uikit_btn_bg_color_selector_red));
            downloadButton.setOnClickListener((view) -> {
                Utils.threadPool.execute(() -> {
                    if (stickerSave.delete())
                        callSelf.run();
                    else
                        Utils.showToast("Failed to delete sticker :(");
                });
            });
        }
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private final String getCDNAssetUrl(BaseSticker baseSticker) {
        return "https://cdn.discordapp.com/stickers/" + baseSticker.d() + baseSticker.b();
    }

}
