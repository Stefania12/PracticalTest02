package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TimeZone;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class ServerThread extends Thread {
    public int port;
    public ServerSocket serverSocket = null;
    public int ceva = 0;

    public Hashtable<String, String> cache = new Hashtable<>();

    private TextView res;

    private class CommunicationThread extends Thread {
        private Socket socket;
        private ServerThread serverThread;

        public CommunicationThread(ServerThread serverThread, Socket socket) {
            Log.d(Constants.TAG, "Server communication thread started");
            this.serverThread = serverThread;
            this.socket = socket;
        }

        private String callApi3(String currency) {
            HttpClient httpClient = new DefaultHttpClient();
            String pageSourceCode = "";

            try {
                HttpGet httpGet = new HttpGet(Constants.GET_URL + currency + ".json");
                HttpResponse httpGetResponse = httpClient.execute(httpGet);


                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);

                }
                Log.d(Constants.TAG, "response: "+pageSourceCode);
                return pageSourceCode;
            } catch (Exception e) {
                Log.d(Constants.TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void run() {
            Log.d(Constants.TAG, "Server communication thread running");

            if (socket == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
                return;
            }

            try {
                BufferedReader bufferedReader = Utilities.getReader(socket);
                PrintWriter printWriter = Utilities.getWriter(socket);
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client");
                // read from client and write back answer

                String currency = bufferedReader.readLine();

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                df.setTimeZone(tz);
                String nowAsISO = df.format(new Date());

                long currentMillis = (new Date()).getTime();

                String clientResponse = "...";
                if (!cache.containsKey(currency) ||
                        currentMillis - (new Date(new JSONObject(cache.get(currency)).getJSONObject("time").getString("updated"))).getTime() > 20000 ) {

                    String result = callApi3(currency);
                    Log.d(Constants.TAG, "Api response: " + result);

                    cache.put(currency, result);
                }

                clientResponse = cache.get(currency);

                String rate = new JSONObject(cache.get(currency)).getJSONObject("bpi").getJSONObject(currency).getString("rate");
                
                printWriter.println(rate);

            } catch (Exception e) {
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        Log.d(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }


            Log.d(Constants.TAG, "Server communication thread stopped");
        }
    }


    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.d(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        }

        Log.d(Constants.TAG, "Server thread started");
    }

    @Override
    public void run() {
        Log.d(Constants.TAG, "Server thread running");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.d(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.d(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                new CommunicationThread(this, socket).start();
            }
        } catch (Exception clientProtocolException) {
            clientProtocolException.printStackTrace();
        }
        Log.d(Constants.TAG, "Server thread finished");
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                ioException.printStackTrace();
            }
        }
    }
}


