package com.lbconsulting.splits.dialogs;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;

public class PlayStore_DialogFragment extends DialogFragment {

	private TextView tvMessage;
	private Button btnBuySplits;
	private Button btnCancel;

	private String mDialogTitle;
	private String mDialogMessage;

	private static final String DIALOG_TITLE = "dialogTitle";
	private static final String DIALOG_ITEM_MESSAGE = "dialogMessage";

	public PlayStore_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static PlayStore_DialogFragment newInstance(String dialogTitle, String dialogMessage) {
		PlayStore_DialogFragment fragment = new PlayStore_DialogFragment();
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putString(DIALOG_ITEM_MESSAGE, dialogMessage);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("PlayStore_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			mDialogTitle = args.getString(DIALOG_TITLE);
			mDialogMessage = args.getString(DIALOG_ITEM_MESSAGE);
		}

		View view = inflater.inflate(R.layout.dialog_play_store, container);
		tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		if (tvMessage != null) {
			tvMessage.setText(mDialogMessage);
			Linkify.addLinks(tvMessage, Linkify.WEB_URLS);
		}

		btnBuySplits = (Button) view.findViewById(R.id.btnBuySplits);
		if (btnBuySplits != null) {
			btnBuySplits.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// send event to Flurry
					FlurryAgent.logEvent("btn_BUY_Splits");

					// start the play store application
					Intent intent = new Intent(Intent.ACTION_VIEW);
					// TODO: verify play store market URI
					intent.setData(Uri.parse("market://details?id=com.lbconsulting.SplitsPaid"));
					startActivity(intent);

					getDialog().dismiss();
				}
			});
		}

		btnCancel = (Button) view.findViewById(R.id.btnCancel);
		if (btnCancel != null) {
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// send event to Flurry
					FlurryAgent.logEvent("btn_CANCEL_BuySplits");
					getDialog().dismiss();
				}
			});
		}

		getDialog().setTitle(mDialogTitle);

		return view;
	}

}
