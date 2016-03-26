package com.nowfloats.signup.UI.Service;

import android.app.Activity;
import android.util.Log;

import com.nowfloats.PreSignUp.SplashScreen_Activity;
import com.nowfloats.signup.UI.API.Retro_Signup_Interface;
import com.nowfloats.signup.UI.Model.Get_FP_Details_Event;
import com.nowfloats.signup.UI.Model.Get_FP_Details_Model;
import com.nowfloats.signup.UI.Model.ProcessFPDetails;
import com.nowfloats.signup.UI.UI.WebSiteAddressActivity;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.squareup.otto.Bus;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by NowFloatsDev on 25/05/2015.
 */
public class Get_FP_Details_Service {
    public Get_FP_Details_Service(final Activity activity, String fpID, String clientID,final Bus bus)
    {
        HashMap<String,String> map = new HashMap<>();
        map.put("clientId",clientID);
        Retro_Signup_Interface getFPDetails = Constants.restAdapter.create(Retro_Signup_Interface.class);
        getFPDetails.post_getFPDetails(fpID,map,new Callback<Get_FP_Details_Model>() {
            @Override
            public void success(Get_FP_Details_Model get_fp_details_model, Response response) {
                if (get_fp_details_model!=null){
                    ProcessFPDetails.storeFPDetails(activity, get_fp_details_model);
                    bus.post(new Get_FP_Details_Event(get_fp_details_model));
                }else { activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.showSnackBarNegative(activity, "Something went wrong! Please try again.");
                        if (WebSiteAddressActivity.pd != null) {
                            WebSiteAddressActivity.pd.dismiss();
                        }
                        if (SplashScreen_Activity.pd!=null)
                        {
                            SplashScreen_Activity.pd.dismiss();
                        }
                    }
                });}
            }
            @Override
            public void failure(RetrofitError error) {
                Log.d("",""+error.getMessage());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.showSnackBarNegative(activity, "Something went wrong! Please try again.");
                        if (WebSiteAddressActivity.pd != null) {
                            WebSiteAddressActivity.pd.dismiss();
                        }
                        if (SplashScreen_Activity.pd!=null)
                        {
                            SplashScreen_Activity.pd.dismiss();
                        }

                    }
                });
            }
        });
    }
}