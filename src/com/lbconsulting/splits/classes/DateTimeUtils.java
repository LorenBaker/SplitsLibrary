package com.lbconsulting.splits.classes;

import java.util.ArrayList;

import android.text.TextUtils;

public class DateTimeUtils {

	public static final int FORMAT_SECONDS = 0;
	public static final int FORMAT_TENTHS = 1;
	public static final int FORMAT_HUNDREDTHS = 2;

	private static final long millsPerSecond = 1000;
	private static final long millsPerMinute = 60 * 1000;
	private static final long millsPerHour = 60 * 60 * 1000;

	public static ArrayList<Integer> getHrsMinSec(long time) {
		ArrayList<Integer> hrsMinSec = new ArrayList<Integer>();

		String milliseconds = String.valueOf(time);
		switch (milliseconds.length()) {
			case 1:
			case 2:
			case 3:
				break;

			default:
				milliseconds = milliseconds.substring(milliseconds.length() - 3, milliseconds.length());
				break;
		}

		int milliSec = Integer.parseInt(milliseconds);

		long timeWholeSeconds = time - Long.valueOf(milliSec);
		long hrs = timeWholeSeconds / millsPerHour;
		timeWholeSeconds = timeWholeSeconds - (hrs * millsPerHour);
		long mins = timeWholeSeconds / millsPerMinute;
		timeWholeSeconds = timeWholeSeconds - (mins * millsPerMinute);
		long secs = timeWholeSeconds / millsPerSecond;

		hrsMinSec.add((int) hrs);
		hrsMinSec.add((int) mins);
		hrsMinSec.add((int) secs);
		hrsMinSec.add(milliSec);

		return hrsMinSec;
	}

	public static long RoundMills(long time, int numberFormat) {
		long roundedTime = -1;
		String milliseconds = String.valueOf(time);

		int milliSec = -1;
		roundedTime = time;

		switch (numberFormat) {
			case FORMAT_SECONDS:
				roundedTime = (roundedTime / 1000) * 1000;
				switch (milliseconds.length()) {
					case 1:
					case 2:
					case 3:
						break;

					default:
						milliseconds = milliseconds.substring(milliseconds.length() - 3, milliseconds.length());
						break;
				}
				milliSec = Integer.parseInt(milliseconds);
				if (milliSec > 499) {
					roundedTime += 1000;
				}
				break;

			case FORMAT_TENTHS:
				roundedTime = (roundedTime / 100) * 100;
				switch (milliseconds.length()) {
					case 1:
					case 2:
						break;
					case 3:
					default:
						milliseconds = milliseconds.substring(milliseconds.length() - 2, milliseconds.length());
						break;
				}
				milliSec = Integer.parseInt(milliseconds);
				if (milliSec > 49) {
					roundedTime += 100;
				}
				break;

			case FORMAT_HUNDREDTHS:
				roundedTime = (roundedTime / 10) * 10;
				switch (milliseconds.length()) {
					case 1:
						break;
					case 2:
					case 3:
					default:
						milliseconds = milliseconds.substring(milliseconds.length() - 1, milliseconds.length());
						break;
				}
				milliSec = Integer.parseInt(milliseconds);
				if (milliSec > 4) {
					roundedTime += 10;
				}
				break;

			default:
				break;
		}
		return roundedTime;
	}

	public static String formatDuration(float time, int numberFormat) {
		long secs = (long) (time / millsPerSecond);
		long mins = (long) (time / millsPerMinute);
		long hrs = (long) (time / millsPerHour);
		// Convert the seconds to String * and format to ensure it has * a leading zero when required
		secs = secs % 60;
		String seconds = String.valueOf(secs);
		if (secs == 0) {
			seconds = "00";
		}
		if (secs < 10 && secs > 0) {
			seconds = "0" + seconds;
		}
		// Convert the minutes to String and format the String
		mins = mins % 60;
		String minutes = String.valueOf(mins);
		if (hrs > 0) {
			if (mins == 0) {
				minutes = "00";
			}
			if (mins < 10 && mins > 0) {
				minutes = "0" + minutes;
			}
		} else {
			if (mins == 0) {
				minutes = "0";
			}
		}

		String milliseconds = String.valueOf((long) time);
		if (milliseconds.length() == 1) {
			milliseconds = "00" + milliseconds;

		} else if (milliseconds.length() == 2) {
			milliseconds = "0" + milliseconds;
		}

		String result = "";

		switch (numberFormat) {

			case FORMAT_SECONDS:
				if (hrs > 0) {
					// Convert the hours to String and format the String
					String hours = String.valueOf(hrs);
					result = hours + ":" + minutes + ":" + seconds;
				} else {
					result = minutes + ":" + seconds;
				}

				break;

			case FORMAT_TENTHS:

				switch (milliseconds.length()) {
					case 1:
					case 2:
						milliseconds = "0";
						break;
					case 3:
						milliseconds = milliseconds.substring(0, 1);
						break;

					default:
						milliseconds = milliseconds.substring(milliseconds.length() - 3, milliseconds.length() - 2);
						break;
				}

				if (hrs > 0) {
					// Convert the hours to String and format the String
					String hours = String.valueOf(hrs);
					result = hours + ":" + minutes + ":" + seconds + "." + milliseconds;
				} else {
					result = minutes + ":" + seconds + "." + milliseconds;
				}
				break;

			case FORMAT_HUNDREDTHS:

				switch (milliseconds.length()) {
					case 1:
						milliseconds = "00";
						break;

					case 2:
						milliseconds = "0" + milliseconds.substring(0, 1);
						break;
					case 3:
						milliseconds = milliseconds.substring(0, 2);
						break;

					default:
						milliseconds = milliseconds.substring(milliseconds.length() - 3, milliseconds.length() - 1);
						break;
				}

				if (hrs > 0) {
					// Convert the hours to String and format the String
					String hours = String.valueOf(hrs);
					result = hours + ":" + minutes + ":" + seconds + "." + milliseconds;
				} else {
					result = minutes + ":" + seconds + "." + milliseconds;
				}
				break;

			default:
				break;

		}
		return result;

		// Setting the timer text to the elapsed time
		// timer.setText(hours + ":" + minutes + ":" + seconds + "." + milliseconds);
		// ((TextView) findViewById(R.id.timerMs)).setText("." + milliseconds);
		// - See more at:
		// http://www.shawnbe.com/index.php/tutorial/tutorial-3-a-simple-stopwatch-lets-add-the-code/#sthash.TsUYrP0G.dpuf
	}

	public static long getTime(ArrayList<Integer> time_HrsMinSec) {
		return time_HrsMinSec.get(0) * millsPerHour
				+ time_HrsMinSec.get(1) * millsPerMinute
				+ time_HrsMinSec.get(2) * millsPerSecond
				+ time_HrsMinSec.get(3);
	}

	public final static boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

}
