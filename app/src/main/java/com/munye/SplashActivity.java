package com.munye;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.munye.user.R;
import com.munye.parse.AsyncTaskCompleteListener;
import com.munye.parse.HttpRequester;
import com.munye.utils.AndyUtils;
import com.munye.utils.Const;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends ActionBarBaseActivity implements View.OnClickListener, AsyncTaskCompleteListener {

    private Button btnSplashSignIn , btnSplashRegister;
    private Timer checkInternetTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        preferenceHelper.putTimeZone(getTimeZone());
        btnSplashSignIn = (Button)findViewById(R.id.btnSplashSignIn);
        btnSplashRegister = (Button)findViewById(R.id.btnSplashRegister);
        btnSplashSignIn.setOnClickListener(this);
        btnSplashRegister.setOnClickListener(this);

        if (AppStatus.getInstance(this).isOnline()) {

           // Toast.makeText(getApplicationContext(), "Online", Toast.LENGTH_SHORT).show();

        } else {

          //  Toast.makeText(getApplicationContext(), "Offline", Toast.LENGTH_SHORT).show();
        }

    }

    public static class AppStatus {

        private static AppStatus instance = new AppStatus();
         static Context context;
        ConnectivityManager connectivityManager;
        NetworkInfo wifiInfo, mobileInfo;
        boolean connected = false;

        public static AppStatus getInstance(Context ctx) {
            context = ctx.getApplicationContext();
            return instance;
        }

        public boolean isOnline() {
            try {
                connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                connected = networkInfo != null && networkInfo.isAvailable() &&
                        networkInfo.isConnected();
                return connected;


            } catch (Exception e) {
                System.out.println("CheckConnectivity Exception: " + e.getMessage());

            }
            return connected;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopInternetCheck();
        checkInternetStatus();
        if(!AndyUtils.isNetworkAvailable(this))
            startInternetCheck();

    }

    private void checkInternetStatus(){
        if(AndyUtils.isNetworkAvailable(this) && preferenceHelper.getId() == null){
            stopInternetCheck();
            getSettings();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnSplashRegister.setVisibility(View.VISIBLE);
                    btnSplashSignIn.setVisibility(View.VISIBLE);
                }
            });
            closeInternetDialog();
        }
        else if(AndyUtils.isNetworkAvailable(this) && preferenceHelper.getId() != null){
            getSettings();
        }
        else {
            openInternetDialog(this);
        }
    }


    private void startInternetCheck(){

        checkInternetTimer = new Timer();
        TimerTask taskCheckInterner = new TimerTask() {
            @Override
            public void run() {
                checkInternetStatus();
            }
        };
        checkInternetTimer.scheduleAtFixedRate(taskCheckInterner, 0 , 5000);
    }

    private void stopInternetCheck(){
        if(checkInternetTimer != null){
            checkInternetTimer.cancel();
            checkInternetTimer.purge();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopInternetCheck();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnSplashSignIn:
                startActivity(new Intent(SplashActivity.this , SignInActivity.class));
                break;

            case R.id.btnSplashRegister:
                startActivity(new Intent(SplashActivity.this , RegisterActivity.class));
                break;

            default:
                AndyUtils.generateLog("No action");
                break;
        }
    }

    private String getTimeZone()
    {
        return java.util.TimeZone.getDefault().getID();
    }



    private void getSettings(){
        HashMap<String , String> map = new HashMap<>();
        map.put(Const.URL , Const.ServiceType.GET_SETTINGS);

        new HttpRequester(this , map , Const.ServiceCode.GET_SETTINGS , Const.httpRequestType.POST , this);
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        if(serviceCode == Const.ServiceCode.GET_SETTINGS && dataParser.isSuccess(response)){
            dataParser.parseSettings(response);
            if(preferenceHelper.getId() != null){
                stopInternetCheck();
                startActivity(new Intent(this , MapActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInternetCheck();
    }
}
