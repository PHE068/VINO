package us.vinotech.Login;

import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;




/**
 * Created by PHE on 2015/11/8.
 */
public class Url_Singleton {
    private Resources resources;
    SSLContext SSL_context;

    String signin_URL = "https://vinotech.us/v1/post/enroll/users";
    String jawbone_data_url = "https://vinotech.us/v1/update/users";

    private static Url_Singleton url_singleton= new Url_Singleton();

    Url_Singleton(){
        resources = signin.getContext().getResources();
        try {
            Certification();
        } catch (IOException e) {
            Log.e("Certification", e.toString());
        }
    }


    public static Url_Singleton getUrl_singleton(){
        return url_singleton;
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    protected String PostUrl(byte[] outputBytes) throws IOException {
        InputStream is ;
        OutputStream os ;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        // Finish: 15/9/5 research about https!
        URL url = new URL(signin_URL);
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

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    protected String UpdateTodaySteps(byte[] outputBytes) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        // Finish: 15/9/5 research about https!
        URL url = new URL(jawbone_data_url);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();


        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("PUT");
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
