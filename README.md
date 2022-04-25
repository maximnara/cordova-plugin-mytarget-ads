# MyTarget Ads for Cordova apps

--------

## Table of Contents

- [State of Development](#state-of-development)
- [Install](#install)
- [Usage](#usage)


## State of Development
- [x] <img src="https://img.shields.io/badge/-Complete-brightgreen.svg?label=Rewarded%20Video%20Support&style=flat-square">
- [x] <img src="https://img.shields.io/badge/-Complete-brightgreen.svg?label=Interstitial%20Support&style=flat-square">
- [ ] <img src="https://img.shields.io/badge/-In%20Development-yellow.svg?label=Banner%20Support&style=flat-square">

-------- 

## Install

```bash
npm i cordova-plugin-mytarget-ads --save
```

-------- 
## Usage

- [Initialization](#initialization)
- [Rewarded Videos](#rewarded-videos)
  - [Load Rewarded Video](#load-rewarded-video)
  - [Show Rewarded Video](#show-rewarded-video)
  - [Rewarded Video Events](#rewarded-video-events)
- [Interstitials](#interstitials)
  - [Load Interstitial](#load-interstitial)
  - [Show Interstitial](#show-interstitial)
  - [Interstitial Events](#interstitial-events)
  
  
All methods support optional `onSuccess` and `onFailure` parameters

### Initialization

```javascript
import * as MyTargetAds from 'cordova-plugin-mytarget-ads/www/mytargetads';
await MyTargetAds.init({ 
  rewardedBlockId: 'YOUR_REWARDER_BLOCK_ID',
  interstitialBlockId: 'YOUR_INTERSTITIAL_ID',
});
```
### Set user consent for GDPR
Call this on every app launch. More info: https://yandex.ru/dev/mobile-ads/doc/android/quick-start/gdpr-about.html

```javascript
MyTargetAds.setUserConsent(true);
```
***
### Rewarded Videos

#### Load Rewarded Video

```javascript
MyTargetAds.loadRewardedVideo({
  onSuccess: function () {

  },
  onFailure: function () {

  },
});
```

#### Show Rewarded Video

```javascript
MyTargetAds.showRewardedVideo();
```

#### Rewarded Video Events
**Rewarded Video Loaded**

```javascript
window.addEventListener("rewardedVideoLoaded", function () {
  MyTargetAds.showRewardedVideo();
});
```
**Rewarded Video Rewarded**
```javascript
window.addEventListener("rewardedVideoRewardReceived", function(){
  // some logics
});
```
**Rewarded Video Started**
```javascript
window.addEventListener("rewardedVideoStarted", function(){

});
```
**Rewarded Video Closed**
```javascript
window.addEventListener("rewardedVideoClosed", function(){

});
```
**Rewarded Video Failed**
```javascript
window.addEventListener("rewardedVideoFailed", function(){

});
```
***
### Interstitial

#### Load Interstitial
_Must be called before `showInterstitial`

```javascript
MyTargetAds.loadInterstitial();
```
***
#### Show Interstitial

```javascript
MyTargetAds.showInterstitial();
```
***
#### Interstitial Events

**Interstitial Loaded**

```javascript
window.addEventListener("interstitialLoaded", function () {
  MyTargetAds.showInterstitial();
});
```
**Interstitial Shown**
```javascript
window.addEventListener("interstitialShown", function(){

});
```
**Interstitial Closed**
```javascript
window.addEventListener("interstitialClosed", function(){

});
```
**Interstitial Failed To Load**
```javascript
window.addEventListener("interstitialFailedToLoad", function(){

});
```

### Feel free to make your PRs for code structure or new functions
