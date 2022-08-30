package ru.kapitann3mo.voidchat.event_listeners;

import ru.kapitann3mo.voidchat.MainActivity;

public class onUserJoin implements EventExecutor {
    @Override
    public void run(String args) {
        MainActivity.chat.writeSystemMessage("User " + args + " connected to chat");
    }
}
