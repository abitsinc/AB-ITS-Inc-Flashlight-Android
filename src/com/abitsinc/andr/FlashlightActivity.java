/*
	This file is part of AB ITS Inc Flashlight Android.

	AB ITS Inc Flashlight Android is free software: you can redistribute it and/or 
	modify it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	AB ITS Inc Flashlight Android is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
	or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
	more details.

	You should have received a copy of the GNU General Public License
	along with AB ITS Inc Flashlight Android.  
	If not, see <http://www.gnu.org/licenses/>. 
 */

package com.abitsinc.andr;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class FlashlightActivity extends Activity {

	private static final String TAG = "ABITSIncFlashlight";
	private android.hardware.Camera cam = null;
	private String oldFlashMode = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if application is being restored due to orientation change
		this.cam = (Camera) getLastNonConfigurationInstance();

		Log.d(TAG, "onCreate - starting...");
		setContentView(R.layout.flashlight);
		Log.d(TAG, "cam " + (cam == null));
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return this.cam;
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG + " onResume", "starting...");
		initCamera();
	}

	@Override
	protected void onPause() {
		super.onPause();

		int gcc = this.getChangingConfigurations();
		Log.d(TAG, "onPause " + (cam != null) + " gCC " + Integer.toHexString(gcc));
		// if config change occurred due to orientation change, then let flash be
		if (gcc == 0x480)
			return;

		if (cam != null) {
			Parameters campam = cam.getParameters();
			if (oldFlashMode != null)
				campam.setFlashMode(oldFlashMode);
			cam.stopPreview();
			cam.release();
			cam = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

	private void initCamera() {
		TextView txtStatus = (TextView) findViewById(R.id.statusText);

		try {
			if (cam == null) {
				int numCameras = Camera.getNumberOfCameras();
				if (numCameras <= 0) {
					Log.d(TAG, "number of cameras " + numCameras);
					throw new Exception("Cameras not present");
				} else {
					cam = Camera.open(0);
					if (cam == null)
						throw new Exception("Camera open failed");
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Camera exception " + e.toString());
			this.useScreenAsFlashlight(txtStatus);
			return;
		}

		completeCameraSetup(txtStatus);
	}

	private void completeCameraSetup(TextView txtStatus) {
		Parameters campam = cam.getParameters();

		oldFlashMode = campam.getFlashMode();
		Log.d(TAG, "flashMode1 " + oldFlashMode);
		if (oldFlashMode == null)
			oldFlashMode = Parameters.FLASH_MODE_AUTO;

		campam.setFlashMode(Parameters.FLASH_MODE_TORCH);
		cam.setParameters(campam);

		txtStatus.setText(R.string.status_set);
		cam.startPreview();

		String tmpFlashMode = campam.getFlashMode();
		if (tmpFlashMode == null) {
			Log.d(TAG, "flashMode not supported");
			this.useScreenAsFlashlight(txtStatus);
		}
	}

	private void useScreenAsFlashlight(TextView txtStatus) {
		txtStatus.setText(R.string.status_nld);
		TextView txtBkupLight = (TextView) findViewById(R.id.tvBkupLightSrc);
		txtBkupLight.setBackgroundColor(Color.WHITE);
	}
}