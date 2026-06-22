package com.ikanbot.tv

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
class FocusableWebView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle
) : WebView(ctx, attrs, defStyleAttr) {
    private var focused = false
    init {
        isFocusableInTouchMode = false
        isFocusable = true
        overScrollMode = OVER_SCROLL_NEVER
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                focused = false
            }
        }
    }
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus != focused) {
            focused = gainFocus
            if (gainFocus) {
                evaluateJavascript("""(function(){
                    var el=document.activeElement;
                    if(el&&el!==document.body) el.style.outline='3px solid #00FF00';
                })()""", null)
            } else {
                evaluateJavascript("""(function(){
                    var els=document.querySelectorAll('[style*="outline"]');
                    for(var i=0;i<els.length;i++) els[i].style.outline='';
                })()""", null)
            }
        }
    }
}