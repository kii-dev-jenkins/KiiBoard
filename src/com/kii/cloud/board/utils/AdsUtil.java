package com.kii.cloud.board.utils;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.LinearLayout;

import com.kii.ad.KiiAdNetLayout;
import com.kii.ad.KiiAdNetTargeting;
import com.kii.ad.core.KiiAdNetManager;

public class AdsUtil {

	public static KiiAdNetLayout getKiiAdsLayout(Activity activity,
			String appId, String appKey) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		float scaledDensity = metrics.scaledDensity;
		KiiAdNetManager.setConfigExpireTimeout(1);

		KiiAdNetTargeting.setTestMode(true);
		KiiAdNetLayout adLayout = new KiiAdNetLayout(activity, appId, appKey);
		adLayout.setMaxHeight((int) (scaledDensity * 52));
		adLayout.setMaxWidth((int) (scaledDensity * 320));

		return adLayout;
	}


	public static void addToLayout(Activity activity, int layoutId, KiiAdNetLayout adLayout){
		try {
			LinearLayout layout = (LinearLayout) activity
					.findViewById(layoutId);
			layout.addView(adLayout);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
