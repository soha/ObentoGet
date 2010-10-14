package jp.android.obento;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ObentoGetActivity extends Activity implements OnClickListener {

	protected static final String SITE_TOP_SSL_URL = "https://www.obentonet.com";
	protected static final String ORDER_TOP_SSL_URL = "https://www.obentonet.com/order_lunch";
	protected static final String ORDER_PART = "lunch_order.asp";
	protected static final String LOGIN_URL = "https://www.obentonet.com/login/";
	protected static final String LOGIN_POST_URL = "https://www.obentonet.com/login/LoginExec.asp";
	protected static final String LUNCH_DAILY_LINK = "/order_lunch/lunch_daily.asp";
	protected static final String LOGOUT_URL = "https://www.obentonet.com/logout.asp";
	

	private static final String COMPANY_CD = "CompanyCD";
	private static final String CUSTOMER_CD = "CustomerCD";
	private static final String PASSWORD = "Password";
	
	private static final String ENCODE = "SJIS";

	/**
	 * Activity生成時
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button orderButton = (Button) findViewById(R.id.OrderButton);
		orderButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 
		            SSLSocketFactory.getSocketFactory(), 443));

		HttpParams params = new BasicHttpParams();
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		HttpClient httpclient = new DefaultHttpClient(mgr, params);
		//httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		try {
			HttpGet httpget = new HttpGet(LOGIN_URL);
			HttpResponse res = httpclient.execute(httpget); // ログイン画面表示
//			Header[] headers = res.getAllHeaders();
//			StringBuffer sbHeader = new StringBuffer();
//			for(int i=0; i<headers.length; i++){
//				sbHeader.append(headers[i].getName());
//				sbHeader.append(headers[i].getValue());
//				sbHeader.append("\r\n");
//			}
//			String head = sbHeader.toString();
			
			HttpPost httppost = new HttpPost(LOGIN_POST_URL);
			
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair(COMPANY_CD, pref
					.getString(COMPANY_CD, "")));
			nameValuePairs.add(new BasicNameValuePair(CUSTOMER_CD, pref
					.getString(CUSTOMER_CD, "")));
			nameValuePairs.add(new BasicNameValuePair(PASSWORD, pref.getString(
					PASSWORD, "")));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200){
				//認証成功
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), ENCODE));
		        String line = null;
		        StringBuffer sb = new StringBuffer();
		        while ((line = br.readLine()) != null) {
		        	sb.append(line);
		        }
		        br.close();
	        	String html = sb.toString();
	        	if(html.contains(LUNCH_DAILY_LINK)){
	    			HttpGet lunchDailyGet = new HttpGet(SITE_TOP_SSL_URL + LUNCH_DAILY_LINK);
	    			HttpResponse lunchDailyRes = httpclient.execute(lunchDailyGet); // 注文画面
	    			InputStream is = lunchDailyRes.getEntity().getContent();
	    			
					br = new BufferedReader(new InputStreamReader(is, ENCODE));
			        sb = new StringBuffer();
			        while ((line = br.readLine()) != null) {
			        	sb.append(line);
			        }
			        br.close();
			        is.close();
			        String orderPage = sb.toString();

	    			Pattern ptn = Pattern.compile("<a.*?href=\"(.*?)\".*?>(.*?)</a>", Pattern.DOTALL);
	    			
	    			Matcher matcher = ptn.matcher(orderPage);

	    			String href = "";
	    			while (matcher.find()) {
	    				//注文リンクの抽出（最後のAタグのhrefを使う）
	    			    href = matcher.group(1).replaceAll("¥¥s", "");
	    			    //String text = matcher.group(2).replaceAll("¥¥s", "");
	    			    if(href.contains(ORDER_PART)){
	    			    	break;
	    			    }
	    			}

	    			HttpGet orderGet = new HttpGet(ORDER_TOP_SSL_URL + "/" + href);
	    			HttpResponse orderGetRes = httpclient.execute(orderGet); // 注文確定画面へ
	    			
	    			//注文確定POST
	    			is = orderGetRes.getEntity().getContent();
	    			
					br = new BufferedReader(new InputStreamReader(is, ENCODE));
			        sb = new StringBuffer();
			        while ((line = br.readLine()) != null) {
			        	sb.append(line);
			        }
			        br.close();
			        String orderDecidePage = sb.toString();
	    			ptn = Pattern.compile("<form.*?action=\"(.*?)\".*?>(.*?)>", Pattern.DOTALL);
	    			matcher = ptn.matcher(orderDecidePage);
	    			String posturl = "";
	    			while (matcher.find()) {
	    				//注文リンクの抽出（最後のAタグのhrefを使う）
	    			    posturl = matcher.group(1).replaceAll("¥¥s", "");
	    			    //String text = matcher.group(2).replaceAll("¥¥s", "");
	    			}
//	    	        <input type="hidden" name="LunchCompanyBranchID" value="1">
//	    	        <input type="hidden" name="OrderMenuID" value="1">
//	    	        <input type="hidden" name="Price" value="460">
//	    	        <input type="hidden" name="DeadLine" value="9:50:00">
//	    			<input type="checkbox" name="Confirm">
	    			HttpPost orderDecidePost = new HttpPost(ORDER_TOP_SSL_URL + "/" +  posturl);
	    			
	    			nameValuePairs = new ArrayList<NameValuePair>(2);
	    			nameValuePairs.add(new BasicNameValuePair("LunchCompanyBranchID", "1"));
	    			nameValuePairs.add(new BasicNameValuePair("OrderMenuID", "1"));
	    			nameValuePairs.add(new BasicNameValuePair("Price", "460"));
	    			nameValuePairs.add(new BasicNameValuePair("DeadLine", "9:50:00"));
	    			nameValuePairs.add(new BasicNameValuePair("Confirm", ""));
	    			nameValuePairs.add(new BasicNameValuePair("Quantity", "1"));
	    			nameValuePairs.add(new BasicNameValuePair("DeliveryID", "1060")); //TODO 画面から取得した方がよい
	    			orderDecidePost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    			// Execute HTTP Post Request
	    			HttpResponse orderResponse = httpclient.execute(orderDecidePost);
			        
			        
	    			
	    			HttpGet logout = new HttpGet(LOGOUT_URL);
	    			HttpResponse logoutRes = httpclient.execute(logout); // ログアウト

			        sb = new StringBuffer();
			        br = new BufferedReader(new InputStreamReader(logoutRes.getEntity().getContent(), ENCODE));
			        while ((line = br.readLine()) != null) {
			        	sb.append(line);
			        }
			        br.close();

	        	}
		        Toast.makeText(this, sb.toString(), 0).show();
	        	
			}else{
				Toast.makeText(this, "認証に失敗しました。", 0).show();
			}
			

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
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

		Intent intent = new Intent(this, jp.android.obento.ConfigActivity.class);
		startActivityForResult(intent, 0);

		return super.onOptionsItemSelected(item);
	}

}