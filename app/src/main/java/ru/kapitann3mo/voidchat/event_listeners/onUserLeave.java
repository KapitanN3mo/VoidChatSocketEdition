package ru.kapitann3mo.voidchat.event_listeners;

import ru.kapitann3mo.voidchat.MainActivity;

public class onUserLeave implements EventExecutor {
    @Override
    public void run(String args) {
        MainActivity.chat.writeSystemMessage("User " + args + " left from chat");
    }
}
