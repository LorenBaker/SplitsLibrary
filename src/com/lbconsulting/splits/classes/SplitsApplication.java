package com.lbconsulting.splits.classes;

import java.util.Locale;

import android.app.Application;
import android.content.Context;

import com.lbconsulting.splits.R;

public class SplitsApplication extends Application {

	private Context mContext;

	public SplitsApplication(Context context) {
		mContext = context;
	}

	public boolean isFreeVersion() {
		/*String packageName = mContext.getPackageName();
		return packageName.toLowerCase(Locale.US).contains(".app.free");*/
		return !isPaidVersion();
	}

	public boolean isPaidVersion() {
		String packageName = mContext.getPackageName();
		int build = getBuild();
		return packageName.toLowerCase(Locale.US).contains(".app.paid") && build == 189259694;
	}

	public String getPackageName() {
		return mContext.getPackageName();
	}

	public String getFlurryApiKey() {
		// free Flurry API key
		String FLURRY_API_KEY = "NDSMQ6YCZZGWG2DX78CN ";
		if (isPaidVersion()) {
			FLURRY_API_KEY = "YCZHS8D67FCDQJZ8WYF7";
		}
		return FLURRY_API_KEY;
	}

	public int getBuild() {
		return mContext.getResources().getInteger(R.integer.build);
	}

}
