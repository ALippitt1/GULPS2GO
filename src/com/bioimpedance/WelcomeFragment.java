/**
 * 
 */
package com.bioimpedance;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author ajl157
 *
 */
public class WelcomeFragment extends Fragment{
	
	/**
	 * This should be called to create the fragment
	 * @return
	 */
	public static WelcomeFragment newInstance() {
		WelcomeFragment f = new WelcomeFragment();
		
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.welcome_page, container, false);
		return v;
	}
	
	public void onClickHandler(int id) {
		
	}
}
