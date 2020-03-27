package com.example.albertz_business.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.albertz_business.R;
import com.example.albertz_business.api.RequestCenter;
import com.example.albertz_business.model.login.user.LoginEvent;
import com.example.albertz_business.model.login.user.User;
import com.example.albertz_business.model.login.manager.UserManager;
import com.example.lib_commin_ui.base.BaseActivity;
import com.example.lib_network.okhttp.exception.OkHttpException;
import com.example.lib_network.okhttp.listener.DisposeDataListener;

import org.greenrobot.eventbus.EventBus;

public class LoginActivity extends BaseActivity {
    public static void start(Context context){
        context.startActivity(new Intent(context,LoginActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.login_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestCenter.login(new DisposeDataListener() {
                    @Override
                    public void onSuccess(Object responseObject) {
                        //处理正常逻辑
                        User user  = (User) responseObject;
                        UserManager.getInstance().setUser(user);
                        EventBus.getDefault().post(new LoginEvent());
                        Log.i("xxxxxx","成功了"+user.ecode+user.data+user.emsg);
                        finish();
                    }

                    @Override
                    public void onFailure(Object responseObject) {
                        OkHttpException okHttpException = (OkHttpException) responseObject;
                        //失败逻辑
                        Log.i("xxxxxx",okHttpException.getEmsg()+""+okHttpException.getEcode());
                    }
                });
            }
        });
    }
}