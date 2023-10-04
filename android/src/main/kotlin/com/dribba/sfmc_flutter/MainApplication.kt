package com.dribba.sfmc_flutter

import android.app.Application
import com.salesforce.marketingcloud.MCLogListener
import com.salesforce.marketingcloud.MarketingCloudConfig
import com.salesforce.marketingcloud.MarketingCloudSdk
import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions
import com.salesforce.marketingcloud.notifications.NotificationManager
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkModuleConfig

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import io.flutter.app.FlutterApplication
import java.util.Random

class MainApplication : FlutterApplication() {
    override fun onCreate() {
        super.onCreate()
        setupSFMC()
    }

    fun setupSFMC(
            appId: String,
            accessToken: String,
            mid: String,
            sfmcURL: String,
            senderId: String?
    ): Boolean {
        val notificationIcon = getNotificationIcon()

        SFMCSdk.configure(
                applicationContext as Application,
                SFMCSdkModuleConfig.build {
                    pushModuleConfig =
                            MarketingCloudConfig.builder()
                                    .apply {
                                        setApplicationId(appId)
                                        setAccessToken(accessToken)
                                        setMarketingCloudServerUrl(sfmcURL)
                                        setMid(mid)
                                        setNotificationCustomizationOptions(
                                                NotificationCustomizationOptions.create(
                                                        notificationIcon,
                                                        NotificationManager.NotificationLaunchIntentProvider { context, notificationMessage ->
                                                            val requestCode = Random().nextInt()
                                                            val url = notificationMessage.url
                                                            when {
                                                                url.isNullOrEmpty() ->
                                                                    PendingIntent.getActivity(
                                                                            context,
                                                                            requestCode,
                                                                            Intent(),
                                                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                                                    )
                                                                else ->
                                                                    PendingIntent.getActivity(
                                                                            context,
                                                                            requestCode,
                                                                            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                                                    )
                                                            }
                                                        },
                                                        NotificationManager.NotificationChannelIdProvider { context, notificationMessage ->
                                                            if (TextUtils.isEmpty(notificationMessage.url)) {
                                                                NotificationManager.createDefaultNotificationChannel(context)
                                                            } else {
                                                                "UrlNotification"
                                                            }
                                                        }
                                                )

                                        )
                                        // Other configuration options
                                    }
                                    .build(context.applicationContext)
                }
        ) { initStatus ->
            setupVerbose(true)
            // TODO handle initialization status
        }
        return true
    }
}
