package ru.kapitann3mo.voidchat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;

import ru.kapitann3mo.voidchat.event_listeners.EventExecutor;
import ru.kapitann3mo.voidchat.event_listeners.onMessage;
import ru.kapitann3mo.voidchat.event_listeners.onUserJoin;
import ru.kapitann3mo.voidchat.event_listeners.onUserLeave;

public class Server {
    private static Socket server;
    private static DataInputStream input_stream;
    private static DataOutputStream output_stream;
    private static BufferedReader in;
    private static PrintWriter out;
    private static final String API_VERSION = "1";
    private MainActivity instance;
    private static HashMap<String, EventExecutor> executors = new HashMap<>();
    private static boolean isSilent = false;

    static {
        executors.put("JOINGLOBAL", new onUserJoin());
        executors.put("LEAVEGLOBAL", new onUserLeave());
        executors.put("MSGGLOBALFROM", new onMessage());
    }

    Server(MainActivity ins) {
        this.instance = ins;
    }

    public void connect() {

        Thread connect_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new Socket("kapitann3mo.ru", 1488);
                    input_stream = new DataInputStream(server.getInputStream());
                    output_stream = new DataOutputStream(server.getOutputStream());
                    in = new BufferedReader(new InputStreamReader(input_stream));
                    out = new PrintWriter(output_stream, true);
                    if (!isSilent) {
                        MainActivity.chat.writeSystemMessage("Connecting successfully");
                    }
                    handshaking();
                } catch (Exception e) {
                    MainActivity.chat.writeSystemMessage(e.getMessage());
                }
            }
        });
        connect_thread.start();
    }

    public void reconnect() {
        isSilent = true;
        connect();
    }

    public void disconnect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                out.println("QUIT");
                try {
                    server.close();
                } catch (IOException e) {
                    MainActivity.chat.writeSystemMessage("Connection close error");
                }
            }
        });
        thread.start();
    }

    private String readIncomingData() {
        try {
            while (true) {
                String response = in.readLine();
                if (!response.startsWith("#")) {
                    return response;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handshaking() {
        out.println("CLIENTHELLO");
        String response = readIncomingData();
        if (response == null) {
            MainActivity.chat.writeSystemMessage("Handshake failed #1");
        } else {
            if (response.equals("SERVERHELLO")) {
                response = readIncomingData();
                if ((response != null) && (response.equals("SERVER " + API_VERSION))) {
                    if (!isSilent) {
                        MainActivity.chat.writeSystemMessage("Handshake complete");
                    }
                    auth();
                } else {
                    MainActivity.chat.writeSystemMessage("API version error");
                }
            } else {
                MainActivity.chat.writeSystemMessage("Handshake failed #2 " + response);
            }
        }
    }


    private void auth() {
        out.println("AUTH " + Base64.getEncoder().encodeToString(MainActivity.username.getBytes()));
        String response = readIncomingData();
        if (response != null) {
            String[] data = response.split(" ");
            if (data[0].equals("AUTHRES")) {
                switch (data[1]) {
                    case "OK":
                        if (!isSilent) {
                            MainActivity.chat.writeSystemMessage("AUTH OK");
                        }
                        this.instance.setConnectState(true);
                        startMessaging();
                        break;
                    case "FAIL_NAME_BUSY":
                        MainActivity.chat.writeSystemMessage("This name is already to use!");
                        this.instance.showNameInput();
                        break;
                    default:
                        MainActivity.chat.writeSystemMessage("Other auth error");
                        this.instance.showNameInput();
                        break;
                }
            } else {
                MainActivity.chat.writeSystemMessage("Protocol error!");
            }
        }
    }

    private void startMessaging() {
        while (true) {
            String incomingData = readIncomingData();
            if (incomingData != null) {
                String[] data = incomingData.split(" ");
                switch (data[0]) {
                    case "EVENT":
                        EventExecutor executor = executors.get(data[1]);
                        if (executor == null) {
                            MainActivity.chat.writeSystemMessage("WARNING! Server received an untreated event: " + data[1]);
                        } else {
                            executor.run(data[2]);
                        }
                        break;
                    //case "MSGGLOBALFROM":
                    //    executor = new onMessage();
                    //    executor.run(data[2]);
//
                }
            } else {
                if (this.instance.getConnectState()) {
                    MainActivity.chat.writeSystemMessage("Connection interrupt");
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.instance.setConnectState(false);
                    this.instance.showNameInput();
                    break;

                } else {
                    break;
                }
            }
        }
    }

    public void sendMessage(String msg) {
        Thread st = new Thread(new Runnable() {
            @Override
            public void run() {
                out.println("MSG " + Base64.getEncoder().encodeToString(msg.getBytes()));
            }
        });
        st.start();
    }

}
