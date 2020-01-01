package com.example.plonka.ui.faq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.plonka.R;

public class FaqFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_faq, container, false);
        WebView webView = root.findViewById(R.id.webview);
        webView.loadUrl("https://people.dsv.su.se/~joeh1022/how-does-it-work.php"); // TODO: Replace with full-blown FAQ-website, but not necessary for this assignment

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Note: links and redirects open in regular browser
        return root;
    }
}