package com.iot.termproject.base;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    public final String TAG = getClass().getSimpleName();
    public VB binding;

    protected abstract VB setViewBinding();

    public void initViewBinding() {
        binding = setViewBinding();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initViewBinding();
        setContentView(binding.getRoot());
        initAfterBinding();
    }

    protected abstract void initAfterBinding();

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void startNextActivity(Class<?> className) {
        Intent intent = new Intent(this, className);
        startActivity(intent);
    }

    public void startNextActivityWithClear(Class<?> className) {
        Intent intent = new Intent(this, className);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void startNextActivityForResult(Class<?> className, int requestCode) {
        Intent intent = new Intent(this, className);
        startActivityForResult(intent, requestCode);
    }
}