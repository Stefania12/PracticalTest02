package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    String address;
    int port;
    Socket socket;
    TextView serverResponseTextView;
    String currency;

    public ClientThread(String address, int port, TextView serverResponseTextView, String currency) {
        this.address = address;
        this.port = port;
        this.serverResponseTextView = serverResponseTextView;
        this.currency = currency;
    }

    private void updateTextView(TextView textView, String newText) {
        textView.post(() -> {
            textView.setText(newText);
        });
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.d(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            // Send stuff to server
            printWriter.println(currency);
            printWriter.flush();

            String serverResponse = bufferedReader.readLine();
            while (serverResponse != null && !serverResponse.equals("")) {
                updateTextView(serverResponseTextView, serverResponse);
                serverResponse = bufferedReader.readLine();
            }
        } catch (IOException ioException) {
            Log.d(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.d(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        }
    }
}
