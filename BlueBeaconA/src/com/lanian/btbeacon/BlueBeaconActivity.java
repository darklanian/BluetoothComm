package com.lanian.btbeacon;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BlueBeaconActivity extends Activity implements
		ActionBar.TabListener, BeaconServiceManager, OnBeaconSelectedListener {

	static final String TAG = "BlueBeacon";
	
	public static final int MSG_HELLO = 1;
	public static final int MSG_SHOW_CHAT_VIEW = 2;
	public static final String MSG_DATA_ADDRESS = "address";
	
	static final int TAB_STORED = 0;
	static final int TAB_CONVERSATION = 1;
	static final int TAB_SCAN = 2;
	static final int TAB_BANNED = 3;
	static final int TAB_MESSAGE = 4;
	
	static class SimpleHandler extends Handler {
		WeakReference<BlueBeaconActivity> target;
		
		public SimpleHandler(BlueBeaconActivity activity) {
			target = new WeakReference<BlueBeaconActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			boolean msgHandled = false;
			BlueBeaconActivity activity = target.get();
			if (activity != null)
				msgHandled = activity.handleMessage(msg);
			if (!msgHandled)
				super.handleMessage(msg);
		}
	}
	
	Messenger beaconService;
	Messenger handler = new Messenger(new SimpleHandler(this));
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_HELLO:
			Log.d(TAG, "OK");
			break;
		case MSG_SHOW_CHAT_VIEW:
			getFragmentManager().beginTransaction().replace(R.id.container, new ChatFragment().setRemoteAddress(msg.getData().getString(MSG_DATA_ADDRESS)).setBeaconServiceManager(this)).addToBackStack("ChatFragment").commit();
			break;
		default:
			return false;
		}
		return true;
	}
	
ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			beaconService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			beaconService = new Messenger(service);
			try {
				Message message = Message.obtain(null, BeaconService.MSG_HELLO);
				message.replyTo = handler;
				beaconService.send(message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		
		bindService(new Intent(this, BeaconService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (beaconService != null)
			unbindService(serviceConnection);
	}
	
	public boolean sendMessageTo(String address, String message) {
		Bundle data = new Bundle();
		data.putString(BeaconService.MSG_DATA_ADDRESS, address);
		data.putString(BeaconService.MSG_DATA_MESSAGE, message);
		
		Message msg = Message.obtain(null, BeaconService.MSG_SEND_MESSAGE);
		msg.setData(data);
		
		try {
			beaconService.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blue_beacon);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blue_beacon, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			
			switch (position) {
			case TAB_STORED:
				return new StoredBeaconListFragment();
			case TAB_SCAN:
				return new ScannedBeaconListFragment();
			case TAB_BANNED:
				return StoredBeaconListFragment.newInstance(true);
			}
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return getResources().getStringArray(R.array.tab_titles).length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			return getResources().getStringArray(R.array.tab_titles)[position];
			
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_blue_beacon,
					container, false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	@Override
	public void onBeaconSelected(String address) {
		mViewPager.setCurrentItem(TAB_MESSAGE);
	}

}
