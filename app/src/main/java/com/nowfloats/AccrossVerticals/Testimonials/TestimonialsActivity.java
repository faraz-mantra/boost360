package com.nowfloats.AccrossVerticals.Testimonials;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dashboard.utils.CodeUtilsKt;
import com.framework.utils.ContentSharing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nowfloats.AccrossVerticals.API.APIInterfaces;
import com.nowfloats.AccrossVerticals.API.model.DeleteTestimonials.DeleteTestimonialsData;
import com.nowfloats.AccrossVerticals.API.model.GetTestimonials.GetTestimonialData;
import com.nowfloats.AccrossVerticals.API.model.GetTestimonials.TestimonialData;
import com.nowfloats.AccrossVerticals.API.model.GetToken.GetTokenData;
import com.nowfloats.AccrossVerticals.API.model.GetToken.WebActionsItem;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.thinksity.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class TestimonialsActivity extends AppCompatActivity implements TestimonialsListener {

    TextView addTestimonialsButton;
    ProgressDialog vmnProgressBar;
    List<TestimonialData> dataList = new ArrayList<>();
    LinearLayout rightButton, backButton;
    ImageView rightIcon;
    TextView title;
    private String headerToken = "59c89bbb5d64370a04c9aea1";
    private String testimonialType = "testimonials";
    public static List<String> allTestimonialType = Arrays.asList("testimonials", "testimonial", "guestreviews");
    private LinearLayout mainLayout, secondaryLayout;
    private com.framework.pref.UserSessionManager session;
    private TestimonialsAdapter testimonialsAdapter;
    private RecyclerView recyclerView;
    private boolean isLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimonials);
        session = new com.framework.pref.UserSessionManager(this);
        setHeader();
        initialization();
        checkIsAdd();
        Log.v("experincecode", " themeID: " + session.getFPDetails(Key_Preferences.GET_FP_WEBTEMPLATE_ID) + " FpTag: " + session.getFpTag() + " exp: " + session.getFPDetails(Key_Preferences.GET_FP_EXPERIENCE_CODE));
    }

    private void checkIsAdd() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean isAdd = bundle.getBoolean("IS_ADD");
            if (isAdd) {
                Intent intent = new Intent(getApplicationContext(), TestimonialsFeedbackActivity.class);
                intent.putExtra("ScreenState", "new");
                startActivityForResult(intent, 202);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getHeaderAuthToken();
    }

    void initialization() {
        vmnProgressBar = new ProgressDialog(this);
        vmnProgressBar.setIndeterminate(true);
        vmnProgressBar.setMessage(getString(R.string.please_wait));
        vmnProgressBar.setCancelable(false);

        addTestimonialsButton = findViewById(R.id.add_testimonials);
        recyclerView = findViewById(R.id.testimonials_recycler);
        testimonialsAdapter = new TestimonialsAdapter(new ArrayList(), this, session, this);
        initialiseRecycler();

        //show or hide if feature is available to user
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        secondaryLayout = (LinearLayout) findViewById(R.id.secondary_layout);

        mainLayout.setOnClickListener(v -> updateRecyclerMenuOption(-1, false));

        addTestimonialsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TestimonialsFeedbackActivity.class);
            intent.putExtra("ScreenState", "new");
            startActivity(intent);
        });
    }

    public void setHeader() {
        title = findViewById(R.id.title);
        backButton = findViewById(R.id.back_button);
        rightButton = findViewById(R.id.right_icon_layout);
        rightIcon = findViewById(R.id.right_icon);
        title.setText("Testimonials");
        rightIcon.setImageResource(R.drawable.ic_add_white);
        rightButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TestimonialsFeedbackActivity.class);
            intent.putExtra("ScreenState", "new");
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void initialiseRecycler() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(testimonialsAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    void loadData() {
        try {
            if (!isLoad) showProgress();
            JSONObject query = new JSONObject();
            query.put("WebsiteId", session.getFpTag());
            APIInterfaces APICalls = new RestAdapter.Builder()
                    .setEndpoint("https://webaction.api.boostkit.dev")
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog("ggg"))
                    .build()
                    .create(APIInterfaces.class);
            Log.v("headerToken", " " + headerToken);
            APICalls.getTestimonialsList(headerToken, testimonialType, query, 0, 1000, new Callback<GetTestimonialData>() {
                @Override
                public void success(GetTestimonialData testimonialModel, Response response) {
                    hideProgress();
                    if (testimonialModel == null || response.getStatus() != 200) {
                        Toast.makeText(getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (testimonialModel.getData().size() > 0) {
                        isLoad = true;
                        dataList = testimonialModel.getData();
                        updateRecyclerView();
                        mainLayout.setVisibility(View.VISIBLE);
                        secondaryLayout.setVisibility(View.GONE);
                        rightButton.setVisibility(View.VISIBLE);
                    } else {
                        mainLayout.setVisibility(View.GONE);
                        secondaryLayout.setVisibility(View.VISIBLE);
                        rightButton.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    hideProgress();
                    Methods.showSnackBarNegative(TestimonialsActivity.this, getString(R.string.something_went_wrong));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateRecyclerView() {
        testimonialsAdapter.updateList(dataList);
        testimonialsAdapter.notifyDataSetChanged();
    }


    @Override
    public void itemMenuOptionStatus(int pos, boolean status) {
        updateRecyclerMenuOption(pos, status);
    }

    @Override
    public void editOptionClicked(TestimonialData data) {
        Intent intent = new Intent(getApplicationContext(), TestimonialsFeedbackActivity.class);
        intent.putExtra("ScreenState", "edit");
        intent.putExtra("data", new Gson().toJson(data));
        startActivity(intent);
    }

    @Override
    public void deleteOptionClicked(TestimonialData data) {
        try {
            DeleteTestimonialsData requestBody = new DeleteTestimonialsData();
            requestBody.setQuery("{_id:'" + data.getId() + "'}");
            requestBody.setUpdateValue("{$set : {IsArchived: true }}");
            requestBody.setMulti(true);

            APIInterfaces APICalls = new RestAdapter.Builder()
                    .setEndpoint("https://webaction.api.boostkit.dev")
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog("ggg"))
                    .setConverter(new GsonConverter(new GsonBuilder().setLenient().create()))
                    .build()
                    .create(APIInterfaces.class);

            APICalls.deleteTestimonials(headerToken, testimonialType, requestBody, new Callback<String>() {
                @Override
                public void success(String data, Response response) {
                    if (response != null && response.getStatus() == 200) {
                        Log.d("deleteTestimonials ->", response.getBody().toString());
                        getHeaderAuthToken();
                        Toast.makeText(getApplicationContext(), "Successfully Deleted.", Toast.LENGTH_LONG).show();
                    } else {
                        Methods.showSnackBarNegative(TestimonialsActivity.this, getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (error != null && error.getResponse() != null && error.getResponse().getStatus() == 200) {
                        getHeaderAuthToken();
                        Toast.makeText(getApplicationContext(), "Successfully Deleted.", Toast.LENGTH_LONG).show();
                    } else {
                        Methods.showSnackBarNegative(TestimonialsActivity.this, getString(R.string.something_went_wrong));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shareOptionClicked(TestimonialData data) {
        String productType = CodeUtilsKt.getProductType(session.getFP_AppExperienceCode());
        String subDomain = "";
        if (productType.equals("PRODUCTS")) {
            subDomain = "all-products";
        } else {
            subDomain = "all-services";
        }
        ContentSharing.Companion.shareTestimonial(this,data.getDescription(), data.getUsername(), session.getRootAliasURI() + "/testimonials", session.getRootAliasURI() + "/"+subDomain, session.getFPPrimaryContactNumber(), false);
    }

    void updateRecyclerMenuOption(int pos, boolean status) {
        testimonialsAdapter.menuOption(pos, status);
        testimonialsAdapter.notifyDataSetChanged();
    }

    private void showProgress() {
        if (!vmnProgressBar.isShowing() && !isFinishing()) {
            vmnProgressBar.show();
        }
    }

    private void hideProgress() {
        if (vmnProgressBar.isShowing() && !isFinishing()) {
            vmnProgressBar.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 202) {
            if (!(data != null && data.getBooleanExtra("IS_REFRESH", false))) onBackPressed();
        }
    }

    private void getHeaderAuthToken() {
        try {
            APIInterfaces APICalls = new RestAdapter.Builder().setEndpoint("https://developer.api.boostkit.dev")
                    .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("ggg"))
                    .build().create(APIInterfaces.class);
            Log.v("newvlue", " " + session.getFPDetails(Key_Preferences.GET_FP_WEBTEMPLATE_ID) + " " + session.getFpTag());
            APICalls.getHeaderAuthorizationtoken(session.getFPDetails(Key_Preferences.GET_FP_WEBTEMPLATE_ID), session.getFpTag(), new Callback<GetTokenData>() {
                @Override
                public void success(GetTokenData s, Response response) {
                    Log.v("experincecode1", " " + s.getToken());
                    int status = response.getStatus();
                    if ((status == 200 || status == 201 || status == 202) && s != null) {
                        Log.v("experincecode", " " + session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY) + " headerToken: " + headerToken);

                        if (s.getWebActions() != null && !s.getWebActions().isEmpty()) {
                            loopBreak:
                            for (WebActionsItem action : s.getWebActions()) {
                                for (String type : allTestimonialType) {
                                    if (action.getName().equalsIgnoreCase(type)) {
                                        testimonialType = action.getName();
                                        break loopBreak;
                                    }
                                }
                            }
                        }

                        headerToken = s.getToken();
                        loadData();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getStatus(), Toast.LENGTH_SHORT).show();
                        headerToken = "";
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.v("experincecode2", " " + error.getBody() + " " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.v("experincecode3", " " + e.getMessage() + " " + e.getStackTrace());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}