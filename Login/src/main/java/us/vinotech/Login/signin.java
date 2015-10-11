package us.vinotech.Login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class signin extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "Signin";

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;
    private static final int REQUEST_OAUTH = 1;

    CallbackManager callbackManager;
    LoginButton loginButton;
    String signin_URL = "https://vinotech.us/v1/post/enroll/users";

    SSLContext SSL_context;
    byte[] outputBytes;
    private Resources resources;

    String email=null,FB_id=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For https
        resources = this.getResources();
        try {
            Certification();
        } catch (IOException e) {
            Log.e("Certification", e.toString());
        }


        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_signin);

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile", "user_friends"));
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code

                        Log.d("Success", loginResult.getAccessToken().toString());

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Log.v("LoginActivity", response.toString());
                                        try {
                                            email=  object.get("email").toString();
                                            FB_id = object.get("id").toString();
                                            Log.e("test",email);

                                        }catch (org.json.JSONException E){
                                            Log.e("FB_OnSuccess",E.toString());
                                        }
                                        Log.e("object",object.toString());
                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(signin.this);
                                        SharedPreferences.Editor editor=preferences.edit();
                                        editor.putString("email", email);
                                        editor.putString("fb_id", FB_id);
                                        editor.apply();

                                        // TODO: 15/10/11  if 原本有devices資料 就直接跳Jawbonedata else: 選devices
                                        Signin_Post_Data();
                                        
                                        // TODO: 15/9/12 change to postexecute save email and go next step
                                        //if(preferences.getString("email",null)==null) {

//                                        Intent chioce=new Intent(signin.this,choice.class);
//                                        startActivity(chioce);
                                        //}
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });



        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }


        // Large sign-in
        ((SignInButton) findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);


        // Set up button click listeners
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                Toast.makeText(signin.this,R.string.signing_in,Toast.LENGTH_LONG).show();
                // [START sign_in_clicked]
                mShouldResolve = true;
                mGoogleApiClient.connect();
                // [END sign_in_clicked]

            }
        });

        // [START create_google_api_client]
        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
//                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(new Scope(Scopes.PLUS_ME))
                .addScope(new Scope(Scopes.PLUS_LOGIN))

                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .build();
        // [END create_google_api_client]


        // Generate this app hashkey!
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "us.vinotech.vino",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Packamanager",e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("Packamanager",e.toString());

        }
        //end hashkey



    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        float fbIconScale = 1.45F;
        Drawable drawable = this.getResources().getDrawable(
                R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * fbIconScale),
                (int) (drawable.getIntrinsicHeight() * fbIconScale));
        loginButton.setCompoundDrawables(drawable, null, null, null);
        loginButton.setCompoundDrawablePadding(this.getResources().
                getDimensionPixelSize(R.dimen.fb_margin_override_textpadding));
        loginButton.setPadding(
                this.getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_lr),
                this.getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_top),
                0,
                this.getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_bottom));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

        // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
        if (requestCode == REQUEST_OAUTH) {
//            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
//       facebook callback!
        callbackManager.onActivityResult(requestCode, resultCode, data);


    }
    // [END on_activity_result]





    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(null);


        email = Plus.AccountApi.getAccountName(mGoogleApiClient);

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String personPhoto = currentPerson.getImage().getUrl();
            String personGooglePlusProfile = currentPerson.getUrl();
            Log.d(TAG, personPhoto+" "+personGooglePlusProfile);

            Toast.makeText(signin.this, personName, Toast.LENGTH_LONG).show();

        }
        Toast.makeText(signin.this, email, Toast.LENGTH_LONG).show();
        // TODO: 15/9/12 change to postexecute save email and go next step

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(signin.this);
        SharedPreferences.Editor editor=preferences.edit();

        Log.e("test_pre", preferences.getString("email", "null"));

        // TODO: 15/10/11  if 原本有devices資料 就直接跳Jawbonedata else: 選devices
        Signin_Post_Data();

//        if(preferences.getString("Gmail",null)==null) {
            editor.putString("email", email);
            editor.apply();
//        }
//        Intent chioce=new Intent(signin.this,choice.class);
//        startActivity(chioce);

        // Show the signed-in UI
//        updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
//            updateUI(false);
            Toast.makeText(signin.this, R.string.sign_out, Toast.LENGTH_LONG).show();

        }
    }
    // [END on_connection_failed]

    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
//                            updateUI(false);
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt, errorCode);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
//            updateUI(false);
        }
    }


    public void Signin_Post_Data(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            try {
                new DownloadWebpageTask().execute(signin_URL);
            }
            catch (Exception e){
                Log.e("Login",e.toString());
                Toast.makeText(getApplicationContext(), "Login Time out!", Toast.LENGTH_LONG).show();

            }
        } else {
            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_LONG).show();
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return PostUrl(urls[0]);
            } catch (IOException e) {
                Log.e("doInBackground",e.toString());
                return "Unable to retrieve web page. URL may be invalid.";
            }

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.e("Result", result);
            // TODO: 15/9/8 finish data to Server!
//                        Toast.makeText(getApplicationContext(),result, Toast.LENGTH_LONG).show();

//            try {
//                JSONObject obj = new JSONObject(result);
//                Log.e("Jsonobj",obj.toString());
//
//
//                for(int i=0;i<10;i++){
//                    String date = new JSONObject(obj.getString(Integer.toString(i))).getString("date");
//                    jason_date[i]=date;
//                    String step = new JSONObject(obj.getString(Integer.toString(i))).getString("step");
//                    jason_step_daily[i]=step;
//                }
//
//
//            }
//            catch (JSONException e){
//                Log.e("Json","Error");
//            }
        }

    }
    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String PostUrl(String myurl) throws IOException {
        JSONObject JSON_DataToServer = new JSONObject();
        try {
            JSON_DataToServer.put("email", email);
            if(FB_id!=null){
                JSON_DataToServer.put("FB_id",FB_id);
            }
            outputBytes = JSON_DataToServer.toString().getBytes("UTF-8");
        }catch (org.json.JSONException  e) {
            Log.e("PostUrl",e.toString());
        }

        InputStream is ;
        OutputStream os ;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        // Finish: 15/9/5 research about https!
        URL url = new URL(myurl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();


        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);

        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setDoOutput(true); //Mean it need body so is post

        conn.setFixedLengthStreamingMode(outputBytes.length);
        conn.setSSLSocketFactory(SSL_context.getSocketFactory());
        // Starts the query
        os = conn.getOutputStream();
        os.write(outputBytes);

        is = conn.getInputStream();
        //copyInputStreamToOutputStream(is, System.out);

        conn.connect();
        int response = conn.getResponseCode();
        Log.d("HttpResponse", "The response is: " + response);

        // Convert the InputStream into a string
        String contentAsString = readIt(is, len);

        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        is.close();
        os.close();
        conn.disconnect();
        return contentAsString;

    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private void Certification() throws IOException {
//        JSONObject JSON_Data = new JSONObject();
//        try {
//            JSON_Data.put("age", 11);
//            JSON_Data.put("user_name", "jim");
//            Log.d("JSON", JSON_Data.toString());
//            outputBytes = JSON_Data.toString().getBytes("UTF-8");  //"{\"age\": 7.5}" work and JsonObject.toString work,too.
//
//        } catch (org.json.JSONException ex) {
//            Log.e("JSON_test", ex.toString());
//        }


        CertificateFactory cf;
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        try {
            cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
//            InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
            InputStream caInput = resources.openRawResource(R.raw.vinotech_us);


            Certificate ca;
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());


            caInput.close();

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSL_context = SSLContext.getInstance("TLS");
            SSL_context.init(null, tmf.getTrustManagers(), null);


        } catch (java.security.cert.CertificateException | java.security.KeyStoreException | java.security.NoSuchAlgorithmException |
                java.security.KeyManagementException exception) {
            Log.e("CertificateFactory", exception.toString());
        }
    }


}
