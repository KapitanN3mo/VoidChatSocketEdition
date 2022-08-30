package ru.kapitann3mo.voidchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    public static String username = null;
    public Button sendMessageButton;
    public TextView chatHistory;
    public ProgressBar connectAnimation;
    public TextInputEditText messageEdit;
    private static Server server = null;
    public static Chat chat;
    private boolean connectState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendMessageButton = findViewById(R.id.send_button);
        messageEdit = findViewById(R.id.message_edit);
        sendMessageButton.setEnabled(false);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable msg = messageEdit.getText();
                if (msg == null || msg.toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
                } else {
                    messageEdit.setText("");
                    server.sendMessage(msg.toString());
                }
            }
        });
        chatHistory = findViewById(R.id.message_history_view);
        connectAnimation = findViewById(R.id.connect_animation);
        chat = new Chat(this);
        if (username == null) {
            showNameInput();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        server.disconnect();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("123", "SAVE");
        outState.putString("username", username);
        outState.putString("chat_history", chatHistory.getText().toString());
        setConnectState(false);
        server.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setConnectState(false);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("123", "RESTORE");
        username = savedInstanceState.getString("username");
        chatHistory.setText(savedInstanceState.getString("chat_history"));
        chat.writeSystemMessage("RESTORE");
        server.reconnect();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        server.reconnect();
    }

    public void setConnectState(boolean mode) {
        this.connectState = mode;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendMessageButton.setEnabled(mode);
                if (mode) {
                    connectAnimation.setVisibility(View.INVISIBLE);
                } else {
                    connectAnimation.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public boolean getConnectState() {
        return connectState;
    }

    private void run_server() {
        Context ctx = MainActivity.this;
        Toast.makeText(ctx, "Connecting...", Toast.LENGTH_LONG).show();
        server = new Server(this);
        server.connect();
    }

    public void showNameInput() {
        MainActivity ins = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = new AlertDialog.Builder(ins).create();
                dialog.setTitle("Input your name");
                final EditText input = new EditText(ins);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                dialog.setView(input);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        username = input.getText().toString();
                        if (server != null) {
                            server.disconnect();
                        }
                        run_server();
                    }

                });
                dialog.show();
            }
        });


    }
}