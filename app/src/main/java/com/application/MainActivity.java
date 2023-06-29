package com.application;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

public class MainActivity extends Activity {
    private ValueCallback mFilePathCallback;
    private final static int FILECHOOSER_NORMAL_REQ_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebContentsDebuggingEnabled(true); // 웹뷰 디버깅 허용 여부
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        WebSettings webSettings = webView.getSettings();

        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setJavaScriptEnabled(true); // 웹페이지 자바스크립트 허용 여부
        webSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부(localStorage)
        webSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        webSettings.setDatabaseEnabled(true); // 데이터베이스 접근 허용 여부
        webSettings.setDisplayZoomControls(true); // 돋보기 없애기
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSettings.setLoadWithOverviewMode(true); // 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
        webSettings.setSupportZoom(false); // 화면 줌 허용 여부
        webSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        webSettings.setAllowContentAccess(true); // 컨텐츠 접근 허용
        webSettings.setAllowFileAccess(true); // 파일 접근 허용 여부
        webSettings.setAllowFileAccessFromFileURLs(true); // 파일 URL로부터 파일 접근 허용
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부

        //webView.loadUrl("https://www.hongsedu.co.kr/");
        webView.loadUrl("http://www.aflk-chat.com/chat/login");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "***** onActivityResult() - requestCode : "+requestCode);
        Log.d(TAG, "***** onActivityResult() - resultCode : "+resultCode);
        Log.d(TAG, "***** onActivityResult() - data : "+data);
        /* 파일 선택 완료 후 처리 */
        switch(requestCode) {
            case FILECHOOSER_NORMAL_REQ_CODE:
                //fileChooser 로 파일 선택 후 onActivityResult 에서 결과를 받아 처리함
                if(resultCode == RESULT_OK) {
                    //파일 선택 완료 했을 경우
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    }else{
                        mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
                    }
                    mFilePathCallback = null;
                } else {
                    //cancel 했을 경우
                    if(mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                        mFilePathCallback = null;
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            // Verbose 로그 출력
            Log.v("Tag", "Verbose 로그 메시지");
            Log.d("WebView", "URL: " + url);
            if (url.startsWith("https://www.youtube.com/")||url.startsWith("https://m.youtube.com/")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    // 패키지 이름으로 유튜브 앱을 명시적으로 지정
                    intent.setPackage("com.google.android.youtube");
                } catch (ActivityNotFoundException e) {
                    Log.d("WebView", "설치되어 있지 않음");
                    // YouTube 앱이 설치되어 있지 않은 경우 대체 동작을 수행하거나 오류 메시지를 표시.
                }
                startActivity(intent);
                return true;
            }else{
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //startActivity(intent);
                //return true;
                return super.shouldOverrideUrlLoading(view, request);
            }
        }
    }


    /* WebChromeClient 를 상속받는 MyWebChromeClient 클래스를 만들어준다 */
    public class MyWebChromeClient extends WebChromeClient {
        /* Android 5.0 이상 카메라 - input type="file" 태그를 선택했을 때 반응 처리 */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Log.d(TAG, "***** onShowFileChooser()");
            //Callback 초기화
            //return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);

            /* 파일 업로드 */
            if (mFilePathCallback != null) {
                //파일을 한번 오픈했으면 mFilePathCallback 를 초기화를 해줘야함
                // -- 그렇지 않으면 다시 파일 오픈 시 열리지 않는 경우 발생
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }
            mFilePathCallback = filePathCallback;
            //권한 체크
            //if(권한 여부) {

            //권한이 있으면 처리
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");  //모든 contentType 파일 표시
            //intent.setType("image/*");  //contentType 이 image 인 파일만 표시
            startActivityForResult(intent, 0);

            //} else {
            //권한이 없으면 처리
            //}
            return true;
        }
    }
}