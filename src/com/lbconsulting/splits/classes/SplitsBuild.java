package com.lbconsulting.splits.classes;

import android.content.Context;

import com.lbconsulting.splits.R;

public class SplitsBuild {

	public final static int FREE = 539739646;
	public final static int PAID = 189259694;

	public static int getBuild(Context context) {
		return context.getResources().getInteger(R.integer.build);
	}

	public static boolean isPaid(Context context) {
		boolean results = false;
		if (context.getResources().getInteger(R.integer.build) == PAID) {
			results = VerfyPaid();
		}
		return results;
	}

	public static boolean isFree(Context context) {
		return !isPaid(context);
	}

	@SuppressWarnings("unused")
	private static boolean VerfyPaid() {
		boolean results = false;

		String sClassName = "com.lbconsulting.splits.paid_classes.VerifiyPaid";
		try {
			Class<?> classToInvestigate = Class.forName(sClassName);
			results = true;
			// Dynamically do stuff with this class
			// List constructors, fields, methods, etc.

		} catch (ClassNotFoundException e) {
			// Class not found ... must be free app
			// return the default false value

		} catch (Exception e) {
			// Unknown exception
			MyLog.e("Class SplitsBuild", "VerfyPaid. Unknown exception.");
			e.printStackTrace();
		}

		return results;
	}

}
