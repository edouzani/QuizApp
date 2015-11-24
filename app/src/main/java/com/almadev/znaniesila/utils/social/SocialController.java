package com.almadev.znaniesila.utils.social;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKShareDialog;

/**
 * Created by Aleksey on 23.11.2015.
 */
public class SocialController {

    private static final String[] sMyScope = new String[]{
            VKScope.WALL,
            VKScope.NOHTTPS,
    };

    private SocialController() {}

    public static void vkShare(Activity activity, FragmentManager fm, FragmentTransaction pTransaction, String text) {

        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(activity, sMyScope);
        }

        try {
            VKShareDialog dialog = new VKShareDialog()
                    .setText(text)
                    .setAttachmentLink("Знание-сила!", "http://www.znanie.tv")
                    .setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                        @Override
                        public void onVkShareComplete(int postId) {
                        }

                        @Override
                        public void onVkShareCancel() {
                        }

                        @Override
                        public void onVkShareError(VKError error) {
                        }
                    });
            if (fm != null) {
                showDialog(dialog, fm);
            } else if (pTransaction != null) {
                showDialog(dialog, pTransaction);
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Произошла ошибка. Попробуйте позднее.", Toast.LENGTH_SHORT).show();
        }

    }

    private static void showDialog(VKShareDialog dialog, FragmentManager fm) {
        dialog.show(fm, "VK_SHARE_DIALOG");
    }

    private static void showDialog(VKShareDialog dialog, FragmentTransaction transaction) {
        dialog.show(transaction, "VK_SHARE_DIALOG");
    }
}
