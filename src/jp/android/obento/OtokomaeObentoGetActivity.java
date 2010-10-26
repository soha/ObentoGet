package jp.android.obento;

import java.util.List;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;

public class OtokomaeObentoGetActivity extends ObentoGetActivity  implements SensorEventListener {
	
	private boolean mRegisteredSensor;
	private SensorManager mSensorManager;

	private static final int FORCE_THRESHOLD = 1000;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;

	private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
	private long mLastTime;
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;
	
	/**
	 * Activity生成時
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.otokomae);

		orderButton = (Button) findViewById(R.id.OrderButton);
		orderButton.setOnClickListener(this);
		
		mRegisteredSensor = false;
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}


	@Override
	protected void onResume() {
		super.onResume();

		List<Sensor> sensors = mSensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors.size() > 0) {
			Sensor sensor = sensors.get(0);
			mRegisteredSensor = mSensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
	}
	
	@Override
	protected void onPause() {
		if (mRegisteredSensor) {
			mSensorManager.unregisterListener(this);
			mRegisteredSensor = false;
		}

		super.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Toast.makeText(this, "onAccuracyChanged", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			// Toast.makeText(this, "NOT ACCELEROMETER",
			// Toast.LENGTH_SHORT).show();
			return;
		}
		// Toast.makeText(this, "ACCELEROMETER", Toast.LENGTH_SHORT).show();
		long now = System.currentTimeMillis();
		float[] values = event.values;

		if ((now - mLastForce) > SHAKE_TIMEOUT) {
			mShakeCount = 0;
		}

		if ((now - mLastTime) > TIME_THRESHOLD) {
			long diff = now - mLastTime;
			float speed = Math.abs(values[SensorManager.DATA_X]
					+ values[SensorManager.DATA_Y]
					+ values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ)
					/ diff * 10000;
			if (speed > FORCE_THRESHOLD) {
				if ((++mShakeCount >= SHAKE_COUNT)
						&& (now - mLastShake > SHAKE_DURATION)) {
					mLastShake = now;
					mShakeCount = 0;

					//Toast.makeText(this, "speed:" + speed + " now:" + now,
					//		Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(OtokomaeObentoGetActivity.this, ObentoGetActivity.class);
					startActivity(intent);
				}
				mLastForce = now;
			}
			mLastTime = now;
			mLastX = values[SensorManager.DATA_X];
			mLastY = values[SensorManager.DATA_Y];
			mLastZ = values[SensorManager.DATA_Z];
		}

	}

}