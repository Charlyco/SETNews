package com.limitless.setnews;

import androidx.appcompat.app.AppCompatActivity;

import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PortalActivity extends AppCompatActivity {
    private WebView portal_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal);

        portal_page = findViewById(R.id.portal_view);
        portal_page.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        portal_page.loadUrl("https://portal.fpno.edu.ng/nekede/");
        WebSettings webSettings = portal_page.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(portal_page.canGoBack()){
            portal_page.goBack();
        }else {
            super.onBackPressed();
        }
    }
}
