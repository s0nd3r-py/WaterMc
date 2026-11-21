package me.s0nd3r.watermc.script;

/** Получатель сообщений {@code send ... to X}: игрок или консоль. */
public interface ScriptMessageable {

    /** Сообщение с цветовыми кодами {@code &a}, {@code &l} и т.п. */
    void sendMessage(String message);
}
