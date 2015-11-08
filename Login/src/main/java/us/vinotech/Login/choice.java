package us.vinotech.Login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


/**
 * Created by PHE on 15/9/12.
 */
public class choice extends Activity {

    ImageButton jawbone,googlefit;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice);

        jawbone = (ImageButton)findViewById(R.id.jawbone);
        googlefit = (ImageButton)findViewById(R.id.google_fit);

        preferences = PreferenceManager.getDefaultSharedPreferences(choice.this);
        editor=preferences.edit();

        jawbone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putString("device", "jawbone");
                editor.apply();

                Toast.makeText(choice.this,"jawbone Click",Toast.LENGTH_LONG).show();
                Intent UnityBridge = new Intent(choice.this,UnityBridge.class );
                startActivity(UnityBridge);
                finish();
            }
        });

        googlefit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("device", "googlefit");
                editor.apply();

                Toast.makeText(choice.this,"Googlefit Click",Toast.LENGTH_LONG).show();
                Intent UnityBridge = new Intent(choice.this,UnityBridge.class );
                startActivity(UnityBridge);
                finish();

            }
        });


    }
}
