package com.integreight.onesheeld.appFragments;

import java.util.Enumeration;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.ArduinoConnectivityPopup;
import com.integreight.onesheeld.FirmwareUpdatingPopup;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.OneSheeldVersionInstallerPopupTesting;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.DeviceListActivity;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.ListViewReversed;
import com.integreight.onesheeld.utils.OneShieldEditText;

public class SheeldsList extends Fragment {
	View v;
	boolean isInflated = false;
	private ListViewReversed shieldsListView;
	private static SheeldsList thisInstance;
	private List<UIShield> shieldsUIList;
	private ShieldsListAdapter adapter;
	private MenuItem bluetoothSearchActionButton;
	private MenuItem bluetoothDisconnectActionButton;
	private MenuItem goToShieldsOperationActionButton;

	private static final String TAG = "ShieldsList";

	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 3;
	private static boolean arduinoConnected;

	public static SheeldsList getInstance() {
		if (thisInstance == null) {
			thisInstance = new SheeldsList();
		}
		return thisInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		isInflated = (v == null);
		if (v == null)
			v = inflater.inflate(R.layout.app_sheelds_list, container, false);
		else {
			try {
				((ViewGroup) v.getParent()).removeView(v);
			} catch (Exception e) {
				// TODO: handle exception
			}
			// if (shieldsListView != null)
			// shieldsListView.setSelection(1);
		}
		return v;
	}

	@Override
	public void onStop() {
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				if (getActivity() != null
						&& v.findViewById(R.id.searchArea) != null) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(((OneShieldEditText) v
							.findViewById(R.id.searchArea)).getWindowToken(), 0);
				}
			}
		});
		super.onStop();
	}

	@Override
	public void onResume() {
		MainActivity.currentShieldTag = null;
		((MainActivity) getActivity()).disableMenu();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				List<Fragment> frags = getActivity()
						.getSupportFragmentManager().getFragments();
				for (Fragment frag : frags) {
					if (frag != null
							&& !frag.getClass().getName()
									.equals(SheeldsList.class.getName())) {
						FragmentTransaction ft = getActivity()
								.getSupportFragmentManager().beginTransaction();
						frag.onDestroy();
						ft.remove(frag);
						ft.commitAllowingStateLoss();
					}
				}
			}
		}, 500);
		getActivity().findViewById(R.id.getAvailableDevices)
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						launchShieldsOperationActivity();
					}
				});
		((MainActivity) getActivity()).getOnConnectionLostHandler().canInvokeOnCloseConnection = true;
		((OneSheeldApplication) getActivity().getApplication())
				.setArduinoFirmataEventHandler(sheeldsFirmataHandler);
		if (((OneSheeldApplication) getActivity().getApplication())
				.getAppFirmata() == null
				|| (((OneSheeldApplication) getActivity().getApplication())
						.getAppFirmata() != null && !((OneSheeldApplication) getActivity()
						.getApplication()).getAppFirmata().isOpen())) {
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// setRetainInstance(true);
		if (isInflated)
			initView();
		super.onActivityCreated(savedInstanceState);
	}

	// Handler searchActionHandler = new Handler();
	// int targetMargin = 0;
	// private boolean isExpanding = false;
	boolean isAnimating = false;

	private void initView() {
		shieldsListView = (ListViewReversed) getView().findViewById(
				R.id.sheeldsList);
		shieldsUIList = UIShield.valuesFiltered();
		// shieldsListView.setEnabled(false);
		adapter = new ShieldsListAdapter(getActivity());
		shieldsListView.setAdapter(adapter);
		final int oneHudredDP = (int) (100 * getResources().getDisplayMetrics().density - .5f);
		final int seventyFiveDB = (int) (75 * getResources()
				.getDisplayMetrics().density - .5f);
		final int listHeight = (seventyFiveDB * shieldsUIList.size())
				+ oneHudredDP;
		final View searchAreaContainer = getView().findViewById(
				R.id.shieldSearchAreaContainer);
		final RelativeLayout.LayoutParams searchAreaParams = (RelativeLayout.LayoutParams) searchAreaContainer
				.getLayoutParams();
		// final Animation translate = new Animation() {
		// @Override
		// protected void applyTransformation(float interpolatedTime,
		// Transformation t) {
		// searchAreaParams.topMargin = (int) (targetMargin * interpolatedTime);
		// searchAreaContainer.requestLayout();
		// super.applyTransformation(interpolatedTime, t);
		// if (interpolatedTime < 1.0f) {
		// if (isExpanding) {
		// searchAreaParams.topMargin = (int) (targetMargin * interpolatedTime);
		// } else {
		// searchAreaParams.topMargin = (int) (targetMargin * (1 -
		// interpolatedTime));
		// }
		// searchAreaContainer.requestLayout();
		// } else {
		// if (isExpanding) {
		// searchAreaParams.topMargin = 0;
		// } else {
		// searchAreaParams.topMargin = -1 * oneHudredDP;
		// }
		// searchAreaContainer.requestLayout();
		// }
		// }
		// };
		// translate.setDuration(200);
		// if (isInflated)
		// shieldsListView.setSelection(1);
		shieldsListView.setCacheColorHint(Color.TRANSPARENT);
		shieldsListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		shieldsListView.setDrawingCacheEnabled(true);
		shieldsListView.setOnScroll(new ListViewReversed.OnScrollListener() {

			@Override
			public void onUp() {
				// Animation anim = new TranslateAnimation(0, 0,
				// searchAreaParams.topMargin, 0);
				// anim.setDuration(300);
				// anim.setAnimationListener(new Animation.AnimationListener() {
				//
				// @Override
				// public void onAnimationStart(Animation animation) {
				// isAnimating = true;
				// }
				//
				// @Override
				// public void onAnimationRepeat(Animation animation) {
				// // TODO Auto-generated method stub
				//
				// }
				//
				// @Override
				// public void onAnimationEnd(Animation animation) {
				searchAreaParams.topMargin = 0;
				searchAreaContainer.requestLayout();
				// isAnimating = false;
				// }
				// });
				// if (searchAreaParams.topMargin < 0)
				// searchAreaContainer.startAnimation(anim);
			}

			@Override
			public void onDown() {
				// Animation anim = new TranslateAnimation(0, 0,
				// searchAreaParams.topMargin, -1 * oneHudredDP);
				// anim.setDuration(300);
				// anim.setAnimationListener(new Animation.AnimationListener() {
				//
				// @Override
				// public void onAnimationStart(Animation animation) {
				// isAnimating = true;
				// }
				//
				// @Override
				// public void onAnimationRepeat(Animation animation) {
				// // TODO Auto-generated method stub
				//
				// }
				//
				// @Override
				// public void onAnimationEnd(Animation animation) {
				searchAreaParams.topMargin = -1 * oneHudredDP;
				searchAreaContainer.requestLayout();
				// isAnimating = false;
				// }
				// });
				// if (searchAreaParams.topMargin > -1 * oneHudredDP)
				// searchAreaContainer.startAnimation(anim);
			}

			@Override
			public void translate(int topMargin) {
				topMargin = (int) ((topMargin * oneHudredDP * 5 / listHeight));
				if (topMargin <= 0) {
					// if (topMargin * -1 <= oneHudredDP) {
					searchAreaParams.topMargin = topMargin;
					searchAreaContainer.requestLayout();
					// }
				} else {
					topMargin = topMargin - oneHudredDP;
					if (topMargin <= 0) {
						searchAreaParams.topMargin = topMargin;
						// else
						// searchAreaParams.topMargin = 0;
						// searchAreaParams.topMargin = 0;
						// searchAreaContainer.requestLayout();
						// searchAreaParams.topMargin = 0;
						searchAreaContainer.requestLayout();
						//
					}
				}
			}
		});
		final OneShieldEditText searchBox = (OneShieldEditText) getView()
				.findViewById(R.id.searchArea);
		searchBox.setAdapter(adapter);
		searchBox.setDropDownHeight(0);
		getView().findViewById(R.id.selectAll).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						for (UIShield shield : UIShield.valuesFiltered()) {
							UIShield.valueOf(shield.name())
									.setMainActivitySelection(true);
						}
						searchBox.setText("");
						adapter.selectAll();
					}
				});
		getView().findViewById(R.id.reset).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						for (UIShield shield : UIShield.valuesFiltered()) {
							UIShield.valueOf(shield.name())
									.setMainActivitySelection(false);
						}
						searchBox.setText("");
						adapter.reset();
					}
				});
	}

	private void disconnectService() {
		if (isOneSheeldServiceRunning()) {
			((MainActivity) getActivity()).stopService();
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}
	}

	private void launchShieldsOperationActivity() {
		if (!isAnyShieldsSelected()) {
			Toast.makeText(getActivity(), "Select at least 1 shield",
					Toast.LENGTH_LONG).show();
			return;
		}
		((MainActivity) getActivity()).replaceCurrentFragment(
				R.id.appTransitionsContainer, ShieldsOperations.getInstance(),
				ShieldsOperations.class.getName(), true, true);
	}

	ArduinoFirmataEventHandler sheeldsFirmataHandler = new ArduinoFirmataEventHandler() {

		@Override
		public void onError(String errorMessage) {
			UIShield.setConnected(false);
			adapter.notifyDataSetChanged();
			arduinoConnected = false;
			if (getActivity().getSupportFragmentManager()
					.getBackStackEntryCount() > 1) {
				getActivity().getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
				getActivity().getSupportFragmentManager()
						.executePendingTransactions();
			}
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}

		@Override
		public void onConnect() {
			Log.e(TAG, "- ARDUINO CONNECTED -");
			if (isOneSheeldServiceRunning()) {
				arduinoConnected = true;
				if (adapter != null)
					adapter.applyToControllerTable();
			}
		}

		@Override
		public void onClose(boolean closedManually) {
			arduinoConnected = false;
			if (getActivity() != null) {
				((MainActivity) getActivity()).getOnConnectionLostHandler().connectionLost = true;
				if (((MainActivity) getActivity()).getOnConnectionLostHandler().canInvokeOnCloseConnection
						|| ((MainActivity) getActivity()).isForground)
					((MainActivity) getActivity()).getOnConnectionLostHandler()
							.sendEmptyMessage(0);
				else {
					List<Fragment> frags = getActivity()
							.getSupportFragmentManager().getFragments();
					for (Fragment frag : frags) {
						if (frag != null
								&& !frag.getClass().getName()
										.equals(SheeldsList.class.getName())
								&& !frag.getClass()
										.getName()
										.equals(ShieldsOperations.class
												.getName())) {
							FragmentTransaction ft = getActivity()
									.getSupportFragmentManager()
									.beginTransaction();
							ft.setCustomAnimations(0, 0, 0, 0);
							frag.onDestroy();
							ft.remove(frag);
							ft.commitAllowingStateLoss();
						}
					}
				}
				Enumeration<String> enumKey = ((OneSheeldApplication) getActivity()
						.getApplication()).getRunningShields().keys();
				while (enumKey.hasMoreElements()) {
					String key = enumKey.nextElement();
					((OneSheeldApplication) getActivity().getApplication())
							.getRunningShields().get(key).resetThis();
					((OneSheeldApplication) getActivity().getApplication())
							.getRunningShields().remove(key);
				}
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflate.inflate(R.menu.main, menu);
		bluetoothSearchActionButton = (MenuItem) menu
				.findItem(R.id.main_activity_action_search);
		bluetoothDisconnectActionButton = (MenuItem) menu
				.findItem(R.id.main_activity_action_disconnect);
		goToShieldsOperationActionButton = (MenuItem) menu
				.findItem(R.id.main_activity_action_forward);

		if (!arduinoConnected || !isOneSheeldServiceRunning())
			changeActionIconsToDisconnected();
		else
			changeActionIconsToConnected();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.main_activity_action_search:
			// Launch the DeviceListActivity to see devices and do scan

			serverIntent = new Intent(getActivity(), DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.main_activity_action_disconnect:
			disconnectService();
			return true;
		case R.id.main_activity_action_forward:
			launchShieldsOperationActivity();
			return true;
		case R.id.open_bootloader_popup:
			if (!OneSheeldVersionInstallerPopupTesting.isOpened)
				new FirmwareUpdatingPopup((MainActivity) getActivity(), false)
						.show();
			return true;
		case R.id.open_bootloader_popup_china:
			if (!OneSheeldVersionInstallerPopupTesting.isOpened)
				new FirmwareUpdatingPopup((MainActivity) getActivity(), true)
						.show();
			return true;
		case R.id.action_settings:
			((OneSheeldApplication) getActivity().getApplication())
					.setLastConnectedDevice(null);
			return true;
		}

		return false;
	}

	private boolean isOneSheeldServiceRunning() {
		if (getActivity() != null) {
			ActivityManager manager = (ActivityManager) getActivity()
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (RunningServiceInfo service : manager
					.getRunningServices(Integer.MAX_VALUE)) {
				if (OneSheeldService.class.getName().equals(
						service.service.getClassName())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isAnyShieldsSelected() {
		int i = 0;
		OneSheeldApplication app = (OneSheeldApplication) getActivity()
				.getApplication();
		// app.setRunningSheelds(new Hashtable<String, ControllerParent<?>>());
		for (UIShield shield : shieldsUIList) {
			if (shield.isMainActivitySelection()
					&& shield.getShieldType() != null) {
				if (app.getRunningShields().get(shield.name()) == null) {
					ControllerParent<?> type = null;
					try {
						type = shield.getShieldType().newInstance();
					} catch (java.lang.InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					type.setActivity((MainActivity) getActivity()).setTag(
							shield.name());
				}
				i++;
			}
		}
		if (i > 0)
			return true;
		return false;
	}

	private void changeActionIconsToConnected() {
		if (bluetoothSearchActionButton != null)
			bluetoothSearchActionButton.setVisible(false);
		if (bluetoothDisconnectActionButton != null)
			bluetoothDisconnectActionButton.setVisible(true);
		if (goToShieldsOperationActionButton != null)
			goToShieldsOperationActionButton.setVisible(true);
	}

	private void changeActionIconsToDisconnected() {
		if (bluetoothSearchActionButton != null)
			bluetoothSearchActionButton.setVisible(true);
		if (bluetoothDisconnectActionButton != null)
			bluetoothDisconnectActionButton.setVisible(false);
		if (goToShieldsOperationActionButton != null)
			goToShieldsOperationActionButton.setVisible(false);
	}
}
