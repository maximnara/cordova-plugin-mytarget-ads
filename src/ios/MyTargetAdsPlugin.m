#import "MyTargetAdsPlugin.h"
#import <Cordova/CDV.h>

static NSString *const EVENT_INTERSTITIAL_LOADED = @"interstitialLoaded";
static NSString *const EVENT_INTERSTITIAL_SHOWN = @"interstitialShown";
static NSString *const EVENT_INTERSTITIAL_CLOSED = @"interstitialClosed";
static NSString *const EVENT_INTERSTITIAL_FAILED_TO_LOAD = @"interstitialFailedToLoad";

static NSString *const EVENT_REWARDED_VIDEO_LOADED = @"rewardedVideoLoaded";
static NSString *const EVENT_REWARDED_VIDEO_FAILED = @"rewardedVideoFailed";
static NSString *const EVENT_REWARDED_VIDEO_REWARDED = @"rewardedVideoRewardReceived";
static NSString *const EVENT_REWARDED_VIDEO_STARTED = @"rewardedVideoStarted";
static NSString *const EVENT_REWARDED_VIDEO_CLOSED = @"rewardedVideoClosed";

static NSString *const EVENT_BANNER_DID_LOAD = @"bannerDidLoad";
static NSString *const EVENT_BANNER_FAILED_TO_LOAD = @"bannerFailedToLoad";
static NSString *const EVENT_BANNER_DID_CLICK = @"bannerDidClick";
static NSString *const EVENT_BANNER_WILL_PRESENT_SCREEN = @"bannerWillPresentScreen";
static NSString *const EVENT_BANNER_DID_DISMISS_SCREEN = @"bannerDidDismissScreen";
static NSString *const EVENT_BANNER_WILL_LEAVE_APPLICATION = @"bannerWillLeaveApplication";

@implementation MyTargetAdsPlugin

#pragma mark - CDVPlugin

/**
 * Init
 * @params {CDVInvokedUrlCommand} command
 */
- (void)init:(CDVInvokedUrlCommand *)command
{
    self.rewardedBlockId = [[command argumentAtIndex:0] intValue];
    self.interstitialBlockId = [[command argumentAtIndex:1] intValue];
    self.bannerBlockId = [[command argumentAtIndex:2] intValue];

    self.bannerAtTop = false;
    self.bannerOverlap = false;

//    [MTRGManager setDebugMode:YES];
//    MTRGManager.sdkConfig = [[MTRGConfig.newBuilder withTestDevices:@[@"255992AB-AF6C-499A-910A-23B1A4EE5BA7"]] build];

    NSArray *bannerSizes = @[
        @{
            @"BANNER_320x50": [NSValue valueWithCGSize:(CGSize){320, 50}],
            @"BANNER_320x100": [NSValue valueWithCGSize:(CGSize){320, 100}],
            @"BANNER_300x250": [NSValue valueWithCGSize:(CGSize){300, 250}],
            @"BANNER_300x300": [NSValue valueWithCGSize:(CGSize){300, 300}],
            @"BANNER_240x400": [NSValue valueWithCGSize:(CGSize){240, 400}],
            @"BANNER_400x240": [NSValue valueWithCGSize:(CGSize){400, 240}],
            @"BANNER_728x90": [NSValue valueWithCGSize:(CGSize){728, 90}],
        }
    ];
    NSString* str = nil;
    NSDictionary* options = [command argumentAtIndex:3 withDefault:[NSNull null]];
    str = [options objectForKey:@"bannerAtTop"];
    NSLog(@"%@", str);
    if(str) self.bannerAtTop = [str boolValue];
    str = [options objectForKey:@"overlap"];
    if(str) self.bannerOverlap = [str boolValue];
    str = [options objectForKey:@"bannerSize"];
    if(str) {
        self.bannerSize = [bannerSizes[0][str] CGSizeValue];
    } else {
        self.bannerSize = [bannerSizes[0][@"BANNER_320x50"] CGSizeValue];
    }

    // Send callback successfull
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * Emit window event
 * @param {NString} - event name
 */
- (void)emitWindowEvent:(NSString *)event
{
    NSString *js = [NSString stringWithFormat:@"cordova.fireWindowEvent('%@')", event];
    [self.commandDelegate evalJs:js];
}

/**
 * Emits window event with data
 * @param {NSString} - event name
 * @param {NSDictionary} - event data
 */
- (void)emitWindowEvent:(NSString *)event withData:(NSDictionary *)data
{
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:kNilOptions error:&error];

    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *js = [NSString stringWithFormat:@"cordova.fireWindowEvent('%@', %@)", event, jsonString];
    [self.commandDelegate evalJs:js];
}

#pragma mark - Rewarded Video Delegate Functions

/**
 * Load rewarded video
 */
- (void)loadRewardedVideo:(CDVInvokedUrlCommand *)command
{
    self.rewardedAd = [MTRGRewardedAd rewardedAdWithSlotId:self.rewardedBlockId];
    self.rewardedAd.delegate = self;

    [self.rewardedAd load];

    // Send callback successfull
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * Show rewarded video
 */
- (void)showRewardedVideo:(CDVInvokedUrlCommand *)command
{
    NSLog(@"%s", "showRewardedVideo");
    [self.rewardedAd showWithController:self.viewController];

    // Send callback successfull
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)onLoadWithRewardedAd:(MTRGRewardedAd *)rewardedAd
{
    NSLog(@"%s", __PRETTY_FUNCTION__);
    [self emitWindowEvent:EVENT_REWARDED_VIDEO_LOADED];
}

- (void)onNoAdWithReason:(NSString *)reason rewardedAd:(MTRGRewardedAd *)rewardedAd
{
    NSLog(@"rewardedAdDidFailToLoad");
    NSLog(@"%@", reason);
    [self emitWindowEvent:EVENT_REWARDED_VIDEO_FAILED];
}

- (void)onDisplayWithRewardedAd:(MTRGRewardedAd *)rewardedAd
{
    [self emitWindowEvent:EVENT_REWARDED_VIDEO_STARTED];
}

- (void)onReward:(MTRGReward *)reward rewardedAd:(MTRGRewardedAd *)rewardedAd
{
    [self emitWindowEvent:EVENT_REWARDED_VIDEO_REWARDED];
}

- (void)onCloseWithRewardedAd:(MTRGRewardedAd *)rewardedAd
{
    [self emitWindowEvent:EVENT_REWARDED_VIDEO_CLOSED];
}


- (void)loadBanner:(CDVInvokedUrlCommand *)command
{
    NSLog(@"loadBanner");
    NSLog(@"%@", self.bannerBlockId);
//    if (self.adView != nil) {
//        [self destroyBanner];
//    }
//    YMAAdSize *adSize = [YMAAdSize fixedSizeWithCGSize:self.bannerSize];
//    self.adView = [[YMAAdView alloc] initWithAdUnitID:self.bannerBlockId adSize:adSize];
//    self.adView.delegate = self;
//    [self.adView loadAd];
}

#pragma mark - Banner Delegate Functions

// Show banner
- (void)showBanner:(CDVInvokedUrlCommand *)command
{
//    if(self.adView)
//    {
//        UIView* parentView = self.bannerOverlap ? self.viewController.view : [self.viewController.view superview];
//
//        if (self.bannerAtTop) {
//            [self.adView displayAtTopInView:parentView];
//        } else {
//            [self.adView displayAtBottomInView:parentView];
//        }
//    }
//
//    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
//    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)hideBanner:(CDVInvokedUrlCommand *)command
{
//    NSLog(@"%s", "hideBanner");
//    [self destroyBanner];
}

- (void)destroyBanner
{
//    [self.adView setDelegate:nil];
//    [self.adView removeFromSuperview];
//    self.adView = nil;
}

//- (void)adViewDidFailLoading:(YMAAdView *)bannerAdView error:(NSError *)error {
//    NSLog(@"%s", "adViewDidFailLoading");
//    NSLog(@"%@", error.description);
//    [self emitWindowEvent:EVENT_BANNER_FAILED_TO_LOAD];
//}
//
//- (void)adViewDidLoad:(YMAAdView *)adView {
//    NSLog(@"%s", "adViewDidLoad");
//    [self emitWindowEvent:EVENT_BANNER_DID_LOAD];
//}
//
//- (void)adViewDidClick:(YMAAdView *)adView {
//    NSLog(@"%s", "adViewDidClick");
//    [self emitWindowEvent:EVENT_BANNER_DID_CLICK];
//}
//
//
//- (void)adViewWillLeaveApplication:(YMAAdView *)adView {
//    NSLog(@"%s", __PRETTY_FUNCTION__);
//    [self emitWindowEvent:EVENT_BANNER_WILL_LEAVE_APPLICATION];
//}
//
//- (void)adView:(YMAAdView *)adView willPresentScreen:(nullable UIViewController *)viewController {
//    NSLog(@"%s", @"willPresentScreen");
//    [self emitWindowEvent:EVENT_BANNER_WILL_PRESENT_SCREEN];
//}

#pragma mark - Intersitial Delegate Functions

- (void)loadInterstitial:(CDVInvokedUrlCommand *)command
{
    self.interstitialAd = [MTRGInterstitialAd interstitialAdWithSlotId:self.interstitialBlockId];
    self.interstitialAd.delegate = self;

    [self.interstitialAd load];

    // Send callback successfull
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)showInterstitial:(CDVInvokedUrlCommand *)command
{
    [self.interstitialAd showWithController:self.viewController];

    // Send callback successfull
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)onLoadWithInterstitialAd:(MTRGInterstitialAd *)interstitialAd
{
    [self emitWindowEvent:EVENT_INTERSTITIAL_LOADED];
}

- (void)onNoAdWithReason:(NSString *)reason interstitialAd:(MTRGInterstitialAd *)interstitialAd
{
    NSLog(@"interstitialAdDidFailToLoad");
    NSLog(@"%@", reason);
    [self emitWindowEvent:EVENT_INTERSTITIAL_FAILED_TO_LOAD];
}

- (void)onDisplayWithInterstitialAd:(MTRGInterstitialAd *)interstitialAd
{
    [self emitWindowEvent:EVENT_INTERSTITIAL_SHOWN];
}

- (void)onCloseWithInterstitialAd:(MTRGInterstitialAd *)interstitialAd
{
    [self emitWindowEvent:EVENT_INTERSTITIAL_CLOSED];
}

- (void)setUserConsent:(CDVInvokedUrlCommand *)command {
    [MTRGPrivacy setUserConsent:[[command argumentAtIndex:0] boolValue]];
    NSLog(@"setUserConsent: ");
    NSLog([[command argumentAtIndex:0] boolValue] ? @"YES" : @"NO");
}

@end
