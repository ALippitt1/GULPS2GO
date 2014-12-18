package com.display;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bioimpedance.R;

public class EditTypeDialog extends DialogFragment implements OnEditorActionListener{
	private EditText mEditText;
	
	public interface EditTypeListener {
		public void onFinishedEditDialog(String text);
	}
	
	public EditTypeDialog() {
		//Empty Constructor required for DialogFragments
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_dialog_fragment, container);
        mEditText = (EditText) view.findViewById(R.id.dialogEditField);
        getDialog().setTitle("Hello");

     // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);

        return view;
    }

	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(EditorInfo.IME_ACTION_DONE == actionId) {
			EditTypeListener activity = (EditTypeListener) getActivity();
            activity.onFinishedEditDialog(mEditText.getText().toString());
			this.dismiss();
			return true;
		}
		return false;
	}

	
}
