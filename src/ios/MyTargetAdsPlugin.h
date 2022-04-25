#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import <MyTargetSDK/MyTargetSDK.h>

@interface MyTargetAdsPlugin : CDVPlugin <MTRGRewardedAdDelegate, MTRGInterstitialAdDelegate, MTRGAdViewDelegate>

@property (nonatomic, strong) MTRGRewardedAd *rewardedAd;
@property (nonatomic, strong) MTRGInterstitialAd *interstitialAd;
@property (nonatomic, strong) MTRGAdView *adView;
@property int *rewardedBlockId;
@property int *interstitialBlockId;
@property int *bannerBlockId;

@property BOOL *bannerAtTop;
@property BOOL *bannerOverlap;
@property CGSize bannerSize;

- (void)init:(CDVInvokedUrlCommand *)command;

- (void)loadRewardedVideo:(CDVInvokedUrlCommand *)command;

- (void)showRewardedVideo:(CDVInvokedUrlCommand *)command;

- (void)loadBanner:(CDVInvokedUrlCommand *)command;

- (void)showBanner:(CDVInvokedUrlCommand *)command;

- (void)hideBanner:(CDVInvokedUrlCommand *)command;

- (void)loadInterstitial:(CDVInvokedUrlCommand *)command;

- (void)showInterstitial:(CDVInvokedUrlCommand *)command;

- (void)setUserConsent:(CDVInvokedUrlCommand *)command;

@end
