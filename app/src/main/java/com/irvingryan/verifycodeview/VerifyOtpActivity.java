package com.irvingryan.verifycodeview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.irvingryan.VerifyCodeView;

public class VerifyOtpActivity extends AppCompatActivity{

    private VerifyCodeView verifyCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        intiView();

    }

    private void intiView() {
        verifyCodeView = (VerifyCodeView) findViewById(R.id.etOTP);
        verifyCodeView.setListener(new VerifyCodeView.OnTextChangListener() {
            @Override
            public void afterTextChanged(String text) {

//                Toast.makeText(VerifyOtpActivity.this,text,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void VerifyOtp(View view){

        String s = verifyCodeView.getText();
        if (s.equals("1234")){
            Toast.makeText(VerifyOtpActivity.this,"OTP is valid",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(VerifyOtpActivity.this,"OTP is invalid",Toast.LENGTH_SHORT).show();
        }
    }
}
