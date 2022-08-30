package ru.kapitann3mo.voidchat;

public class Chat {
    MainActivity instance;

    Chat(MainActivity instance) {
        this.instance = instance;
    }

    public void writeSystemMessage(String message) {
        MainActivity inst = this.instance;
        this.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inst.chatHistory.append("[SYSTEM] " + message+"\n");
            }
        });
    }

    public void writeChatMessage(String author_name, String message) {
        MainActivity inst = this.instance;
        this.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inst.chatHistory.append("[<]" + author_name + ": " + message+"\n");
            }
        });
    }
}
