package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.PluginManager;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.utils.IOUtils;
import com.discord.utilities.icon.IconUtils;
import com.discord.widgets.user.profile.UserProfileHeaderViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Pattern;

@SuppressWarnings({"unused"})
@AliucordPlugin
public class UserBG extends Plugin {
    private static final String url = "https://usrbg.cumcord.com/";
    private final String regex = ".*?\\(\"(.*?)\"";
    private String css;
    private final Logger log = new Logger();
    @Override
    public void start(Context ctx) {
        try {
            new Thread(() -> {
                try {
                    final File cachedFile = new File(ctx.getCacheDir(), "db.css");
                    cachedFile.createNewFile();

                    css = loadFromCache(cachedFile);
                    log.debug("Loaded USRBG database.");

                    if (ifRecache(cachedFile.lastModified())) {
                        log.debug("Downloading USRBG database...");
                        Http.simpleDownload(url, cachedFile);
                        log.debug("Done downloading database.");

                        css = loadFromCache(cachedFile);
                        log.debug("Updated USRBG database.");
                    }
                } catch (Throwable e) { log.error(e); }
            }).start();

            patcher.patch(IconUtils.class.getDeclaredMethod("getForUserBanner", long.class, String.class, Integer.class, boolean.class), new PinePatchFn(callFrame -> {
                if (css == null) return; // could not get USRBG database in time or wasn't available

                var id = (long) callFrame.args[0];
                var matcher = Pattern.compile(id + regex, Pattern.DOTALL).matcher(css);
                if (matcher.find()) {
                    log.debug(matcher.group(1));
                    callFrame.setResult(withSize(matcher.group(1), (Integer) callFrame.args[2]));
                }
            }));

            if (PluginManager.isPluginEnabled("ViewProfileImages")) { // inb4 ven asks what the hell im doing
                patcher.patch(UserProfileHeaderViewModel.ViewState.Loaded.class.getDeclaredMethod("getBanner"), new PinePatchFn(callFrame -> {
                    if (css == null)
                        return; // could not get USRBG database in time or wasn't available
                    var user = ((UserProfileHeaderViewModel.ViewState.Loaded) callFrame.thisObject).getUser();
                    var id = (long) user.getId();
                    var matcher = Pattern.compile(id + regex, Pattern.DOTALL).matcher(css);
                    if (matcher.find()) {
                        callFrame.setResult(matcher.group(1));
                    }
                }));
            }
        } catch (Throwable e) { log.error(e); }
    }

    private String loadFromCache(File cache) {
        try {
            return new String(IOUtils.readBytes(new FileInputStream(cache)));
        } catch (Throwable e) {
            log.error(e);
        }

        return null;
    }

    private String withSize(String background, Integer size) {
        return background + "?size=" + IconUtils.getMediaProxySize(size);
    }

    private boolean ifRecache(long lastModified) {
        return (System.currentTimeMillis() - lastModified) > (6*60*60*1000); // 6 hours
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
        css = null;
    }
}