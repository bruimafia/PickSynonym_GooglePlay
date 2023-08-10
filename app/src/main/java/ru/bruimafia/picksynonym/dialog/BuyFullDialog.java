package ru.bruimafia.picksynonym.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.databinding.DialogBuyFullBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class BuyFullDialog extends BottomSheetDialogFragment implements PurchasesUpdatedListener {

    private DialogBuyFullBinding binding;
    private Context context;
    private BillingClient billingClient;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBuyFullBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        binding.btnBuyFullApp.setOnClickListener(v -> establishConnection());

        return binding.getRoot();
    }

    // обновление интерфейса после удачной покупки
    private void refreshFragment() {
        binding.tvTitle.setVisibility(View.GONE);
        binding.btnBuyFullApp.setVisibility(View.GONE);
        binding.tvAdvantages.setText(R.string.restart_app);
    }

    // обновление информации о покупках
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            for (Purchase purchase : list)
                handlePurchase(purchase);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            refreshFragment();
            SharedPreferencesManager.getInstance(context).setIsFullVersion(true);
        } else handleBillingError(billingResult.getResponseCode());
    }

    // установка соединения с google play для покупок
    private void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    getSingleInAppDetail();
                } else retryBillingServiceConnection();
            }

            @Override
            public void onBillingServiceDisconnected() {
                retryBillingServiceConnection();
            }
        });
    }

    // повторное соединение с google play для покупок
    private void retryBillingServiceConnection() {
        final int[] tries = {1};
        int maxTries = 3;
        final boolean[] isConnectionEstablished = {false};

        do {
            try {
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingServiceDisconnected() {
                        retryBillingServiceConnection();
                    }

                    @Override
                    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                        tries[0]++;
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                            isConnectionEstablished[0] = true;
                        else if (tries[0] == maxTries)
                            handleBillingError(billingResult.getResponseCode());
                    }
                });
            } catch (Exception e){
                tries[0]++;
            }
        } while (tries[0] <= maxTries && !isConnectionEstablished[0]);

        if (!isConnectionEstablished[0])
            handleBillingError(-1);
    }

    // список доступных покупок
    private void getSingleInAppDetail() {
        SkuDetailsParams.Builder paramsBuilder = SkuDetailsParams.newBuilder();
        paramsBuilder.setSkusList(Collections.singletonList(getString(R.string.billing_google_product_id)));
        paramsBuilder.setType(BillingClient.SkuType.INAPP);
        SkuDetailsParams params = paramsBuilder.build();

        billingClient.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList.get(0))
                        .build();
                billingClient.launchBillingFlow(getActivity(), flowParams);
            }
        });
    }

    // запуск покупки
    private void launchPurchaseFlow(ProductDetails productDetails) {
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();
        productList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
        );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();
        billingClient.launchBillingFlow(getActivity(), billingFlowParams);
    }

    // запуск покупки
    private void handlePurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build(),
                    billingResult -> {
                        for (String pur : purchase.getProducts()) {
                            if (pur != null && pur.equals(getString(R.string.billing_google_product_id))) {
                                Log.d("TAG", "Purchase is successful");
                                Log.d("TAG", "Yay! Purchased");
                                refreshFragment();
                                SharedPreferencesManager.getInstance(context).setIsFullVersion(true);
                                consumePurchase(purchase);
                            }
                        }
                    });
        }
    }

    // запуск покупки
    private void consumePurchase(Purchase purchase) {
        ConsumeParams params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.consumeAsync(params, (billingResult, s) -> {
            Log.d("TAG", "Consuming Successful: " + s);
            Log.d("TAG", "Product Consumed");
        });
    }

    // обработка ошибок о покупках с google play
    private void handleBillingError(int responseCode) {
        String errorMessage;
        switch (responseCode) {
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                errorMessage = "Billing service is currently unavailable. Please try again later.";
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                errorMessage = "An error occurred while processing the request. Please try again later.";
                break;
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                errorMessage = "This feature is not supported on your device.";
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                errorMessage = "You already own this item.";
                break;
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                errorMessage = "You do not own this item.";
                break;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                errorMessage = "This item is not available for purchase.";
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                errorMessage = "Billing service has been disconnected. Please try again later.";
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                errorMessage = "The purchase has been canceled.";
                break;
            default:
                errorMessage = "An unknown error occurred.";
                break;
        }
        Log.d("TAG", errorMessage);
    }

}
