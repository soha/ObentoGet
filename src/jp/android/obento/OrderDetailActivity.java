package jp.android.obento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class OrderDetailActivity extends Activity {

	private static final String ENCODE = "UTF-8";
	private static final String MIME_TYPE = "text/html";
	private static final String DETAIL_PAGE_CONTENTS_KEY = "html";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity.  You can find it
        // in res/layout/hello_activity.xml
        setContentView(R.layout.detail);
        WebView wb = (WebView) this.findViewById(R.id.webview);
        
        Intent intent = getIntent();
        String html = intent.getStringExtra(DETAIL_PAGE_CONTENTS_KEY);
		//wb.loadData(html, "text/html", "UTF-8"); //これはAPI側のバグで正しく表示されない
		wb.loadDataWithBaseURL("baseUrlDummy", html, MIME_TYPE, ENCODE, null);
		
    }
}