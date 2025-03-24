package uz.event.service;

import uz.event.bot.MainBot;
import uz.event.entity.State;
import uz.event.util.Util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.*;

public class UserService extends MainBot {
    public void service(Long chatId, String text) {
        stateMap.putIfAbsent(chatId, State.MAIN_USER);

        State currentState = stateMap.get(chatId);

        if (currentState == State.MAIN_USER) {
            switch (text) {
                case "/start" -> sendMessage(chatId, "Hello", keyboard(Util.userMain));
                case showEventUser, eventInfo -> showEvents(chatId);
                case buyTicket -> buyEvents(chatId);
                case balance -> {
                    stateMap.put(chatId, State.MONEY);
                    sendMessage(chatId, "Pul miqdorini kiriting!");
                }
            }
        } else if (currentState == State.MONEY) {
            if (isDigit(text)) {
                userMap.get(chatId).setBalance(userMap.get(chatId).getBalance() + Integer.parseInt(text));
                stateMap.remove(chatId);
                sendMessage(chatId,"To'ldirildi");
            } else {
                sendMessage(chatId, "Xato");
            }
        }
    }


    private boolean isDigit(String text) {
        char[] charArray = text.toCharArray();
        for (char c : charArray) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

//    public static String showEvents() {
//        StringBuilder result = new StringBuilder();
//        AtomicInteger index = new AtomicInteger(1);
//
//
//        eventMap.forEach((id, event) -> {
//            result.append(index.getAndIncrement()).append(". 📅 Tadbir: ").append(event.getName()).append("\n\n")
//                    .append("———————————————\n\n");
//        });
//
//        return result.toString();
//    }

    public void showEvents(Long chatId) {
        StringBuilder result = new StringBuilder();
        result.append("📅 Tadbirlar ro'yxati:\n\n");

        if (eventMap.isEmpty()) {
            sendMessage(chatId, "Tadbirlar mavjud emas.");
            return;
        }

        List<String[]> inlineButtonList = new ArrayList<>();
        List<String[]> dataList = new ArrayList<>();

        List<String> rowButtons = new ArrayList<>();
        List<String> rowData = new ArrayList<>();

        AtomicInteger index = new AtomicInteger(1);

        eventMap.forEach((id, event) -> {
            result.append(index.get()).append(". ")
                    .append(event.getName())
                    .append("\n");

            rowButtons.add(String.valueOf(index.get()));
            rowData.add("event:" + id);

            if (index.get() % 5 == 0 || index.get() == eventMap.size()) {
                inlineButtonList.add(rowButtons.toArray(new String[0]));
                dataList.add(rowData.toArray(new String[0]));

                rowButtons.clear();
                rowData.clear();
            }

            index.getAndIncrement();
        });

        String[][] inlineButton = inlineButtonList.toArray(new String[0][0]);
        String[][] data = dataList.toArray(new String[0][0]);

        sendMessage(chatId, result.toString(), inlineKeyboard(inlineButton, data));
    }


    public void buyEvents(Long chatId) {
        StringBuilder result = new StringBuilder();
        result.append("📅 Tadbirlar ro'yxati:\n\n");

        if (eventMap.isEmpty()) {
            sendMessage(chatId, "Tadbirlar mavjud emas.");
            return;
        }

        List<String[]> inlineButtonList = new ArrayList<>();
        List<String[]> dataList = new ArrayList<>();

        List<String> rowButtons = new ArrayList<>();
        List<String> rowData = new ArrayList<>();

        AtomicInteger index = new AtomicInteger(1);

        eventMap.forEach((id, event) -> {
            result.append(index.get()).append(". ")
                    .append(event.getName())
                    .append("\n");

            rowButtons.add(String.valueOf(index.get()));
            rowData.add("buy:" + id);

            if (index.get() % 5 == 0 || index.get() == eventMap.size()) {
                inlineButtonList.add(rowButtons.toArray(new String[0]));
                dataList.add(rowData.toArray(new String[0]));

                rowButtons.clear();
                rowData.clear();
            }

            index.getAndIncrement();
        });

        String[][] inlineButton = inlineButtonList.toArray(new String[0][0]);
        String[][] data = dataList.toArray(new String[0][0]);

        sendMessage(chatId, result.toString(), inlineKeyboard(inlineButton, data));
    }

}