package jp.android.obento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ObentoGetActivity extends Activity {
	
	/**
	 * Activity生成時
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	/**
	 * オプションメニュー作成
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, Menu.NONE, "AccountSettings");

		return ret;
	}
	
	/**
	 * オプションメニュー押下時
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast.makeText(this, "GroupI:"+item.getGroupId(), 0).show();
		Toast.makeText(this, "ItemId:"+item.getItemId(), 0).show();
		System.out.println(item.getGroupId());
		System.out.println(item.getItemId());
		
		Intent intent = new Intent(this, jp.android.obento.ConfigActivity.class);
		startActivityForResult(intent, 0);
		
		return super.onOptionsItemSelected(item);
	} 
}