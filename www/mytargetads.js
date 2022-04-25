/* eslint-disable */

let MyTargetAds = (function ()
{
    let initialized = false;

    return {

        BANNER_320x50: 'BANNER_320x50',
        BANNER_320x100: 'BANNER_320x100',
        BANNER_300x250: 'BANNER_300x250',
        BANNER_300x300: 'BANNER_300x300',
        BANNER_240x400: 'BANNER_240x400',
        BANNER_400x240: 'BANNER_400x240',
        BANNER_728x90: 'BANNER_728x90',

        /**
         * Returns the state of initialization
         */
        isInitialized: function isInitialized()
        {
            return initialized;
        },

        /**
         * Validate Integration
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        validateIntegration: function validateIntegration(params)
        {
            params = defaults(params, {});

            callPlugin('validateIntegration', [], params.onSuccess, params.onFailure);
        },

        /**
         * Initializes iron source
         * @param {Function} params.onSuccess - optional on success callback
         */
        init: function init(params)
        {
            params = defaults(params, {});

            if (params.hasOwnProperty('rewardedBlockId') === false && params.hasOwnProperty('interstitialBlockId') === false && params.hasOwnProperty('bannerBlockId') === false)
            {
                throw new Error('YandexAds::init - rewardedBlockId or interstitialBlockId or bannerBlockId is required');
            }

            callPlugin('init', [params.rewardedBlockId, params.interstitialBlockId, params.bannerBlockId, params.options || {}], function ()
            {

                initialized = true;

                if (isFunction(params.onSuccess))
                {
                    params.onSuccess();
                }

            }, params.onFailure);
        },

        /**
         * Shows rewarded video
         * @param {String} params.placement - optional placement name
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} param.onFailure - optional on failure callback
         */
        loadRewardedVideo: function loadRewardedVideo(params)
        {
            params = defaults(params, { placement: 'default' });

            callPlugin('loadRewardedVideo', [], params.onSuccess, params.onFailure);
        },

        /**
         * Shows rewarded video
         * @param {String} params.placement - optional placement name
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} param.onFailure - optional on failure callback
         */
        showRewardedVideo: function showRewardedVideo(params)
        {
            params = defaults(params, { placement: 'default' });

            callPlugin('showRewardedVideo', [], params.onSuccess, params.onFailure);
        },

        /**
         * Shows banner if avaialble
         * @param {Function} params.onSuccess
         */
        showBanner: function showBanner(params)
        {
            params = defaults(params, {});

            callPlugin('showBanner', [], params.onSuccess, params.onFailure);
        },

        /**
         * Shows banner if avaialble
         * @param {Function} params.onSuccess
         */
        hideBanner: function showBanner(params)
        {
            params = defaults(params, {});

            callPlugin('hideBanner', [], params.onSuccess, params.onFailure);
        },

        /**
         * Loads interstitial
         */
        loadBanner: function loadBanner(params)
        {
            params = defaults(params, {});

            callPlugin('loadBanner', [], params.onSuccess, params.onFailure);
        },

        /**
         * Loads interstitial
         */
        loadInterstitial: function loadInterstitial(params)
        {
            params = defaults(params, {});

            callPlugin('loadInterstitial', [], params.onSuccess, params.onFailure);
        },

        /**
         * Show interstitial
         */
        showInterstitial: function showInterstitial(params)
        {
            params = defaults(params, {});

            callPlugin('showInterstitial', [], params.onSuccess, params.onFailure);
        },

        setUserConsent: function (value, params)
        {
            params = defaults(params, {});

            callPlugin('setUserConsent', [value], params.onSuccess, params.onFailure);
        },
    }
})();



/**
* Helper function to call cordova plugin
* @param {String} name - function name to call
* @param {Array} params - optional params
* @param {Function} onSuccess - optional on sucess function
* @param {Function} onFailure - optional on failure functioin
*/
function callPlugin(name, params, onSuccess, onFailure)
{
    cordova.exec(function callPluginSuccess(result)
    {

        if (isFunction(onSuccess))
        {
            onSuccess(result);
        }
    }, function callPluginFailure(error)
        {
            if (isFunction(onFailure))
            {
                onFailure(error)
            }
        }, 'MyTargetAdsPlugin', name, params);
}

/**
* Helper function to check if a function is a function
* @param {Object} functionToCheck - function to check if is function
*/
function isFunction(functionToCheck)
{
    var getType = {};
    var isFunction = functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
    return isFunction === true;
}

/**
* Helper function to do a shallow defaults (merge). Does not create a new object, simply extends it
* @param {Object} o - object to extend
* @param {Object} defaultObject - defaults to extend o with
*/
function defaults(o, defaultObject)
{
    if (typeof o === 'undefined')
    {
        return defaults({}, defaultObject);
    }

    for (var j in defaultObject)
    {
        if (defaultObject.hasOwnProperty(j) && o.hasOwnProperty(j) === false)
        {
            o[j] = defaultObject[j];
        }
    }

    return o;
}


if (typeof module !== undefined && module.exports)
{
    module.exports = MyTargetAds;
}
