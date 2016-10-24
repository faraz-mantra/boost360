package com.nowfloats.BusinessProfile.UI.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NFXApi.NfxRequestClient;
import com.nowfloats.NavigationDrawer.API.FacebookFeedPullAutoPublishAsyncTask;
import com.nowfloats.NavigationDrawer.API.twitter.FacebookFeedPullRegistrationAsyncTask;
import com.nowfloats.NavigationDrawer.API.twitter.PrepareRequestTokenActivity;
import com.nowfloats.Twitter.ITwitterCallbacks;
import com.nowfloats.Twitter.TokenRequest;
import com.nowfloats.Twitter.TwitterAuthenticationActivity;
import com.nowfloats.Twitter.TwitterConstants;
import com.nowfloats.Twitter.Utils;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.BoostLog;
import com.nowfloats.util.Constants;
import com.nowfloats.util.DataBase;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.thinksity.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Social_Sharing_Activity extends AppCompatActivity implements ITwitterCallbacks, NfxRequestClient.NfxCallBackListener {
    private Toolbar toolbar;
    int size = 0;
    boolean[] checkedPages;
    UserSessionManager session;


    TextView connectTextView, autoPostTextView, topFeatureTextView;
    final Facebook facebook = new Facebook(Constants.FACEBOOK_API_KEY);
    private SharedPreferences pref = null;
    SharedPreferences.Editor prefsEditor;
    private ImageView facebookHome;
    private ImageView facebookPage;
    private ImageView twitter;
    private TextView facebookHomeStatus, facebookPageStatus, twitterStatus, fbPullStatus;
    private CheckBox facebookHomeCheckBox, facebookPageCheckBox, twitterCheckBox;
    private CheckBox facebookautopost;
    private TextView headerText;
    ArrayList<String> items;
    private int numberOfUpdates = 0;
    private boolean numberOfUpdatesSelected = false;
    private Activity activity;
    private MaterialDialog materialProgress;

    //Rahul Twitter

    //Variables are required to store twitter key and sec
    private String mConsumerKey = null;
    private String mConsumerSecret = null;
    private String mCallbackUrl = null;
    private String mAuthVerifier = null;
    private String mTwitterVerifier = null;
    private Twitter mTwitter = null;
    private RequestToken mRequestToken = null;
    private SharedPreferences mSharedPreferences = null;
    private boolean called = false;
    private ProgressDialog pd = null;


    //Rahul Twitter


    private final int FBTYPE = 0;
    private final int FBPAGETYPE = 1;
    private final int TWITTERTYPE = 2;
    private final int FB_DECTIVATION = 3;
    private final int FB_PAGE_DEACTIVATION = 4;
    private final int TWITTER_DEACTIVATION = 11;

    private final int FROM_AUTOPOST = 1;
    private final int FROM_FB_PAGE = 0;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_social_sharing);
        session = new UserSessionManager(getApplicationContext(), Social_Sharing_Activity.this);
        // Facebook_Auto_Publish_API.autoPublish(Social_Sharing_Activity.this,session.getFPID());
        Methods.isOnline(Social_Sharing_Activity.this);
        pref = this.getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE);
        prefsEditor = pref.edit();
        TwitterAuthenticationActivity.setListener(this);
        mSharedPreferences = this.getSharedPreferences(TwitterConstants.PREF_NAME,MODE_PRIVATE);
        activity = Social_Sharing_Activity.this;

        toolbar = (Toolbar) findViewById(R.id.app_bar_social);

        Typeface myCustomFont = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
        Typeface myCustomFont_Medium = Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf");

        setSupportActionBar(toolbar);
        headerText = (TextView) toolbar.findViewById(R.id.titleTextView);
        headerText.setText("Social Sharing");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        facebookHome = (ImageView) findViewById(R.id.social_sharing_facebook_profile_image);
        facebookPage = (ImageView) findViewById(R.id.social_sharing_facebook_page_image);
        twitter = (ImageView) findViewById(R.id.social_sharing_twitter_image);

        facebookHomeStatus = (TextView) findViewById(R.id.social_sharing_facebook_profile_flag_text);
        facebookPageStatus = (TextView) findViewById(R.id.social_sharing_facebook_page_flag_text);
        twitterStatus = (TextView) findViewById(R.id.social_sharing_twitter_flag_text);
        fbPullStatus = (TextView) findViewById(R.id.tv_fb_page_name);
        connectTextView = (TextView) findViewById(R.id.connectTextView);
        autoPostTextView = (TextView) findViewById(R.id.autoPostTextView);
        topFeatureTextView = (TextView) findViewById(R.id.topFeatureText);

        facebookHomeStatus.setTypeface(myCustomFont);
        facebookPageStatus.setTypeface(myCustomFont);
        twitterStatus.setTypeface(myCustomFont);
        fbPullStatus.setTypeface(myCustomFont);

        facebookHomeCheckBox = (CheckBox) findViewById(R.id.social_sharing_facebook_profile_checkbox);
        facebookPageCheckBox = (CheckBox) findViewById(R.id.social_sharing_facebook_page_checkbox);
        twitterCheckBox = (CheckBox) findViewById(R.id.social_sharing_twitter_checkbox);
        facebookautopost = (CheckBox) findViewById(R.id.social_sharing_facebook_page_auto_post);

        connectTextView.setTypeface(myCustomFont_Medium);
        autoPostTextView.setTypeface(myCustomFont);
        topFeatureTextView.setTypeface(myCustomFont_Medium);


        facebookPageCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebookPageCheckBox.isChecked()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fbPageData(FROM_FB_PAGE);
                        }
                    }, 200);

                } else {
                    NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("FACEBOOKPAGE")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmUserAccountId("")
                            .setmCallType(FB_PAGE_DEACTIVATION)
                            .setmName("");
                    requestClient.connectNfx();

                    pd = ProgressDialog.show(Social_Sharing_Activity.this, "", getString(R.string.wait_while_unsubscribing));
                }
            }
        });

        facebookHomeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebookHomeCheckBox.isChecked()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            fbData();
                        }
                    }, 200);
                } else {

                    NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("FACEBOOK")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmUserAccountId("")
                            .setmCallType(FB_DECTIVATION)
                            .setmName("");
                    requestClient.connectNfx();

                    pd = ProgressDialog.show(Social_Sharing_Activity.this, "", getString(R.string.wait_while_unsubscribing));

                }
            }
        });

        facebookautopost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (facebookautopost.isChecked()) {

                    if (session.getShowUpdates() && !Util.isNullOrEmpty(Constants.fbPageFullUrl))
                        selectNumberUpdatesDialog();
                    if(!called){
                        autoPostSelectListener();
                    }


                } else {
                    session.setShowUpdates(false);
                    final JSONObject obj = new JSONObject();
                    try {
                        obj.put("fpId", session.getFPID());
                        obj.put("autoPublish", false);
                        obj.put("clientId", Constants.clientId);
                        obj.put("FacebookPageName", Constants.fbFromWhichPage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FacebookFeedPullAutoPublishAsyncTask fap = new FacebookFeedPullAutoPublishAsyncTask(Social_Sharing_Activity.this, obj, false, facebookPageStatus);
                    fap.execute();


                }*/
                if(facebookautopost.isChecked()){
                    fbPageData(FROM_AUTOPOST);
                }else {
                    numberOfUpdatesSelected = false;
                    final JSONObject obj = new JSONObject();
                    try {
                        obj.put("fpId", session.getFPID());
                        obj.put("autoPublish", false);
                        obj.put("clientId", Constants.clientId);
                        obj.put("FacebookPageName", session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FacebookFeedPullAutoPublishAsyncTask fap = new FacebookFeedPullAutoPublishAsyncTask(Social_Sharing_Activity.this, obj, false, fbPullStatus, session);
                    fap.execute();
                }

            }
        });

        twitterCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (twitterCheckBox.isChecked()) {
                    /*twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_active));
                    twitterStatus.setText("Connected");
                    twitterCheckBox.setHighlightColor(getResources().getColor(R.color.primaryColor));
                    Constants.twitterShareEnabled = true;
                    MixPanelController.track(EventKeysWL.CREATE_MESSAGE_ACTIVITY_TWITTER, null);*/
                    //Rahul twitter
                    if (!Utils.isNetworkConnected(Social_Sharing_Activity.this)) {
                        showAlertBox();
                    } else {
                        mConsumerKey = getApplicationContext().getResources().getString(R.string.twitter_consumer_key);
                        mConsumerSecret = getApplicationContext().getResources().getString(R.string.twitter_consumer_secret);
                        mAuthVerifier = "oauth_verifier";
                        final ConfigurationBuilder builder = new ConfigurationBuilder();
                        builder.setOAuthConsumerKey(mConsumerKey);
                        builder.setOAuthConsumerSecret(mConsumerSecret);
                        final Configuration configuration = builder.build();
                        final TwitterFactory factory = new TwitterFactory(configuration);
                        mTwitter = factory.getInstance();
                        new TokenRequest(Social_Sharing_Activity.this).execute();
                        initTwitterSDK(Social_Sharing_Activity.this);
                    }
                    //Rahul twitter
                }else {
                    NfxRequestClient requestClient1 = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("TWITTER")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmUserAccountId(String.valueOf(""))
                            .setmCallType(TWITTER_DEACTIVATION)
                            .setmName("");
                    requestClient1.connectNfx();
                    pd = ProgressDialog.show(Social_Sharing_Activity.this, "", getString(R.string.wait_while_unsubscribing));

                }
            }
        });

        /*findViewById(R.id.iv_help_nfx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "You need to have an account on the social platforms you select.";
                showDialog(message);
            }
        });
        findViewById(R.id.iv_help_auto_pull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Updates will reflect on your website one hour after getting posted on the Facebook Page. Do not select this option if you are using social share from your website.";
                showDialog(message);
            }
        });*/
        InitShareResources();
    }

    private void autoPostSelectListener(String pageName) {
        /*called = true;
        boolean FbRegistered = pref.getBoolean("FacebookFeedRegd", false);
        if (FbRegistered == false) {
            if (!Util.isNullOrEmpty(Constants.fbPageFullUrl)) {
                pullFacebookFeedDialog();
            } else {
                Util.toast("Please select a Facebook page", getApplicationContext());
                facebookautopost.setChecked(false);
            }
        } else {
            final JSONObject obj = new JSONObject();
            try {
                obj.put("fpId", session.getFPID());
                obj.put("autoPublish", true);
                obj.put("clientId", Constants.clientId);
                obj.put("FacebookPageName", Constants.fbFromWhichPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FacebookFeedPullAutoPublishAsyncTask fap = new FacebookFeedPullAutoPublishAsyncTask(Social_Sharing_Activity.this, obj, true, facebookPageStatus);
            fap.execute();
        }*/
        if(numberOfUpdatesSelected){
            final JSONObject obj = new JSONObject();
            try {
                obj.put("Tag", session.getFpTag());
                obj.put("AutoPublish",true);
                obj.put("ClientId", Constants.clientId);
                obj.put("FacebookPageName", pageName);
                obj.put("IsEnterpriseUpdate",true);
                obj.put("Count", numberOfUpdates);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FacebookFeedPullRegistrationAsyncTask fap = new FacebookFeedPullRegistrationAsyncTask(Social_Sharing_Activity.this, obj, fbPullStatus, facebookautopost, session);
            fap.execute();
        }else {
            facebookautopost.setChecked(false);
        }
    }


    private void selectNumberUpdatesDialog(final String name) {
        final String[] array = getResources().getStringArray(R.array.post_updates);
        new MaterialDialog.Builder(Social_Sharing_Activity.this)
                .title(getString(R.string.post_on_facebook))
                .items(array)
                .negativeText(getString(R.string.cancel))
                .negativeColorRes(R.color.light_gray)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .widgetColorRes(R.color.primaryColor)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {
                        numberOfUpdatesSelected = true;
                        //session.storeShowUpdates(false);
                        if (position == 0) {
                            numberOfUpdates = 5;
                        }

                        if (position == 1) {
                            numberOfUpdates = 10;
                        }
                        autoPostSelectListener(name);
                        dialog.dismiss();
                        return true;
                    }
                }).show();
    }

    @Override
    public void returnToken(Intent data) {
        if (materialProgress != null) {
            materialProgress.dismiss();
        }
        if (data != null) {
            mTwitterVerifier = data.getExtras().getString(mAuthVerifier);
            AccessToken accessToken;
            try {
                if (mTwitter == null) {
                    final ConfigurationBuilder builder = new ConfigurationBuilder();
                    builder.setOAuthConsumerKey(mConsumerKey);
                    builder.setOAuthConsumerSecret(mConsumerSecret);
                    final Configuration configuration = builder.build();
                    final TwitterFactory factory = new TwitterFactory(configuration);
                    mTwitter = factory.getInstance();
                }
                accessToken = mTwitter.getOAuthAccessToken(mRequestToken, mTwitterVerifier);
                long userID = accessToken.getUserId();
                final User user = mTwitter.showUser(userID);
                String username = user.getName();
                twitterStatus.setText(username);
                saveTwitterInformation(accessToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(Social_Sharing_Activity.this, getString(R.string.problem_with_twitter_try_later), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
        if (materialProgress != null) {
            materialProgress.dismiss();
        }
    }

    public void fbPageData(final int from) {
        final String[] PERMISSIONS = new String[]{"photo_upload",
                "user_photos", "publish_stream", "read_stream",
                "offline_access", "manage_pages", "publish_actions"};

        facebook.authorize(this, PERMISSIONS, new Facebook.DialogListener() {
            public void onComplete(Bundle values) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject pageMe = new JSONObject(facebook.request("me/accounts"));
                            Constants.FbPageList = pageMe.getJSONArray("data");
                            if (Constants.FbPageList != null) {
                                size = Constants.FbPageList.length();

                                checkedPages = new boolean[size];
                                if (size > 0) {
                                    items = new ArrayList<String>();
                                    for (int i = 0; i < size; i++) {
                                        items.add(i, (String) ((JSONObject) Constants.FbPageList
                                                .get(i)).get("name"));
                                        //BoostLog.d("ILUD Test: ", (String) ((JSONObject) Constants.FbPageList
                                        //.get(i)).get("name"));
                                    }

                                    for (int i = 0; i < size; i++) {
                                        checkedPages[i] = false;
                                    }


                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        } finally {
                            Social_Sharing_Activity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (items != null && items.size() > 0) {
                                        final String[] array = items.toArray(new String[items.size()]);
                                        new MaterialDialog.Builder(Social_Sharing_Activity.this)
                                                .title(getString(R.string.select_page))
                                                .items(array)
                                                .widgetColorRes(R.color.primaryColor)
                                                .cancelable(false)
                                                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                                    @Override
                                                    public boolean onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {
                                                        String strName = array[position];
                                                        String FACEBOOK_PAGE_ID = null;
                                                        String page_access_token = null;
                                                        try {
                                                            FACEBOOK_PAGE_ID = (String) ((JSONObject) Constants.FbPageList.get(position)).get("id");
                                                            page_access_token = ((String) ((JSONObject) Constants.FbPageList.get(position)).get("access_token"));
                                                        }catch (JSONException e){

                                                        }
                                                        if(from==FROM_FB_PAGE && !Util.isNullOrEmpty(FACEBOOK_PAGE_ID) && !Util.isNullOrEmpty(page_access_token)) {
                                                            session.storePageAccessToken(page_access_token);
                                                            session.storeFacebookPageID(FACEBOOK_PAGE_ID);
                                                            pageSeleted(position, strName, session.getFacebookPageID(), session.getPageAccessToken());
                                                        }else if(from==FROM_AUTOPOST){
                                                            selectNumberUpdatesDialog(strName);
                                                        }
                                                        dialog.dismiss();
                                                        return true;
                                                    }
                                                }).show();
                                    } else {
                                        Methods.materialDialog(activity, "Uh oh~", getString(R.string.look_like_no_facebook_page));
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }

            @Override
            public void onCancel() {
                onFBPageError(from);
            }

            @Override
            public void onFacebookError(FacebookError e) {
                onFBPageError(from);
            }

            @Override
            public void onError(DialogError e) {
                onFBPageError(from);
            }


        });
    }

    public void pageSeleted(int id, final String pageName, String pageID, String pageAccessToken) {
        String s = "";
        JSONObject obj;
        session.storeFacebookPage(pageName);
        JSONArray data = new JSONArray();

        NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                .setmFpId(session.getFPID())
                .setmType("FACEBOOKPAGE")
                .setmUserAccessTokenKey(pageAccessToken)
                .setmUserAccessTokenSecret("null")
                .setmUserAccountId(pageID)
                .setmCallType(FBPAGETYPE)
                .setmName(pageName);
        requestClient.connectNfx();

        pd = ProgressDialog.show(this, "", getString(R.string.wait_while_subscribing));

        DataBase dataBase = new DataBase(activity);
        dataBase.updateFacebookPage(pageName, pageID, pageAccessToken);

        obj = new JSONObject();
        try {
            obj.put("id", pageID);
            obj.put("access_token", pageAccessToken);
            data.put(obj);

            Constants.fbPageFullUrl = "https://www.facebook.com/pages/" + pageName + "/" + pageID;
            Constants.fbFromWhichPage = pageName;
            prefsEditor.putString("fbPageFullUrl",
                    Constants.fbPageFullUrl);
            prefsEditor.putString("fbFromWhichPage",
                    Constants.fbFromWhichPage);
            prefsEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        obj = new JSONObject();
        try {
            obj.put("data", data);
            Constants.FbPageList = data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String fbPageData = obj.toString();
        if (!Util.isNullOrEmpty(fbPageData)) {
            if (fbPageData.equals("{\"data\":[]}")) {
                prefsEditor.putString("fbPageData", "");
                Constants.fbPageShareEnabled = false;
                prefsEditor.putBoolean("fbPageShareEnabled",
                        Constants.fbPageShareEnabled);
                prefsEditor.commit();
                Constants.FbPageList = null;
                //InitShareResources();

            } else {
                Constants.fbPageShareEnabled = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_social__sharing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        return super.onOptionsItemSelected(item);
    }

    public void fbData() {
        final String[] PERMISSIONS = new String[]{"photo_upload",
                "user_photos", "publish_stream", "read_stream",
                "offline_access", "publish_actions"};
//        materialProgress = new MaterialDialog.Builder(this)
//                .widgetColorRes(R.color.accentColor)
//                .content("Please Wait...")
//                .progress(true, 0).show();
        facebook.authorize(this, PERMISSIONS, new Facebook.DialogListener() {

            public void onComplete(Bundle values) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject me;
                        try {
                            me = new JSONObject(facebook.request("me"));
                            Constants.FACEBOOK_USER_ACCESS_ID = facebook.getAccessToken();
                            Constants.FACEBOOK_USER_ID = (String) me.getString("id");



                            String FACEBOOK_ACCESS_TOKEN = facebook.getAccessToken();
                            String FACEBOOK_USER_NAME = (String) me.getString("name");

                            //BoostLog.d("FB token and Id:", FACEBOOK_ACCESS_TOKEN + "    " + Constants.FACEBOOK_USER_ID);

                            NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                                    .setmFpId(session.getFPID())
                                    .setmType("FACEBOOK")
                                    .setmUserAccessTokenKey(facebook.getAccessToken())
                                    .setmUserAccessTokenSecret("null")
                                    .setmUserAccountId(me.getString("id"))
                                    .setmCallType(FBTYPE)
                                    .setmName(FACEBOOK_USER_NAME);
                            requestClient.connectNfx();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pd = ProgressDialog.show(Social_Sharing_Activity.this, "", "Wait While Subscribing...");
                                }
                            });

                            BoostLog.d("FPID: ", session.getFPID());

                            session.storeFacebookName(FACEBOOK_USER_NAME);
                            session.storeFacebookAccessToken(FACEBOOK_ACCESS_TOKEN);
                            DataBase dataBase = new DataBase(activity);
                            dataBase.updateFacebookNameandToken(FACEBOOK_USER_NAME, FACEBOOK_ACCESS_TOKEN);

//                            try {
//
//                                // code runs in a thread
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        facebookHome.setImageDrawable(getResources().getDrawable(R.drawable.facebook_icon));
//                                        facebookHomeCheckBox.setChecked(true);
//                                        facebookHomeStatus.setText(Constants.FACEBOOK_USER_NAME);
//                                    }
//                                });
//                            } catch (final Exception ex) {
//                                BoostLog.i("---", "Exception in thread");
//                            }


                            //      facebookHomeStatus.setText(Constants.FACEBOOK_USER_NAME);
                            prefsEditor.putString("fbId", Constants.FACEBOOK_USER_ID);
                            prefsEditor.putString("fbAccessId", Constants.FACEBOOK_USER_ACCESS_ID);
                            prefsEditor.putString("fbUserName", FACEBOOK_USER_NAME);
                            prefsEditor.commit();

                        } catch (Exception e1) {
                            e1.printStackTrace();

                        }

//                        try {
//
//                                // code runs in a thread
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        facebookHome.setImageDrawable(getResources().getDrawable(
//                                                R.drawable.facebook_icon));
//                                        facebookHomeCheckBox.setChecked(true);
//                                        facebookHomeStatus.setText(Constants.FACEBOOK_USER_NAME);
//                                    }
//                                });
//                            } catch (final Exception ex) {
//                                BoostLog.i("---", "Exception in thread");
//                            }
//

                    }

                }).start();


            }

            @Override
            public void onCancel() {
                onFBError();
            }

            @Override
            public void onFacebookError(FacebookError e) {
                onFBError();
            }

            @Override
            public void onError(DialogError e) {
                onFBError();
            }

        });


//        facebookHome.setImageDrawable(getResources().getDrawable(
//                R.drawable.facebook_icon));
//        facebookHomeCheckBox.setChecked(true);
//        facebookHomeStatus.setText(Constants.FACEBOOK_USER_NAME);
    }

    void onFBError() {
        Constants.fbShareEnabled = false;
        prefsEditor.putBoolean("fbShareEnabled", false);
        prefsEditor.commit();
    }

    void onFBPageError(int from) {
        Constants.fbPageShareEnabled = false;
        prefsEditor.putBoolean("fbPageShareEnabled", false);
        prefsEditor.commit();
        if(from==FROM_AUTOPOST){
            facebookautopost.setChecked(false);
        }else if(from==FROM_FB_PAGE){
            facebookPageCheckBox.setChecked(false);
        }

    }


    public void twitterData() {
        Intent it = new Intent(this, PrepareRequestTokenActivity.class);
        startActivity(it);
    }


    public void InitShareResources() {
        Constants.FACEBOOK_USER_ID = pref.getString("fbId", "");
        Constants.FACEBOOK_USER_ACCESS_ID = pref.getString("fbAccessId", "");
        Constants.fbShareEnabled = pref.getBoolean("fbShareEnabled", false);
//        Constants.FACEBOOK_PAGE_ID 			= pref.getString("fbPageId", "");
        Constants.FACEBOOK_PAGE_ACCESS_ID = pref.getString("fbPageAccessId", "");
        Constants.fbPageShareEnabled = pref.getBoolean("fbPageShareEnabled", false);
        Constants.twitterShareEnabled = pref.getBoolean("twitterShareEnabled", false);
        Constants.TWITTER_TOK = pref.getString(OAuth.OAUTH_TOKEN, "");
        Constants.TWITTER_SEC = pref.getString(OAuth.OAUTH_TOKEN_SECRET, "");
        Constants.FbFeedPullAutoPublish = pref.getBoolean("FBFeedPullAutoPublish", false);
        Constants.fbPageFullUrl = pref.getString("fbPageFullUrl", "");
        Constants.fbFromWhichPage = pref.getString("fbFromWhichPage", "");

        if(session.getFPDetails(Key_Preferences.FB_PULL_ENABLED).equals("true")){
            facebookautopost.setChecked(true);
            fbPullStatus.setVisibility(View.VISIBLE);
            fbPullStatus.setText(getString(R.string.subscribe_for_pulling) + session.getFPDetails(Key_Preferences.FB_PULL_COUNT) + getString(R.string.update_from) + session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME) +getString(R.string.facebook_page));
        }



        if (!Util.isNullOrEmpty(Constants.FACEBOOK_USER_ACCESS_ID)) {
            facebookHome.setImageDrawable(getResources().getDrawable(R.drawable.facebook_icon));
            facebookHomeStatus.setText(getString(R.string.connected));
            String fbUName = pref.getString("fbUserName", "");
            prefsEditor.putBoolean("fbShareEnabled", true);
            //   facebookHomeCheckBox.setChecked(true);
            prefsEditor.commit();
        }

    }


    public void createFacebookAutoPost() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("ClientId", Constants.clientId);
            obj.put("Count", 5);
            obj.put("Tag", session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG));
            obj.put("FacebookPageName", Constants.fbFromWhichPage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*public void pullFacebookFeedDialog() {

        final JSONObject obj = new JSONObject();
        try {
            obj.put("ClientId", Constants.clientId);
            obj.put("Count", 5);
            obj.put("Tag", session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG));
            obj.put("FacebookPageName", Constants.fbPageFullUrl);
            obj.put("AutoPublish", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FacebookFeedPullRegistrationAsyncTask fpa = new FacebookFeedPullRegistrationAsyncTask(Social_Sharing_Activity.this, obj, facebookPageStatus, facebookautopost);
        fpa.execute();

    }*/


    @Override
    protected void onResume() {
        super.onResume();
        Methods.isOnline(Social_Sharing_Activity.this);
        facebookHome.setImageDrawable(getResources().getDrawable(R.drawable.facebook_icon_inactive));
        facebookHomeCheckBox.setChecked(false);
        facebookPageStatus.setText(getString(R.string.disconnected));

        facebookPage.setImageDrawable(getResources().getDrawable(R.drawable.facebookpage_icon_inactive));
        facebookPageCheckBox.setChecked(false);
        facebookHomeStatus.setText(getString(R.string.disconnected));

        if (!Util.isNullOrEmpty(session.getFacebookName())) {
            facebookHome.setImageDrawable(getResources().getDrawable(R.drawable.facebook_icon));
            facebookHomeCheckBox.setChecked(true);
            facebookHomeStatus.setText(session.getFacebookName());

        }
        if (!Util.isNullOrEmpty(session.getFacebookPage())) {
            facebookPage.setImageDrawable(getResources().getDrawable(R.drawable.facebook_page));
            facebookPageCheckBox.setChecked(true);
            String text = session.getFacebookPage();
            facebookPageStatus.setText(session.getFacebookPage());
        }
        if (!isAuthenticated()) {
            //twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_inactive));
            // String fbUName = pref.getString(TwitterConstants.PREF_USER_NAME, "");
            twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_inactive));
            twitterCheckBox.setChecked(false);
            twitterStatus.setText(getString(R.string.disconnected));
        } else {
            twitterCheckBox.setChecked(true);
            String twitterName = mSharedPreferences.getString(TwitterConstants.PREF_USER_NAME, "");
            twitterStatus.setText("@" + twitterName);
            twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_active));
        }
    }



    //Rahul Twitter handling
    private void initTwitterSDK(Context context) { // it is fixed for user
        // check whether this could be changed or not
        /*If key and secret key are null ,then it not possbile to communicate with twitter*/
        if (TextUtils.isEmpty(mConsumerKey) || TextUtils.isEmpty(mConsumerSecret)) {
            return;
        }

        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(mCallbackUrl)) {
            String verifier = uri.getQueryParameter(mAuthVerifier);
            try {
                AccessToken accessToken = mTwitter.getOAuthAccessToken(
                        mRequestToken, verifier);
                //send twitter info
                saveTwitterInformation(accessToken);
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                BoostLog.d("Failed to login ",
                        e.getMessage());
            }

        }
    }
    //check about aleady authenticated
    protected boolean isAuthenticated() {
        return mSharedPreferences.getBoolean(TwitterConstants.PREF_KEY_TWITTER_LOGIN, false);
    }
    private void saveTwitterInformation(AccessToken accessToken) {
        {
            long userID = accessToken.getUserId();
            User user;
            try {
                user = mTwitter.showUser(userID);
                String username = user.getName();

                NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                        .setmFpId(session.getFPID())
                        .setmType("TWITTER")
                        .setmUserAccessTokenKey(accessToken.getToken())
                        .setmUserAccessTokenSecret(accessToken.getTokenSecret())
                        .setmUserAccountId(String.valueOf(userID))
                        .setmCallType(TWITTERTYPE)
                        .setmName(username);
                requestClient.connectNfx();

                pd = ProgressDialog.show(this, "", getString(R.string.wait_while_subscribing));

                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putString(TwitterConstants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                e.putString(TwitterConstants.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
                //e.putBoolean(TwitterConstants.PREF_KEY_TWITTER_LOGIN, true);
                e.putString(TwitterConstants.PREF_USER_NAME, username);
                e.commit();

            } catch (TwitterException e1) {
                BoostLog.d("Failed to Save", e1.getMessage());
            }
        }
    }
    public  void logoutFromTwitter() {
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.remove(TwitterConstants.PREF_KEY_OAUTH_TOKEN);
        e.remove(TwitterConstants.PREF_KEY_OAUTH_SECRET);
        e.remove(TwitterConstants.PREF_KEY_TWITTER_LOGIN);
        e.remove(TwitterConstants.PREF_USER_NAME);
        e.commit();
        Constants.twitterShareEnabled = false;
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }
    @Override
    public void startWebAuthentication(String url, RequestToken requestToken) {
        mRequestToken = requestToken;
        final Intent intent = new Intent(this,
                com.nowfloats.Twitter.TwitterAuthenticationActivity.class);
        intent.putExtra(com.nowfloats.Twitter.TwitterAuthenticationActivity.EXTRA_URL, url);
        startActivityForResult(intent, TwitterConstants.WEBVIEW_REQUEST_CODE);
    }
    private void showAlertBox() {
        AlertDialog malertDialog = null;
        AlertDialog.Builder mdialogBuilder = null;
        if (mdialogBuilder == null) {
            mdialogBuilder = new AlertDialog.Builder(Social_Sharing_Activity.this);
            mdialogBuilder.setTitle(getString(R.string.alert));
            mdialogBuilder.setMessage(getString(R.string.no_network));

            mdialogBuilder.setPositiveButton(getString(R.string.enable),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // launch setting Activity
                            startActivityForResult(new Intent(
                                            android.provider.Settings.ACTION_SETTINGS),
                                    0);
                        }
                    });

            mdialogBuilder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);

            if (malertDialog == null) {
                malertDialog = mdialogBuilder.create();
                malertDialog.show();
            }

        }

    }

    private void showDialog(String message){
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }




    /*
     * This callback is called from NfxClient when there is a successfull or failure post
     * to the api for NFX. Cases are there to determine the type of call.
     */
    @Override
    public void nfxCallBack(String response, int callType, String name) {
        if(pd!=null){
            pd.dismiss();
        }
        if(response.equals("error")){
            Toast.makeText(this, "Something went wrong!!! Please try later.", Toast.LENGTH_SHORT).show();
            return;
        }
        BoostLog.d("Nfxresponse: ", response + callType + ":");
        switch (callType){
            case FBTYPE:
                facebookHome.setImageDrawable(getResources().getDrawable(R.drawable.facebook_icon));
                facebookHomeCheckBox.setChecked(true);
                facebookHomeStatus.setText(name);
                Constants.fbShareEnabled = true;
                prefsEditor = pref.edit();
                prefsEditor.putBoolean("fbShareEnabled", true);
                prefsEditor.commit();
                break;
            case FBPAGETYPE:
                facebookPage.setImageDrawable(getResources().getDrawable(R.drawable.facebook_page));
                facebookPageStatus.setText("" + name);
                facebookPageCheckBox.setChecked(true);
                break;
            case TWITTERTYPE:
                Constants.twitterShareEnabled = true;
                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putBoolean(TwitterConstants.PREF_KEY_TWITTER_LOGIN, true);
                e.commit();
                twitterStatus.setText("@" + name);
                twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_active));
                //twitterStatus.setText("Connected");
                twitterCheckBox.setHighlightColor(getResources().getColor(R.color.primaryColor));
                twitterCheckBox.setChecked(true);
                //Constants.twitterShareEnabled = true;
                MixPanelController.track(EventKeysWL.CREATE_MESSAGE_ACTIVITY_TWITTER, null);
                break;
            case FB_PAGE_DEACTIVATION:
                DataBase dataBase = new DataBase(activity);
                dataBase.updateFacebookPage("", "", "");
                session.storeFacebookPage("");
                session.storeFacebookPageID("");
                session.storeFacebookAccessToken("");
                facebookPage.setImageDrawable(getResources().getDrawable(R.drawable.facebookpage_icon_inactive));
                facebookPageStatus.setText("Disconnected");
                prefsEditor = pref.edit();
                prefsEditor.putBoolean("fbPageShareEnabled", false);
                break;
            case FB_DECTIVATION:
                DataBase fb_dataBase = new DataBase(activity);
                fb_dataBase.updateFacebookNameandToken("", "");
                session.storeFacebookName("");
                session.storeFacebookAccessToken("");
                facebookHome.setImageDrawable(getResources().getDrawable(R.drawable.facebook_icon_inactive));
                facebookHomeStatus.setText("Disconnected");
                prefsEditor = pref.edit();
                prefsEditor.putBoolean("fbShareEnabled", false);
                break;
            case TWITTER_DEACTIVATION:
                BoostLog.d("Oh God:", "This is getting Called");
                twitterStatus.setText("Disconnected");
                twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_inactive));
                logoutFromTwitter();
                twitterCheckBox.setChecked(false);
                break;

        }
    }
}
