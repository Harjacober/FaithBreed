package primetechnologies.faith_breed.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetwotkUtils {
    public static boolean isConnected(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobile.getState() == NetworkInfo.State.CONNECTED || wifi.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    private static String checkNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifi.getState() == NetworkInfo.State.CONNECTED) {
                Log.d("eeeeee", "wifi");
                return "WIFI";
            } else if (mobile.getState() == NetworkInfo.State.CONNECTED) {
                Log.d("eeeeee", "mobile");
                return "MOBILE";
            }
        }
        return null;
    }
}
