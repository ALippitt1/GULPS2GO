package savingPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * General dialog fragment list. Send in the Strings to be displayed, 
 * Calling Activity (e.g. MainActivity or a fragment) needs to implement the SelectListener
 * @author ajl157
 *
 */
public class SelectDisplayList extends DialogFragment {

	private int selectedDevice;
	private static ArrayList<String> btListNames;

	public static final int RESULT_SELECT = 0;
	public static final int RESULT_CANCEL = 1;
	public static final int RESULT_REDISCOVER = 2;
	
	private static final String TITLE = "Title";
	
	private static SelectListener mCallback;

	// Container Activity must implement this interface
	// Used to pass data to the top level
	public interface SelectListener {
		public void buttonClicked(); // This requires implementation e.g.
											// what to pass
	}

	// Empty constructor
	public SelectDisplayList() {
	}

	public static SelectDisplayList newInstance(int num, String title, ArrayList<String> deviceNames) {

		SelectDisplayList dialogFragment = new SelectDisplayList();
		btListNames = new ArrayList<String>(deviceNames);
		Bundle bundle = new Bundle();
		bundle.putInt("num", num);
		bundle.putString(TITLE, title);
		dialogFragment.setArguments(bundle);

		return dialogFragment;

	}
	
	/**
	 * Checks to make sure the callback is implemented
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (SelectListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SelectListener");
		}
	}

	public void initFragment(ArrayList<String> deviceNames) {
		btListNames = new ArrayList<String>(deviceNames);

	}

	public int getSelectedDevice() {
		return selectedDevice;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// final BluetoothFragment fragment = (BluetoothFragment)
		// getFragmentManager().findFragmentByTag("BluetoothFragment");
		String title = "";
		if(savedInstanceState != null) {
			title = savedInstanceState.getString(TITLE);
		}
		
		List<Object> list = Arrays.asList(btListNames.toArray());
		CharSequence[] stuff = list.toArray(new CharSequence[list.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set the dialog title
		builder.setTitle(title)
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
								mCallback.buttonClicked();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mCallback.buttonClicked();
							}
						});
						

		// Create the AlertDialog object and return it
		return builder.create();
	}
}