package com.lanian.btbeacon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AliasDialogFragment extends DialogFragment {

	String address;
	String alias;
	EditText editText_alias;
	
	public static AliasDialogFragment newInstance(String address, String alias) {
		AliasDialogFragment dlg = new AliasDialogFragment();
		dlg.address = address;
		dlg.alias = alias;
		return dlg;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_alias, null);
		editText_alias = (EditText)dialogView.findViewById(R.id.editText_alias);
		if (alias != null)
			editText_alias.setText(alias);
		builder.setView(dialogView);
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setAlias();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
	
	private boolean setAlias() {
		String newAlias = editText_alias.getText().toString().trim();
		if (newAlias == null || newAlias.isEmpty() || alias.equals(newAlias))
			return false;
		
		ContentValues values = new ContentValues();
		values.put(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS, newAlias);
		return 0 != getActivity().getContentResolver().update(BlueBeaconProvider.CONTENT_URI_BEACON, values, 
				BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS+"=?", new String[] {address});
		
	}
}
