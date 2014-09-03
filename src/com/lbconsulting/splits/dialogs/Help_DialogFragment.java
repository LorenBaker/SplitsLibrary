package com.lbconsulting.splits.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;

public class Help_DialogFragment extends DialogFragment {

	// private static final String ACTIVE_FRAGMENT = "activeFragment";
	private static final String DIALOG_MESSAGE = "dialogMessage";

	// private int mActiveFragment;
	private String mDialogMessage;

	private TextView tvDialogMessage;
	private Button btnOKgotIt;

	public Help_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static Help_DialogFragment newInstance(String dialogMessage) {
		Help_DialogFragment fragment = new Help_DialogFragment();
		Bundle args = new Bundle();
		// args.putInt(ACTIVE_FRAGMENT, activeFragment);
		args.putString(DIALOG_MESSAGE, dialogMessage);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Help_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			// mActiveFragment = args.getInt(ACTIVE_FRAGMENT, -1);
			mDialogMessage = args.getString(DIALOG_MESSAGE);
		}

		View view = inflater.inflate(R.layout.help_dialog_fragment, container);

		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(false);
		// getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		tvDialogMessage = (TextView) view.findViewById(R.id.tvDialogMessage);
		if (tvDialogMessage != null) {
			tvDialogMessage.setText(Html.fromHtml(mDialogMessage));
		}
		btnOKgotIt = (Button) view.findViewById(R.id.btnOKgotIt);
		if (btnOKgotIt != null) {
			btnOKgotIt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
			});
		}

		return view;
	}

	@Override
	public void onResume() {
		MyLog.i("Help_DialogFragment", "onResume()");
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Help_DialogFragment", "onPause()");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Help_DialogFragment", "onDestroy()");
		super.onDestroy();
	}

}
