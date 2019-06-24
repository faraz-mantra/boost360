package com.nowfloats.BusinessProfile.UI.UI;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.nowfloats.Analytics_Screen.VmnCallCardsActivity;
import com.nowfloats.BusinessProfile.UI.API.Retro_Business_Profile_Interface;
import com.nowfloats.BusinessProfile.UI.API.UpdatePrimaryNumApi;
import com.nowfloats.BusinessProfile.UI.Model.ContactInformationUpdateModel;
import com.nowfloats.BusinessProfile.UI.Model.WhatsAppBusinessNumberModel;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.helper.ui.BaseActivity;
import com.nowfloats.manageinventory.interfaces.WebActionCallInterface;
import com.nowfloats.manageinventory.models.WAAddDataModel;
import com.nowfloats.manageinventory.models.WaUpdateDataModel;
import com.nowfloats.manageinventory.models.WebActionModel;
import com.nowfloats.signup.UI.Model.ContactDetailsModel;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.thinksity.R;
import com.thinksity.databinding.ActivityContactInformationBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ContactInformationActivity extends BaseActivity
{
    ActivityContactInformationBinding binding;
    private UserSessionManager session;
    private MaterialDialog dialog, otpDialog, progressbar;
    private boolean VMN_Dialog;
    private WhatsAppBusinessNumberModel numberModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_information);

        setSupportActionBar(binding.appBar.toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle("");
        }

        binding.editPrimaryContactNumber.setInputType(InputType.TYPE_NULL);
        binding.appBar.toolbarTitle.setText(getResources().getString(R.string.contact__info));
        session = new UserSessionManager(getApplicationContext(), ContactInformationActivity.this);

        binding.editPrimaryContactNumber.setOnTouchListener((v, event)-> {

            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                    if (Constants.PACKAGE_NAME.equals("com.biz2.nowfloats") || Constants.PACKAGE_NAME.equals("com.digitalseoz"))
                    {
                        showOtpDialog();
                    }

                    else
                    {
                        dialog().show();
                    }
                }

                return true;
            });

        binding.tvVmnReport.setOnClickListener(v -> {

            Intent i = new Intent(ContactInformationActivity.this, VmnCallCardsActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        this.initProgressBar();
        this.setData();
        this.getWhatsAppNumber(session.getFpTag());
    }


    private void initProgressBar()
    {
        if(progressbar == null)
        {
            progressbar = new MaterialDialog.Builder(this)
                    .autoDismiss(false)
                    .progress(true, 0)
                    .build();
        }
    }


    private void showOtpDialog()
    {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null);
        final EditText number = view.findViewById(R.id.editText);

        dialog = new MaterialDialog.Builder(this)
                .customView(view, false)
                .negativeText("CANCEL")
                .positiveText("SEND OTP")
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .negativeColorRes(R.color.gray_transparent)
                .positiveColorRes(R.color.primary_color)
                .onPositive((dialog, which)-> {

                    String numText = number.getText().toString().trim();

                    if (numText.length() >= 6)
                    {
                        sendSms(numText);
                    }

                    else
                    {
                        Toast.makeText(ContactInformationActivity.this, getResources().getString(R.string.enter_password_6to12_char), Toast.LENGTH_SHORT).show();
                    }
                })
                .onNegative((dialog, which)-> dialog.dismiss()).show();
    }


    private void otpVerifyDialog(final String number)
    {
        if(otpDialog != null && otpDialog.isShowing())
        {
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verify, null);
        final EditText otp = view.findViewById(R.id.editText);
        final TextView tvNumber = view.findViewById(R.id.tv_number);
        tvNumber.setText("(" + number + ")");
        TextView resend = view.findViewById(R.id.resend_tv);

        resend.setOnClickListener(v-> sendSms(number));

        otpDialog = new MaterialDialog.Builder(this)
                .customView(view, false)
                .autoDismiss(false)
                .negativeText("CANCEL")
                .positiveText("VERIFY")
                .canceledOnTouchOutside(false)
                .negativeColorRes(R.color.gray_transparent)
                .positiveColorRes(R.color.primary_color)
                .onPositive((dialog, which)-> {

                    String numText = otp.getText().toString().trim();

                    if (numText.length() > 0)
                    {
                        verifySms(number, numText);
                    }

                    else
                    {
                        Toast.makeText(ContactInformationActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                    }
                })
                .onNegative((dialog, which)-> dialog.dismiss()).show();
    }


    private MaterialDialog dialog()
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_link_layout, null, false);
        TextView message = dialogView.findViewById(R.id.toast_message_to_contact);

        if (VMN_Dialog)
        {
            //message.setText("Call tracker is enabled. You will receive the call on your primary number." + getString(R.string.primary_contact_number_message));
            message.setText("This is your Virtual Mobile Number which is displayed on your website. All activity on this number is tracked and you will receive calls made to this number on your primary number.");
        }

        else
        {
            message.setText(getString(R.string.primary_contact_number_message));
        }

        return new MaterialDialog.Builder(ContactInformationActivity.this)
                .title("Call Tracker is enabled")
                .customView(dialogView, false)
                .positiveText(getString(R.string.ok))
                .positiveColorRes(R.color.primaryColor)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        super.onPositive(dialog);
                    }

                })
                .build();
    }


    private void hideOtpDialog()
    {
        if (dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
        }
    }

    private void otpDialogDismiss()
    {
        if (otpDialog != null && otpDialog.isShowing())
        {
            otpDialog.dismiss();
        }
    }

    private void showProgressbar(String content)
    {
        if (progressbar != null && !progressbar.isShowing())
        {
            progressbar.setContent(content);
            progressbar.show();
        }
    }

    private void hideProgressbar()
    {
        if (progressbar != null && progressbar.isShowing())
        {
            progressbar.dismiss();
        }
    }

    private void setData()
    {
        if ("VMN".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NAME_1)) ||
                "VMN".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NAME_3)) ||
                "VMN".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NAME))) {

            VMN_Dialog = true;

            binding.layoutDisplayContactNumber1.setVisibility(View.GONE);
            binding.layoutDisplayContactNumber2.setVisibility(View.GONE);
            binding.layoutDisplayContactNumber3.setVisibility(View.GONE);
            binding.layoutCallTrackerNumber.setVisibility(View.VISIBLE);

            if("VMN".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NAME)))
            {
                binding.editCallTrackerNumber.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NUMBER));
            }

            else if("VMN".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NAME_1)))
            {
                binding.editCallTrackerNumber.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NUMBER_1));
            }

            else if("VMN".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NAME_3)))
            {
                binding.editCallTrackerNumber.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NUMBER_3));
            }
        }

        else
        {
            binding.layoutDisplayContactNumber1.setVisibility(View.VISIBLE);
            binding.layoutDisplayContactNumber2.setVisibility(View.VISIBLE);
            binding.layoutDisplayContactNumber3.setVisibility(View.VISIBLE);
            binding.layoutCallTrackerNumber.setVisibility(View.GONE);

            binding.editDisplayContactNumber1.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NUMBER));
            binding.editDisplayContactNumber2.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NUMBER_1));
            binding.editDisplayContactNumber3.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NUMBER_3));
        }

        binding.editPrimaryContactNumber.setText(session.getFPDetails(Key_Preferences.MAIN_PRIMARY_CONTACT_NUM));
        binding.editBusinessEmailAddress.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_EMAIL));

        String website = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_WEBSITE);

        if (!TextUtils.isEmpty(website))
        {
            if (website.split("://").length == 2 && website.split("://")[0].equals("http"))
            {
                binding.spinnerHttpProtocol.setSelection(0);
                binding.editWebsiteAddress.setText(website.split("://")[1]);
            }

            else if (website.split("://").length == 2 && website.split("://")[0].equals("https"))
            {
                binding.spinnerHttpProtocol.setSelection(1);
                binding.editWebsiteAddress.setText(website.split("://")[1]);
            }

            else
            {
                binding.spinnerHttpProtocol.setSelection(0);
                binding.editWebsiteAddress.setText(website);
            }
        }

        binding.editFbPageWidget.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_FBPAGENAME));
    }


    private void saveInformation()
    {
        showProgressbar("Updating Information...");

        ContactInformationUpdateModel model = new ContactInformationUpdateModel();

        ArrayList<ContactInformationUpdateModel.Update> updates = new ArrayList<>();

        String url = binding.spinnerHttpProtocol.getSelectedItem().toString().concat(binding.editWebsiteAddress.getText().toString());

        updates.add(new ContactInformationUpdateModel.Update("URL", url));
        updates.add(new ContactInformationUpdateModel.Update("EMAIL", binding.editBusinessEmailAddress.getText().toString()));

        if(!VMN_Dialog)
        {
            List<ContactDetailsModel> contacts = new ArrayList<>();

            String number1 = binding.editDisplayContactNumber1.getText().toString().trim();
            String number2 = binding.editDisplayContactNumber2.getText().toString().trim();
            String number3 = binding.editDisplayContactNumber3.getText().toString().trim();

            contacts.add(new ContactDetailsModel(number1, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NAME)));
            contacts.add(new ContactDetailsModel(number2, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NAME_1)));
            contacts.add(new ContactDetailsModel(number3, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NAME_3)));

            updates.add(new ContactInformationUpdateModel.Update("CONTACTS", new Gson().toJson(contacts) /*number1.concat("#").concat(number2).concat("#").concat(number3)*/));
        }

        updates.add(new ContactInformationUpdateModel.Update("FB", binding.editFbPageWidget.getText().toString()));

        StringBuilder webWidgets = new StringBuilder();

        for (String widget: Constants.StoreWidgets)
        {
            webWidgets.append(widget).append("#");
        }

        webWidgets.append("FBLIKEBOX");

        updates.add(new ContactInformationUpdateModel.Update("WEBWIDGETS", webWidgets.toString()));

        model.setClientId(Constants.clientId);
        model.setFpTag(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG).toUpperCase());
        model.setUpdates(updates);

        Retro_Business_Profile_Interface profile_interface = Constants.restAdapter.create(Retro_Business_Profile_Interface.class);
        profile_interface.updateContactInformation(model, new Callback<ArrayList<String>>() {

            @Override
            public void success(ArrayList<String> strings, Response response) {

                hideProgressbar();

                session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_WEBSITE, url);
                session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_FBPAGENAME, binding.editFbPageWidget.getText().toString());
                session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_EMAIL, binding.editBusinessEmailAddress.getText().toString());

                session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NUMBER, binding.editDisplayContactNumber1.getText().toString());
                session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NUMBER_1, binding.editDisplayContactNumber2.getText().toString());
                session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_ALTERNATE_NUMBER_3, binding.editDisplayContactNumber3.getText().toString());

                Methods.showSnackBarPositive(ContactInformationActivity.this, "Information Updated Successfully");
            }

            @Override
            public void failure(RetrofitError error) {

                hideProgressbar();
                Methods.showSnackBarNegative(ContactInformationActivity.this, "Failed to Update Information");
            }
        });


        if(numberModel == null && binding.editWhatsappNumber.getText().toString().trim().length() > 0)
        {
            WhatsAppBusinessNumberModel whatsAppBusinessNumberModel = new WhatsAppBusinessNumberModel();
            whatsAppBusinessNumberModel.setWhatsAppNumber(binding.editWhatsappNumber.getText().toString());

            WAAddDataModel<WhatsAppBusinessNumberModel> dataModel = new WAAddDataModel<>();
            dataModel.setWebsiteId(session.getFpTag());
            dataModel.setActionData(whatsAppBusinessNumberModel);

            addWhatsAppNumber(dataModel);
        }

        else if(numberModel != null && !binding.editWhatsappNumber.getText().toString().equals(numberModel.getWhatsAppNumber()))
        {
            WaUpdateDataModel update = new WaUpdateDataModel();
            update.setQuery(String.format("{_id:'%s'}", numberModel.getId()));

            update.setUpdateValue(String.format("{$set:{active_whatsapp_number:'%s', IsArchived:'%s'}}",
                    binding.editWhatsappNumber.getText().toString(),
                    false));

            update.setMulti(true);
            updateWhatsAppNumber(update);
        }
    }


    private boolean isValid()
    {
        /*if(TextUtils.isEmpty(binding.editPrimaryContactNumber.getText().toString()))
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.primary_num_can_not_empty));
            return false;
        }

        if (binding.editPrimaryContactNumber.getText().toString().trim().length() > 0 && binding.editPrimaryContactNumber.getText().toString().trim().length() <= 6)
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_password_6to12_char));
            return false;
        }*/

        if (binding.editDisplayContactNumber1.getText().toString().trim().length() > 0 && binding.editDisplayContactNumber1.getText().toString().trim().length() < 6)
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_password_6to12_char));
            binding.editDisplayContactNumber1.requestFocus();
            return false;
        }

        if (binding.editDisplayContactNumber2.getText().toString().trim().length() > 0 && binding.editDisplayContactNumber2.getText().toString().trim().length() < 6)
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_password_6to12_char));
            binding.editDisplayContactNumber2.requestFocus();
            return false;
        }

        if (binding.editDisplayContactNumber3.getText().toString().trim().length() > 0 && binding.editDisplayContactNumber3.getText().toString().trim().length() < 6)
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_password_6to12_char));
            binding.editDisplayContactNumber3.requestFocus();
            return false;
        }

        if (binding.editWhatsappNumber.getText().toString().trim().length() > 0 && binding.editWhatsappNumber.getText().toString().trim().length() < 6)
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_password_6to12_char));
            binding.editWhatsappNumber.requestFocus();
            return false;
        }

        if(binding.editWebsiteAddress.getText().toString().trim().length() > 0 && !isValidWebsite(binding.editWebsiteAddress.getText().toString()))
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_valid_website));
            binding.editWebsiteAddress.requestFocus();
            return false;
        }

        if(binding.editBusinessEmailAddress.getText().toString().trim().length() > 0 && !isValidEmail(binding.editBusinessEmailAddress.getText().toString()))
        {
            Methods.showSnackBarNegative(this, getResources().getString(R.string.enter_valid_email));
            binding.editBusinessEmailAddress.requestFocus();
            return false;
        }

        return true;
    }


    private boolean isValidEmail(String email)
    {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidWebsite(String website)
    {
        Pattern pattern = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
        //Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(website);
        return matcher.matches();
    }


    public void onSaveClick(View view)
    {
        if(!Methods.isOnline(this))
        {
            return;
        }

        if(!isValid())
        {
            return;
        }

        saveInformation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:

                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void sendSms(String number)
    {
        showProgressbar("Please Wait...");

        Methods.SmsInterface smsApi = Constants.restAdapterDev1.create(Methods.SmsInterface.class);

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("mobileNumber", number);
        hashMap.put("clientId", Constants.clientId);

        smsApi.sendSms(hashMap, new Callback<Boolean>() {

            @Override
            public void success(Boolean model, Response response)
            {
                hideProgressbar();

                if(response.getStatus() == 200 && model)
                {
                    hideOtpDialog();
                    otpVerifyDialog(number);

                    Toast.makeText(ContactInformationActivity.this, "OTP Sent to " + number, Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(ContactInformationActivity.this, "Failed to Send OTP", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error)
            {
                hideProgressbar();
                Toast.makeText(ContactInformationActivity.this, getString(R.string.something_went_wrong_try_again), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void verifySms(String number, String otp)
    {
        showProgressbar("Verifying OTP...");
        Methods.SmsInterface smsApi = Constants.restAdapterDev1.create(Methods.SmsInterface.class);

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("mobileNumber", number);
        hashMap.put("otp", otp);
        hashMap.put("clientId", Constants.clientId);

        smsApi.verifySms(hashMap, new Callback<Boolean>() {

            @Override
            public void success(Boolean model, Response response)
            {
                hideProgressbar();

                if (model == null)
                {
                    Toast.makeText(ContactInformationActivity.this, "Failed to Verify OTP", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(response.getStatus() == 200 && model)
                {
                    changePrimary(number);
                    return;
                }

                Toast.makeText(ContactInformationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error)
            {
                hideProgressbar();
                Toast.makeText(ContactInformationActivity.this, getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePrimary(final String number)
    {
        UpdatePrimaryNumApi updateApi = Constants.restAdapter.create(UpdatePrimaryNumApi.class);
        updateApi.changeNumber(session.getFPID(), Constants.clientId, number, new Callback<String>() {

            @Override
            public void success(String s, Response response)
            {
                hideProgressbar();
                otpDialogDismiss();

                if (s == null || response.getStatus() != 200)
                {
                    Methods.showSnackBarNegative(ContactInformationActivity.this, getString(R.string.something_went_wrong_try_again));
                    return;
                }

                MixPanelController.track(MixPanelController.PRIMARY_NUMBER_CHANGE, null);
                session.storeFPDetails(Key_Preferences.MAIN_PRIMARY_CONTACT_NUM, number);
                binding.editPrimaryContactNumber.setText(number);
                Methods.showSnackBarPositive(ContactInformationActivity.this, "Primary number changed successfully");
            }

            @Override
            public void failure(RetrofitError error)
            {
                hideProgressbar();
                otpDialogDismiss();

                if(error.getResponse().getStatus() == 400)
                {
                    showOtpDialog();
                    Methods.showSnackBarNegative(ContactInformationActivity.this, "This primary number is already used");
                    return;
                }

                Methods.showSnackBarNegative(ContactInformationActivity.this, getString(R.string.something_went_wrong_try_again));
            }
        });
    }


    private void getWhatsAppNumber(String websiteId) {

        Constants.webActionAdapter.create(WebActionCallInterface.class)
                .getWhatsAppNumber(String.format("{WebsiteId:'%s'}", websiteId), new Callback<WebActionModel<WhatsAppBusinessNumberModel>>() {

                    @Override
                    public void success(WebActionModel<WhatsAppBusinessNumberModel> model, Response response) {

                        if (model != null && model.getData() != null && model.getData().size() > 0)
                        {
                            numberModel = model.getData().get(0);
                            String whatsAppNumber = numberModel.getWhatsAppNumber() == null ? "" : numberModel.getWhatsAppNumber();
                            binding.editWhatsappNumber.setText(whatsAppNumber);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {

                    }
                });
    }


    private void addWhatsAppNumber(WAAddDataModel<WhatsAppBusinessNumberModel> addDataModel) {

        Constants.webActionAdapter.create(WebActionCallInterface.class)
                .addWhatsAppNumber(addDataModel, new Callback<String>() {

                    @Override
                    public void success(String id, Response response) {

                        numberModel = new WhatsAppBusinessNumberModel();
                        numberModel.setId(id);
                        numberModel.setWhatsAppNumber(binding.editWhatsappNumber.getText().toString());

                        Log.d("updateWhatsAppNumber", "SUCCESS");
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        Log.d("updateWhatsAppNumber", "FAIL");
                    }
                });
    }


    private void updateWhatsAppNumber(WaUpdateDataModel updateDataModel) {

        Constants.webActionAdapter.create(WebActionCallInterface.class)
                .updateWhatsAppNumber(updateDataModel, new Callback<String>() {

                    @Override
                    public void success(String model, Response response) {

                        if(numberModel != null)
                        {
                            numberModel.setWhatsAppNumber(binding.editWhatsappNumber.getText().toString());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {

                    }
                });
    }
}