package com.aliucord.plugins;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PineInsteadFn;
import com.aliucord.widgets.BottomSheet;
import com.discord.widgets.roles.RolesListView$updateView$$inlined$forEach$lambda$1;
import com.lytefast.flexinput.R;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class RoleContextMenu extends Plugin {
    public static class RoleBottomSheet extends BottomSheet {
        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            TextView textView = new TextView(view.getContext(), null, 0, R.h.UiKit_Settings_Item_Icon);
            textView.setText("Copy ID");
            textView.setCompoundDrawablesWithIntrinsicBounds(R.d.ic_content_copy_white_a60_24dp, 0,0, 0);
            textView.setOnClickListener(text -> {
                copyToClipboard(getArguments().getString("roleId", "0"));
            });

            TextView textView1 = new TextView(view.getContext(), null, 0, R.h.UiKit_Settings_Item_Icon);
            textView1.setText("Copy Color");
            textView1.setCompoundDrawablesWithIntrinsicBounds(R.d.ic_content_copy_white_a60_24dp, 0,0, 0);
            textView1.setOnClickListener(text -> {
                copyToClipboard(getArguments().getString("roleColor", "000000"));
            });

            getLinearLayout().addView(textView);
            getLinearLayout().addView(textView1);
        }

        private void copyToClipboard(String copy) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied to clipboard.", copy);
            Toast.makeText(getContext(), "Copied to clipboard.", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        }
    }

    private final Logger log = new Logger();

    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) throws NoSuchMethodException {
        patcher.patch(RolesListView$updateView$$inlined$forEach$lambda$1.class.getDeclaredMethod("onClick", View.class), new PineInsteadFn(callFrame -> {
            try {
                var role = ((RolesListView$updateView$$inlined$forEach$lambda$1) callFrame.thisObject).$role;

                Bundle args = new Bundle();
                args.putString("roleColor", String.format("%06x", role.b()));
                args.putString("roleId", String.valueOf(role.getId()));

                var roleMenu = new RoleBottomSheet();
                roleMenu.setArguments(args);
                roleMenu.show(Utils.appActivity.getSupportFragmentManager(), "Role Menu");
            } catch (Exception e) {
                log.error(e);
            }
            return null;
        }));
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
