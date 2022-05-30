package ro.pub.cs.systems.eim.practicaltest02;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalTest02MainActivity extends AppCompatActivity {

    EditText serverPortEditText;
    EditText clientAddrEditText;
    EditText clientPortEditText;
    EditText clientCurrency;

    Button startServerButton;
    Button callServerButton;

    TextView serverResponseTextView;


    ServerThread serverThread;
    ClientThread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        EditText serverPortEditText = findViewById(R.id.server_port);
        EditText clientAddrEditText = findViewById(R.id.client_addr_edit_text);
        EditText clientPortEditText = findViewById(R.id.client_port_edit_text);
        EditText clientCurrency = findViewById(R.id.client_currency);

        TextView serverResponseTextView = findViewById(R.id.server_response);

        Button startServerButton = findViewById(R.id.start_server_button);
        startServerButton.setOnClickListener((v) -> {
            String serverPort = serverPortEditText.getText().toString();
            if (serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.serverSocket == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        });


        Button callServerButton = findViewById(R.id.client_call_server);
        callServerButton.setOnClickListener((v) -> {
            String clientAddress = clientAddrEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();
            if (clientAddress.isEmpty() || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            serverResponseTextView.setText("");

            clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), serverResponseTextView, clientCurrency.getText().toString());
            clientThread.start();
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}