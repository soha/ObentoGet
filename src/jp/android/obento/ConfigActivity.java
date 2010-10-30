package jp.android.obento;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

public class ConfigActivity extends Activity implements OnClickListener {

	protected static final String COMPANY_CD = "CompanyCD";
	protected static final String CUSTOMER_CD = "CustomerCD";
	protected static final String PASSWORD = "Password";

	protected EditText companyCdEdit;
	protected EditText customerCdEdit;
	protected EditText passwordEdit;
	protected Button registButton;
	protected CheckBox alarmCheckBox;
	protected TimePicker tp;

	protected SharedPreferences pref;

	protected static final String TIMER_CHECKED = "timerChecked";
	protected static final String TIME_HOUR = "hour";
	protected static final String TIME_MINUTES = "minitutes";

	/**
	 * Activity生成時
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);

		companyCdEdit = (EditText) findViewById(R.id.CompanyCdEditText);
		customerCdEdit = (EditText) findViewById(R.id.CustomerCdEditText);
		passwordEdit = (EditText) findViewById(R.id.PasswordEditText);
		registButton = (Button) findViewById(R.id.RegistButton);
		registButton.setOnClickListener(this);
		alarmCheckBox = (CheckBox) findViewById(R.id.AlarmCheckBox);

		tp = (TimePicker) findViewById(R.id.setting_time);
		tp.setIs24HourView(true);

		pref = PreferenceManager.getDefaultSharedPreferences(this
				.getApplicationContext());
		companyCdEdit.setText(pref.getString(COMPANY_CD, ""));
		customerCdEdit.setText(pref.getString(CUSTOMER_CD, ""));
		passwordEdit.setText(pref.getString(PASSWORD, ""));
		alarmCheckBox.setChecked(pref.getBoolean(TIMER_CHECKED, false));

		// 保存されていたら、時間を設定
		if (pref.contains(TIME_HOUR)) {
			tp.setCurrentHour(pref.getInt(TIME_HOUR, tp.getCurrentHour()));
			tp.setCurrentMinute(pref
					.getInt(TIME_MINUTES, tp.getCurrentMinute()));
		}

	}

	@Override
	public void onClick(View v) {

		if (v == registButton) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this.getApplicationContext());
			SharedPreferences.Editor editor = pref.edit();
			editor.putString(COMPANY_CD, companyCdEdit.getText().toString());
			editor.putString(CUSTOMER_CD, customerCdEdit.getText().toString());
			editor.putString(PASSWORD, passwordEdit.getText().toString());
			editor.commit();


			// AlarmManagerのインスタンスを取得
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			PendingIntent sender = PendingIntent.getActivity(
					ConfigActivity.this, 0, new Intent(ConfigActivity.this,
							OtokomaeObentoGetActivity.class),
					PendingIntent.FLAG_CANCEL_CURRENT);

			if(alarmCheckBox.isChecked()){
				editor.putBoolean(TIMER_CHECKED, true);
				editor.putInt(TIME_HOUR, tp.getCurrentHour());
				editor.putInt(TIME_MINUTES, tp.getCurrentMinute());
				editor.commit();

				// 現在の時刻を取得
				Time t = new Time();
				t.setToNow();
				long now_time = t.toMillis(false);

				// アラーム起動時刻を設定
				t.hour = tp.getCurrentHour();
				t.minute = tp.getCurrentMinute();
				t.second = 0;
				long next_time = t.toMillis(false);
	
				// 翌日の時間に設定
				if (now_time > next_time) {
					next_time += AlarmManager.INTERVAL_DAY;
				}
	
	
				// アラームを設定
				am.setRepeating(AlarmManager.RTC_WAKEUP, next_time,
						AlarmManager.INTERVAL_DAY, sender);
			}else{
				// アラーム解除
				if (pref.contains(TIME_HOUR)) {
					editor.putBoolean(TIMER_CHECKED, false);
					editor.remove(TIME_HOUR);
					editor.remove(TIME_MINUTES);
					editor.commit();
				}
				am.cancel(sender);
			}

			finish();
		}
	}

}
