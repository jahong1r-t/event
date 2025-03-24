package uz.event.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import uz.event.bot.MainBot;
import uz.event.entity.*;
import uz.event.util.Util;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.*;

public class UserService extends MainBot {
    public void service(Long chatId, String text, Message message) {
        stateMap.putIfAbsent(chatId, State.MAIN_USER);

        State currentState = stateMap.get(chatId);

        if (currentState == State.MAIN_USER) {
            switch (text) {
                case "/start" -> sendMessage(chatId, "Hello", keyboard(Util.userMain));
                case showEventUser, eventInfo -> showEvents(chatId);
                case buyTicket -> buyEvents(chatId);
                case cancelEvent -> cancelEvent(chatId);
                case balance -> {
                    stateMap.put(chatId, State.MONEY);

                    sendMessage(chatId, "Balnce: " + userMap.get(chatId).getBalance() + "\nPul miqdorini kiriting!");
                }
                case "Show balance" -> sendMessage(chatId, "Balance:" + userMap.get(chatId).getBalance());
                case upcomingEvents -> getUpcomingEvents(chatId);
                case myEvents -> getMyEvents(chatId);
            }
        } else if (currentState == State.MONEY) {
            if (isDigit(text)) {
                userMap.get(chatId).setBalance(userMap.get(chatId).getBalance() + Integer.parseInt(text));
                stateMap.remove(chatId);
                sendMessage(chatId, "To'ldirildi");

                String userName = message.getFrom().getUserName() != null ? message.getFrom().getUserName() : message.getFrom().getFirstName();

                int unixTimestamp = message.getDate();
                Date date = new Date(unixTimestamp * 1000L);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatter.format(date);

                History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.FILLED_BALANCE).build();

                historyMap.put(build.getId(), build);
            } else {
                sendMessage(chatId, "Xato");
            }
        }
    }

    public void cancelEvent(Long chatId) {
        User user = userMap.get(chatId);
        ArrayList<String> eventIds = user.getEventIds();

        StringBuilder result = new StringBuilder();
        result.append("📅 Siz ro'yxatdan o'tgan tadbirlar:\n\n");

        if (eventIds.isEmpty()) {
            sendMessage(chatId, "Siz hech qanday tadbirga ro'yxatdan o'tmagansiz.");
            return;
        }

        List<String[]> inlineButtonList = new ArrayList<>();
        List<String[]> dataList = new ArrayList<>();

        List<String> rowButtons = new ArrayList<>();
        List<String> rowData = new ArrayList<>();

        AtomicInteger index = new AtomicInteger(1);

        for (String eventId : eventIds) {
            Event event = eventMap.get(eventId);

            result.append(index.get()).append(". ")
                    .append(event.getName()).append("\n");

            rowButtons.add(String.valueOf(index.get()));
            rowData.add("cancel:" + event.getId());

            if (index.get() % 5 == 0 || index.get() == eventIds.size()) {
                inlineButtonList.add(rowButtons.toArray(new String[0]));
                dataList.add(rowData.toArray(new String[0]));

                rowButtons.clear();
                rowData.clear();
            }

            index.getAndIncrement();
        }

        String[][] inlineButton = inlineButtonList.toArray(new String[0][0]);
        String[][] data = dataList.toArray(new String[0][0]);

        sendMessage(chatId, result.toString(), inlineKeyboard(inlineButton, data));
    }


    private void getUpcomingEvents(Long chatId) {
        StringBuilder result = new StringBuilder();
        Integer index = 0;

        if (userMap.get(chatId).getEventIds().isEmpty()) {
            sendMessage(chatId, "Hech narsa yo'q");
            return;
        }

        for (String eventId : userMap.get(chatId).getEventIds()) {
            Event event = eventMap.get(eventId);
            result.append("📅 Tadbir: ")
                    .append(event.getName())
                    .append("\n")
                    .append("💰 Narxi: ")
                    .append(event.getPrice())
                    .append(" so'm\n")
                    .append("📝 Tavsif: ")
                    .append(event.getDescription())
                    .append("\n").append("👥 Sig‘imi: ")
                    .append(event.getCapacity())
                    .append("\n").append("🎟 Mavjud joylar: ")
                    .append(event.getAvailableSpace())
                    .append("\n").append("📆 Sana: ")
                    .append(event.getDate())
                    .append("\n\n");
        }

        sendMessage(chatId, result.toString());
    }

    private void getMyEvents(Long chatId) {
        StringBuilder result = new StringBuilder();
        Integer index = 0;

        if (userMap.get(chatId).getEventIds().isEmpty()) {
            sendMessage(chatId, "Hech narsa yo'q");
            return;
        }

        for (String eventId : userMap.get(chatId).getEventIds()) {
            index++;
            Event event = eventMap.get(eventId);
            result.append(index).append(". ")
                    .append("\n").append("💰 Narxi: ")
                    .append(event.getPrice()).append(" so'm\n")
                    .append("📆 Sana: ")
                    .append(event.getDate())
                    .append("\n");
        }

        sendMessage(chatId, result.toString());
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