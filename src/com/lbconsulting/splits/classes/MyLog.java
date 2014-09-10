package com.lbconsulting.splits.classes;

import android.util.Log;

public class MyLog {

	public static final String TAG = "MeetTimer";

	public static void d(String className, String msg) {
		/*	if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, className + ": " + msg);
			}*/
	}

	public static void i(String className, String msg) {
		/*if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, className + ": " + msg);
		}*/
	}

	public static void e(String className, String msg) {
		if (Log.isLoggable(TAG, Log.ERROR)) {
			Log.e(TAG, className + ": " + msg);
		}
	}

	public static void v(String className, String msg) {
		/*if (Log.isLoggable(TAG, Log.VERBOSE)) {
			Log.v(TAG, className + ": " + msg);
		}*/
	}

	public static void w(String className, String msg) {
		/*if (Log.isLoggable(TAG, Log.WARN)) {
			Log.w(TAG, className + ": " + msg);
		}*/
	}
}
