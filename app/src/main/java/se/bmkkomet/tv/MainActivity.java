package se.bmkkomet.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TV_URL = "https://tv.bmkkomet.se/display/main";
    private static final int RETRY_DELAY_MS = 5000;          // Retry every 5 seconds
    private static final long RELOAD_INTERVAL_MS = 6 * 60 * 60 * 1000L; // Reload every 6 hours

    private WebView webView;
    private LinearLayout errorView;
    private TextView errorText;
    private Handler handler;
    private boolean isShowingError = false;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Full-screen immersive mode
        enterImmersiveMode();

        // Set up handler for retries and periodic reload
        handler = new Handler(Looper.getMainLooper());

        // Create layout programmatically (no XML dependency)
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        // WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        setupWebView();
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Error/Reconnecting overlay
        errorView = new LinearLayout(this);
        errorView.setOrientation(LinearLayout.VERTICAL);
        errorView.setGravity(android.view.Gravity.CENTER);
        errorView.setBackgroundColor(Color.BLACK);
        errorView.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("🏸 Komet TV");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setGravity(android.view.Gravity.CENTER);

        errorText = new TextView(this);
        errorText.setText("Reconnecting...");
        errorText.setTextColor(Color.GRAY);
        errorText.setTextSize(18);
        errorText.setGravity(android.view.Gravity.CENTER);
        errorText.setPadding(0, 24, 0, 0);

        errorView.addView(title);
        errorView.addView(errorText);
        root.addView(errorView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);

        // Load the TV page
        loadPage();

        // Schedule periodic reload (every 6 hours) as safety mechanism
        schedulePeriodicReload();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Enable hardware acceleration for video
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Page loaded successfully - hide error
                hideError();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // Only handle main frame errors
                if (request.isForMainFrame()) {
                    showError("Connection lost. Reconnecting...");
                    scheduleRetry();
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (failingUrl != null && failingUrl.equals(TV_URL)) {
                    showError("Connection lost. Reconnecting...");
                    scheduleRetry();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
    }

    private void loadPage() {
        if (isNetworkAvailable()) {
            webView.loadUrl(TV_URL);
        } else {
            showError("No network connection. Reconnecting...");
            scheduleRetry();
        }
    }

    private void showError(String message) {
        isShowingError = true;
        errorText.setText(message);
        errorView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);
    }

    private void hideError() {
        isShowingError = false;
        errorView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    private void scheduleRetry() {
        handler.postDelayed(() -> {
            if (isShowingError) {
                loadPage();
            }
        }, RETRY_DELAY_MS);
    }

    private void schedulePeriodicReload() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Reload the page as a safety mechanism
                webView.loadUrl(TV_URL);
                handler.postDelayed(this, RELOAD_INTERVAL_MS);
            }
        }, RELOAD_INTERVAL_MS);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    // --- Immersive Mode ---

    private void enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    // --- Back Button Protection ---

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent accidental back-button exit
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Don't go back or exit — this is a kiosk-style app
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // --- Lifecycle ---

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        webView.destroy();
    }
}
