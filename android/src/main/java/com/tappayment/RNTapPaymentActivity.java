package com.tappayment;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import android.util.DisplayMetrics;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.os.Handler;
import android.view.View;
import android.content.Intent;

import company.tap.gosellapi.GoSellSDK;
import company.tap.gosellapi.internal.api.models.Token;
import company.tap.gosellapi.open.enums.AppearanceMode;

import company.tap.gosellapi.open.controllers.ThemeObject;
import company.tap.gosellapi.open.controllers.SDKSession;
import company.tap.gosellapi.open.models.CardsList;
import company.tap.gosellapi.open.models.TapCurrency;
import company.tap.gosellapi.open.enums.TransactionMode;
import java.math.BigDecimal;
import company.tap.gosellapi.internal.api.models.PhoneNumber;
import company.tap.gosellapi.open.buttons.PayButtonView;
import company.tap.gosellapi.open.models.Customer;
import company.tap.gosellapi.internal.api.models.Charge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import company.tap.gosellapi.internal.api.callbacks.GoSellError;
import company.tap.gosellapi.internal.api.models.Authorize;
import company.tap.gosellapi.open.delegate.SessionDelegate;
import company.tap.gosellapi.open.models.Receipt;

public class RNTapPaymentActivity extends AppCompatActivity implements SessionDelegate {
    private SDKSession sdkSession;
    private PayButtonView payButtonView;
    private final int SDK_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        String SecretAPIkey = extras.getString("SecretAPIkey");
        String AppID = extras.getString("AppID");

        GoSellSDK.init(this, SecretAPIkey, AppID);

        configureSDKThemeObject();
        configureSDKSession();

    }

    private void configureSDKThemeObject() {

        ThemeObject.getInstance()
        // set Appearance mode [Full Screen Mode - Windowed Mode]
        .setAppearanceMode(AppearanceMode.FULLSCREEN_MODE) // **Required**
        .setHeaderTextColor(getResources().getColor(R.color.black1))
        .setHeaderTextSize(17)
        .setHeaderBackgroundColor(getResources().getColor(R.color.french_gray_new))
        .setCardInputTextColor(getResources().getColor(R.color.black))
        .setCardInputInvalidTextColor(getResources().getColor(R.color.red))
        .setCardInputPlaceholderTextColor(getResources().getColor(R.color.gray))
        .setSaveCardSwitchOffThumbTint(getResources().getColor(R.color.french_gray_new))
        .setSaveCardSwitchOnThumbTint(getResources().getColor(R.color.vibrant_green))
        .setSaveCardSwitchOffTrackTint(getResources().getColor(R.color.french_gray))
        .setSaveCardSwitchOnTrackTint(getResources().getColor(R.color.vibrant_green_pressed))

        .setScanIconDrawable(getResources().getDrawable(R.drawable.btn_card_scanner_normal))

        .setPayButtonResourceId(R.drawable.btn_pay_selector)  //btn_pay_merchant_selector
        .setPayButtonDisabledTitleColor(getResources().getColor(R.color.white))
        .setPayButtonEnabledTitleColor(getResources().getColor(R.color.white))
        .setPayButtonTextSize(14)
        .setPayButtonLoaderVisible(true)
        .setPayButtonSecurityIconVisible(true);
    }

    private void configureSDKSession() {

        Bundle extras = getIntent().getExtras();
        int price = extras.getInt("price");
        String Currency = extras.getString("Currency");

        // Instantiate SDK Session
        if (sdkSession == null)
            sdkSession = new SDKSession(); // ** Required **

        // pass your activity as a session delegate to listen to SDK internal payment
        // process follow
        sdkSession.addSessionDelegate(this); // ** Required **

        // initiate PaymentDataSource
        sdkSession.instantiatePaymentDataSource(); // ** Required **

        // set transaction currency associated to your account
        sdkSession.setTransactionCurrency(new TapCurrency(Currency)); // ** Required **


        // Using static CustomerBuilder method available inside TAP Customer Class you
        // can populate TAP Customer object and pass it to SDK
        sdkSession.setCustomer(getCustomer()); // ** Required **

        // Enable or Disable Saving Card
        sdkSession.isUserAllowedToSaveCard(true); //  ** Required ** you can pass boolean

        // Set Total Amount. The Total amount will be recalculated according to provided
        // Taxes and Shipping
        sdkSession.setAmount(new BigDecimal(price)); // ** Required **

        // set transaction mode [TransactionMode.PURCHASE -
        // TransactionMode.AUTHORIZE_CAPTURE - TransactionMode.SAVE_CARD -
        // TransactionMode.TOKENIZE_CARD ]
        sdkSession.setTransactionMode(TransactionMode.AUTHORIZE_CAPTURE); // ** Required **

        // Post URL
        sdkSession.setPostURL("https://integrations.dev.pyypl.io/pckt/tap/transactionevent"); // ** Optional **

        // Payment Description
        sdkSession.setPaymentDescription(""); // ** Optional **

        // Payment Reference
        sdkSession.setPaymentReference(null); // ** Optional ** you can pass null

        // Payment Statement Descriptor
        sdkSession.setPaymentStatementDescriptor(""); // ** Optional **

        // Enable or Disable 3DSecure
        sdkSession.isRequires3DSecure(true);

        // Set Receipt Settings [SMS - Email ]
        sdkSession.setReceiptSettings(new Receipt(false, true)); // ** Optional ** you can pass Receipt object or null

        // Set Authorize Action
        sdkSession.setAuthorizeAction(null); // ** Optional ** you can pass AuthorizeAction object or null

        sdkSession.setDestination(null); // ** Optional ** you can pass Destinations object or null

        sdkSession.setMerchantID(null); // ** Optional ** you can pass merchant id or null

        // initPayButton();

        /**
         * Use this method where ever you want to show TAP SDK Main Screen. This method
         * must be called after you configured SDK as above This method will be used in
         * case of you are not using TAP PayButton in your activity.
         */
        sdkSession.start(this);
    }

    private Customer getCustomer() {
        Bundle extras = getIntent().getExtras();
        String CustomerId = extras.getString("CustomerId");
        return new Customer.CustomerBuilder(CustomerId).email("pankaj@teamlance.io")
                .firstName("firstname").lastName("lastname").metadata("").phone(new PhoneNumber("000", "0000000"))
                .middleName("middlename").build();
    }

    public void paymentSucceed(@NonNull Charge charge) {
        System.out.println("Payment Succeeded : " + charge.getStatus());
        System.out.println("Payment Succeeded : " + charge.getDescription());
        System.out.println("Payment Succeeded : " + charge.getCustomer());
        System.out.println("Payment Succeeded : " + charge.getResponse().getMessage());
        System.out.println("Payment Succeeded : " + charge.getId());
        showDialog(charge.getId(), charge.getResponse().getMessage(),
                company.tap.gosellapi.R.drawable.ic_checkmark_normal);
        WritableMap params = Arguments.createMap();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("STATUS", charge.getStatus());
        setResult(100, resultIntent);
        finish();
    };

    public void paymentFailed(@Nullable Charge charge) {
        System.out.println("Payment Failed : " + charge.getStatus());
        System.out.println("Payment Failed : " + charge.getDescription());
        System.out.println("Payment Failed : " + charge.getResponse().getMessage());
        showDialog(charge.getId(), charge.getResponse().getMessage(), company.tap.gosellapi.R.drawable.icon_failed);
        Intent resultIntent = new Intent();
        setResult(101, resultIntent);
        finish();
    };

    public void authorizationSucceed(@NonNull Authorize authorize) {
        showDialog(authorize.getId(), authorize.getResponse().getMessage(),
                company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    };

    public void authorizationFailed(Authorize authorize) {
        showDialog(authorize.getId(), authorize.getResponse().getMessage(),
                company.tap.gosellapi.R.drawable.icon_failed);
    };

    public void cardSaved(@NonNull Charge charge) {
        showDialog(charge.getId(), charge.getStatus().toString(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    };

    public void cardSavingFailed(@NonNull Charge charge) {
        showDialog(charge.getId(), charge.getStatus().toString(), company.tap.gosellapi.R.drawable.icon_failed);
    }

    @Override
    public void cardTokenizedSuccessfully(@NonNull Token token) {

    }

    @Override
    public void savedCardsList(@NonNull CardsList cardsList) {

    }

    ;

    public void cardTokenizedSuccessfully(@NonNull String token) {
    };

    public void sdkError(@Nullable GoSellError goSellError) {
        showDialog(goSellError.getErrorCode() + "", goSellError.getErrorMessage(),
                company.tap.gosellapi.R.drawable.icon_failed);
    };

    public void sessionIsStarting() {
        System.out.println("session started : ");
    };

    public void sessionHasStarted() {
    };

    public void sessionCancelled() {
        Intent resultIntent = new Intent();
        setResult(102, resultIntent);
        finish();
    };

    public void sessionFailedToStart() {
    }

    @Override
    public void invalidCardDetails() {

    }

    @Override
    public void backendUnknownError(String message) {

    }

    @Override
    public void invalidTransactionMode() {

    }

    @Override
    public void invalidCustomerID() {

    }

    private void showDialog(String chargeID, String msg, int icon) {
        System.out.println("message is => " + msg);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        PopupWindow popupWindow;
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {

                View layout = inflater.inflate(company.tap.gosellapi.R.layout.charge_status_layout, findViewById(
                        company.tap.gosellapi.R.id.popup_element));
                popupWindow = new PopupWindow(layout, width, 250, true);
                ImageView status_icon = layout.findViewById(company.tap.gosellapi.R.id.status_icon);
                TextView statusText = layout.findViewById(company.tap.gosellapi.R.id.status_text);
                TextView chargeText = layout.findViewById(company.tap.gosellapi.R.id.charge_id_txt);
                status_icon.setImageResource(icon);
                chargeText.setText(chargeID);
                statusText.setText((msg!=null&& msg.length()>30)?msg.substring(0,29):msg);
                popupWindow.showAtLocation(layout, Gravity.TOP, 0, 50);
                popupWindow.getContentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.popup_show));

                setupTimer(popupWindow);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void setupTimer(PopupWindow popupWindow) {
        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = () -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            this.sessionCancelled();
        };

        popupWindow.setOnDismissListener(() -> handler.removeCallbacks(runnable));

        handler.postDelayed(runnable, 4000);
    }

}
