package primetechnologies.faith_breed.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public class PrefUtils {

    public static final PrefUtils prefUtils = new PrefUtils();
    public static SharedPreferences myPrefs = null;

    public static PrefUtils getInstance(Context context) {
        if (null == myPrefs)
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefUtils;
    }

    public void saveSecretKey(String value, String KEY) {
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString(KEY, value);
        editor.commit();
    }

    public String getSecretKey(String KEY) {
        return myPrefs.getString(KEY, null);
    }
}