package com.lbconsulting.splits.dialogs;

import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;

public class Help_DialogFragment extends DialogFragment implements OnClickListener {

	private int mHelpScreen = 0;
	private final int MAX_NUMBER_OF_SCREENS = 3;
	// private static final String ACTIVE_FRAGMENT = "activeFragment";
	// private LinearLayout mLayout;

	private ImageView mOverlayImageView;
	private Button btnHelpOverlayBack;
	private Button btnHelpOverlayNext;
	private Button btnHelpOverlayOK;
	private String mPNGfile = "";

	public Help_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static Help_DialogFragment newInstance() {
		Help_DialogFragment fragment = new Help_DialogFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Help_DialogFragment", "onCreateView");

		View view = inflater.inflate(R.layout.help_dialog_fragment_overlay, container);

		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		/*mLayout = (LinearLayout) view.findViewById(R.id.overlay);
		if (mLayout != null) {
			mLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();

				}
			});

		}*/

		mOverlayImageView = (ImageView) view.findViewById(R.id.overlayImageView);
		if (mOverlayImageView != null) {
			ShowHelpScreen();
		}

		btnHelpOverlayBack = (Button) view.findViewById(R.id.btnHelpOverlayBack);
		btnHelpOverlayBack.setOnClickListener(this);
		btnHelpOverlayBack.setEnabled(false);

		btnHelpOverlayNext = (Button) view.findViewById(R.id.btnHelpOverlayNext);
		btnHelpOverlayNext.setOnClickListener(this);

		btnHelpOverlayOK = (Button) view.findViewById(R.id.btnHelpOverlayOK);
		btnHelpOverlayOK.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.btnHelpOverlayBack) {
			mHelpScreen--;
			if (mHelpScreen < 0) {
				mHelpScreen = 0;
			}
			setBtnEnabled();
			ShowHelpScreen();

		} else if (id == R.id.btnHelpOverlayNext) {
			mHelpScreen++;
			if (mHelpScreen > MAX_NUMBER_OF_SCREENS - 1) {
				mHelpScreen = MAX_NUMBER_OF_SCREENS - 1;
			}
			setBtnEnabled();
			ShowHelpScreen();

		} else if (id == R.id.btnHelpOverlayOK) {
			getDialog().dismiss();
		}

	}

	private void setBtnEnabled() {
		if (mHelpScreen == 0) {
			btnHelpOverlayBack.setEnabled(false);
		} else {
			btnHelpOverlayBack.setEnabled(true);
		}

		if (mHelpScreen == MAX_NUMBER_OF_SCREENS - 1) {
			btnHelpOverlayNext.setEnabled(false);
		} else {
			btnHelpOverlayNext.setEnabled(true);
		}
	}

	private void ShowHelpScreen() {
		switch (mHelpScreen) {
			case 0:
				mPNGfile = "help0_swipe";
				break;

			case 1:
				mPNGfile = "help1_app_setup";
				break;

			case 2:
				mPNGfile = "help2_start_race";
				break;
			default:

				break;

		}

		int imageId = getResources().getIdentifier(mPNGfile, "drawable", getActivity().getPackageName());
		Drawable res = getResources().getDrawable(imageId);
		mOverlayImageView.setImageDrawable(res);
	}

	@Override
	public void onDestroy() {
		MyLog.i("Help_DialogFragment", "onDestroy");
		super.onDestroy();
	}

}
