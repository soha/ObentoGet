package jp.android.obento;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.text.format.Time;

public class TimerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {0, 1000, 500, 2000}; // OFF/ON/OFF/ON...
		vibrator.vibrate(pattern, -1);

		// AlarmManagerのインスタンスを取得
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, OtokomaeObentoGetActivity.class);
		i.putExtra(ObentoGetActivity.ON_TIMER, true);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 

		PendingIntent sender = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		// アラームを設定
		Time t = new Time();
		t.setToNow();
		long now_time = t.toMillis(false);
		long wake_time = now_time + 3500; // 0 + 1000 + 500 + 2000
		am.set(AlarmManager.RTC_WAKEUP, wake_time, sender);
	}

}
