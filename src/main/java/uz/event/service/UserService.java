package uz.event.service;

import uz.event.bot.MainBot;
import uz.event.entity.State;
import uz.event.util.Util;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.showEventUser;

public class UserService extends MainBot {
    public void service(Long chatId, String text) {
        stateMap.putIfAbsent(chatId, State.MAIN_USER);

        State currentState = stateMap.get(chatId);

        if (currentState == State.MAIN_USER) {
            switch (text) {
                case "/start" -> sendMessage(chatId, "Hello", keyboard(Util.userMain));
                case showEventUser -> sendMessage(chatId, "salom");
            }
        }
    }
}
