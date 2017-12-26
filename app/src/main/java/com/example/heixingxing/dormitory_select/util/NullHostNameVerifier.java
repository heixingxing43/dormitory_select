package com.example.heixingxing.dormitory_select.util;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by heixingxing on 2017/12/24.
 */

public class NullHostNameVerifier implements HostnameVerifier{
    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i("network","Approving certificate for " + hostname);
        return true;
    }
}
