package ru.kapitann3mo.voidchat.event_listeners;


import java.util.Base64;

import ru.kapitann3mo.voidchat.MainActivity;

public class onMessage implements EventExecutor {
    @Override
    public void run(String args) {
        String[] data = args.split(",");
        byte[] bytes_msg = Base64.getDecoder().decode(data[1]);
        StringBuilder msg = new StringBuilder();
        for (byte s : bytes_msg) {
            msg.append((char) s);
        }
        MainActivity.chat.writeChatMessage(data[0], msg.toString());

    }
}
