//
//
//  Copyright 2012 Kii Corporation
//  http://kii.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//

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
