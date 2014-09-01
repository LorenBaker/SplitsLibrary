package com.lbconsulting.splits.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.DialogFragment;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.SplitsEvents.AddThumbnailToMemoryCache;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.database.AthletesTable;

import de.greenrobot.event.EventBus;

public class ContactAdder_DialogFragment extends DialogFragment implements OnAccountsUpdateListener {

	private Spinner mAccountSpinner;
	private Button btnSave;
	private Button btnCancel;

	private String mDialogTitle;
	private long mAthleteID;
	private String mAthleteDisplayName;

	private ArrayList<AccountData> mAccounts;
	private AccountAdapter mAccountAdapter;
	private EditText mContactEmailEditText;
	private ArrayList<Integer> mContactEmailTypes;
	private Spinner mContactEmailTypeSpinner;
	private EditText mContactNameEditText;
	private EditText mContactPhoneEditText;
	private ArrayList<Integer> mContactPhoneTypes;
	private Spinner mContactPhoneTypeSpinner;

	private AccountData mSelectedAccount;

	private static final String DIALOG_ATHLETE_ID = "dialogAthleteID";
	private static final String DIALOG_ATHLETE_DISPLAY_NAME = "dialogAthleteDisplayName";

	public ContactAdder_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static ContactAdder_DialogFragment newInstance(long athleteID, String athleteDisplayName) {
		ContactAdder_DialogFragment fragment = new ContactAdder_DialogFragment();
		Bundle args = new Bundle();
		args.putLong(DIALOG_ATHLETE_ID, athleteID);
		args.putString(DIALOG_ATHLETE_DISPLAY_NAME, athleteDisplayName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("ContactAdder_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			mDialogTitle = "Save To Contacts";
			mAthleteID = args.getLong(DIALOG_ATHLETE_ID, -1);
			mAthleteDisplayName = args.getString(DIALOG_ATHLETE_DISPLAY_NAME);
		}

		View view = inflater.inflate(R.layout.dialog_contact_adder, container);

		mAccountSpinner = (Spinner) view.findViewById(R.id.accountSpinner);
		mAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long i) {
				updateAccountSelection();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// We don't need to worry about nothing being selected, since Spinners don't allow this.
			}

		});

		mContactNameEditText = (EditText) view.findViewById(R.id.contactNameEditText);
		if (mContactNameEditText != null) {
			if (mAthleteDisplayName != null && !mAthleteDisplayName.isEmpty()) {
				mContactNameEditText.setText(mAthleteDisplayName);
			}
		}

		mContactPhoneEditText = (EditText) view.findViewById(R.id.contactPhoneEditText);
		mContactPhoneEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					mContactPhoneEditText.setText(PhoneNumberUtils.formatNumber(mContactPhoneEditText.getText()
							.toString().trim()));
				}

			}
		});
		mContactPhoneTypeSpinner = (Spinner) view.findViewById(R.id.contactPhoneTypeSpinner);

		mContactEmailEditText = (EditText) view.findViewById(R.id.contactEmailEditText);
		mContactEmailTypeSpinner = (Spinner) view.findViewById(R.id.contactEmailTypeSpinner);

		// Prepare list of supported account types
		// Note: Other types are available in ContactsContract.CommonDataKinds
		// Also, be aware that type IDs differ between Phone and Email, and MUST be computed separately.
		mContactPhoneTypes = new ArrayList<Integer>();
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);

		mContactEmailTypes = new ArrayList<Integer>();
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_HOME);
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_WORK);
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_MOBILE);
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_OTHER);

		// Prepare model for account spinner
		mAccounts = new ArrayList<AccountData>();
		mAccountAdapter = new AccountAdapter(getActivity(), mAccounts);
		mAccountSpinner.setAdapter(mAccountAdapter);

		// Populate list of account types for phone
		ArrayAdapter<String> adapter;
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Iterator<Integer> iter;
		iter = mContactPhoneTypes.iterator();
		while (iter.hasNext()) {
			adapter.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(
					this.getResources(),
					iter.next(),
					getString(R.string.undefinedTypeLabel)).toString());
		}
		mContactPhoneTypeSpinner.setAdapter(adapter);
		mContactPhoneTypeSpinner.setPrompt(getString(R.string.selectLabel));

		// Populate list of account types for email
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		iter = mContactEmailTypes.iterator();
		while (iter.hasNext()) {
			adapter.add(ContactsContract.CommonDataKinds.Email.getTypeLabel(
					this.getResources(),
					iter.next(),
					getString(R.string.undefinedTypeLabel)).toString());
		}
		mContactEmailTypeSpinner.setAdapter(adapter);
		mContactEmailTypeSpinner.setPrompt(getString(R.string.selectLabel));

		// Prepare the system account manager. On registering the listener below, we also ask for
		// an initial callback to pre-populate the account list.
		AccountManager.get(getActivity()).addOnAccountsUpdatedListener(this, null, true);

		// Register handlers for UI elements
		mAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long i) {
				updateAccountSelection();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// We don't need to worry about nothing being selected, since Spinners don't allow
				// this.
			}
		});

		btnSave = (Button) view.findViewById(R.id.btnSave);
		if (btnSave != null) {
			btnSave.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					createContactEntry();
					getDialog().dismiss();
				}

			});
		}

		btnCancel = (Button) view.findViewById(R.id.btnCancel);
		if (btnCancel != null) {
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Do Nothing
					getDialog().dismiss();
				}
			});
		}
		getDialog().setTitle(mDialogTitle);

		// Show soft keyboard automatically
		mContactPhoneEditText.requestFocus();
		getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return view;
	}

	/**
	 * Creates a contact entry from the current UI values in the account named by mSelectedAccount.
	 */
	protected void createContactEntry() {
		// Get values from UI
		String name = mContactNameEditText.getText().toString();
		String phone = mContactPhoneEditText.getText().toString();
		String email = mContactEmailEditText.getText().toString();
		int phoneType = mContactPhoneTypes.get(mContactPhoneTypeSpinner.getSelectedItemPosition());
		int emailType = mContactEmailTypes.get(mContactEmailTypeSpinner.getSelectedItemPosition());

		// Prepare contact creation request
		//
		// Note: We use RawContacts because this data must be associated with a particular account.
		// The system will aggregate this with any other data for this contact and create a
		// corresponding entry in the ContactsContract.Contacts provider for us.

		if (name != null && !name.isEmpty()) {
			// look for a contact with the provided name
			Uri uri = Contacts.CONTENT_URI;
			String[] projection = { Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME,
					Contacts.PHOTO_THUMBNAIL_URI, Contacts.PHOTO_URI };

			String selection = Contacts.DISPLAY_NAME + " = ?";
			String[] selectionArgs = { name };
			String sortOrder = null;
			Cursor cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs,
					sortOrder);
			if (cursor != null && cursor.getCount() > 0) {
				// the contact already exists
				UpdateAthlete(cursor, mAthleteID);
			} else {

				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
						.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, mSelectedAccount.getType())
						.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, mSelectedAccount.getName())
						.build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
						.build());

				if (phone != null && !phone.isEmpty()) {
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
							.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
							.build());
				}

				if (email != null && !email.isEmpty()) {
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
							.withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType)
							.build());
				}
				// Ask the Contact provider to create a new contact
				try {
					getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
				} catch (Exception e) {
					// Display warning
					Context ctx = getActivity().getApplicationContext();
					CharSequence txt = getString(R.string.contactCreationFailure);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(ctx, txt, duration);
					toast.show();

					// Log exception
					MyLog.e("ContactAdder_DialogFragment",
							"createContactEntry(); Exceptoin encoutered while inserting contact.");
					e.printStackTrace();
				}

				//
				if (cursor != null) {
					cursor.close();
				}
				cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs,
						sortOrder);
				if (cursor != null && cursor.getCount() > 0) {
					UpdateAthlete(cursor, mAthleteID);
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private void UpdateAthlete(Cursor cursor, long athleteID) {
		if (cursor != null && athleteID > 1) {
			cursor.moveToFirst();
			long id = cursor.getLong(0);
			String lookupKey = cursor.getString(1);
			Uri contactUri = ContactsContract.Contacts.getLookupUri(id, lookupKey);
			String photoThumbnail = cursor.getString(3);
			String photo = cursor.getString(4);

			ContentValues newFieldValues = new ContentValues();
			if (contactUri != null) {
				newFieldValues.put(AthletesTable.COL_CONTACT_URI, contactUri.toString());
				newFieldValues.put(AthletesTable.COL_LOOKUP_KEY, lookupKey);
			}
			if (photoThumbnail != null && !photoThumbnail.isEmpty()) {
				newFieldValues.put(AthletesTable.COL_PHOTO_THUMBNAIL_URI, photoThumbnail);
			}

			if (photo != null && !photo.isEmpty()) {
				newFieldValues.put(AthletesTable.COL_PHOTO_URI, photo);
			}

			AthletesTable.UpdateAthleteFieldValues(getActivity(), athleteID, newFieldValues);
			if (photoThumbnail != null && !photoThumbnail.isEmpty()) {
				EventBus.getDefault().post(new AddThumbnailToMemoryCache(athleteID, photoThumbnail));
			}

			cursor.close();
		}
	}

	@Override
	public void onDestroy() {
		MyLog.i("ContactAdder_DialogFragment", "onDestroy");
		super.onDestroy();
	}

	/**
	 * Updates account list spinner when the list of Accounts on the system changes. Satisfies
	 * OnAccountsUpdateListener implementation.
	 */
	public void onAccountsUpdated(Account[] a) {
		MyLog.i("ContactAdder_DialogFragment", "onAccountsUpdated: Account list update detected");
		// Clear out any old data to prevent duplicates
		mAccounts.clear();

		// Get account data from system
		AuthenticatorDescription[] accountTypes = AccountManager.get(getActivity()).getAuthenticatorTypes();

		// Populate tables
		for (int i = 0; i < a.length; i++) {
			// The user may have multiple accounts with the same name, so we need to construct a
			// meaningful display name for each.
			String systemAccountType = a[i].type;
			AuthenticatorDescription ad = getAuthenticatorDescription(systemAccountType,
					accountTypes);
			AccountData data = new AccountData(a[i].name, ad);
			mAccounts.add(data);
		}

		// Update the account spinner
		mAccountAdapter.notifyDataSetChanged();
	}

	/**
	 * Obtain the AuthenticatorDescription for a given account type.
	 * @param type The account type to locate.
	 * @param dictionary An array of AuthenticatorDescriptions, as returned by AccountManager.
	 * @return The description for the specified account type.
	 */
	private static AuthenticatorDescription getAuthenticatorDescription(String type,
			AuthenticatorDescription[] dictionary) {
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i].type.equals(type)) {
				return dictionary[i];
			}
		}
		// No match found
		throw new RuntimeException("Unable to find matching authenticator");
	}

	/**
	 * Update account selection. If NO_ACCOUNT is selected, then we prohibit inserting new contacts.
	 */
	private void updateAccountSelection() {
		// Read current account selection
		mSelectedAccount = (AccountData) mAccountSpinner.getSelectedItem();
	}

	/**
	 * A container class used to repreresent all known information about an account.
	 */
	private class AccountData {

		private String mName;
		private String mType;
		private CharSequence mTypeLabel;
		private Drawable mIcon;

		/**
		 * @param name The name of the account. This is usually the user's email address or
		 *        username.
		 * @param description The description for this account. This will be dictated by the
		 *        type of account returned, and can be obtained from the system AccountManager.
		 */
		public AccountData(String name, AuthenticatorDescription description) {
			mName = name;
			if (description != null) {
				mType = description.type;

				// The type string is stored in a resource, so we need to convert it into something
				// human readable.
				String packageName = description.packageName;
				PackageManager pm = getActivity().getPackageManager();

				if (description.labelId != 0) {
					mTypeLabel = pm.getText(packageName, description.labelId, null);
					if (mTypeLabel == null) {
						throw new IllegalArgumentException("LabelID provided, but label not found");
					}
				} else {
					mTypeLabel = "";
				}

				if (description.iconId != 0) {
					mIcon = pm.getDrawable(packageName, description.iconId, null);
					if (mIcon == null) {
						throw new IllegalArgumentException("IconID provided, but drawable not " +
								"found");
					}
				} else {
					mIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
				}
			}
		}

		public String getName() {
			return mName;
		}

		public String getType() {
			return mType;
		}

		public CharSequence getTypeLabel() {
			return mTypeLabel;
		}

		public Drawable getIcon() {
			return mIcon;
		}

		public String toString() {
			return mName;
		}
	}

	/**
	 * Custom adapter used to display account icons and descriptions in the account spinner.
	 */
	private class AccountAdapter extends ArrayAdapter<AccountData> {

		public AccountAdapter(Context context, ArrayList<AccountData> accountData) {
			super(context, android.R.layout.simple_spinner_item, accountData);
			setDropDownViewResource(R.layout.dialog_account_entry);
		}

		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			// Inflate a view template
			if (convertView == null) {
				LayoutInflater layoutInflater = getActivity().getLayoutInflater();
				convertView = layoutInflater.inflate(R.layout.dialog_account_entry, parent, false);
			}
			TextView firstAccountLine = (TextView) convertView.findViewById(R.id.firstAccountLine);
			TextView secondAccountLine = (TextView) convertView.findViewById(R.id.secondAccountLine);
			ImageView accountIcon = (ImageView) convertView.findViewById(R.id.accountIcon);

			// Populate template
			AccountData data = getItem(position);
			firstAccountLine.setText(data.getName());
			secondAccountLine.setText(data.getTypeLabel());
			Drawable icon = data.getIcon();
			if (icon == null) {
				icon = getResources().getDrawable(android.R.drawable.ic_menu_search);
			}
			accountIcon.setImageDrawable(icon);
			return convertView;
		}
	}
}
