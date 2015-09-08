package us.vinotech.vino;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jawbone.upplatformsdk.api.ApiManager;
import com.jawbone.upplatformsdk.api.response.OauthAccessTokenResponse;
import com.jawbone.upplatformsdk.oauth.OauthUtils;
import com.jawbone.upplatformsdk.oauth.OauthWebViewActivity;
import com.jawbone.upplatformsdk.utils.UpPlatformSdkConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by PHE on 15/9/8.
 */
public class Jawbone extends Activity{
    private static final String TAG = Jawbone.class.getSimpleName();

    // These are obtained after registering on Jawbone Developer Portal
    // Credentials used here are created for "Test-App1"
    private static final String CLIENT_ID = "IDUouOBE_nk";
    private static final String CLIENT_SECRET = "4e4719571b8134897b699177d9ed6eb7b4d844e3";

    // This has to be identical to the OAuth redirect url setup in Jawbone Developer Portal
    private static final String OAUTH_CALLBACK_URL = "up-platform://redirect";

    private List<UpPlatformSdkConstants.UpPlatformAuthScope> authScope;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    String Access_Token;
    // jason data
    String jawbone_data_url = "https://vinotech.us/v1/post/data/";
    Object Jawbone_Json_Data;
    SSLContext SSL_context;
    byte[] outputBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Decide login or not before.
        preferences = PreferenceManager.getDefaultSharedPreferences(Jawbone.this);
        Access_Token= preferences.getString(UpPlatformSdkConstants.UP_PLATFORM_ACCESS_TOKEN, null);
        if(Access_Token==null) {
            Intent intent = getIntentForWebView();
            startActivityForResult(intent, UpPlatformSdkConstants.JAWBONE_AUTHORIZE_REQUEST_CODE);
        }
        else {
            // get data from jawbone.
            Jawbone_data();
        }

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Set required levels of permissions here, for demonstration purpose
        // we are requesting all permissions
        authScope = new ArrayList<UpPlatformSdkConstants.UpPlatformAuthScope>();
        authScope.add(UpPlatformSdkConstants.UpPlatformAuthScope.ALL);

        Intent intent = getIntentForWebView();
        startActivityForResult(intent, UpPlatformSdkConstants.JAWBONE_AUTHORIZE_REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UpPlatformSdkConstants.JAWBONE_AUTHORIZE_REQUEST_CODE && resultCode == RESULT_OK) {

            String code = data.getStringExtra(UpPlatformSdkConstants.ACCESS_CODE);
            if (code != null) {
                //first clear older accessToken, if it exists..
                ApiManager.getRequestInterceptor().clearAccessToken();

                ApiManager.getRestApiInterface().getAccessToken(
                        CLIENT_ID,
                        CLIENT_SECRET,
                        code,
                        accessTokenRequestListener);
            }
        }
    }

    private Callback accessTokenRequestListener = new Callback<OauthAccessTokenResponse>() {
        @Override
        public void success(OauthAccessTokenResponse result, Response response) {

            if (result.access_token != null) {
                editor = preferences.edit();
                editor.putString(UpPlatformSdkConstants.UP_PLATFORM_ACCESS_TOKEN, result.access_token);
                editor.putString(UpPlatformSdkConstants.UP_PLATFORM_REFRESH_TOKEN, result.refresh_token);
                editor.apply();

                Access_Token=result.access_token;
                Jawbone_data();

                Log.e(TAG, "accessToken:" + result.access_token);
            } else {
                Log.e(TAG, "accessToken not returned by Oauth call, exiting...");
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG, "failed to get accessToken:" + retrofitError.getMessage());
        }
    };

    private Intent getIntentForWebView() {
        Uri.Builder builder = OauthUtils.setOauthParameters(CLIENT_ID, OAUTH_CALLBACK_URL, authScope);

        Intent intent = new Intent(OauthWebViewActivity.class.getName());
        intent.putExtra(UpPlatformSdkConstants.AUTH_URI, builder.build());
        return intent;
    }

    private static HashMap<String, Integer> getMoveEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");

        return queryHashMap;
    }

    public void Jawbone_data(){
        ApiManager.getRequestInterceptor().setAccessToken(Access_Token);
        Log.e(TAG, "making Get Move Events List api call ...");
        ApiManager.getRestApiInterface().getMoveEventsList(
                UpPlatformSdkConstants.API_VERSION_STRING,
                getMoveEventsListRequestParams(),
                genericCallbackListener);
    }



    private Callback genericCallbackListener = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            Log.e(TAG, "api call successful, json output: " + o.toString());
            Toast.makeText(getApplicationContext(), o.toString(), Toast.LENGTH_LONG).show();

            Gson gson=new Gson();
            gson.toJson(Jawbone_Json_Data);

            Log.d("JSON", gson.toJson(Jawbone_Json_Data));

            // TODO: 15/9/8 put Data to Unity
            Jawbone_Json_Data=o;
            Jawbone_Post_Data();

        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG, "api call failed, error message: " + retrofitError.getMessage());
            Toast.makeText(getApplicationContext(), "Some Error,Try again!", Toast.LENGTH_LONG).show();
//          If failure, clear SharedPreferences and relogin get access token!
            preferences.edit().clear();
            preferences.edit().apply();



        }
    };

    public void Jawbone_Post_Data(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            try {
                new DownloadWebpageTask().execute( jawbone_data_url);

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
        InputStream is = null;
        OutputStream os = null;
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



}
