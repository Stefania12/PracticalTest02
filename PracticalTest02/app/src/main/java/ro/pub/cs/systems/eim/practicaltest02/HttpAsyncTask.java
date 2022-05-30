package ro.pub.cs.systems.eim.practicaltest02;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

public class HttpAsyncTask extends AsyncTask<String, Void, String> {
    private String result;

    public HttpAsyncTask(String result) {
        this.result = result;
        Log.d(Constants.TAG, "HttpAsyncTask initialized");
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(Constants.TAG, "HttpAsyncTask doInBackground");

        String method = params[0];
        String currency = params[1];


        String errorMessage = null;
        if (currency == null || currency.isEmpty()) {
            errorMessage = "HttpAsyncTask Error: operator1 is empty";
        }

        if (errorMessage != null) {
            return errorMessage;
        }


        HttpClient httpClient = new DefaultHttpClient();
        if (method.equals("GET")) {
            Log.d(Constants.TAG, "HttpAsyncTask GET method");

            HttpGet httpGet = new HttpGet(Constants.GET_URL+currency+".json");
            ResponseHandler<String> responseHandlerGet = new BasicResponseHandler();
            try {
                return httpClient.execute(httpGet, responseHandlerGet);
            } catch (Exception e) {
                Log.d(Constants.TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        Log.d(Constants.TAG,  "HttpAsyncTask no method");
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        this.result = result;
        Log.d(Constants.TAG, "Api returned: " + result);
    }
}