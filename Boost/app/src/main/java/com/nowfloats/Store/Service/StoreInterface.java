package com.nowfloats.Store.Service;

import com.nowfloats.Store.Model.EnablePackageResponse;
import com.nowfloats.Store.Model.StoreModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by guru on 29-04-2015.
 */
/*https://api.withfloats.com/Support/v1/
            GetFloatingPointPackages?
            identifier=DB96EA35A6E44C0F8FB4A6BAA94DB017C0DFBE6F9944B14AA6C3C48641B3D70&
            clientId=DB96EA35A6E44C0F8FB4A6BAA94DB017C0DFBE6F9944B14AA6C3C48641B3D70*/
    //?identifier=DB96EA35A6E44C0F8FB4A6BAA94DB017C0DFBE6F9944B14AA6C3C48641B3D70&clientId=DB96EA35A6E44C0F8FB4A6BAA94DB017C0DFBE6F9944B14AA6C3C48641B3D70
public interface StoreInterface {
    @GET("/Support/v1/GetFloatingPointPackages")
    void getStoreList(@QueryMap Map<String,String> map, Callback<ArrayList<StoreModel>> callback);

    //https://api.withfloats.com/Discover/v1/floatingPoint/5406bd254ec0a40d409f2b2b/requestplan?
    // clientId=2FA76D4AFCD84494BD609FDB4B3D76782F56AE790A3744198E6F517708CAAA21&plantype=mach3
    @GET("/Discover/v1/floatingPoint/{fpid}/requestplan")
    void requestWidget(@Path("fpid") String fpid,@QueryMap Map<String,String> map,Callback<String> callback);

    ///DomainService/v1/checkAvailability/yahoo?clientId=DB96EA35A6E44C0F8FB4A6BAA94DB017C0DFBE6F9944B14AA6C3C48641B3D70&domainType=.com
    @GET("/DomainService/v1/checkAvailability/{tagField_}")
    void checkDomain(@Path("tagField_") String tagField_, @QueryMap Map<String,String> map,Callback<String> callback);

    /* @"clientId",@"domainType",@"domainName",@"existingFPTag" */
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("/domainservice/v1/requestdomainpurchase")
    void purchaseDomain(@Body HashMap<String,String> map, Callback<String> callback);

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("/Support/v1/floatingpont/enablePackage")
    void enableWidgetPack(@Body HashMap<String,String> map, Callback<EnablePackageResponse> callback);
}
