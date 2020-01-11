package com.example.plonka.ui.faq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.plonka.R;

/**
 * The FaqFragment is used to display a website with frequently asked questions within the app
 */
public class FaqFragment extends Fragment {

    /**
     * Set up the UI with the correct website etc when the fragment lifecycle begins
     * @param inflater necessary for inflating view
     * @param container necessary for inflating view
     * @param savedInstanceState provided instance state, not used here
     * @return View the view that was created by the fragment
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_faq, container, false);
        WebView webView = root.findViewById(R.id.webview);
        webView.loadUrl("https://people.dsv.su.se/~joeh1022/how-does-it-work.php");

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Note: links and redirects open in regular browser
        return root;
    }
}