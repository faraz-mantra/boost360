package com.nowfloats.Product_Gallery;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.google.gson.Gson;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.Product_Gallery.Adapter.SpinnerAdapter;
import com.nowfloats.Product_Gallery.Model.AddressInformation;
import com.nowfloats.Product_Gallery.Model.AssuredPurchase;
import com.nowfloats.Product_Gallery.Model.BankInformation;
import com.nowfloats.Product_Gallery.Model.ProductImageResponseModel;
import com.nowfloats.Product_Gallery.Model.Product_Gallery_Update_Model;
import com.nowfloats.Product_Gallery.Model.UpdateValue;
import com.nowfloats.Product_Gallery.Service.FileUpload;
import com.nowfloats.Product_Gallery.Service.MultipleFileUpload;
import com.nowfloats.Product_Gallery.Service.ProductGalleryInterface;
import com.nowfloats.Product_Gallery.Service.UploadImage;
import com.nowfloats.Product_Gallery.fragments.ProductPickupAddressFragment;
import com.nowfloats.helper.Helper;
import com.nowfloats.helper.ui.ImageLoader;
import com.nowfloats.manageinventory.models.WAAddDataModel;
import com.nowfloats.manageinventory.models.WaUpdateDataModel;
import com.nowfloats.manageinventory.models.WebActionModel;
import com.nowfloats.sellerprofile.model.WebResponseModel;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.webactions.WebAction;
import com.nowfloats.webactions.WebActionsFilter;
import com.nowfloats.webactions.models.ProductImage;
import com.nowfloats.webactions.models.WebActionError;
import com.nowfloats.webactions.webactioninterfaces.IFilter;
import com.nowfloats.widget.WidgetKey;
import com.squareup.picasso.Picasso;
import com.thinksity.R;
import com.thinksity.databinding.FragmentManageProductBinding;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.app.Activity.RESULT_OK;
import static com.nowfloats.util.Constants.DEV_ASSURED_PURCHASE_URL;


public class ManageProductFragment extends Fragment implements UploadImage.ImageUploadListener, FileUpload.OnFileUpload{

    private String TAG = ManageProductFragment.class.getSimpleName();

    private String currencyValue;
    private String currencyType = "";
    private String productType = "";
    private int MAX_IMAGE_ALLOWED = 8;

    private List<ProductImageResponseModel> imageList = new ArrayList<>();

    private ProductSpecificationRecyclerAdapter adapter;
    private ProductImageRecyclerAdapter adapterImage;
    private ProductPickupAddressRecyclerAdapter adapterAddress;

    private final int CAMERA_PERMISSION_REQUEST_CODE = 1;

    private final int CAMERA_PRIMARY_IMAGE_REQUEST_CODE = 101;
    private final int CAMERA_SECONDARY_IMAGE_REQUEST_CODE = 102;
    private final int CAMERA_PROOF_IMAGE_REQUEST_CODE = 103;

    private final int GALLERY_PRIMARY_IMAGE_REQUEST_CODE = 201;
    private final int GALLERY_SECONDARY_IMAGE_REQUEST_CODE = 202;
    private final int GALLERY_PROOF_IMAGE_REQUEST_CODE = 203;

    private final int DIALOG_REQUEST_CODE_PRIMARY = 1;
    private final int DIALOG_REQUEST_CODE_SECONDARY = 2;

    public static final String FILE_EXTENSIONS [] = new String[] { "doc", "docx", "xls", "xlsx", "pdf" };

    private Uri primaryUri, secondaryUri, proofUri;
    private File file;

    private String CATEGORY;
    private UserSessionManager session;
    private MaterialDialog materialDialog;

    private Constants.PaymentAndDeliveryMode paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE;

    private com.nowfloats.Product_Gallery.Model.Product product;
    private AssuredPurchase assuredPurchase;
    private BankInformation bankInformation;
    private AddressInformation addressInformation;
    private List<AddressInformation> addressInformationList;

    private BottomSheetBehavior sheetBehavior;
    private BottomSheetBehavior sheetBehaviorAddress;

    private ProductPickupAddressFragment pickupAddressFragment;
    private String[] paymentOptionTitles;
    private WebAction mWebAction;

    public static ManageProductFragment newInstance(String productType, String category, com.nowfloats.Product_Gallery.Model.Product product)
    {
        ManageProductFragment fragment = new ManageProductFragment();

        Bundle args = new Bundle();
        args.putString("CATEGORY", category);
        args.putString("PRODUCT_TYPE", productType);
        args.putSerializable("PRODUCT", product);
        fragment.setArguments(args);

        return fragment;
    }


    private FragmentManageProductBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if(bundle != null)
        {
            this.product = (com.nowfloats.Product_Gallery.Model.Product) bundle.getSerializable("PRODUCT");

            if (product != null && product.productId != null)
            {
                setHasOptionsMenu(true);
            }
        }

        mWebAction = getWebAction();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_product, container, false);
        sheetBehavior = BottomSheetBehavior.from(binding.layoutBottomSheet.getRoot());
        sheetBehaviorAddress = BottomSheetBehavior.from(binding.layoutBottomSheetAddress.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        session = new UserSessionManager(getContext(), getActivity());
        this.paymentOptionTitles = getResources().getStringArray(R.array.payment_method_titles);

        initProductSpecificationRecyclerView(binding.layoutProductSpecification.productSpecificationList);
        initProductImageRecyclerView(binding.productImageList);
        initProductPickupAddressRecyclerView(binding.layoutBottomSheetAddress.pickupAddressList);

        addSpinnerListener();

        initCurrencyList();
        addPropertyListener();
        addQuantityListener();
        addSwitchVariantListener();
        addImagePickerListener();
        addPaymentConfigListener();
        initPaymentAdapter();
        spinnerAddressListener();
        addBottomSheetListener();
        addTextChangeListener();

        placeholder();

        binding.layoutShippingMatrixDetails.editWeight.setKeyListener(DigitsKeyListener.getInstance(true,true));
        binding.layoutShippingMatrixDetails.editHeight.setKeyListener(DigitsKeyListener.getInstance(true,true));
        binding.layoutShippingMatrixDetails.editLength.setKeyListener(DigitsKeyListener.getInstance(true,true));
        binding.layoutShippingMatrixDetails.editThickness.setKeyListener(DigitsKeyListener.getInstance(true,true));
        binding.editBasePrice.setKeyListener(DigitsKeyListener.getInstance(true,true));
        binding.editDiscount.setKeyListener(DigitsKeyListener.getInstance(true,true));
        binding.editGst.setKeyListener(DigitsKeyListener.getInstance(true,true));

        binding.layoutInventory.labelInventoryHint.setText(String.valueOf("Inventory availability"));
        binding.layoutInventory.labelInventoryQuantityHint.setText(String.valueOf("Available quantity"));
        binding.layoutInventoryOnline.labelInventoryHint.setText(String.valueOf("Accept online payment"));
        binding.layoutInventoryOnline.labelInventoryQuantityHint.setText(String.valueOf("Max quantity per order"));
        binding.layoutInventoryCod.labelInventoryHint.setText(String.valueOf("Accept COD payment"));
        binding.layoutInventoryCod.labelInventoryQuantityHint.setText(String.valueOf("Max quantity per order"));

        binding.btnPublish.setOnClickListener(view -> saveProduct());

        displayPaymentAcceptanceMessage();

        Bundle bundle = getArguments();

        if(bundle != null)
        {
            CATEGORY = bundle.getString("CATEGORY");
            productType = bundle.getString("PRODUCT_TYPE");

            if(product != null && product.productId != null )
            {
                setProductData();

                getAssuredPurchase(product.productId);
                displayImagesForProduct(product.productId);

                ((ManageProductActivity) getActivity()).setTitle(String.valueOf("Edit " + product.Name));
            }

            else
            {
                ((ManageProductActivity) getActivity()).setTitle(String.valueOf("Listing " + CATEGORY));
            }

            if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue())
                    && productType.equalsIgnoreCase("products"))
            {
                binding.layoutBottomSheet.tvPickAddress.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.layoutPickupAddressInfo.setVisibility(View.VISIBLE);

                binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.VISIBLE);
                binding.layoutInventoryCod.layoutInventory.setVisibility(View.VISIBLE);
                binding.layoutInventoryOnline.layoutInventory.setVisibility(View.VISIBLE);
            }

            else
            {
                binding.layoutBottomSheet.tvPickAddress.setVisibility(View.GONE);
                binding.layoutBottomSheet.layoutPickupAddressInfo.setVisibility(View.GONE);

                binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);
            }
        }

        this.getBankInformation();
        this.getAddressInformation();
    }


    private void setProductData()
    {
        if(product == null)
        {
            return;
        }

        binding.editBrand.setText(product.brandName != null ? product.brandName : "");
        binding.editProductName.setText(product.Name != null ? product.Name : "");
        binding.editProductDescription.setText(product.Description != null ? product.Description : "");

        binding.editBasePrice.setText(product.Price > 0 ? String.valueOf(product.Price) : "");
        binding.editDiscount.setText(product.DiscountAmount > 0 ? String.valueOf(product.DiscountAmount) : "");

        this.setFinalPrice();

        /**
         * Product availability and quantity
         */
        if(!product.IsAvailable)
        {
            binding.layoutInventory.spinnerStockAvailability.setSelection(2);
        }

        else if(product.availableUnits > 0)
        {
            binding.layoutInventory.spinnerStockAvailability.setSelection(0);
            binding.layoutInventory.quantityValue.setText(String.valueOf(product.availableUnits));
        }

        else
        {
            binding.layoutInventory.spinnerStockAvailability.setSelection(1);
        }


        /**
         * COD product availability and quantity
         */
        if(product.codAvailable)
        {
            binding.layoutInventoryCod.spinnerStockAvailability.setSelection(0);
        }

        else
        {
            binding.layoutInventoryCod.spinnerStockAvailability.setSelection(1);
            binding.layoutInventoryCod.quantityValue.setText(String.valueOf(product.codAvailable));
        }

        /**
         * Prepaid product availability and quantity
         */
        if(product.prepaidOnlineAvailable)
        {
            binding.layoutInventoryOnline.spinnerStockAvailability.setSelection(0);
        }

        else
        {
            binding.layoutInventoryOnline.spinnerStockAvailability.setSelection(1);
            binding.layoutInventoryOnline.quantityValue.setText(String.valueOf(product.maxPrepaidOnlineAvailable));
        }

        if(product.keySpecification != null)
        {
            binding.layoutProductSpecification.layoutKeySpecification.editKey.setText(product.keySpecification.key != null ? product.keySpecification.key : "");
            binding.layoutProductSpecification.layoutKeySpecification.editValue.setText(product.keySpecification.value != null ? product.keySpecification.value : "");
        }

        if(product.paymentType != null)
        {
            if(product.paymentType.equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue()))
            {
                paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE;
                binding.layoutPaymentMethod.tvPaymentConfiguration.setText(paymentOptionTitles[0]);

                binding.layoutBottomSheet.spinnerPaymentOption.setSelection(0);

                binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setVisibility(View.VISIBLE);
                binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setText(getString(R.string.payment_methud_message));

                binding.layoutPaymentMethod.layoutPaymentExternalPurchaseUrl.setVisibility(View.GONE);

                if(productType.equalsIgnoreCase("products"))
                {
                    binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.VISIBLE);
                    binding.layoutInventoryCod.layoutInventory.setVisibility(View.VISIBLE);
                    binding.layoutInventoryOnline.layoutInventory.setVisibility(View.VISIBLE);
                }

                else
                {
                    binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                    binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                    binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);
                }
            }

            else if(product.paymentType.equalsIgnoreCase(Constants.PaymentAndDeliveryMode.UNIQUE_PAYMENT_URL.getValue()))
            {
                paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.UNIQUE_PAYMENT_URL;
                binding.layoutPaymentMethod.tvPaymentConfiguration.setText(paymentOptionTitles[1]);

                binding.layoutBottomSheet.spinnerPaymentOption.setSelection(1);
                binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setVisibility(View.GONE);

                binding.layoutPaymentMethod.layoutPaymentExternalPurchaseUrl.setVisibility(View.VISIBLE);

                binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);

                if(product.BuyOnlineLink != null)
                {
                    binding.layoutPaymentMethod.editDescription.setText(product.BuyOnlineLink.description != null ? product.BuyOnlineLink.description : "");
                    binding.layoutPaymentMethod.editPurchaseUrlLink.setText(product.BuyOnlineLink.url != null ? product.BuyOnlineLink.url : "");
                }
            }

            else if(product.paymentType.equalsIgnoreCase(Constants.PaymentAndDeliveryMode.DONT_WANT_TO_SELL.getValue()))
            {
                paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.DONT_WANT_TO_SELL;
                binding.layoutPaymentMethod.tvPaymentConfiguration.setText(paymentOptionTitles[2]);

                binding.layoutBottomSheet.spinnerPaymentOption.setSelection(2);

                binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setVisibility(View.VISIBLE);
                binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setText(getString(R.string.payment_method_dont_want_to_sell));

                binding.layoutPaymentMethod.layoutPaymentExternalPurchaseUrl.setVisibility(View.GONE);

                binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);
            }
        }

        try
        {
            String image_url = product.TileImageUri;

            if (image_url != null && image_url.length() > 0 && !image_url.equals("null"))
            {
                if (!image_url.contains("http"))
                {
                    image_url = Constants.BASE_IMAGE_URL + product.TileImageUri;
                }

                Picasso.with(getActivity()).load(image_url).placeholder(R.drawable.default_product_image).into(binding.ivPrimaryImage);
            }

            else
            {
                Picasso.with(getActivity()).load(R.drawable.default_product_image).into(binding.ivPrimaryImage);
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setAssuredPurchaseData()
    {
        if(assuredPurchase == null)
        {
            return;
        }

        binding.layoutShippingMatrixDetails.editHeight.setText(assuredPurchase.height > 0 ? String.valueOf(assuredPurchase.height) : "");
        binding.layoutShippingMatrixDetails.editWeight.setText(assuredPurchase.weight > 0 ? String.valueOf(assuredPurchase.weight) : "");
        binding.layoutShippingMatrixDetails.editLength.setText(assuredPurchase.length > 0 ? String.valueOf(assuredPurchase.length) : "");
        binding.layoutShippingMatrixDetails.editThickness.setText(assuredPurchase.width > 0 ? String.valueOf(assuredPurchase.width) : "");
        binding.editGst.setText(assuredPurchase.gstCharge > 0 ? String.valueOf(assuredPurchase.gstCharge) : "");
    }


    private void setBankInformationData()
    {
        if(bankInformation == null)
        {
            return;
        }

        binding.layoutBottomSheet.editGst.setText(bankInformation.gstn != null ? bankInformation.gstn : "");

        if(bankInformation.bankAccount == null)
        {
            return;
        }

        binding.layoutBottomSheet.editIfscCode.setText(bankInformation.bankAccount.ifsc != null ? bankInformation.bankAccount.ifsc : "");
        binding.layoutBottomSheet.editBankAccount.setText(bankInformation.bankAccount.number != null ? bankInformation.bankAccount.number : "");
    }


    private void addTextChangeListener()
    {
        binding.editBasePrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                setFinalPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        binding.editDiscount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                setFinalPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setFinalPrice()
    {
        if(binding.editBasePrice.getText().toString().isEmpty())
        {
            binding.editDiscount.setEnabled(false);
        }

        else
        {
            binding.editDiscount.setEnabled(true);
        }

        String basePrice = binding.editBasePrice.getText().toString().isEmpty() ? "0" : binding.editBasePrice.getText().toString();
        String discount = binding.editDiscount.getText().toString().isEmpty() ? "0" : binding.editDiscount.getText().toString();

        double finalPrice = Double.valueOf(basePrice) - Double.valueOf(discount);
        binding.labelFinalPrice.setText(Helper.getCurrencyFormatter().format(finalPrice));
    }

    private void placeholder()
    {
        String category;

        if(productType.equalsIgnoreCase("services"))
        {
            category = "Service";
        }

        else
        {
            category = "Product";
        }

        binding.labelProductName.setText(String.format(getString(R.string.label_product_name), category));
        binding.editProductName.setHint(String.format(getString(R.string.hint_product_name), category.toLowerCase()));

        binding.labelProductDescription.setText(String.format(getString(R.string.label_product_description), category));
        binding.editProductDescription.setHint(String.format(getString(R.string.hint_product_description), category.toLowerCase()));
    }


    private void addPropertyListener()
    {
        binding.layoutProductSpecification.buttonAddProperty.setOnClickListener(view -> {

            if(product.otherSpecification == null)
            {
                product.otherSpecification = new ArrayList<>();
            }

            product.otherSpecification.add(new com.nowfloats.Product_Gallery.Model.Product.Specification());
            adapter.notifyItemInserted(product.otherSpecification.size());
        });
    }


    private void displayPaymentAcceptanceMessage()
    {
        try
        {
            String transactionFees = WidgetKey.getPropertyValue(WidgetKey.WIDGET_TRANSACTION_FEES, WidgetKey.WIDGET_PROPERTY_TRANSACTION_FEES);

            if(Double.valueOf(transactionFees) > 0)
            {
                transactionFees = transactionFees.contains("%") ? transactionFees : transactionFees.concat("%");
                binding.layoutBottomSheet.tvPaymentConfigurationAcceptanceMessage.setText(String.format(String.valueOf(getString(R.string.payment_config_acceptance_message)), transactionFees));

                binding.layoutBottomSheet.layoutPaymentMethodAcceptance.setVisibility(View.VISIBLE);
            }

            else
            {
                binding.layoutBottomSheet.layoutPaymentMethodAcceptance.setVisibility(View.GONE);
            }
        }

        catch (Exception e)
        {
            binding.layoutBottomSheet.layoutPaymentMethodAcceptance.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void initPaymentAdapter()
    {
        binding.layoutBottomSheet.layoutAssuredPurchase.setVisibility(View.GONE);

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(getActivity());
        binding.layoutBottomSheet.spinnerPaymentOption.setAdapter(spinnerAdapter);

        binding.layoutBottomSheet.spinnerPaymentOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 0:

                        binding.layoutBottomSheet.layoutAssuredPurchase.setVisibility(View.VISIBLE);
                        break;

                    default:

                        binding.layoutBottomSheet.layoutAssuredPurchase.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.layoutBottomSheet.btnCancel.setOnClickListener(v -> toggleBottomSheet());
        binding.layoutBottomSheet.btnSaveInfo.setOnClickListener(v -> {

            switch (binding.layoutBottomSheet.spinnerPaymentOption.getSelectedItemPosition())
            {
                case 0:

                    paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE;
                    displayPaymentAcceptanceMessage();

                    if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue())
                            && productType.equalsIgnoreCase("products"))
                    {
                        binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.VISIBLE);
                        binding.layoutInventoryCod.layoutInventory.setVisibility(View.VISIBLE);
                        binding.layoutInventoryOnline.layoutInventory.setVisibility(View.VISIBLE);
                    }

                    else
                    {
                        binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                        binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                        binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);
                    }

                    if(binding.layoutBottomSheet.layoutPaymentMethodAcceptance.getVisibility() == View.VISIBLE)
                    {
                        Toast.makeText(getContext(), "Please accept terms and condition", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(isValidBankInformation())
                    {
                        saveBankInformation();
                    }

                    break;

                case 1:

                    paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.UNIQUE_PAYMENT_URL;

                    binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                    binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                    binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);

                    binding.layoutBottomSheet.layoutPaymentMethodAcceptance.setVisibility(View.GONE);

                    binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setVisibility(View.GONE);
                    binding.layoutPaymentMethod.layoutPaymentExternalPurchaseUrl.setVisibility(View.VISIBLE);

                    binding.layoutPaymentMethod.tvPaymentConfiguration.setText(paymentOptionTitles[1]);

                    toggleBottomSheet();
                    break;

                case 2:

                    paymentAndDeliveryMode = Constants.PaymentAndDeliveryMode.DONT_WANT_TO_SELL;
                    binding.layoutShippingMatrixDetails.layoutShippingMatrix.setVisibility(View.GONE);
                    binding.layoutInventoryCod.layoutInventory.setVisibility(View.GONE);
                    binding.layoutInventoryOnline.layoutInventory.setVisibility(View.GONE);

                    binding.layoutBottomSheet.layoutPaymentMethodAcceptance.setVisibility(View.GONE);

                    binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setVisibility(View.VISIBLE);
                    binding.layoutPaymentMethod.layoutPaymentExternalPurchaseUrl.setVisibility(View.GONE);

                    binding.layoutPaymentMethod.tvPaymentConfiguration.setText(paymentOptionTitles[2]);
                    binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setText(getString(R.string.payment_method_dont_want_to_sell));

                    toggleBottomSheet();
                    break;
            }
        });
    }


    private void addSwitchVariantListener()
    {
        binding.switchVariants.setOnToggledListener((labeledSwitch, isOn) -> {

        });
    }


    private void spinnerAddressListener()
    {
       binding.layoutBottomSheetAddress.buttonAddNew.setOnClickListener(v -> {

           sheetBehaviorAddress.setState(BottomSheetBehavior.STATE_COLLAPSED);
           openAddressDialog(null);
       });

       binding.layoutBottomSheet.tvPickAddress.setOnClickListener(view -> toggleAddressBottomSheet());
    }


    private void addBottomSheetListener()
    {
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View view, int i) {

                sheetBehaviorAddress.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }


    /**
     * Initialize service adapter
     * @param recyclerView
     */
    private void initProductSpecificationRecyclerView(RecyclerView recyclerView)
    {
        adapter = new ProductSpecificationRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    /**
     * Initialize product image adapter
     * @param recyclerView
     */
    private void initProductImageRecyclerView(RecyclerView recyclerView)
    {
        adapterImage = new ProductImageRecyclerAdapter();
        recyclerView.setAdapter(adapterImage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    /**
     * Initialize service adapter
     * @param recyclerView
     */
    private void initProductPickupAddressRecyclerView(RecyclerView recyclerView)
    {
        adapterAddress = new ProductPickupAddressRecyclerAdapter();
        recyclerView.setAdapter(adapterAddress);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void openAddressDialog(AddressInformation addressInformation)
    {
        if(addressInformation == null)
        {
            pickupAddressFragment = ProductPickupAddressFragment.newInstance();
        }

        else if(pickupAddressFragment == null)
        {
            pickupAddressFragment = ProductPickupAddressFragment.newInstance();
        }

        pickupAddressFragment.setAddress(addressInformation);
        pickupAddressFragment.isFileSelected(false);

        pickupAddressFragment.show(getFragmentManager(), "Address");

        pickupAddressFragment.setOnClickListener(information -> saveAddressInformation(information));

        pickupAddressFragment.setFileChooserListener(() -> chooseFile(Constant.REQUEST_CODE_PICK_FILE));
    }


    public void addPaymentConfigListener()
    {
        binding.layoutPaymentMethod.tvPaymentConfiguration.setOnClickListener(view -> toggleBottomSheet());
    }

    private void addImagePickerListener()
    {
        binding.cardPrimaryImage.setOnClickListener(v -> choosePicture(DIALOG_REQUEST_CODE_PRIMARY));
        binding.btnSecondaryImage.setOnClickListener(v -> choosePicture(DIALOG_REQUEST_CODE_SECONDARY));
    }


    private void choosePicture(int requestCode)
    {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.featuredimage_popup, true)
                .show();

        final PorterDuffColorFilter whiteLabelFilter_pop_ip = new PorterDuffColorFilter(getResources().getColor(R.color.primaryColor), PorterDuff.Mode.SRC_IN);

        View view = dialog.getCustomView();

        LinearLayout takeCamera = view.findViewById(R.id.cameraimage);
        LinearLayout takeGallery = view.findViewById(R.id.galleryimage);
        ImageView cameraImg = view.findViewById(R.id.pop_up_camera_imag);
        ImageView galleryImg = view.findViewById(R.id.pop_up_gallery_img);
        cameraImg.setColorFilter(whiteLabelFilter_pop_ip);
        galleryImg.setColorFilter(whiteLabelFilter_pop_ip);

        takeCamera.setOnClickListener(v -> {

            switch (requestCode)
            {
                case DIALOG_REQUEST_CODE_PRIMARY:

                    cameraIntent(CAMERA_PRIMARY_IMAGE_REQUEST_CODE);
                    break;

                case DIALOG_REQUEST_CODE_SECONDARY:

                    cameraIntent(CAMERA_SECONDARY_IMAGE_REQUEST_CODE);
                    break;
            }

            dialog.dismiss();
        });

        takeGallery.setOnClickListener(v -> {

            switch (requestCode)
            {
                case DIALOG_REQUEST_CODE_PRIMARY:

                    openImagePicker(GALLERY_PRIMARY_IMAGE_REQUEST_CODE, 1);
                    break;

                case DIALOG_REQUEST_CODE_SECONDARY:

                    int max = MAX_IMAGE_ALLOWED - adapterImage.getItemCount();
                    max = max > 0 ? max : 1;
                    openImagePicker(GALLERY_SECONDARY_IMAGE_REQUEST_CODE, max);
                    break;
            }

            dialog.dismiss();
        });
    }


    private void chooseFile(int requestCode)
    {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.layout_file_upload_dialog, true)
                .show();

        final PorterDuffColorFilter whiteLabelFilter_pop_ip = new PorterDuffColorFilter(getResources().getColor(R.color.primaryColor), PorterDuff.Mode.SRC_IN);

        View view = dialog.getCustomView();

        LinearLayout takeCamera = view.findViewById(R.id.cameraimage);
        LinearLayout takeGallery = view.findViewById(R.id.galleryimage);
        LinearLayout takeFile = view.findViewById(R.id.filepicker);

        ImageView cameraImg = view.findViewById(R.id.pop_up_camera_imag);
        ImageView galleryImg = view.findViewById(R.id.pop_up_gallery_img);
        ImageView fileImg = view.findViewById(R.id.pop_up_file_img);

        cameraImg.setColorFilter(whiteLabelFilter_pop_ip);
        galleryImg.setColorFilter(whiteLabelFilter_pop_ip);
        fileImg.setColorFilter(whiteLabelFilter_pop_ip);

        takeCamera.setOnClickListener(v -> {

            cameraIntent(CAMERA_PROOF_IMAGE_REQUEST_CODE);
            dialog.dismiss();
        });

        takeGallery.setOnClickListener(v -> {

            openImagePicker(GALLERY_PROOF_IMAGE_REQUEST_CODE, 1);
            dialog.dismiss();
        });

        takeFile.setOnClickListener(v -> {

            openFileChooser();
            dialog.dismiss();
        });
    }


    private void initCurrencyList()
    {
        currencyValue = getString(R.string.currency_text);
        binding.editCurrency.setText(currencyValue);

        Helper.loadCurrency();

        binding.editCurrency.setOnClickListener(view -> {

            String[] array = Constants.currencyArray.toArray(new String[Constants.currencyArray.size()]);
            Arrays.sort(array);

            showCurrencyList(array);
        });

        try
        {
            currencyValue = Constants.Currency_Country_Map.get(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRY).toLowerCase());
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void addQuantityListener()
    {
        binding.layoutInventory.quantityValue.setText(String.valueOf(product.availableUnits));
        binding.layoutInventoryOnline.quantityValue.setText(String.valueOf(product.maxPrepaidOnlineAvailable));
        binding.layoutInventoryCod.quantityValue.setText(String.valueOf(product.maxCodOrders));

        binding.layoutInventory.addQuantity.setOnClickListener(view -> {

            try
            {
                product.availableUnits++;
                binding.layoutInventory.quantityValue.setText(String.valueOf(product.availableUnits));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        binding.layoutInventory.removeQuantity.setOnClickListener(view -> {

            try
            {
                if(product.availableUnits > 0)
                {
                    product.availableUnits--;
                }

                binding.layoutInventory.quantityValue.setText(String.valueOf(product.availableUnits));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        binding.layoutInventoryOnline.addQuantity.setOnClickListener(view -> {

            try
            {
                product.maxPrepaidOnlineAvailable++;
                binding.layoutInventoryOnline.quantityValue.setText(String.valueOf(product.maxPrepaidOnlineAvailable));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        binding.layoutInventoryOnline.removeQuantity.setOnClickListener(view -> {

            try
            {
                if(product.maxPrepaidOnlineAvailable > 0)
                {
                    product.maxPrepaidOnlineAvailable--;
                }

                binding.layoutInventoryOnline.quantityValue.setText(String.valueOf(product.maxPrepaidOnlineAvailable));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        binding.layoutInventoryCod.addQuantity.setOnClickListener(view -> {

            try
            {
                product.maxCodOrders++;
                binding.layoutInventoryCod.quantityValue.setText(String.valueOf(product.maxCodOrders));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        binding.layoutInventoryCod.removeQuantity.setOnClickListener(view -> {

            try
            {
                if(product.maxCodOrders > 0)
                {
                    product.maxCodOrders--;
                }

                binding.layoutInventoryCod.quantityValue.setText(String.valueOf(product.maxCodOrders));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }


    private void addSpinnerListener()
    {
        String[] stockOptions = getResources().getStringArray(R.array.stock_options);
        String[] stockAvailability = getResources().getStringArray(R.array.stock_availability);

        ArrayAdapter<String> spinner1 = new ArrayAdapter<>(getActivity(), R.layout.customized_spinner_item, stockAvailability);
        ArrayAdapter<String> spinner2 = new ArrayAdapter<>(getActivity(), R.layout.customized_spinner_item, stockOptions);

        binding.layoutInventory.spinnerStockAvailability.setAdapter(spinner1);
        binding.layoutInventoryOnline.spinnerStockAvailability.setAdapter(spinner2);
        binding.layoutInventoryCod.spinnerStockAvailability.setAdapter(spinner2);

        binding.layoutInventory.spinnerStockAvailability.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 0:

                        binding.layoutInventory.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_availble_indicator));
                        binding.layoutInventory.layoutQuantityMain.setVisibility(View.VISIBLE);
                        break;

                    case 1:

                        binding.layoutInventory.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_availble_indicator));
                        binding.layoutInventory.layoutQuantityMain.setVisibility(View.INVISIBLE);
                        break;

                    case 2:

                        binding.layoutInventory.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_unavailble_indicator));
                        binding.layoutInventory.layoutQuantityMain.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.layoutInventoryOnline.spinnerStockAvailability.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 0:

                        binding.layoutInventoryOnline.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_availble_indicator));
                        binding.layoutInventoryOnline.layoutQuantityMain.setVisibility(View.VISIBLE);
                        break;

                    case 1:

                        binding.layoutInventoryOnline.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_unavailble_indicator));
                        binding.layoutInventoryOnline.layoutQuantityMain.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.layoutInventoryCod.spinnerStockAvailability.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 0:

                        binding.layoutInventoryCod.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_availble_indicator));
                        binding.layoutInventoryCod.layoutQuantityMain.setVisibility(View.VISIBLE);
                        break;

                    case 1:

                        binding.layoutInventoryCod.ivStockIndicator.setImageDrawable(getResources().getDrawable(R.drawable.ic_unavailble_indicator));
                        binding.layoutInventoryCod.layoutQuantityMain.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public String showCurrencyList(final String[] currencyList)
    {
        String currencyVal = binding.editCurrency.getText().toString().trim();
        int index = 0;

        if (!Util.isNullOrEmpty(currencyVal))
        {
            index = Arrays.asList(currencyList).indexOf(currencyVal);
        }

        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.select_currency))
                .items(currencyList)
                .widgetColorRes(R.color.primaryColor)
                .itemsCallbackSingleChoice(index, (dialog, view, position, text) -> {

                        try
                        {
                            currencyType = currencyList[position];
                            String s = currencyType.split("-")[1];
                            binding.editCurrency.setText(s);
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                        return true;
                }).show();

        return currencyType;
    }


    /**
     * Product Specification Dynamic Input Filed
     */
    class ProductSpecificationRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_product_specification_input, viewGroup, false);
            return new ProductSpecificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i)
        {
            if (holder instanceof ProductSpecificationViewHolder)
            {
                final ProductSpecificationViewHolder viewHolder = (ProductSpecificationViewHolder) holder;

                viewHolder.ibRemove.setVisibility(View.VISIBLE);

                viewHolder.ibRemove.setOnClickListener(view -> {

                    product.otherSpecification.remove(viewHolder.getAdapterPosition());
                    notifyItemRemoved(viewHolder.getAdapterPosition());
                });

                com.nowfloats.Product_Gallery.Model.Product.Specification specification = product.otherSpecification.get(i);

                viewHolder.editKey.setText(specification.key != null ? specification.key : "");
                viewHolder.editValue.setText(specification.value != null ? specification.value : "");

                viewHolder.editKey.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        product.otherSpecification.get(viewHolder.getAdapterPosition()).key = s.toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                viewHolder.editValue.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        product.otherSpecification.get(viewHolder.getAdapterPosition()).value = s.toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        }

        @Override
        public int getItemCount()
        {
            return product.otherSpecification == null ? 0 : product.otherSpecification.size();
        }


        class ProductSpecificationViewHolder extends RecyclerView.ViewHolder
        {
            ImageButton ibRemove;
            EditText editKey;
            EditText editValue;

            private ProductSpecificationViewHolder(View itemView)
            {
                super(itemView);

                ibRemove = itemView.findViewById(R.id.ib_remove);
                editKey = itemView.findViewById(R.id.editKey);
                editValue = itemView.findViewById(R.id.editValue);
            }
        }

        public void setData(List<com.nowfloats.Product_Gallery.Model.Product.Specification> specificationList)
        {
            product.otherSpecification.addAll(specificationList);
            notifyDataSetChanged();
        }

        public boolean isValid()
        {
            boolean isValid = true;

            if(product.otherSpecification == null)
            {
                return isValid;
            }

            for(com.nowfloats.Product_Gallery.Model.Product.Specification specification: product.otherSpecification)
            {
                if(TextUtils.isEmpty(specification.key) || TextUtils.isEmpty(specification.value))
                {
                    isValid = false;
                    break;
                }
            }

            return isValid;
        }
    }


    /**
     * Product Image Dynamic Input Filed
     */
    class ProductImageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_product_secondary_image, viewGroup, false);
            return new ProductImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i)
        {
            if (holder instanceof ProductImageViewHolder)
            {
                final ProductImageViewHolder viewHolder = (ProductImageViewHolder) holder;

                viewHolder.ib_remove.setOnClickListener(view -> {

                    if(!TextUtils.isEmpty(imageList.get(viewHolder.getAdapterPosition()).getId()))
                    {
                        deleteImage(imageList.get(viewHolder.getAdapterPosition()));
                    }

                    imageList.remove(viewHolder.getAdapterPosition());
                    notifyItemRemoved(viewHolder.getAdapterPosition());
                    displayImageAddButton();
                });

                ProductImageResponseModel image = imageList.get(i);
                ImageLoader.load(getContext(), image.getImage().url, viewHolder.iv_image);
                viewHolder.tv_image_name.setText(image.getImage().description != null ? image.getImage().description : "");
            }
        }

        @Override
        public int getItemCount()
        {
            return imageList.size();
        }


        public void setData(List<ProductImageResponseModel> images)
        {
            imageList.addAll(images);
            notifyDataSetChanged();
        }

        class ProductImageViewHolder extends RecyclerView.ViewHolder
        {
            Button btn_change;
            ImageButton ib_remove;
            ImageView iv_image;
            TextView tv_image_name;

            private ProductImageViewHolder(View itemView)
            {
                super(itemView);

                btn_change = itemView.findViewById(R.id.btn_change);
                ib_remove = itemView.findViewById(R.id.ib_remove);
                iv_image = itemView.findViewById(R.id.iv_image);
                tv_image_name = itemView.findViewById(R.id.label_image_name);
            }
        }
    }



    /**
     * Product Pickup Address Dynamic Input Filed
     */
    class ProductPickupAddressRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_pickup_address, viewGroup, false);
            return new ProductPickupAddressViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i)
        {
            if (holder instanceof ProductPickupAddressViewHolder)
            {
                final ProductPickupAddressViewHolder viewHolder = (ProductPickupAddressViewHolder) holder;

                AddressInformation addressInformation = addressInformationList.get(i);

                viewHolder.tvType.setText(addressInformation.areaName != null ? addressInformation.areaName : "");
                viewHolder.tvAddress.setText(addressInformation.toString());
            }
        }

        @Override
        public int getItemCount()
        {
            return addressInformationList == null ? 0 : addressInformationList.size();
        }

        class ProductPickupAddressViewHolder extends RecyclerView.ViewHolder
        {
            TextView tvType;
            TextView tvAddress;

            private ProductPickupAddressViewHolder(View itemView)
            {
                super(itemView);

                tvType = itemView.findViewById(R.id.label_type);
                tvAddress = itemView.findViewById(R.id.label_address);

                tvType.setPaintFlags(tvType.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                tvType.setOnClickListener(v -> {

                    sheetBehaviorAddress.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    openAddressDialog(addressInformationList.get(getAdapterPosition()));
                });

                itemView.setOnClickListener(v -> {

                    product.pickupAddressReferenceId = addressInformationList.get(getAdapterPosition()).id;
                    sheetBehaviorAddress.setState(BottomSheetBehavior.STATE_COLLAPSED);
                });
            }
        }

        public void setData(List<AddressInformation> informationList)
        {
            if(addressInformationList == null)
            {
                addressInformationList = new ArrayList<>();
            }

            addressInformationList.clear();
            addressInformationList.addAll(informationList);
            notifyDataSetChanged();
        }

        public void addData(AddressInformation information)
        {
            if(addressInformationList == null)
            {
                addressInformationList = new ArrayList<>();
            }

            addressInformationList.add(information);
            notifyItemInserted(addressInformationList.size());
        }
    }


    private void openFileChooser()
    {
        int limit = 1;

        Intent intent4 = new Intent(getActivity(), NormalFilePickActivity.class);
        intent4.putExtra(Constant.MAX_NUMBER, limit);
        intent4.putExtra(NormalFilePickActivity.SUFFIX, FILE_EXTENSIONS);
        startActivityForResult(intent4, Constant.REQUEST_CODE_PICK_FILE);
    }


    private void openImagePicker(int requestCode, int max)
    {
        boolean folderMode = true;
        boolean multipleMode = true;

        ImagePicker.with(getActivity())
                .setFolderMode(folderMode)
                .setShowCamera(false)
                .setFolderTitle("Album")
                .setMultipleMode(multipleMode)
                .setMaxSize(max)
                .setBackgroundColor("#212121")
                .setAlwaysShowDoneButton(true)
                .setRequestCode(requestCode)
                .setKeepScreenOn(true)
                .start();
    }

    private void displayImageAddButton()
    {
        if(MAX_IMAGE_ALLOWED > adapterImage.getItemCount())
        {
            binding.layoutAddImage.setVisibility(View.VISIBLE);
        }

        else
        {
            binding.layoutAddImage.setVisibility(View.GONE);
        }
    }

    private void cameraIntent(int requestCode)
    {
        try
        {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {

                    Methods.showApplicationPermissions("Camera And Storage Permission", "We need these permission to enable capture and upload images", getActivity());
                }

                else
                {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }

            else
            {
                startCamera(requestCode);
            }
        }

        catch (ActivityNotFoundException e)
        {
            String errorMessage = getString(R.string.device_does_not_support_capturing_image);
            Methods.showSnackBarNegative(getActivity(), errorMessage);
        }
    }


    private void startCamera(int requestCode)
    {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "boost");
        Uri tempUri;

        if(!mediaStorageDir.exists())
        {
            mediaStorageDir.mkdir();
        }

        /**
         * Check if we're running on Android 5.0 or higher
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            tempUri = FileProvider.getUriForFile(getActivity(),
                    Constants.PACKAGE_NAME + ".provider",
                    new File(mediaStorageDir + "/" + System.currentTimeMillis() + ".jpg"));
        }

        else
        {
            tempUri = Uri.fromFile(new File(mediaStorageDir + "/" + System.currentTimeMillis() + ".jpg"));
        }

        switch (requestCode)
        {
            case CAMERA_PRIMARY_IMAGE_REQUEST_CODE:

                primaryUri = tempUri;
                break;

            case CAMERA_SECONDARY_IMAGE_REQUEST_CODE:

                secondaryUri = tempUri;
                break;

            case CAMERA_PROOF_IMAGE_REQUEST_CODE:

                proofUri = tempUri;
                break;
        }

        try
        {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
            startActivityForResult(intent, requestCode);
        }

        catch (Exception e)
        {
            Toast.makeText(getContext(), "Failed to Open Camera", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleBottomSheet()
    {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
        {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        else
        {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }


    private void toggleAddressBottomSheet()
    {
        if (sheetBehaviorAddress.getState() != BottomSheetBehavior.STATE_EXPANDED)
        {
            sheetBehaviorAddress.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        else
        {
            sheetBehaviorAddress.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && /*requestCode == Config.RC_PICK_IMAGES*/ (requestCode == GALLERY_PRIMARY_IMAGE_REQUEST_CODE ||
                requestCode == GALLERY_SECONDARY_IMAGE_REQUEST_CODE || requestCode == GALLERY_PROOF_IMAGE_REQUEST_CODE) && data != null)
        {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);

            if(images.size() > 0 && requestCode == GALLERY_PRIMARY_IMAGE_REQUEST_CODE)
            {
                primaryUri = Uri.fromFile(new File(images.get(0).getPath()));
                display_image(primaryUri.getPath(), GALLERY_PRIMARY_IMAGE_REQUEST_CODE);
            }

            if(images.size() > 0 && requestCode == GALLERY_PROOF_IMAGE_REQUEST_CODE)
            {
                file = new File(images.get(0).getPath());

                pickupAddressFragment.setFileName(file.getName());
                pickupAddressFragment.isFileSelected(true);
            }

            else if(requestCode == GALLERY_SECONDARY_IMAGE_REQUEST_CODE)
            {
                for (Image image: images)
                {
                    display_image(image.getPath(), GALLERY_SECONDARY_IMAGE_REQUEST_CODE);
                }
            }
        }

        else if(resultCode == RESULT_OK && (requestCode == CAMERA_PRIMARY_IMAGE_REQUEST_CODE ||
                requestCode == CAMERA_SECONDARY_IMAGE_REQUEST_CODE || requestCode == CAMERA_PROOF_IMAGE_REQUEST_CODE)) {

            switch (requestCode)
            {
                case CAMERA_PRIMARY_IMAGE_REQUEST_CODE:

                    display_image(primaryUri.getPath(), requestCode);
                    break;

                case CAMERA_SECONDARY_IMAGE_REQUEST_CODE:

                    display_image(secondaryUri.getPath(), requestCode);
                    break;

                case CAMERA_PROOF_IMAGE_REQUEST_CODE:

                    file = new File(proofUri.getPath());

                    pickupAddressFragment.setFileName(file.getName());
                    pickupAddressFragment.isFileSelected(true);
                    break;
            }
        }

        else if(requestCode == Constant.REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null)
        {
            ArrayList<NormalFile> files = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);

            if(files.size() > 0)
            {
                file = new File(files.get(0).getPath());

                pickupAddressFragment.setFileName(file.getName());
                pickupAddressFragment.isFileSelected(true);
            }
        }
    }


    private void display_image(String path, int requestCode)
    {
        if(Helper.fileExist(path))
        {
            try
            {

                File file = new File(path);

                // bitmap factory
                // BitmapFactory.Options options = new BitmapFactory.Options();
                // downsizing image as it throws OutOfMemory Exception for larger
                // images
                // options.inSampleSize = 4;
                // final Bitmap bitmap = BitmapFactory.decodeFile(f.getPath(), options);
                // binding.ivPrimaryImage.setImageBitmap(bitmap);

                if(requestCode == CAMERA_PRIMARY_IMAGE_REQUEST_CODE || requestCode == GALLERY_PRIMARY_IMAGE_REQUEST_CODE)
                {
                    ImageLoader.load(getContext(), file, binding.ivPrimaryImage);
                }

                if(requestCode == CAMERA_SECONDARY_IMAGE_REQUEST_CODE || requestCode == GALLERY_SECONDARY_IMAGE_REQUEST_CODE)
                {
                    List<ProductImageResponseModel> imageList = new ArrayList<>();

                    ProductImageResponseModel responseModel = new ProductImageResponseModel();
                    responseModel.setImage(new ProductImage(path, file.getName()));
                    imageList.add(responseModel);

                    adapterImage.setData(imageList);
                }
            }

            catch(Exception e)
            {
                Toast.makeText(getContext(), "Failed to Set Image", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            finally
            {
                displayImageAddButton();
            }
        }

        else
        {
            Toast.makeText(getContext(), "File Not Found", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Product Validation
     * @return
     */
    private boolean isValidProduct()
    {
        if(product == null)
        {
            return false;
        }

        if (product.productId == null && primaryUri == null)
        {
            Toast.makeText(getContext(), "Add product image", Toast.LENGTH_LONG).show();
            return false;
        }

        if (binding.editProductName.getText().toString().trim().length() == 0)
        {
            Toast.makeText(getContext(), "Enter product name", Toast.LENGTH_LONG).show();
            return false;
        }

        if (binding.editProductDescription.getText().toString().trim().length() == 0)
        {
            Toast.makeText(getContext(), "Enter product description", Toast.LENGTH_LONG).show();
            return false;
        }

        if (binding.editBasePrice.getText().toString().trim().length() > 0) {

            try
            {
                Double.valueOf(binding.editBasePrice.getText().toString().trim());
            }

            catch (Exception e)
            {
                Toast.makeText(getContext(), "Enter valid price", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (binding.editDiscount.getText().toString().trim().length() > 0)
        {
            try
            {
                Double.valueOf(binding.editDiscount.getText().toString().trim());
            }

            catch (Exception e)
            {
                Toast.makeText(getContext(), "Enter valid discount", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        /*if(binding.layoutProductSpecification.layoutKeySpecification.editKey.getText().toString().trim().length() == 0 ||
                binding.layoutProductSpecification.layoutKeySpecification.editValue.getText().toString().trim().length() == 0)
        {
            Toast.makeText(getContext(), "Enter product specification", Toast.LENGTH_LONG).show();
            return false;
        }*/

        if(!adapter.isValid())
        {
            Toast.makeText(getContext(), "Enter all specification values", Toast.LENGTH_LONG).show();
            return false;
        }

        if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue())
                && productType.equalsIgnoreCase("products"))
        {
            if(product.pickupAddressReferenceId == null || product.pickupAddressReferenceId.isEmpty())
            {
                Toast.makeText(getContext(), "Pickup Address Required", Toast.LENGTH_LONG).show();
                return false;
            }

            return isValidAssuredPurchase();
        }

        return true;
    }


    /**
     * Assured Purchase Validation
     * @return
     */
    private boolean isValidAssuredPurchase()
    {
        if(binding.layoutShippingMatrixDetails.editWeight.getText().toString().trim().length() == 0)
        {
            binding.layoutShippingMatrixDetails.editWeight.requestFocus();
            Toast.makeText(getContext(), "Enter product weight", Toast.LENGTH_LONG).show();
            return false;
        }

        if(binding.layoutShippingMatrixDetails.editLength.getText().toString().trim().length() == 0)
        {
            binding.layoutShippingMatrixDetails.editLength.requestFocus();
            Toast.makeText(getContext(), "Enter product length", Toast.LENGTH_LONG).show();
            return false;
        }

        if(binding.layoutShippingMatrixDetails.editHeight.getText().toString().trim().length() == 0)
        {
            binding.layoutShippingMatrixDetails.editHeight.requestFocus();
            Toast.makeText(getContext(), "Enter product height", Toast.LENGTH_LONG).show();
            return false;
        }

        if(binding.layoutShippingMatrixDetails.editThickness.getText().toString().trim().length() == 0)
        {
            binding.layoutShippingMatrixDetails.editThickness.requestFocus();
            Toast.makeText(getContext(), "Enter product thickness", Toast.LENGTH_LONG).show();
            return false;
        }

        try
        {
            Double.valueOf(binding.layoutShippingMatrixDetails.editWeight.getText().toString().trim());
            Double.valueOf(binding.layoutShippingMatrixDetails.editLength.getText().toString().trim());
            Double.valueOf(binding.layoutShippingMatrixDetails.editHeight.getText().toString().trim());
            Double.valueOf(binding.layoutShippingMatrixDetails.editThickness.getText().toString().trim());
        }

        catch (Exception e)
        {
            Toast.makeText(getContext(), "Enter valid package dimensions", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    private boolean isValidBankInformation()
    {
        if(binding.layoutBottomSheet.editBankAccount.getText().toString().trim().length() == 0)
        {
            binding.layoutBottomSheet.editBankAccount.requestFocus();
            Toast.makeText(getContext(), "Enter bank account number", Toast.LENGTH_LONG).show();
            return false;
        }

        if(binding.layoutBottomSheet.editIfscCode.getText().toString().trim().length() == 0)
        {
            binding.layoutBottomSheet.editIfscCode.requestFocus();
            Toast.makeText(getContext(), "Enter IFSC", Toast.LENGTH_LONG).show();
            return false;
        }

        if(binding.layoutBottomSheet.editGst.toString().trim().length() == 0)
        {
            binding.layoutBottomSheet.editGst.requestFocus();
            Toast.makeText(getContext(), "Enter GST/Tax ID", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    private boolean isValidAddress()
    {
        if(file == null)
        {
            Toast.makeText(getContext(), "Address proof required", Toast.LENGTH_LONG).show();
            return false;
        }

        return pickupAddressFragment.isValid();
    }


    private void saveAddress()
    {
        Constants.assuredPurchaseRestAdapterDev.create(ProductGalleryInterface.class)
                .savePickupAddress(addressInformation, new Callback<WebResponseModel<AddressInformation>>() {

                    @Override
                    public void success(WebResponseModel<AddressInformation> webResponseModel, Response response) {

                        hideDialog();

                        if(webResponseModel != null && webResponseModel.getData() != null)
                        {
                            AddressInformation addressResponse = webResponseModel.getData();

                            if(TextUtils.isEmpty(addressInformation.id))
                            {
                                adapterAddress.addData(addressResponse);
                                Toast.makeText(getContext(), "Address Added Successfully", Toast.LENGTH_LONG).show();
                            }

                            else
                            {
                                for(int i=0; i<addressInformationList.size(); i++)
                                {
                                    if(addressInformation.id.equals(addressInformationList.get(i).id))
                                    {
                                        addressInformationList.add(i, addressResponse);
                                        adapterAddress.notifyItemChanged(i);
                                        break;
                                    }
                                }

                                Toast.makeText(getContext(), "Address Updated Successfully", Toast.LENGTH_LONG).show();
                            }

                            product.pickupAddressReferenceId = webResponseModel.getData().id;
                            addressInformation.id = webResponseModel.getData().id;
                        }

                        Log.d("PRODUCT_JSON", "Address Successfully Added/Updated");
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        hideDialog();
                        Toast.makeText(getContext(), "Failed to save address", Toast.LENGTH_LONG).show();
                        Log.d("PRODUCT_JSON", "FAIL " + error.getMessage() + " CODE " + error.getSuccessType());
                    }
                });
    }


    private void saveAddressInformation(AddressInformation information)
    {
        if(!isValidAddress())
        {
            return;
        }

        addressInformation = information;
        addressInformation.websiteId = session.getFPID();

        this.uploadFile(file);
    }


    private void getAddressInformation()
    {
        Constants.assuredPurchaseRestAdapterDev.create(ProductGalleryInterface.class)
                .getPickupAddress(session.getFPID(), new Callback<WebResponseModel<List<AddressInformation>>>() {

                    @Override
                    public void success(WebResponseModel<List<AddressInformation>> webResponseModel, Response response) {

                        if(webResponseModel != null && webResponseModel.getData() != null)
                        {
                            adapterAddress.setData(webResponseModel.getData());
                        }

                        Log.d("PRODUCT_JSON", "GET ADDRESS");
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        Log.d("PRODUCT_JSON", "GET ADDRESS FAIL");
                    }
                });
    }

    /**
     * Initialize Product Object
     * @return
     */
    private void initProduct() {

        try
        {
            product.CurrencyCode = binding.editCurrency.getText().toString();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        product.Name = binding.editProductName.getText().toString();
        product.Description = binding.editProductDescription.getText().toString();
        product.brandName = binding.editBrand.getText().toString().trim().length() > 0 ? binding.editBrand.getText().toString() : null;
        product.Price = binding.editBasePrice.getText().toString().trim().length() > 0 ? Double.valueOf(binding.editBasePrice.getText().toString().trim()) : 0;
        product.DiscountAmount = binding.editDiscount.getText().toString().trim().length() > 0 ? Double.valueOf(binding.editDiscount.getText().toString().trim()) : 0;

        product.category = CATEGORY;
        product.paymentType = paymentAndDeliveryMode.getValue();

        if(product.keySpecification == null)
        {
            product.keySpecification = new com.nowfloats.Product_Gallery.Model.Product.Specification();
        }

        if(!TextUtils.isEmpty(product.keySpecification.key))
        {
            product.keySpecification.key = binding.layoutProductSpecification.layoutKeySpecification.editKey.getText().toString();
        }

        if(!TextUtils.isEmpty(product.keySpecification.value))
        {
            product.keySpecification.value = binding.layoutProductSpecification.layoutKeySpecification.editValue.getText().toString();
        }

        if(binding.layoutInventory.spinnerStockAvailability.getSelectedItemPosition() == 0)
        {
            product.IsAvailable = true;
            product.availableUnits = Integer.valueOf(binding.layoutInventory.quantityValue.getText().toString().trim());
        }

        if(binding.layoutInventory.spinnerStockAvailability.getSelectedItemPosition() == 1)
        {
            product.IsAvailable = true;
            product.availableUnits = -1;
        }

        if(binding.layoutInventory.spinnerStockAvailability.getSelectedItemPosition() == 2)
        {
            product.IsAvailable = false;
            product.availableUnits = 0;
        }

        if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue())
                && productType.equalsIgnoreCase("products"))
        {
            product.codAvailable = (binding.layoutInventoryCod.spinnerStockAvailability.getSelectedItemPosition() == 0);
            product.prepaidOnlineAvailable = (binding.layoutInventoryOnline.spinnerStockAvailability.getSelectedItemPosition() == 0);

            product.maxCodOrders = Integer.valueOf(binding.layoutInventoryCod.quantityValue.getText().toString().trim());
            product.maxPrepaidOnlineAvailable = Integer.valueOf(binding.layoutInventoryOnline.quantityValue.getText().toString().trim());
        }

        else
        {
            product.codAvailable = false;
            product.prepaidOnlineAvailable = false;

            product.maxCodOrders = 0;
            product.maxPrepaidOnlineAvailable = 0;
        }

        if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.UNIQUE_PAYMENT_URL.getValue()))
        {
            if(product.BuyOnlineLink == null)
            {
                product.BuyOnlineLink = new com.nowfloats.Product_Gallery.Model.Product.BuyOnlineLink();
            }

            product.BuyOnlineLink.description = binding.layoutPaymentMethod.editDescription.getText().toString();
            product.BuyOnlineLink.url = binding.layoutPaymentMethod.editPurchaseUrlLink.getText().toString();
        }
    }


    /**
     * Initialize Assured Purchase
     * @param productId
     * @return
     */
    private AssuredPurchase initAssuredPurchase(String productId)
    {
        if(assuredPurchase == null)
        {
            assuredPurchase = new AssuredPurchase();
        }

        try
        {
            assuredPurchase.productId = productId;
            assuredPurchase.merchantId = session.getFPID();

            assuredPurchase.height = Double.valueOf(binding.layoutShippingMatrixDetails.editHeight.getText().toString().trim());
            assuredPurchase.weight = Double.valueOf(binding.layoutShippingMatrixDetails.editWeight.getText().toString().trim());
            assuredPurchase.length = Double.valueOf(binding.layoutShippingMatrixDetails.editLength.getText().toString().trim());
            assuredPurchase.width = Double.valueOf(binding.layoutShippingMatrixDetails.editThickness.getText().toString().trim());
            assuredPurchase.gstCharge = Double.valueOf(binding.editGst.getText().toString().trim());
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        return assuredPurchase;
    }


    private BankInformation initBankInformation()
    {
        if(bankInformation == null)
        {
            bankInformation = new BankInformation();
        }

        if(bankInformation.bankAccount == null)
        {
            bankInformation.bankAccount = new BankInformation.BankAccount();
        }

        bankInformation.bankAccount.number = binding.layoutBottomSheet.editBankAccount.getText().toString();
        bankInformation.bankAccount.ifsc = binding.layoutBottomSheet.editIfscCode.getText().toString();

        bankInformation.gstn = binding.layoutBottomSheet.editGst.getText().toString();

        return bankInformation;
    }



    private void uploadProductImage(String productId)
    {
        try
        {
            String valuesStr = "clientId=" + Constants.clientId
                    + "&requestType=sequential&requestId=" + Constants.deviceId
                    + "&totalChunks=1&currentChunkNumber=1&productId=" + productId;

            String url = Constants.NOW_FLOATS_API_URL + "/Product/v1/AddImage?" + valuesStr;

            byte[] imageBytes = Methods.compressToByte(primaryUri.getPath(), getActivity());

            UploadImage upload = new UploadImage(url, imageBytes, productId);
            upload.setImageUploadListener(this);
            upload.execute();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Methods.showSnackBarNegative(getActivity(), getString(R.string.something_went_wrong_try_again));
        }
    }


    /**
     * Update assured purchase
     * @param productId
     */
    private void updateAssuredPurchase(String productId)
    {
        WaUpdateDataModel update = new WaUpdateDataModel();
        final AssuredPurchase assuredPurchase = initAssuredPurchase(productId);

        update.setQuery(String.format("{product_id:'%s'}", assuredPurchase.productId));

        update.setUpdateValue(String.format("{$set:{length:'%s', width:'%s', weight:'%s', height:'%s', gst_slab:'%s'}}",
                assuredPurchase.length,
                assuredPurchase.width,
                assuredPurchase.weight,
                assuredPurchase.height,
                assuredPurchase.gstCharge));

        update.setMulti(true);


        /**
         * Update API call
         */
        Constants.webActionAdapter.create(ProductGalleryInterface.class)
                .updateAssuredPurchaseDetails(update, new Callback<String>() {

                    @Override
                    public void success(String s, Response response) {

                    }

                    @Override
                    public void failure(RetrofitError error) {

                        if(error.getResponse().getStatus() == 200)
                        {

                        }
                    }
                });
    }


    /**
     * Save bank information
     */
    private void saveBankInformation()
    {
        showDialog("Updating Seller Profile ...");

        BankInformation bankInformation = initBankInformation();
        bankInformation.sellerId = session.getFpTag();

        Log.d("PRODUCT_JSON", "BANK INFO JSON: " + new Gson().toJson(bankInformation));

        Constants.assuredPurchaseRestAdapterDev.create(ProductGalleryInterface.class)
                .saveBankInformation(bankInformation, new Callback<WebResponseModel<Object>>() {

                    @Override
                    public void success(WebResponseModel<Object> webResponseModel, Response response) {

                        Log.d("PRODUCT_JSON", "Bank Information Saved");

                        Toast.makeText(getContext(), "Seller Profile Updated", Toast.LENGTH_SHORT).show();
                        hideDialog();

                        binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setVisibility(View.VISIBLE);
                        binding.layoutPaymentMethod.layoutPaymentExternalPurchaseUrl.setVisibility(View.GONE);

                        binding.layoutPaymentMethod.tvPaymentConfiguration.setText(paymentOptionTitles[0]);
                        binding.layoutPaymentMethod.tvPaymentConfigurationMessage.setText(getString(R.string.payment_methud_message));

                        toggleBottomSheet();
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        Toast.makeText(getContext(), "Failed to Update Seller Profile", Toast.LENGTH_SHORT).show();
                        hideDialog();
                        Log.d("PRODUCT_JSON", "Failed to Save Bank Information");
                    }
                });
    }


    /**
     * Save bank information
     */
    private void getBankInformation()
    {
        Constants.assuredPurchaseRestAdapterDev.create(ProductGalleryInterface.class)
                .getBankInformation(session.getFpTag(), new Callback<WebResponseModel<BankInformation>>() {

                    @Override
                    public void success(WebResponseModel<BankInformation> webResponseModel, Response response) {

                        if(webResponseModel !=  null)
                        {
                            bankInformation = webResponseModel.getData();
                            setBankInformationData();
                        }

                        Log.d("PRODUCT_JSON", "SUCCESS");
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        Log.d("PRODUCT_JSON", "FAIL " + error.getMessage() + " CODE " + error.getSuccessType());
                    }
                });
    }

    /**
     * Save assured purchase
     * @param productId
     */
    private void saveAssuredPurchase(String productId)
    {
        AssuredPurchase assuredPurchase = initAssuredPurchase(productId);

        WAAddDataModel<AssuredPurchase> waModel = new WAAddDataModel<>();
        waModel.setWebsiteId(session.getFPID());
        waModel.setActionData(assuredPurchase);

        Log.d("PRODUCT_JSON", "JSON: " + new Gson().toJson(waModel));

        Constants.webActionAdapter.create(ProductGalleryInterface.class)
                .addAssuredPurchaseDetails(waModel, new Callback<String>() {

                    @Override
                    public void success(String s, Response response) {

                        Log.d("PRODUCT_JSON", "Assured Purchase Saved : " + s);
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        Log.d("PRODUCT_JSON", "Failed to Save Assured Purchase " + error.getMessage());
                        Log.d("PRODUCT_JSON", "Failed to Save Assured Purchase " + error.getBody());
                    }
                });
    }

    private void updateProduct()
    {
        ProductGalleryInterface productInterface = Constants.restAdapterDev.create(ProductGalleryInterface.class);

        try
        {
            if (isValidProduct())
            {
                initProduct();

                ArrayList<UpdateValue> updates = new ArrayList<>();

                JSONObject json = new JSONObject(new Gson().toJson(product));
                Iterator<String> keys = json.keys();

                while(keys.hasNext())
                {
                    String key = keys.next();
                    updates.add(new UpdateValue(key, json.get(key).toString()));
                }

                Product_Gallery_Update_Model model = new Product_Gallery_Update_Model(Constants.clientId, product.productId, updates);

                showDialog("Please Wait...");

                productInterface.put_UpdateGalleryUpdate(model, new Callback<ArrayList<String>>() {

                    @Override
                    public void success(ArrayList<String> strings, Response response) {

                        Log.d("PRODUCT_JSON", "Product Updated Successfully");

                        /*if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue())
                                && productType.equalsIgnoreCase("products"))
                        {
                            updateAssuredPurchase(product.productId);
                        }*/

                        updateAssuredPurchase(product.productId);

                        for(ProductImageResponseModel image: imageList)
                        {
                            if(TextUtils.isEmpty(image.getId()))
                            {
                                new MultipleFileUpload(product.productId, session, mWebAction, image.getImage()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }

                        if(primaryUri != null)
                        {
                            uploadProductImage(product.productId);
                        }

                        else
                        {
                            Toast.makeText(getContext(), "Information updated successfully", Toast.LENGTH_SHORT).show();
                            hideDialog();

                            if(getActivity() != null)
                            {
                                Intent data = new Intent();
                                data.putExtra("LOAD", true);
                                getActivity().setResult(RESULT_OK, data);
                                getActivity().finish();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                        Log.d("PRODUCT_JSON", "Failed to Save Product");

                        hideDialog();
                        Toast.makeText(getContext(), "Failed to update information", Toast.LENGTH_LONG).show();
                        Log.d("PRODUCT_JSON", "FAIL " + error.getMessage());
                    }
                });

                Log.d("JSON_PRODUCT_UPDATE", "" + new Gson().toJson(model));
            }
        }

        catch (Exception e)
        {
            Log.d("JSON_PRODUCT_UPDATE", "ERROR " + e.getMessage());
        }
    }


    /**
     * Save product information
     */
    private void saveProduct()
    {
        if (product != null && product.productId != null)
        {
            updateProduct();
            return;
        }

        ProductGalleryInterface productInterface = Constants.restAdapterDev.create(ProductGalleryInterface.class);

        try
        {
            if (isValidProduct())
            {
                initProduct();

                product.ClientId = Constants.clientId;
                product.FPTag = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG).toUpperCase();
                product.productType = productType;

                Log.d("PRODUCT_JSON", "JSON: " + new Gson().toJson(product));

                showDialog("Please Wait...");

                productInterface.addProduct(product, new Callback<String>() {

                    @Override
                    public void success(String productId, Response response) {

                        Log.d("PRODUCT_JSON", "Product Saved Successfully : " + productId);

                        product.productId = productId;

                        /*if(paymentAndDeliveryMode.getValue().equalsIgnoreCase(Constants.PaymentAndDeliveryMode.ASSURED_PURCHASE.getValue())
                                && productType.equalsIgnoreCase("products"))
                        {
                            saveAssuredPurchase(productId);
                        }*/

                        saveAssuredPurchase(productId);

                        for(ProductImageResponseModel image: imageList)
                        {
                            new MultipleFileUpload(productId, session, mWebAction, image.getImage()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        if(primaryUri != null)
                        {
                            uploadProductImage(productId);
                        }

                        else
                        {
                            Toast.makeText(getContext(), "Information saved successfully", Toast.LENGTH_SHORT).show();
                            hideDialog();

                            if(getActivity() != null)
                            {
                                Intent data = new Intent();
                                data.putExtra("LOAD", true);
                                getActivity().setResult(RESULT_OK, data);
                                getActivity().finish();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                        Log.d("PRODUCT_JSON", "Failed to Save Product");

                        hideDialog();
                        Toast.makeText(getContext(), "Failed to save information", Toast.LENGTH_LONG).show();
                        Log.d("PRODUCT_JSON", "FAIL " + error.getMessage());
                    }
                });
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void deleteProduct()
    {
        ProductGalleryInterface productInterface = Constants.restAdapterDev.create(ProductGalleryInterface.class);

        try
        {
            HashMap<String, String> map = new HashMap<>();

            map.put("clientId", Constants.clientId);
            map.put("productId", product.productId);
            map.put("identifierType", "SINGLE");

            showDialog("Please Wait...");

            productInterface.removeProduct(map, new Callback<String>() {

                @Override
                public void success(String productId, Response response) {

                    Log.d("PRODUCT_JSON", "SUCCESS : Product Deleted Successfully");

                    Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    hideDialog();

                    if(getActivity() != null)
                    {
                        Intent data = new Intent();
                        data.putExtra("LOAD", true);
                        getActivity().setResult(RESULT_OK, data);
                        getActivity().finish();
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                    hideDialog();
                    Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_LONG).show();
                    Log.d("PRODUCT_JSON", "FAIL " + error.getMessage());
                }
            });
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getAssuredPurchase(String productId)
    {
        Constants.webActionAdapter.create(ProductGalleryInterface.class)
                .getAssuredPurchaseDetails(String.format("{product_id:'%s'}", productId), new Callback<WebActionModel<AssuredPurchase>>() {

                    @Override
                    public void success(WebActionModel<AssuredPurchase> webActionModel, Response response) {

                        if (webActionModel.getData().size() > 0)
                        {
                            assuredPurchase = webActionModel.getData().get(0);
                            setAssuredPurchaseData();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {

                    }
                });
    }

    private void initProgressDialog(String content)
    {
        if(materialDialog != null)
        {
            materialDialog.setContent(content);
            return;
        }

        materialDialog = new MaterialDialog.Builder(getActivity())
                .widgetColorRes(R.color.accentColor)
                .content(content)
                .progress(true, 0).build();

        materialDialog.setCancelable(false);
    }

    private void hideDialog()
    {
        if(materialDialog != null && materialDialog.isShowing())
        {
            materialDialog.dismiss();
        }
    }

    private void showDialog(String content)
    {
        initProgressDialog(content);

        if(!materialDialog.isShowing())
        {
            materialDialog.show();
        }
    }

    @Override
    public void onPreExecute()
    {

    }

    @Override
    public void onPostExecute(String result, int responseCode)
    {
        hideDialog();

        if(responseCode == 200 || responseCode == 202)
        {
            Toast.makeText(getContext(), "Information saved successfully", Toast.LENGTH_LONG).show();
        }

        else
        {
            Toast.makeText(getContext(), "Failed to save information", Toast.LENGTH_LONG).show();
        }

        if(getActivity() != null)
        {
            Intent data = new Intent();
            data.putExtra("LOAD", true);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_delete, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_delete:

                deleteConfirmation();
                break;
        }

        return true;
    }


    private void deleteConfirmation()
    {
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.are_you_sure_want_to_delete))
                .positiveText(getString(R.string.delete))
                .positiveColorRes(R.color.primaryColor)
                .negativeText(getString(R.string.cancel))
                .negativeColorRes(R.color.light_gray)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        super.onPositive(dialog);
                        deleteProduct();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).show();

    }


    private WebAction getWebAction()
    {
        if(mWebAction != null)
        {
            return mWebAction;
        }

        mWebAction = new WebAction.WebActionBuilder()
                .setAuthHeader(Constants.WA_KEY)
                .build();

        mWebAction.setWebActionName("product_images");

        return mWebAction;
    }

    private void displayImagesForProduct(String productId)
    {
        if (TextUtils.isEmpty(product.productId))
        {
            return;
        }

        IFilter filter = new WebActionsFilter();
        filter = filter.eq("_pid", productId);

        mWebAction.findProductImages(filter, new WebAction.WebActionCallback<List<ProductImageResponseModel>>() {

            @Override
            public void onSuccess(List<ProductImageResponseModel> result)
            {
                if(result != null)
                {
                    adapterImage.setData(result);
                    displayImageAddButton();
                }
            }

            @Override
            public void onFailure(WebActionError error)
            {
                Log.d("IMAGE_UPLOAD_RESPONSE", "GET IMAGE FAIL");
            }
        });
    }


    private void deleteImage(ProductImageResponseModel image)
    {
        IFilter filter = new WebActionsFilter();
        filter = filter.eq("_id", image.getId());

        mWebAction.delete(filter, false, new WebAction.WebActionCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {

                Toast.makeText(getContext(), "Image Removed Successfully", Toast.LENGTH_LONG).show();
                Log.d(TAG, "" + true);
            }

            @Override
            public void onFailure(WebActionError error)
            {
                Toast.makeText(getContext(), "Failed to Remove Image", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Fail");
            }
        });
    }

    private void uploadFile(File file)
    {
        String valuesStr;

        if(TextUtils.isEmpty(addressInformation.id))
        {
            valuesStr = "clientId=" + Constants.clientId
                    + "&requestType=sequential"
                    + "&totalChunks=1&currentChunkNumber=1&fileName=" + file.getName();
        }

        else
        {
            String url = addressInformation.addressProof;
            String fileName = url.substring(url.lastIndexOf('/') + 1);

            valuesStr = "clientId=" + Constants.clientId
                    + "&requestType=sequential"
                    + "&totalChunks=1&currentChunkNumber=1&fileName=" + file.getName() + "&proofFileId=" + fileName;
        }

        String url = DEV_ASSURED_PURCHASE_URL + "/api/seller/UploadOrReplaceFile?" + valuesStr;

        FileUpload upload = new FileUpload(file);
        upload.setFileUploadListener(this);
        upload.execute(url);
    }


    @Override
    public void onSuccess(String url) {

        Log.d("PRODUCT_JSON", "URL - " + url);
        addressInformation.addressProof = url;
        saveAddress();
    }

    @Override
    public void onFailure() {

        Log.d("PRODUCT_JSON", "FAILURE");
        Toast.makeText(getContext(), "Failed to upload address proof", Toast.LENGTH_LONG).show();
        hideDialog();
    }

    @Override
    public void onPreUpload() {

        Log.d("PRODUCT_JSON", "PREUPLOAD");
        showDialog("Please Wait...");
    }
}