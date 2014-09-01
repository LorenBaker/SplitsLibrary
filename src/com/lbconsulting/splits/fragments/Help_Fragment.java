package com.lbconsulting.splits.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;

public class Help_Fragment extends Fragment {

	private String mHtmlFormattedMessage;
	private TextView tvHelp;
	private Button btnOkFinished;

	public Help_Fragment() {
		// Empty constructor
	}

	public static Help_Fragment newInstance(String htmlFormattedMessage) {
		MyLog.i("Help_Fragment", "newInstance()");
		Help_Fragment fragment = new Help_Fragment();
		Bundle args = new Bundle();
		args.putString(MySettings.HELP_MESSAGE, htmlFormattedMessage);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Help_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_help, container, false);

		Bundle args = getArguments();
		if (args != null) {
			mHtmlFormattedMessage = args.getString(MySettings.HELP_MESSAGE);
		}

		tvHelp = (TextView) view.findViewById(R.id.tvHelp);
		if (tvHelp != null) {
			tvHelp.setText(Html.fromHtml(mHtmlFormattedMessage));
		}

		btnOkFinished = (Button) view.findViewById(R.id.btnOkFinished);
		if (btnOkFinished != null) {
			btnOkFinished.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});
		}

		return view;
	}

}
