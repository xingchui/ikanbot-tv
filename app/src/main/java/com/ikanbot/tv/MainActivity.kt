package com.ikanbot.tv

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var errorView: TextView

    private val VIDEO_PATTERNS = listOf(
        "/play/", "/vod/", "/watch/", "/detail/",
        "player", "video", "m3u8", ".mp4",
        "/ep/", "/episode/", "/film/", "/series/"
    )

    private val VERIFY_KEYWORDS = listOf(
        "验证", "captcha", "geetest", "security",
        "正在验证", "自动程序", "安全服务"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupImmersiveMode()
        initViews()
        setupWebView()
        loadHomePage()
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        errorView = findViewById(R.id.errorView)
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.loadsImagesAutomatically = true
        settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 12; TV) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        settings.mediaPlaybackRequiresUserGesture = false
        webView.isFocusable = true
        webView.isFocusableInTouchMode = false
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
            enableThirdPartyCookies(webView)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectAntiDetection()
                hideLoading()
            }
            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showError("页面加载失败: ${error?.description}")
                }
            }
        }
    }

    private fun injectAntiDetection() {
        webView.evaluateJavascript("""
            (function() {
                Object.defineProperty(navigator, 'webdriver', {get: ()=>undefined});
                if (!window.chrome) window.chrome = {runtime: {}};
                var oq = navigator.permissions.query;
                navigator.permissions.query = function(p) {
                    if (p.name==='notifications') return Promise.resolve({state: Notification.permission});
                    return oq.call(navigator.permissions, p);
                };
                Object.defineProperty(navigator, 'plugins', {get: ()=>[1,2,3,4,5]});
                Object.defineProperty(navigator, 'languages', {get: ()=>['zh-CN','zh','en-US','en']});
                Object.defineProperty(navigator, 'platform', {get: ()=>'Android TV'});
                Object.defineProperty(navigator, 'hardwareConcurrency', {get: ()=>4});
                if (!navigator.deviceMemory) Object.defineProperty(navigator, 'deviceMemory', {get: ()=>4});
            })();
        """.trimIndent(), null)
    }

    private fun loadHomePage() { showLoading(); webView.loadUrl("https://www.ikanbot.com/") }

    private fun isVideoUrl(url: String) = VIDEO_PATTERNS.any { url.contains(it, true) }

    private fun isVerifyPage(url: String?, title: String?) =
        listOfNotNull(url, title).joinToString(" ").lowercase().let { txt ->
            VERIFY_KEYWORDS.any { txt.contains(it.lowercase()) }
        }

    private fun openInBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            Toast.makeText(this, "将在浏览器中打开", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "未找到可用的浏览器", Toast.LENGTH_LONG).show()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event?.let {
            when (it.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    if (it.action == KeyEvent.ACTION_UP) {
                        webView.requestFocus()
                        return webView.dispatchKeyEvent(it)
                    }
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (it.action == KeyEvent.ACTION_UP) {
                        if (webView.canGoBack()) { webView.goBack(); return true }
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() { super.onResume(); webView.visibility = View.VISIBLE }
    override fun onPause() { super.onPause(); webView.visibility = View.INVISIBLE }

    private fun showLoading() { loadingOverlay.visibility = View.VISIBLE; errorView.visibility = View.GONE }
    private fun hideLoading() { loadingOverlay.visibility = View.GONE; webView.visibility = View.VISIBLE }
    private fun showError(msg: String) {
        loadingOverlay.visibility = View.GONE; errorView.visibility = View.VISIBLE
        errorView.text = msg
    }
}