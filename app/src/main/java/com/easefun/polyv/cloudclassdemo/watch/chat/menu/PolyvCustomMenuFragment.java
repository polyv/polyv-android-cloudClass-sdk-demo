package com.easefun.polyv.cloudclassdemo.watch.chat.menu;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.liveInfo.PolyvSafeWebView;
import com.easefun.polyv.cloudclassdemo.watch.chat.liveInfo.PolyvWebViewHelper;


public class PolyvCustomMenuFragment extends Fragment {
    private boolean isInitialized;
    private View view;
    private LinearLayout ll_parent;
    private PolyvSafeWebView wv_desc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_custommenu, null) : view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isInitialized)
            return;
        isInitialized = true;
        ll_parent = (LinearLayout) view.findViewById(R.id.ll_parent);
        wv_desc = new PolyvSafeWebView(getContext());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -1);
        wv_desc.setLayoutParams(llp);
        ll_parent.addView(wv_desc);
        PolyvWebViewHelper.initWebView(getContext(), wv_desc);
        boolean isIFrameMenu = getArguments().getBoolean("isIFrameMenu");
        if (!isIFrameMenu) {
            String content = getArguments().getString("text");
            if (!TextUtils.isEmpty(content)) {
                String style = "style=\" width:100%;\"";
                content = content.replaceAll("img src=\"//", "img src=\\\"https://");
                content = content.replace("<img ", "<img " + style + " ");
                content = content.replaceAll("<p>", "<p style=\"word-break:break-all\">");
                content = content.replaceAll("<table>", "<table border='1' rules=all>");
                content = content.replaceAll("<td>", "<td width=\"36\">");
//                wv_desc.loadData(content, "text/html; charset=UTF-8", null);
                wv_desc.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
            }
        } else {
            String url = getArguments().getString("url");
            wv_desc.loadUrl(url);
        }
    }

    public boolean goBack() {
        if (wv_desc == null || !wv_desc.canGoBack())
            return false;
        wv_desc.goBack();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wv_desc != null) {
            wv_desc.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wv_desc != null) {
            wv_desc.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ll_parent != null) {
            ll_parent.removeView(wv_desc);
        }
        if (wv_desc != null) {
            wv_desc.stopLoading();
            wv_desc.clearMatches();
            wv_desc.clearHistory();
            wv_desc.clearSslPreferences();
            wv_desc.clearCache(true);
            wv_desc.loadUrl("about:blank");
            wv_desc.removeAllViews();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                wv_desc.removeJavascriptInterface("AndroidNative");
            }
            wv_desc.destroy();
        }
        wv_desc = null;
    }
}
