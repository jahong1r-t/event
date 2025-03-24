package uz.event.service;

import uz.event.bot.MainBot;
import uz.event.entity.State;
import uz.event.util.Util;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.*;

public class AdminService extends MainBot {
    public void service(Long chatId, String text) {
        stateMap.putIfAbsent(chatId, State.MAIN_ADMIN);

        State currentState = stateMap.get(chatId);

        if(currentState == State.MAIN_ADMIN){
            switch (text){
                case "start" -> sendMessage(chatId,"Welcome to our bot",keyboard(Util.adminMain));
                case addEvent -> sendMessage(chatId,"Enter event name");
                case showEvent -> sendMessage();
                case editEvent -> sendMessage();
                case deleteEvent -> sendMessage();
                case history -> sendMessage();
            }
        }
    }
}
