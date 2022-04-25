package io.luzh.cordova.plugin;

import android.util.Log;
import android.text.TextUtils;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.yandex.mobile.ads.banner.BannerAdView;
//import com.yandex.mobile.ads.banner.AdSize;
//import com.yandex.mobile.ads.banner.BannerAdEventListener;
//import com.yandex.mobile.ads.common.ImpressionData;
//import com.yandex.mobile.ads.common.InitializationListener;

import com.my.target.common.MyTargetPrivacy;
import com.my.target.ads.RewardedAd;
import com.my.target.ads.Reward;
import com.my.target.ads.InterstitialAd;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MyTargetAdsPlugin extends CordovaPlugin {

    private static final String TAG = "MY_TARGET_ADS";

    private static final String EVENT_INTERSTITIAL_LOADED = "interstitialLoaded";
    private static final String EVENT_INTERSTITIAL_FAILED_TO_LOAD = "interstitialFailedToLoad";
    private static final String EVENT_INTERSTITIAL_SHOWN = "interstitialShown";
    private static final String EVENT_INTERSTITIAL_CLOSED = "interstitialClosed";

    private static final String EVENT_REWARDED_VIDEO_LOADED = "rewardedVideoLoaded";
    private static final String EVENT_REWARDED_VIDEO_REWARDED = "rewardedVideoRewardReceived";
    private static final String EVENT_REWARDED_VIDEO_FAILED = "rewardedVideoFailed";
    private static final String EVENT_REWARDED_VIDEO_STARTED = "rewardedVideoStarted";
    private static final String EVENT_REWARDED_VIDEO_CLOSED = "rewardedVideoClosed";

    private static final String EVENT_BANNER_DID_LOAD = "bannerDidLoad";
    private static final String EVENT_BANNER_FAILED_TO_LOAD = "bannerFailedToLoad";
    private static final String EVENT_BANNER_DID_CLICK = "bannerDidClick";

    private RelativeLayout bannerContainerLayout;
    private CordovaWebView cordovaWebView;
    private ViewGroup parentLayout;
    private Boolean bannerLoaded = false;
    private Boolean bannerOverlap = false;
    private Boolean bannerAtTop = false;
    private String bannerSize = "BANNER_320x50";

    private RewardedAd mRewardedAd;
    private InterstitialAd mInterstitialAd;
    //private BannerAdView mBannerAdView;
    private Integer rewardedBlockId;
    private Integer interstitialBlockId;
    private Integer bannerBlockId;

//    private static Map<String, AdSize> bannerSizes;
//    static
//    {
//        bannerSizes = new HashMap<>();
//        bannerSizes.put("BANNER_240x400", AdSize.BANNER_240x400);
//        bannerSizes.put("BANNER_300x250", AdSize.BANNER_300x250);
//        bannerSizes.put("BANNER_300x300", AdSize.BANNER_300x300);
//        bannerSizes.put("BANNER_320x50", AdSize.BANNER_320x50);
//        bannerSizes.put("BANNER_320x100", AdSize.BANNER_320x100);
//        bannerSizes.put("BANNER_400x240", AdSize.BANNER_400x240);
//        bannerSizes.put("BANNER_728x90", AdSize.BANNER_728x90);
//    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("init")) {
            this.initAction(args, callbackContext);
            return true;
        }

        else if (action.equals("loadRewardedVideo")) {
            this.loadRewardedAction(args, callbackContext);
            return true;
        }

        else if (action.equals("showRewardedVideo")) {
            this.showRewardedVideoAction(args, callbackContext);
            return true;
        }

        else if (action.equals("loadBanner")) {
            this.loadBannerAction(args, callbackContext);
            return true;
        }

        else if (action.equals("showBanner")) {
            this.showBannerAction(args, callbackContext);
            return true;
        }

        else if (action.equals("hideBanner")) {
            this.hideBannerAction(args, callbackContext);
            return true;
        }

        else if (action.equals("loadInterstitial")) {
            this.loadInterstitialAction(args, callbackContext);
            return true;
        }

        else if (action.equals("showInterstitial")) {
            this.showInterstitialAction(args, callbackContext);
            return true;
        }

        else if (action.equals("setUserConsent")) {
            this.setUserConsentAction(args.getBoolean(0), callbackContext);
            return true;
        }

        return false;
    }

    /** --------------------------------------------------------------- */

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        cordovaWebView = webView;
        super.initialize(cordova, webView);
    }

    /** ----------------------- UTILS --------------------------- */

    private void emitWindowEvent(final String event) {
        final CordovaWebView view = this.webView;
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.loadUrl(String.format("javascript:cordova.fireWindowEvent('%s');", event));
            }
        });
    }

    private void emitWindowEvent(final String event, final JSONObject data) {
        final CordovaWebView view = this.webView;
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.loadUrl(String.format("javascript:cordova.fireWindowEvent('%s', %s);", event, data.toString()));
            }
        });
    }

    /** ----------------------- INITIALIZATION --------------------------- */

    /**
     * Intilization action Initializes Yandex Ads
     */
    private void initAction(JSONArray args, final CallbackContext callbackContext) throws JSONException {

        final MyTargetAdsPlugin self = this;
        rewardedBlockId = args.getInt(0);
        interstitialBlockId = args.getInt(1);
        bannerBlockId = args.getInt(2);

        JSONObject options = args.optJSONObject(3);
        if(options.has("overlap")) bannerOverlap = options.optBoolean("overlap");
        if(options.has("bannerAtTop")) bannerAtTop = options.optBoolean("bannerAtTop");
        if(options.has("bannerSize")) bannerSize = options.optString("bannerSize");
    }

    /**
     * Initializes Rewarded ads
     *
     * @todo Provide
     */
    private void initRewarded(Integer blockId) {

        final MyTargetAdsPlugin self = this;
        mRewardedAd = new RewardedAd(blockId, this.cordova.getActivity());
        mRewardedAd.setListener(new RewardedAd.RewardedAdListener()
        {
            @Override
            public void onLoad(RewardedAd ad)
            {
                Log.d(TAG, EVENT_REWARDED_VIDEO_LOADED);
                self.emitWindowEvent(EVENT_REWARDED_VIDEO_LOADED);
            }

            @Override
            public void onNoAd(String reason, RewardedAd ad)
            {
                Log.d(TAG, EVENT_REWARDED_VIDEO_FAILED + ": " + reason);
                self.emitWindowEvent(EVENT_REWARDED_VIDEO_FAILED);
            }

            @Override
            public void onClick(RewardedAd ad)
            {
            }

            @Override
            public void onDisplay(RewardedAd ad)
            {
                Log.d(TAG, EVENT_REWARDED_VIDEO_STARTED);
                self.emitWindowEvent(EVENT_REWARDED_VIDEO_STARTED);
            }

            @Override
            public void onDismiss(RewardedAd ad)
            {
                Log.d(TAG, EVENT_REWARDED_VIDEO_CLOSED);
                self.emitWindowEvent(EVENT_REWARDED_VIDEO_CLOSED);
            }

            @Override
            public void onReward(@NonNull Reward var1, @NonNull RewardedAd var2)
            {
                Log.d(TAG, EVENT_REWARDED_VIDEO_REWARDED);
                self.emitWindowEvent(EVENT_REWARDED_VIDEO_REWARDED);
            }
        });
    }

    private void initInterstitial(Integer blockId) {

        final MyTargetAdsPlugin self = this;

        mInterstitialAd = new InterstitialAd(blockId, this.cordova.getActivity());
        mInterstitialAd.setListener(new InterstitialAd.InterstitialAdListener()
        {
            @Override
            public void onLoad(InterstitialAd ad)
            {
                Log.d(TAG, EVENT_INTERSTITIAL_LOADED);
                self.emitWindowEvent(EVENT_INTERSTITIAL_LOADED);
            }

            @Override
            public void onNoAd(String reason, InterstitialAd ad)
            {
                Log.d(TAG, EVENT_INTERSTITIAL_FAILED_TO_LOAD + ": " + reason);
                self.emitWindowEvent(EVENT_INTERSTITIAL_FAILED_TO_LOAD);
            }

            @Override
            public void onClick(InterstitialAd ad)
            {
            }

            @Override
            public void onDisplay(InterstitialAd ad)
            {
                Log.d(TAG, EVENT_INTERSTITIAL_SHOWN);
                self.emitWindowEvent(EVENT_INTERSTITIAL_SHOWN);
            }

            @Override
            public void onDismiss(InterstitialAd ad)
            {
                Log.d(TAG, EVENT_INTERSTITIAL_CLOSED);
                self.emitWindowEvent(EVENT_INTERSTITIAL_CLOSED);
            }

            @Override
            public void onVideoCompleted(InterstitialAd ad)
            {
            }
        });
    }

    /**
     * ----------------------- VALIDATION INTEGRATION ---------------------------
     */

    /**
     * Validates integration action
     */
    private void validateIntegrationAction(JSONArray args, final CallbackContext callbackContext) {
    }

    /** ----------------------- REWARDED VIDEO --------------------------- */

    private void loadRewardedAction(JSONArray args, final CallbackContext callbackContext) {
        final MyTargetAdsPlugin self = this;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                self.initRewarded(rewardedBlockId);
                mRewardedAd.load();
                callbackContext.success();
            }
        });
    }

    private void showRewardedVideoAction(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mRewardedAd.show();
                callbackContext.success();
            }
        });
    }

    /** ----------------------- INTERSTITIAL --------------------------- */

    private void loadInterstitialAction(JSONArray args, final CallbackContext callbackContext) {
        final MyTargetAdsPlugin self = this;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                self.initInterstitial(interstitialBlockId);
                mInterstitialAd.load();
                callbackContext.success();
            }
        });
    }

    private void showInterstitialAction(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mInterstitialAd.show();
                callbackContext.success();
            }
        });
    }

    /** ----------------------- BANNER --------------------------- */
    private void showBannerAction(JSONArray args, final CallbackContext callbackContext) {
//        final MyTargetAdsPlugin self = this;
//
//        cordova.getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//                if (mBannerAdView != null && bannerLoaded) {
//                    if (bannerOverlap) {
//                        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
//                                RelativeLayout.LayoutParams.MATCH_PARENT,
//                                RelativeLayout.LayoutParams.WRAP_CONTENT);
//                        params2.addRule(bannerAtTop ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);
//
//                        RelativeLayout adViewLayout = new RelativeLayout(cordova.getActivity());
//                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//                        try {
//                            ((ViewGroup)(((View)webView.getClass().getMethod("getView").invoke(webView)).getParent())).addView(adViewLayout, params);
//                        } catch (Exception e) {
//                            ((ViewGroup) webView).addView(adViewLayout, params);
//                        }
//
//                        adViewLayout.addView(mBannerAdView, params2);
//                        adViewLayout.bringToFront();
//                    } else {
//                        parentLayout = (ViewGroup) cordovaWebView.getView().getParent();
//
//                        View view = cordovaWebView.getView();
//
//                        ViewGroup wvParentView = (ViewGroup) view.getParent();
//
//                        LinearLayout parentView = new LinearLayout(cordovaWebView.getContext());
//
//                        if (wvParentView != null && wvParentView != parentView) {
//                            ViewGroup rootView = (ViewGroup) (view.getParent());
//                            wvParentView.removeView(view);
//                            ((LinearLayout) parentView).setOrientation(LinearLayout.VERTICAL);
//                            parentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                    ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
//                            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                    ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
//                            parentView.addView(view);
//                            rootView.addView(parentView);
//                        }
//
//                        bannerContainerLayout = new RelativeLayout(self.cordova.getActivity());
//
//                        bannerContainerLayout.setGravity(Gravity.BOTTOM);
//
//                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//                        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//
//                        bannerContainerLayout.addView(mBannerAdView, layoutParams);
//
//                        mBannerAdView.setLayoutParams(layoutParams);
//
//                        if (bannerAtTop) {
//                            parentView.addView(bannerContainerLayout, 0);
//                        } else {
//                            parentView.addView(bannerContainerLayout);
//                        }
//                    }
//                }
//                callbackContext.success();
//            }
//        });
    }

    private void loadBannerAction(JSONArray args, final CallbackContext callbackContext) {
//        final MyTargetAdsPlugin self = this;
//        cordova.getActivity().runOnUiThread(new Runnable() {
//
//            public void run() {
//                if (bannerSizes.get(bannerSize) != null) {
//                    hideBannerView();
//
//                    mBannerAdView = new BannerAdView(self.cordova.getActivity());
//                    mBannerAdView.setAdUnitId(bannerBlockId);
//                    mBannerAdView.setAdSize(bannerSizes.get(bannerSize));
//
//                    final AdRequest adRequest = new AdRequest.Builder().build();
//
//                    mBannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
//                        @Override
//                        public void onAdLoaded() {
//                            bannerLoaded = true;
//                            Log.d(TAG, EVENT_BANNER_DID_LOAD);
//                            self.emitWindowEvent(EVENT_BANNER_DID_LOAD);
//                        }
//
//                        @Override
//                        public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
//                            Log.d(TAG, EVENT_BANNER_FAILED_TO_LOAD + ": " + adRequestError.getDescription());
//                            self.emitWindowEvent(EVENT_BANNER_FAILED_TO_LOAD);
//                        }
//
//                        @Override
//                        public void onAdClicked() {
//                            Log.d(TAG, EVENT_BANNER_DID_CLICK);
//                            self.emitWindowEvent(EVENT_BANNER_DID_CLICK);
//                        }
//
//                        @Override
//                        public void onImpression(@Nullable ImpressionData var1) {
//                        }
//
//                        @Override
//                        public void onLeftApplication() {
//
//                        }
//
//                        @Override
//                        public void onReturnedToApplication() {
//
//                        }
//                    });
//
//                    mBannerAdView.loadAd(adRequest);
//                }
//                callbackContext.success();
//            }
//        });
    }

    /**
     * Destroys Yandex Ads Banner and removes it from the container
     */
    private void hideBannerAction(JSONArray args, final CallbackContext callbackContext) {
//        cordova.getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//                hideBannerView();
//                callbackContext.success();
//            }
//        });
    };

    private void hideBannerView() {
//        cordova.getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//                if (mBannerAdView != null) {
//                    if (parentLayout != null && bannerContainerLayout != null) {
//                        if (mBannerAdView.getParent() != null) {
//                            bannerContainerLayout.removeView(mBannerAdView);
//                        }
//                        if (bannerContainerLayout.getParent() != null) {
//                            parentLayout.removeView(bannerContainerLayout);
//                        }
//                    }
//                    destroyBanner();
//                }
//            }
//        });
    }

    /**
     * Destory Banner
     */
    private void destroyBanner() {
//        if (mBannerAdView != null) {
//            mBannerAdView.destroy();
//            mBannerAdView = null;
//        }
    }

    private void setUserConsentAction(Boolean value, final CallbackContext callbackContext) {
        MyTargetPrivacy.setUserConsent(value);
        Log.d(TAG, "setUserConsent: " + value);
        callbackContext.success();
    }
}
