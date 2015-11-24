package com.almadev.znaniesila.utils.social;

import android.app.Activity;
import android.support.v4.app.FragmentManager;

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

    public static void vkShare(Activity activity, FragmentManager fm, String text) {

        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(activity, sMyScope);
        }

        new VKShareDialog()
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
                })
                .show(fm, "VK_SHARE_DIALOG");
    }
}
