package jp.android.obento;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ConfigActivity extends Activity implements OnClickListener {

	private static final String COMPANY_CD = "CompanyCD";
	private static final String CUSTOMER_CD = "CustomerCD";
	private static final String PASSWORD = "Password";
	
	protected EditText companyCdEdit;
	protected EditText customerCdEdit;
	protected EditText passwordEdit;
	Button registButton;
	
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
		
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		companyCdEdit.setText(pref.getString(COMPANY_CD, ""));
		customerCdEdit.setText(pref.getString(CUSTOMER_CD, ""));
		passwordEdit.setText(pref.getString(PASSWORD, ""));
		
	}

	@Override
	public void onClick(View v) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(COMPANY_CD, companyCdEdit.getText().toString());
		editor.putString(CUSTOMER_CD, customerCdEdit.getText().toString());
		editor.putString(PASSWORD, passwordEdit.getText().toString());
		editor.commit();
		
		finish();
	}
	
	
}
