package us.vinotech.Login;

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

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    Object Jawbone_Json_Data;
    byte[] outputBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //Decide login or not before.
        preferences = PreferenceManager.getDefaultSharedPreferences(Jawbone.this);
        Access_Token = preferences.getString(UpPlatformSdkConstants.UP_PLATFORM_ACCESS_TOKEN, null);


        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Set required levels of permissions here, for demonstration purpose
        // we are requesting all permissions
        authScope = new ArrayList<UpPlatformSdkConstants.UpPlatformAuthScope>();
        authScope.add(UpPlatformSdkConstants.UpPlatformAuthScope.ALL);

        if(Access_Token==null) {
            Intent intent = getIntentForWebView();
            startActivityForResult(intent, UpPlatformSdkConstants.JAWBONE_AUTHORIZE_REQUEST_CODE);
        }
        else {
            // get data from jawbone.
            Jawbone_data();
        }



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

        //Get today date to query!
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = sDateFormat.format(new java.util.Date());
        Log.e("date",date);

        //uncomment to add as needed parameters
          queryHashMap.put("date", Integer.parseInt(date));
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
            JSONObject JSON_DataToServer = new JSONObject();


            Log.e(TAG, "api call successful, json output: " + o.toString());
            Toast.makeText(getApplicationContext(), o.toString(), Toast.LENGTH_LONG).show();
            Gson gson=new Gson();


            Jawbone_Json_Data=o;
            Log.d("JSON", gson.toJson(Jawbone_Json_Data));
            try {
                JSONObject jawbone_Json = new JSONObject(gson.toJson(Jawbone_Json_Data));
                JSONObject items = jawbone_Json.getJSONObject("data").getJSONArray("items").getJSONObject(0);
                JSONObject details = items.getJSONObject("details");
//                Log.e("Jawbone date",items.toString());

                int date = items.getInt("date");
                int steps = details.getInt("steps");
                double calories = details.getDouble("calories");
                int distance = details.getInt("distance");

                Log.e("Jawbone date",Integer.toString(date));
                Log.e("Jawbone steps", Integer.toString(steps));
                Log.e("calories",Double.toString(calories));
                Log.e("distance", Integer.toString(distance));

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Jawbone.this);

                //if: fb login send fb_id and email else: send email
                if(preferences.getString("email",null)!=null) {
                    JSON_DataToServer.put("steps", steps);
                    JSON_DataToServer.put("calories", calories);
                    JSON_DataToServer.put("date", date);
                    JSON_DataToServer.put("distance", distance);
                    JSON_DataToServer.put("email", preferences.getString("email", null));

                    if (preferences.getString("fb_id", null) != null) {
                        JSON_DataToServer.put("fb_id", preferences.getString("fb_id", null));
//                    jawbone_data_url = jawbone_data_url + preferences.getString("email", null) + "/" + Integer.toString(steps)+"/"+preferences.getString("fb_id",null);
                    }

//                    jawbone_data_url = jawbone_data_url + preferences.getString("email", null) + "/" + Integer.toString(steps);
                    outputBytes = JSON_DataToServer.toString().getBytes("UTF-8");
                    Jawbone_Post_Data();


                }else {
                    Log.e("Json data callback","email null");
                }
             }
             catch (org.json.JSONException | java.io.UnsupportedEncodingException e){
                 Log.e("jawbone_Json",e.toString());
             }


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
                new DownloadWebpageTask().execute();
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
                Url_Singleton url_singleton= Url_Singleton.getUrl_singleton();

                return url_singleton.UpdateTodaySteps(outputBytes);
            } catch (IOException e) {
                Log.e("doInBackground",e.toString());
                return "Unable to retrieve web page. URL may be invalid.";
            }

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.e("Result", result);

        }

    }





}
