package us.vinotech.Login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


/**
 * Created by PHE on 15/9/12.
 */
public class choice extends Activity {

    ImageButton jawbone,googlefit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice);

        jawbone = (ImageButton)findViewById(R.id.jawbone);
        googlefit = (ImageButton)findViewById(R.id.google_fit);

        jawbone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //// TODO: 15/10/11  Save is google fit or Jawbone!
                // TODO: 15/10/11 choice preferences  
                Toast.makeText(choice.this,"jawbone Click",Toast.LENGTH_LONG).show();
                Intent jawbone = new Intent(choice.this,Jawbone.class );
                startActivity(jawbone);
                finish();
            }
        });

        googlefit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(choice.this,"Googlefit Click",Toast.LENGTH_LONG).show();

            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(choice.this);

        Log.e("test_pre", preferences.getString("Gmail", "null"));
        Log.e("test_fb", preferences.getString("FB_mail", "null"));


    }
}
