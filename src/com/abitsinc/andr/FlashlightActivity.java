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

		Log.d(TAG, "onCreate - starting...");
		setContentView(R.layout.flashlight);
		Log.d(TAG, "cam " + (cam == null));
		initCamera();
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

		Log.d(TAG, "onPause " + (cam != null));
		if (cam != null) {
			Parameters campam = cam.getParameters();
			if (oldFlashMode != null)
				campam.setFlashMode(oldFlashMode);
			cam.stopPreview();
			cam.release();
			cam = null;
		}
	}

	private void initCamera() {
		if (cam != null) {
			Log.d(TAG, "cam not null during initCam");
			return; // should not happen
		}
		TextView txtStatus = (TextView) findViewById(R.id.statusText);

		int numCameras = Camera.getNumberOfCameras();
		if (numCameras <= 0) {
			Log.d(TAG, "number of cameras " + numCameras);
			this.useScreenAsFlashlight(txtStatus);
		} else {
			try { // some devices throw an exception here instead :(
				cam = Camera.open(0);
				if (cam != null) {
					completeCameraSetup(txtStatus);
				} else {
					throw new Exception("Unable to open camera");
				}
			} catch (Exception e) {
				Log.d(TAG, "Camera exception " + e.toString());
				this.useScreenAsFlashlight(txtStatus);
			}
		}
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