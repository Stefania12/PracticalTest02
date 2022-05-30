package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    public int port;
    public ServerSocket serverSocket = null;
    public int ceva = 0;

    private class CommunicationThread extends Thread {
        private Socket socket;
        private ServerThread serverThread;

        public CommunicationThread(ServerThread serverThread, Socket socket) {
            Log.d(Constants.TAG, "Server communication thread started");
            this.serverThread = serverThread;
            this.socket = socket;
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

                printWriter.println("Hello " + currency + " " + (ceva++));

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


