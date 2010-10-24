package jp.android.obento;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
	private static final int HTTP_OK = 200;
	private static final String OK_CODE = "OK";
	private static final String DETAIL_PAGE_CONTENTS_KEY = "html";
	

	Button orderButton;
	Button detailButton;
	
	/**
	 * Activity生成時
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		orderButton = (Button) findViewById(R.id.OrderButton);
		orderButton.setOnClickListener(this);
		detailButton = (Button) findViewById(R.id.DetailButton);
		detailButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if(v == orderButton) {
			OrderRequestTask task = new OrderRequestTask(this);
			task.execute();
		}else if(v == detailButton){
			CheckOrderRequestTask task = new CheckOrderRequestTask(this);
			task.execute();
		}
	}

	public class OrderRequestTask extends AsyncTask<String, Integer, String> {

		Activity activity;
		ProgressDialog progressDialog;

		public OrderRequestTask(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			// プログレスバー設定
			progressDialog = new ProgressDialog(activity);
			progressDialog.setTitle("注文中");
			progressDialog.setIndeterminate(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(100); // 進捗最大値を設定
			progressDialog.incrementProgressBy(0);
			
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... taskParams) {

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));

			HttpParams params = new BasicHttpParams();
			params.setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);

			SingleClientConnManager mgr = new SingleClientConnManager(params,
					schemeRegistry);

			HttpClient httpclient = new DefaultHttpClient(mgr, params);

			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this.activity
							.getApplicationContext());

			String responseStr = "";

			try {
				HttpGet httpget = new HttpGet(LOGIN_URL);
				// ログイン画面表示
				progressDialog.incrementProgressBy(10);
				HttpResponse res = httpclient.execute(httpget);
				progressDialog.incrementProgressBy(20);
				int statusCode = res.getStatusLine().getStatusCode();
				if (statusCode != HTTP_OK) {
					return "アクセスエラー発生";
				}

				HttpPost httppost = new HttpPost(LOGIN_POST_URL);

				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair(COMPANY_CD, pref
						.getString(COMPANY_CD, "")));
				nameValuePairs.add(new BasicNameValuePair(CUSTOMER_CD, pref
						.getString(CUSTOMER_CD, "")));
				nameValuePairs.add(new BasicNameValuePair(PASSWORD, pref
						.getString(PASSWORD, "")));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				progressDialog.incrementProgressBy(30);
				HttpResponse response = httpclient.execute(httppost);
				progressDialog.incrementProgressBy(40);
				statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HTTP_OK) {
					return "レスポンスエラー発生";
				}

				// 注 レスポンスが返って来ただけで認証成功とは限らない
				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent(), ENCODE));
				String line = null;
				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				String html = sb.toString();
				if (!html.contains(LUNCH_DAILY_LINK)) {
					return "認証失敗";
				}else{
					// 注文画面へのリンクが返って来たなら認証成功とみなし、処理を続ける
					HttpGet lunchDailyGet = new HttpGet(SITE_TOP_SSL_URL
							+ LUNCH_DAILY_LINK);
					progressDialog.incrementProgressBy(50);
					HttpResponse lunchDailyRes = httpclient.execute(lunchDailyGet); // 注文画面
					progressDialog.incrementProgressBy(60);
					
					InputStream is = lunchDailyRes.getEntity().getContent();

					br = new BufferedReader(new InputStreamReader(is, ENCODE));
					sb = new StringBuffer();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					is.close();
					String orderPage = sb.toString();

					Pattern ptn = Pattern.compile(
							"<a.*?href=\"(.*?)\".*?>(.*?)</a>", Pattern.DOTALL);

					Matcher matcher = ptn.matcher(orderPage);

					String href = "";
					boolean canOrder = false;
					while (matcher.find()) {
						// 注文確定画面へのリンクの抽出（最後のAタグのhrefを使う）
						href = matcher.group(1).replaceAll("¥¥s", "");
						// String text = matcher.group(2).replaceAll("¥¥s",
						// "");
						if (href.contains(ORDER_PART)) {
							canOrder = true;
							break;
						}
					}

					if(!canOrder){
						return "注文できません";
					}
					HttpGet orderGet = new HttpGet(ORDER_TOP_SSL_URL + "/"
							+ href);
					progressDialog.incrementProgressBy(70);
					HttpResponse orderGetRes = httpclient.execute(orderGet); // 注文確定画面へ
					progressDialog.incrementProgressBy(80);
					
					// 注文確定POST処理ここから
					is = orderGetRes.getEntity().getContent();

					br = new BufferedReader(new InputStreamReader(is, ENCODE));
					sb = new StringBuffer();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					String orderDecidePage = sb.toString();
					ptn = Pattern.compile("<form.*?action=\"(.*?)\".*?>(.*?)>",
							Pattern.DOTALL);
					matcher = ptn.matcher(orderDecidePage);
					String posturl = "";
					while (matcher.find()) {
						// 注文確定リンクの抽出（最後のAタグのhrefを使う）
						posturl = matcher.group(1).replaceAll("¥¥s", "");
						// String text = matcher.group(2).replaceAll("¥¥s",
						// "");
					}
					if (!"".equals(posturl)) {
						// <input type="hidden" name="LunchCompanyBranchID"
						// value="1">
						// <input type="hidden" name="OrderMenuID" value="1">
						// <input type="hidden" name="Price" value="460">
						// <input type="hidden" name="DeadLine" value="9:50:00">
						// <input type="checkbox" name="Confirm">
						HttpPost orderDecidePost = new HttpPost(
								ORDER_TOP_SSL_URL + "/" + posturl);

						nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair(
								"LunchCompanyBranchID", "1"));
						nameValuePairs.add(new BasicNameValuePair(
								"OrderMenuID", "1"));
						nameValuePairs.add(new BasicNameValuePair("Price",
								"460"));
						nameValuePairs.add(new BasicNameValuePair("DeadLine",
								"9:50:00"));
						nameValuePairs
								.add(new BasicNameValuePair("Confirm", ""));
						nameValuePairs.add(new BasicNameValuePair("Quantity",
								"1"));
						nameValuePairs.add(new BasicNameValuePair("DeliveryID",
								"1060")); // TODO 画面から取得した方がよい
						orderDecidePost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));

						// 注文確定
						progressDialog.incrementProgressBy(90);
						HttpResponse orderResponse = httpclient
								.execute(orderDecidePost);
						progressDialog.incrementProgressBy(95);
						
						sb = new StringBuffer();
						br = new BufferedReader(new InputStreamReader(
								orderResponse.getEntity().getContent(), ENCODE));
						while ((line = br.readLine()) != null) {
							sb.append(line);
						}
						br.close();
						responseStr = sb.toString();

					} else {
						// POST先URLが取得できないのは想定外ページに遷移したということ
						return "注文失敗";
					}

					// ログアウト処理
					HttpGet logout = new HttpGet(LOGOUT_URL);
					progressDialog.incrementProgressBy(95);
					httpclient.execute(logout); // ログアウト
					progressDialog.incrementProgressBy(100);

					//return responseStr;
					return "注文完了";
				}

			} catch (Exception e) {
				return "エラー発生";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss(); // プログレスバー消す
			Toast.makeText(activity, result, Toast.LENGTH_LONG).show();

		}

	}
	
	/**
	 * 注文はせず注文情報画面を表示するのみ
	 * @author you
	 *
	 */
	public class CheckOrderRequestTask extends AsyncTask<String, Integer, String> {

		Activity activity;
		ProgressDialog progressDialog;
		String orderPageHtml;

		public CheckOrderRequestTask(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			// プログレスバー設定
			progressDialog = new ProgressDialog(activity);
			progressDialog.setTitle("確認中");
			progressDialog.setIndeterminate(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// progressDialog.setMax(100); // 進捗最大値を設定

			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... taskParams) {

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));

			HttpParams params = new BasicHttpParams();
			params.setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);

			SingleClientConnManager mgr = new SingleClientConnManager(params,
					schemeRegistry);

			HttpClient httpclient = new DefaultHttpClient(mgr, params);

			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this.activity
							.getApplicationContext());

			//String responseStr = "";

			try {
				HttpGet httpget = new HttpGet(LOGIN_URL);
				// ログイン画面表示
				HttpResponse res = httpclient.execute(httpget);
				int statusCode = res.getStatusLine().getStatusCode();
				if (statusCode != HTTP_OK) {
					return "アクセスエラー発生";
				}

				HttpPost httppost = new HttpPost(LOGIN_POST_URL);

				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair(COMPANY_CD, pref
						.getString(COMPANY_CD, "")));
				nameValuePairs.add(new BasicNameValuePair(CUSTOMER_CD, pref
						.getString(CUSTOMER_CD, "")));
				nameValuePairs.add(new BasicNameValuePair(PASSWORD, pref
						.getString(PASSWORD, "")));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HTTP_OK) {
					return "レスポンスエラー発生";
				}

				// 注 レスポンスが返って来ただけで認証成功とは限らない
				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent(), ENCODE));
				String line = null;
				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				String html = sb.toString();
				if (!html.contains(LUNCH_DAILY_LINK)) {
					return "認証失敗";
				}else{
					// 注文画面へのリンクが返って来たなら認証成功とみなし、処理を続ける
					HttpGet lunchDailyGet = new HttpGet(SITE_TOP_SSL_URL
							+ LUNCH_DAILY_LINK);
					HttpResponse lunchDailyRes = httpclient
							.execute(lunchDailyGet); // 注文画面
					InputStream is = lunchDailyRes.getEntity().getContent();

					br = new BufferedReader(new InputStreamReader(is, ENCODE));
					sb = new StringBuffer();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					is.close();
					orderPageHtml = sb.toString();

					// 先にログアウト処理しておく
					HttpGet logout = new HttpGet(LOGOUT_URL);
					httpclient.execute(logout); // ログアウト


					return OK_CODE;
				}


			} catch (Exception e) {
				return "エラー発生";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss(); // プログレスバー消す
			Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
			
			if(OK_CODE.equals(result)){
				//注文画面を表示
				Intent intent = new Intent(ObentoGetActivity.this, OrderDetailActivity.class);
				intent.putExtra(DETAIL_PAGE_CONTENTS_KEY, orderPageHtml);
				startActivity(intent);
			}
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