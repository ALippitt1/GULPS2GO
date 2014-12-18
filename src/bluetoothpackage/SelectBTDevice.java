package bluetoothpackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SelectBTDevice extends DialogFragment {

	private int selectedDevice;
	private static ArrayList<String> btDeviceNames;

	public static final int RESULT_SELECT = 0;
	public static final int RESULT_CANCEL = 1;
	public static final int RESULT_REDISCOVER = 2;

	// Empty constructor
	public SelectBTDevice() {
	}

	public static SelectBTDevice newInstance(int num) {

		SelectBTDevice dialogFragment = new SelectBTDevice();
		Bundle bundle = new Bundle();
		bundle.putInt("num", num);
		dialogFragment.setArguments(bundle);

		return dialogFragment;

	}

	public void initFragment(ArrayList<String> deviceNames) {
		btDeviceNames = new ArrayList<String>(deviceNames);

	}

	public int getSelectedDevice() {
		return selectedDevice;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		List<Object> list = Arrays.asList(btDeviceNames.toArray());
		CharSequence[] stuff = list.toArray(new CharSequence[list.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set the dialog title
		builder.setTitle("Select a device to connect to")
				// Specify the list array, the items to be selected by default
				// (null for none),
				// and the listener through which to receive callbacks when
				// items are selected
				.setSingleChoiceItems(stuff, -1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								selectedDevice = which;
							}
						})
				// Set the action buttons
				.setPositiveButton("Select",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Connect to selected device

								getTargetFragment().onActivityResult(
										getTargetRequestCode(), RESULT_SELECT,
										getActivity().getIntent());
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								getTargetFragment().onActivityResult(
										getTargetRequestCode(), RESULT_CANCEL,
										getActivity().getIntent());
							}
						})
				.setNeutralButton("Rediscover",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Rediscover devices
								getTargetFragment().onActivityResult(
										getTargetRequestCode(),
										RESULT_REDISCOVER,
										getActivity().getIntent());
							}
						});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}