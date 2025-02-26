package com.tappayment;

import java.util.Locale;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import android.text.TextUtils;
import android.content.res.Configuration;

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

        Bundle extras = getIntent().getExtras();
        String UILanguage = extras.getString("UILanguage");

        Locale locale = new Locale(UILanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);
        String SecretAPIkey = extras.getString("SecretAPIkey");
        String AppID = extras.getString("AppID");

        GoSellSDK.init(this, SecretAPIkey, AppID);

        configureSDKThemeObject();
        configureSDKSession();

    }

    private void configureSDKThemeObject() {
        Bundle extras = getIntent().getExtras();
        String UILanguage = extras.getString("UILanguage");
        ThemeObject.getInstance()
                // set SDK Locale
                .setSdkLanguage(UILanguage) // **Required**

                // set Appearance mode [Full Screen Mode - Windowed Mode]
                .setAppearanceMode(AppearanceMode.FULLSCREEN_MODE) // **Required**

                // Setup header text size
                .setHeaderTextSize(17) // **Optional**
                .setCardInputTextColor(getResources().getColor(R.color.black))
                // setup card input hint text color
                .setCardInputPlaceholderTextColor(getResources().getColor(R.color.black))// **Optional**
                // setup card input field text color in case of invalid input
                .setCardInputInvalidTextColor(getResources().getColor(R.color.red))// **Optional**

                .setSaveCardSwitchOffThumbTint(getResources().getColor(R.color.french_gray_new))
                .setSaveCardSwitchOnThumbTint(getResources().getColor(R.color.vibrant_green))
                .setSaveCardSwitchOffTrackTint(getResources().getColor(R.color.french_gray))
                .setSaveCardSwitchOnTrackTint(getResources().getColor(R.color.vibrant_green_pressed))

                // setup pay button text size
                .setPayButtonTextSize(14) // **Optional**

                // show/hide pay button loader
                .setPayButtonLoaderVisible(true) // **Optional**
                // .setPayButtonEnabledBackgroundColor(getResources().getColor(R.color.yellow))
                // .setPayButtonDisabledBackgroundColor(getResources().getColor(R.color.french_gray))

                // show/hide pay button security icon
                .setPayButtonSecurityIconVisible(true) // **Optional**
                .setPayButtonDisabledTitleColor(getResources().getColor(R.color.black))
                .setPayButtonEnabledTitleColor(getResources().getColor(R.color.white));

    }

    private void configureSDKSession() {

        Bundle extras = getIntent().getExtras();
        double price = extras.getDouble("price");
        String PostUrl = extras.getString("PostUrl");
        String Currency = extras.getString("Currency");
        String transactionMode = extras.getString("transactionMode").equals("") ? "AUTHORIZE_CAPTURE" : extras.getString("transactionMode");

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

        // set transaction mode [TransactionMode.PURCHASE -
        // TransactionMode.AUTHORIZE_CAPTURE - TransactionMode.SAVE_CARD -
        // TransactionMode.TOKENIZE_CARD ]
        sdkSession.setTransactionMode(TransactionMode.valueOf(transactionMode)); // ** Required **

        // Using static CustomerBuilder method available inside TAP Customer Class you
        // can populate TAP Customer object and pass it to SDK
        sdkSession.setCustomer(getCustomer()); // ** Required **

        // Set Total Amount. The Total amount will be recalculated according to provided
        // Taxes and Shipping
        sdkSession.setAmount(new BigDecimal(price)); // ** Required **

        // Post URL
        sdkSession.setPostURL(PostUrl); // ** Optional **

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
        if(TextUtils.equals(CustomerId, "")) {
            String firstName = extras.getString("firstName");
            String lastName = extras.getString("lastName");
            String email = extras.getString("email");
            String countryCode = extras.getString("countryCode");
            String phoneNumber = extras.getString("phoneNumber");
            return new Customer.CustomerBuilder("").email(email)
                .firstName(firstName).lastName(lastName).phone(new PhoneNumber(countryCode  , phoneNumber))
                .build();
        } else {
            return new Customer.CustomerBuilder(CustomerId).build();
        }
    }

    public void paymentSucceed(@NonNull Charge charge) {
        System.out.println("Payment Succeeded : " + charge.getStatus());
        System.out.println("Payment Succeeded : " + charge.getDescription());
        System.out.println("Payment Succeeded : " + charge.getResponse().getMessage());
        WritableMap params = Arguments.createMap();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", charge.getResponse().getMessage());
        resultIntent.putExtra("resultCode", charge.getResponse().getCode());
        resultIntent.putExtra("chargeId", charge.getId());
        resultIntent.putExtra("custId", charge.getCustomer().getIdentifier());
        resultIntent.putExtra("currency", charge.getCurrency());
        resultIntent.putExtra("amount", charge.getAmount());
        setResult(Integer.parseInt(charge.getResponse().getCode()), resultIntent);
        finish();
    };

    @Override
    public void userEnabledSaveCardOption(boolean saveCardEnabled){

    }

    @Override
    public void invalidCustomerID(){

    }

    @Override
    public void invalidTransactionMode(){

    }

    @Override
    public void backendUnknownError(String error){

    }

    @Override
    public void invalidCardDetails(){

    }

    @Override
    public void savedCardsList(@NonNull CardsList cardsListst){

    }

    public void paymentFailed(@Nullable Charge charge) {
        System.out.println("Payment Failed : " + charge.getStatus());
        System.out.println("Payment Failed : " + charge.getDescription());
        System.out.println("Payment Failed : " + charge.getResponse().getMessage());
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", charge.getResponse().getMessage());
        resultIntent.putExtra("resultCode", charge.getResponse().getCode());
        resultIntent.putExtra("chargeId", charge.getId());
        resultIntent.putExtra("custId", charge.getCustomer().getIdentifier());
        resultIntent.putExtra("currency", charge.getCurrency());
        resultIntent.putExtra("amount", charge.getAmount());
        setResult(Integer.parseInt(charge.getResponse().getCode()), resultIntent);
        finish();
    };

    public void authorizationSucceed(@NonNull Authorize authorize) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", authorize.getResponse().getMessage());
        resultIntent.putExtra("resultCode", authorize.getResponse().getCode());
        setResult(Integer.parseInt(authorize.getResponse().getCode()), resultIntent);
        finish();
    };

    public void authorizationFailed(Authorize authorize) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", authorize.getResponse().getMessage());
        resultIntent.putExtra("resultCode", authorize.getResponse().getCode());
        setResult(Integer.parseInt(authorize.getResponse().getCode()), resultIntent);
        finish();
    };

    public void cardSaved(@NonNull Charge charge) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", charge.getResponse().getMessage());
        resultIntent.putExtra("resultCode", charge.getResponse().getCode());
        setResult(Integer.parseInt(charge.getResponse().getCode()), resultIntent);
        finish();
    };

    public void cardSavingFailed(@NonNull Charge charge) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", charge.getResponse().getMessage());
        resultIntent.putExtra("resultCode", charge.getResponse().getCode());
        setResult(Integer.parseInt(charge.getResponse().getCode()), resultIntent);
        finish();
    }

    @Override
    public void cardTokenizedSuccessfully(@NonNull Token token) {

    };


    public void cardTokenizedSuccessfully(@NonNull String token) {
    };

    public void sdkError(@Nullable GoSellError goSellError) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultMessage", goSellError.getErrorMessage());
        resultIntent.putExtra("resultCode", goSellError.getErrorCode());
        setResult(goSellError.getErrorCode(), resultIntent);
        finish();
    };

    public void sessionIsStarting() {
        System.out.println("session started : ");
    };

    public void sessionHasStarted() {
    };

    public void sessionCancelled() {
        Intent resultIntent = new Intent();
        setResult(102, resultIntent);
        resultIntent.putExtra("resultMessage", "");
        resultIntent.putExtra("resultCode", "102");
        finish();
    };

    public void sessionFailedToStart() {
    };

}