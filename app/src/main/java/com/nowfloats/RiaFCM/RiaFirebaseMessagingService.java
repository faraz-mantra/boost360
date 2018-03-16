package com.nowfloats.RiaFCM;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.anachat.chatsdk.AnaCore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.nowfloats.managecustomers.FacebookChatDetailActivity;
import com.nowfloats.managecustomers.models.FacebookChatDataModel;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.thinksity.R;

import java.util.Map;

/**
 * Created by NowFloats on 05-10-2016.
 */

public class RiaFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    public static String deepLinkUrl;
    private SharedPreferences pref;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        Map<String,String> mapResult = remoteMessage.getData();
        if (mapResult.containsKey("payload")) {
            AnaCore.handlePush(this, mapResult.get("payload"));
        }else{

            sendNotification(mapResult);
            Constants.GCM_Msg = true;
        }


        Log.v("Message", "received bubble");
    }

    private static final String SAM_BUBBLE_MSG = "I have Got some data";
    private static final String SAM_BUBBLE_MSG_KEY = "100";

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(Map<String, String> message) {

        Log.d("Message", message.toString());

        if (message == null || message.size() == 0) {

        } else {
            if ((message.containsKey("mp_message") && message.get("mp_message").equalsIgnoreCase(SAM_BUBBLE_MSG))
                    || (message.containsKey("mp_message_key") && message.get("mp_message_key").equalsIgnoreCase(SAM_BUBBLE_MSG_KEY))) {
                /*MixPanelController.track(MixPanelController.SAM_BUBBLE_NOTIFICATION, null);
                pref.edit().putBoolean(Key_Preferences.HAS_SUGGESTIONS, true).apply();
                pref.edit().putBoolean(Key_Preferences.IS_CUSTOMER_ASSISTANT_ENABLED, true).apply();
                if (Methods.hasOverlayPerm(this)) {
                    if (!Methods.isMyServiceRunning(this, CustomerAssistantService.class)) {
                        Intent bubbleIntent = new Intent(this, CustomerAssistantService.class);
                        startService(bubbleIntent);
                    }
                }*/
                message.put("url","thirdPartyQueries");
                message.put("mp_message","You have new enquires from Third Party, check now.");
            }

            deepLinkUrl = message.get("url");
            if (deepLinkUrl != null && !deepLinkUrl.contains(Constants.PACKAGE_NAME)) {
                return;
            }
            if (Methods.isUserLoggedIn(this) && Methods.isMyAppOpen(this)) {
                MixPanelController.track("$campaign_received", null);
            }
            String title = message.get("title");
            Intent intent = null;
            if (!Util.isNullOrEmpty(deepLinkUrl)) {
                final PackageManager manager = getPackageManager();
                intent = manager.getLaunchIntentForPackage(getPackageName());
                if (intent == null) return;
                intent.putExtra("from", "notification");
                intent.putExtra("url", deepLinkUrl);
                if (deepLinkUrl.contains(getString(R.string.facebook_chat))) {
                    SharedPreferences pref = getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE);
                    pref.edit().putBoolean("IsNewFacebookMessage", true).apply();
                    intent.putExtra("user_data", message.get("user_data"));
                    Intent messageIntent = new Intent(FacebookChatDetailActivity.INTENT_FILTER);
                    messageIntent.putExtra("user_data", message.get("user_data"));
                    messageIntent.putExtra("message", message.get("message"));
                    if (LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)) {
                        if (!TextUtils.isEmpty(pref.getString("facebookChatUser", "")) &&
                                message.get("user_data").contains(pref.getString("facebookChatUser", ""))) {
                            return;
                        }
                    }

                }

            }
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.app_launcher2)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_launcher))
                    .setContentText(message.get("mp_message"))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setColor(ContextCompat.getColor(this, R.color.primaryColor))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message.get("mp_message")))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            if (!Util.isNullOrEmpty(title)) {
                notificationBuilder.setContentTitle(title);
            } else {
                notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
            }
            if (pendingIntent != null) {
                notificationBuilder.setContentIntent(pendingIntent);
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = null;
            if (notificationManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    channel = new NotificationChannel("0001", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                if (!Util.isNullOrEmpty(deepLinkUrl) && deepLinkUrl.contains(getString(R.string.facebook_chat))) {
                    FacebookChatDataModel.UserData data = new Gson().fromJson(message.get("user_data"), FacebookChatDataModel.UserData.class);
                    if (data.getId() != null) {
                        notificationManager.notify(data.getId().hashCode(), notificationBuilder.build());
                    }
                } else {
                    notificationManager.notify(0, notificationBuilder.build());
                }
            }
        }

    }
}
