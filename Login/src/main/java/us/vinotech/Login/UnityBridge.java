package us.vinotech.Login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by PHE on 2015/11/8.
 */
public class UnityBridge extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(UnityBridge.this);
        if (preferences.getString("device", "null").equals("jawbone")) {
            Intent jawbone = new Intent(UnityBridge.this, Jawbone.class);
            startActivity(jawbone);
            finish();
        } else if (preferences.getString("device", "null").equals("googlefit")) {
            // TODO: 2015/11/8 go google fit get data

            finish();

        } else {
            Intent choice = new Intent(UnityBridge.this, choice.class);
            startActivity(choice);
            finish();
        }

    }
}
