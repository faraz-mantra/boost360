package com.nowfloats.Product_Gallery;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.melnykov.fab.FloatingActionButton;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.HomeActivity;
import com.nowfloats.Product_Gallery.Model.ProductListModel;
import com.nowfloats.Product_Gallery.Service.ProductAPIService;
import com.nowfloats.accessbility.ProductItemClickCallback;
import com.nowfloats.util.BusProvider;
import com.nowfloats.util.Constants;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.thinksity.R;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by guru on 08-06-2015.
 */
public class Product_Gallery_Fragment extends Fragment {
    public static Bus bus;
    public static LinearLayout empty_layout, progressLayout;
    GridView gridView;
    public ProductGalleryAdapter adapter;
    public static ArrayList<ProductListModel> productItemModelList;
    private Activity activity;
    UserSessionManager session;
    int visibilityFlag = 1;
    private boolean userScrolled = false;
    private ProductAPIService apiService;
    private String currencyValue;
    private FROM from = FROM.DEFAULT;
    ;
    public static final String KEY_FROM = "KEY_FROM";
    private boolean isAnyProductSelected = false;

    public enum FROM {
        BUBBLE,
        DEFAULT
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        if (getArguments() != null)
            from = (FROM) getArguments().get(KEY_FROM);

        bus = BusProvider.getInstance().getBus();
        session = new UserSessionManager(activity.getApplicationContext(), activity);
        apiService = new ProductAPIService();
        currencyValue = getString(R.string.inr);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Constants.Currency_Country_Map == null) {
                    Constants.Currency_Country_Map = new HashMap<String, String>();
                    Constants.currencyArray = new ArrayList<String>();
                }
                if (Constants.Currency_Country_Map.size() == 0) {
                    for (Locale locale : Locale.getAvailableLocales()) {
                        try {
                            if (locale != null && locale.getISO3Country() != null && Currency.getInstance(locale) != null) {
                                Currency currency = Currency.getInstance(locale);
                                String loc_currency = currency.getCurrencyCode();
                                String country = locale.getDisplayCountry();
                                if (!Constants.Currency_Country_Map.containsKey(country.toLowerCase())) {
                                    Constants.Currency_Country_Map.put(country.toLowerCase(), loc_currency);
                                    Constants.currencyArray.add(country + "-" + loc_currency);
                                }
                            }
                        } catch (Exception e) {
                            System.gc();
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    currencyValue = Constants.Currency_Country_Map.get(
                            session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRY).toLowerCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Log.d("Product_Gallery", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Product_Gallery", "onCreateView");
        getProducts("0");
        return inflater.inflate(R.layout.fragment_product__gallery, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        empty_layout = (LinearLayout) view.findViewById(R.id.emptyproductlayout);
        progressLayout = (LinearLayout) view.findViewById(R.id.progress_productlayout);
        progressLayout.setVisibility(View.VISIBLE);
        gridView = (GridView) view.findViewById(R.id.product_gridview);
        final FloatingActionButton addProduct = (FloatingActionButton) view.findViewById(R.id.fab_product);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MixPanelController.track(EventKeysWL.PRODUCT_GALLERY_ADD, null);
                Intent intent;
                if (/*session.getWebTemplateType().equals("6")*/false) {
                    intent = new Intent(activity, Product_Detail_Activity.class);
                    intent.putExtra("new", "");
                } else {
                    intent = new Intent(activity, Product_Detail_Activity_V45.class);
                    intent.putExtra("new", "");
                }
                startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (from == FROM.DEFAULT) {
                    Intent intent;
                    if (/*session.getWebTemplateType().equals("6")*/false) {
                        intent = new Intent(activity, Product_Detail_Activity.class);
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("product", productItemModelList.get(position));
                        intent.putExtra("product", position + "");
                    } else {
                        intent = new Intent(activity, Product_Detail_Activity_V45.class);
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("product", productItemModelList.get(position));
                        intent.putExtra("product", position + "");
                    }
                    Methods.launchFromFragment(activity, view, intent);
                } else {

                    final ProductListModel productItemModel = (ProductListModel) view.getTag(R.string.key_details);
                    if (isAnyProductSelected && !productItemModel.isProductSelected) {
                        Toast.makeText(activity, "You can select only one product", Toast.LENGTH_LONG).show();
                    } else {
                        productItemModel.isProductSelected = !productItemModel.isProductSelected;
                        FrameLayout flMain = (FrameLayout) view.findViewById(R.id.flMain);
                        FrameLayout flOverlay = (FrameLayout) view.findViewById(R.id.flOverlay);
                        View vwOverlay = view.findViewById(R.id.vwOverlay);
                        if (productItemModel.isProductSelected) {
                            flOverlay.setVisibility(View.VISIBLE);
                            setOverlay(vwOverlay, 200, flMain.getWidth(), flMain.getHeight());
                            isAnyProductSelected = true;
                        } else {
                            isAnyProductSelected = false;
                            flOverlay.setVisibility(View.GONE);
                        }
                    }

//                    if (productItemModel.picimageURI == null) {
//                        Picasso.with(activity).load(productItemModel.TileImageUri).placeholder(R.drawable.default_product_image).into(new Target() {
//                            @Override
//                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//                                if (productItemModel.picimageURI == null)
//                                    productItemModel.picimageURI = getImageUri(mutableBitmap, productItemModel);
//                            }
//
//                            @Override
//                            public void onBitmapFailed(Drawable errorDrawable) {
//
//                            }
//
//                            @Override
//                            public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                            }
//                        });
//                    }
                }

            }
        });


        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    if (visibilityFlag == 0) {
                        visibilityFlag = 1;
                        YoYo.with(Techniques.SlideInUp).interpolate(new DecelerateInterpolator()).duration(200).playOn(addProduct);
                    }

                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                    if (visibilityFlag == 1) {
                        YoYo.with(Techniques.SlideOutDown).interpolate(new AccelerateInterpolator()).duration(200).playOn(addProduct);
                        visibilityFlag = 0;

                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((userScrolled) && (lastInScreen == totalItemCount) && (totalItemCount % 10 == 0)) {
                    userScrolled = false;
                    //TODO load more
                    getProducts("" + totalItemCount);
                }
            }
        });

        if (from == FROM.BUBBLE) {
            addProduct.setVisibility(View.GONE);
        }
    }

    public Uri getImageUri(Bitmap inImage, ProductListModel productItemModel) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), inImage, productItemModel.Name, productItemModel.Description);
        return Uri.parse(path);
    }


    public void setOverlay(View v, int opac, int width, int height) {
        int opacity = opac; // from 0 to 255
        v.setBackgroundColor(opacity * 0x1000000); // black with a variable alpha
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(width, height);
        params.gravity = Gravity.NO_GRAVITY;
        v.setLayoutParams(params);
        v.invalidate();
    }

    private void getProducts(String skip) {
        HashMap<String, String> values = new HashMap<>();
        values.put("clientId", Constants.clientId);
        values.put("skipBy", skip);
        values.put("fpTag", session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG));
        //invoke getProduct api
        apiService.getProductList(activity, values, bus);
    }

    @Subscribe
    public void loadMore(LoadMoreProductEvent event) {
        try {
            progressLayout.setVisibility(View.GONE);
            if (event.data != null) {
                //int addPos = productItemModelList.size();
                for (int i = 0; i < event.data.size(); i++) {
                    productItemModelList.add(event.data.get(i));
                    //addPos++;
                }
                adapter.refreshDetails(productItemModelList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.gc();
        }
    }

    private static final String PRODUCT_SEARCH = "PRODUCT_SEARCH";

    public void filterProducts(final String searchText) {

        if (productItemModelList != null && productItemModelList.size() > 0) {

            synchronized (PRODUCT_SEARCH) {

                try {
                    ArrayList<ProductListModel> arrModelTemp = null;
                    if (TextUtils.isEmpty(searchText)) {
                        arrModelTemp = productItemModelList;
                    } else {
                        Predicate<ProductListModel> searchItem = new Predicate<ProductListModel>() {
                            public boolean apply(ProductListModel productListModel) {
                                return (!TextUtils.isEmpty(productListModel.Description)
                                        && productListModel.Description.toLowerCase().contains(searchText.toLowerCase()))
                                        || (!TextUtils.isEmpty(productListModel.Name)
                                        && productListModel.Name.toLowerCase().contains(searchText.toLowerCase()));
                            }
                        };
                        arrModelTemp = (ArrayList<ProductListModel>)
                                filter(productItemModelList, searchItem);
                    }
                    adapter.refreshDetails(arrModelTemp);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public interface Predicate<T> {
        boolean apply(T type);
    }

    public static <T> Collection<T> filter(Collection<T> col, Predicate<T> predicate) {

        Collection<T> result = new ArrayList<T>();
        if (col != null) {
            for (T element : col) {
                if (predicate.apply(element)) {
                    result.add(element);
                }
            }
        }
        return result;
    }


    @Subscribe
    public void getProductList(ArrayList<ProductListModel> data) {
        progressLayout.setVisibility(View.GONE);
        if (data != null) {
            //Log.i("","PRoduct List Size--"+data.size());
            //Log.d("Product Id", data.get(0)._id);

            productItemModelList = data;
            adapter = new ProductGalleryAdapter(activity, currencyValue, from);
            gridView.setAdapter(adapter);
            gridView.invalidateViews();
            adapter.refreshDetails(productItemModelList);

            if (productItemModelList.size() == 0) {
                empty_layout.setVisibility(View.VISIBLE);
            } else {
                empty_layout.setVisibility(View.GONE);
            }
        } else {
            if (productItemModelList == null || productItemModelList.size() == 0) {
                Product_Gallery_Fragment.empty_layout.setVisibility(View.VISIBLE);
            } else {
                Product_Gallery_Fragment.empty_layout.setVisibility(View.GONE);
            }
            Methods.showSnackBarNegative(activity, getString(R.string.something_went_wrong_try_again));
        }
    }

//    public ArrayList<Uri> getSelectedProducts() {
//
//        ArrayList<Uri> arrayList = new ArrayList<Uri>();
//        for (ProductListModel productListModel : productItemModelList) {
//            if (productListModel.isProductSelected && productListModel.picimageURI != null) {
//                arrayList.add(productListModel.picimageURI);
//            }
//        }
//        return arrayList;
//    }

    public String getSelectedProducts() {

        String selectedProducts = "";
        for (ProductListModel productListModel : productItemModelList) {
            if (productListModel.isProductSelected) {

                try {

                    if (!TextUtils.isEmpty(session.getRootAliasURI())) {
                        selectedProducts = selectedProducts + session.getRootAliasURI();
                    } else {
                        selectedProducts = selectedProducts + "https://" + session.getFpTag() + ".nowfloats.com/";
                    }
                    selectedProducts = selectedProducts + URLEncoder.encode(productListModel.Name, "UTF-8") + "/p" + productListModel.ProductIndex;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return selectedProducts;
    }


    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        if (productItemModelList != null && productItemModelList.size() == 0 && empty_layout != null) {
            empty_layout.setVisibility(View.VISIBLE);
        } else {
            empty_layout.setVisibility(View.GONE);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (gridView != null) gridView.invalidateViews();
        if (HomeActivity.plusAddButton != null)
            HomeActivity.plusAddButton.setVisibility(View.GONE);
        if (HomeActivity.headerText != null)
            HomeActivity.headerText.setText(getString(R.string.product_gallery));
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }
}