package com.example.ria_android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.ria_web_container);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //invoke SSO call after page is finished
                view.evaluateJavascript("setupRmtSso(null, elementId, userData, tokenCallback, checkLoggedInCallback, ssoParams);", value -> {
                    Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();});
            }
        });

        /****
         * Web console messages from the SDK will be logged directly with the tag "RIA" in android logcat
         */
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("RIA", consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });


        WebSettings webViewSettings = webView.getSettings();
        //Javascript must be manually enabled on webview
        webViewSettings.setJavaScriptEnabled(true);
        //DOM storage seems like it's required for RIA SDK
        webViewSettings.setDomStorageEnabled(true);
        //JS Interface to get callbacks to android code
        webView.addJavascriptInterface(this, "PLS");

        /**
         * LOAD RIA SDK/HTML HERE
         */
        /*****
         * Option 1 - Load the HTML file directly
         * RESULT - does not load
         * There are some security restrictions on RIA side that requires the SDK to be loaded
         * from a web server or at least from the origin being http(s)
         *****/
//        webView.loadUrl("file:///android_asset/ria.html");



        /*****
         * Option 2 - Load raw HTML file with base URL into the webview
         * using localhost:8080 as the fake origin
         * RESULT - fails to load properly
         * The SDK does load, but calling setupRmtSso just shows an error message "Are you still there?"
         *****/
        try {
            String webpage = readRawText(getAssets().open("ria.html"));
            webView.loadDataWithBaseURL("http://localhost:8080", webpage, "text/html", "UTF-8", "about:blank");
        } catch (Exception e) {
            Log.e("ria load error", e.getMessage());
        }

        /******
         * Option 3 - Run RIA SDK in local webserver and then load from device or emulator
         * RESULT - fails to load properly, same as option 2
         * Running RIA SDK in a basic Angular web app and then loading the url
         * Same result as option 2, error message "Are you still there?".
         *****/
//        webView.loadUrl(("http://10.0.2.2:4200/"));

    }

    public String readRawText(InputStream input) throws IOException {
        byte[] buf = new byte[input.available()];
        input.read(buf);
        input.close();
        return new String(buf);
    }

    @JavascriptInterface
    public void logToken(String token) {
        Log.i("SSO Token", token);
    }
}
