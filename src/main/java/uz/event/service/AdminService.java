
package uz.event.service;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.event.bot.MainBot;
import uz.event.entity.Event;
import uz.event.entity.History;
import uz.event.entity.HistoryState;
import uz.event.entity.State;
import uz.event.util.Bot;
import uz.event.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.*;

public class AdminService extends MainBot {
    Event event;

    @SneakyThrows
    public void service(Long chatId, String text, Message message) {
        stateMap.putIfAbsent(chatId, State.MAIN_ADMIN);
        State currentState = stateMap.get(chatId);


        if (currentState == State.MAIN_ADMIN) {
            switch (text) {
                case "/start" -> sendMessage(Bot.ADMIN, "Welcome to our bot", keyboard(Util.adminMain));
                case addEvent -> {
                    sendMessage(Bot.ADMIN, "Enter event name");
                    stateMap.put(chatId, State.valueOf("ADD_EVENT_NAME"));
                }
                case showEvent -> sendMessage(Bot.ADMIN, formatEvents());
                case editEvent -> {
                    sendMessage(Bot.ADMIN, "Enter event ID to edit:\n\n" + formatEvents());
                    stateMap.put(chatId, State.valueOf("EDIT_EVENT_ID"));
                }
                case deleteEvent -> {
                    sendMessage(Bot.ADMIN, "Enter event ID to delete:\n\n" + formatEvents());
                    stateMap.put(chatId, State.valueOf("DELETE_EVENT"));
                }
                case history -> sendMessage(Bot.ADMIN, formatHistory());

                default -> handleStateInput(chatId, text);
            }
        } else if (currentState == State.ADD_EVENT_NAME) {
            Event newEvent = Event.builder().name(text).build();
            userMap.get(chatId).getEventIds().add(newEvent.getId());
            sendMessage(Bot.ADMIN, "Enter event price");
            stateMap.put(chatId, State.valueOf("ADD_EVENT_PRICE"));
            eventMap.put(newEvent.getId(), newEvent);
        } else if (currentState == State.ADD_EVENT_PRICE) {
            try {
                int price = Integer.parseInt(text);
                String eventId = getLastEventId(chatId);
                Event event = eventMap.get(eventId);
                event.setPrice(price);
                sendMessage(Bot.ADMIN, "Enter event description");
                stateMap.put(chatId, State.valueOf("ADD_EVENT_DESC"));
            } catch (NumberFormatException e) {
                sendMessage(Bot.ADMIN, "Please enter a valid number for price");
            }
        } else if (currentState == State.ADD_EVENT_DESC) {
            String eventId = getLastEventId(chatId);
            Event event = eventMap.get(eventId);
            event.setDescription(text);
            sendMessage(Bot.ADMIN, "Enter event capacity");
            stateMap.put(chatId, State.valueOf("ADD_EVENT_CAPACITY"));
        } else if (currentState == State.ADD_EVENT_CAPACITY) {
            try {
                int capacity = Integer.parseInt(text);
                String eventId = getLastEventId(chatId);
                Event event = eventMap.get(eventId);
                event.setCapacity(capacity);
                event.setAvailableSpace(capacity);
                sendMessage(Bot.ADMIN, "Enter event date (YYYY-MM-DD)");
                stateMap.put(chatId, State.valueOf("ADD_EVENT_DATE"));
            } catch (NumberFormatException e) {
                sendMessage(Bot.ADMIN, "Please enter a valid number for capacity");
            }
        } else if (currentState == State.ADD_EVENT_DATE) {
            String eventId = getLastEventId(chatId);
            Event event = eventMap.get(eventId);
            event.setDate(text);
            event.setId(UUID.randomUUID().toString());
            sendMessage(Bot.ADMIN, "Event created successfully!\n\n" + formatEvent(event),
                    keyboard(Util.adminMain));
            stateMap.put(chatId, State.MAIN_ADMIN);

            String userName = message.getFrom().getUserName() != null ? message.getFrom().getUserName() : message.getFrom().getFirstName();

            int unixTimestamp = message.getDate();
            Date date = new Date(unixTimestamp * 1000L);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = formatter.format(date);

            History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.CREATE).build();

            historyMap.put(build.getId(), build);

        } else if (currentState == State.EDIT_EVENT_ID) {


            if (eventMap.containsKey(text)) {
                userMap.get(chatId).getEventIds().add(text);
                sendMessage(Bot.ADMIN, "What would you like to edit?\n1. Name\n2. Price\n3. Description\n4. Date");
                stateMap.put(chatId, State.valueOf("EDIT_EVENT_FIELD"));
            } else {
                sendMessage(Bot.ADMIN, "Invalid event ID");
            }
        } else if (currentState == State.DELETE_EVENT) {
            if (eventMap.containsKey(text)) {
                eventMap.remove(text);
                sendMessage(Bot.ADMIN, "Event deleted successfully!", keyboard(Util.adminMain));
                stateMap.put(chatId, State.MAIN_ADMIN);
            } else {
                sendMessage(Bot.ADMIN, "Invalid event ID");
            }
        } else if (currentState == State.EDIT_EVENT_FIELD) {
            switch (text) {
                case "1" -> {
                    stateMap.put(chatId, State.EDIT_EVENT_NAME);
                    sendMessage(chatId, "enter name to edit");
                }
                case "2" -> {
                    stateMap.put(chatId, State.EDIT_EVENT_PRICE);
                    sendMessage(chatId, "enter name to edit");
                }
                case "3" -> {
                    stateMap.put(chatId, State.EDIT_EVENT_DESC);
                    sendMessage(chatId, "enter name to edit");
                }

                case "4" -> {
                    stateMap.put(chatId, State.EDIT_EVENT_DATE);
                    sendMessage(chatId, "enter name to edit");
                }

            }
        } else if (currentState == State.EDIT_EVENT_NAME) {
            String s = userMap.get(chatId).getEventIds().get(0);
            Event event1 = eventMap.get(s);
            event1.setName(text);
            sendMessage(chatId, "Successfully");
            stateMap.remove(chatId);
            userMap.get(chatId).getEventIds().remove(0);

            String userName = message.getFrom().getUserName() != null ? message.getFrom().getUserName() : message.getFrom().getFirstName();

                int unixTimestamp = message.getDate();
                Date date = new Date(unixTimestamp * 1000L);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatter.format(date);

                History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.EDIT).build();

                historyMap.put(build.getId(), build);

        } else if (currentState == State.EDIT_EVENT_PRICE) {
            String s = userMap.get(chatId).getEventIds().get(0);
            Event event1 = eventMap.get(s);
            event1.setPrice(Integer.parseInt(text));
            sendMessage(chatId, "Successfully");
            stateMap.remove(chatId);
            userMap.get(chatId).getEventIds().remove(0);

            String userName = message.getFrom().getUserName() != null ? message.getFrom().getUserName() : message.getFrom().getFirstName();

                int unixTimestamp = message.getDate();
                Date date = new Date(unixTimestamp * 1000L);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatter.format(date);

                History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.EDIT).build();

                historyMap.put(build.getId(), build);
        } else if (currentState == State.EDIT_EVENT_DESC) {
            String s = userMap.get(chatId).getEventIds().get(0);
            Event event1 = eventMap.get(s);
            event1.setDescription(text);
            sendMessage(chatId, "Successfully");
            stateMap.remove(chatId);
            userMap.get(chatId).getEventIds().remove(0);

            String userName = message.getFrom().getUserName() != null ? message.getFrom().getUserName() : message.getFrom().getFirstName();

                int unixTimestamp = message.getDate();
                Date date = new Date(unixTimestamp * 1000L);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatter.format(date);

                History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.EDIT).build();

                historyMap.put(build.getId(), build);
        } else if (currentState == State.EDIT_EVENT_DATE) {
            String s = userMap.get(chatId).getEventIds().get(0);
            Event event1 = eventMap.get(s);
            event1.setDate(text);
            sendMessage(chatId, "Successfully");
            stateMap.remove(chatId);
            userMap.get(chatId).getEventIds().remove(0);

            String userName = message.getFrom().getUserName() != null ? message.getFrom().getUserName() : message.getFrom().getFirstName();

                int unixTimestamp = message.getDate();
                Date date = new Date(unixTimestamp * 1000L);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatter.format(date);

                History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.EDIT).build();

                historyMap.put(build.getId(), build);
        }
    }



    @SneakyThrows
    private void handleStateInput(Long chatId, String text) {
        State currentState = stateMap.get(chatId);
        if (currentState == null) return;

        switch (currentState) {
            case ADD_EVENT_NAME -> {
                Event newEvent = Event.builder().name(text).build();
                userMap.get(chatId).getEventIds().add(newEvent.getId());
                sendMessage(Bot.ADMIN, "Enter event price");
                stateMap.put(chatId, State.valueOf("ADD_EVENT_PRICE"));
                eventMap.put(newEvent.getId(), newEvent);
            }
            case ADD_EVENT_PRICE -> {
                try {
                    int price = Integer.parseInt(text);
                    String eventId = getLastEventId(chatId);
                    Event event = eventMap.get(eventId);
                    event.setPrice(price);
                    sendMessage(Bot.ADMIN, "Enter event description");
                    stateMap.put(chatId, State.valueOf("ADD_EVENT_DESC"));
                } catch (NumberFormatException e) {
                    sendMessage(Bot.ADMIN, "Please enter a valid number for price");
                }
            }
            case ADD_EVENT_DESC -> {
                String eventId = getLastEventId(chatId);
                Event event = eventMap.get(eventId);
                event.setDescription(text);
                sendMessage(Bot.ADMIN, "Enter event capacity");
                stateMap.put(chatId, State.valueOf("ADD_EVENT_CAPACITY"));
            }
            case ADD_EVENT_CAPACITY -> {
                try {
                    int capacity = Integer.parseInt(text);
                    String eventId = getLastEventId(chatId);
                    Event event = eventMap.get(eventId);
                    event.setCapacity(capacity);
                    event.setAvailableSpace(capacity);
                    sendMessage(Bot.ADMIN, "Enter event date (YYYY-MM-DD)");
                    stateMap.put(chatId, State.valueOf("ADD_EVENT_DATE"));
                } catch (NumberFormatException e) {
                    sendMessage(Bot.ADMIN, "Please enter a valid number for capacity");
                }
            }
            case ADD_EVENT_DATE -> {
                String eventId = getLastEventId(chatId);
                Event event = eventMap.get(eventId);
                event.setDate(text);
                event.setId(UUID.randomUUID().toString());
                sendMessage(Bot.ADMIN, "Event created successfully!\n\n" + formatEvent(event),
                        keyboard(Util.adminMain));
                stateMap.put(chatId, State.MAIN_ADMIN);
            }
            case EDIT_EVENT_ID -> {
                if (eventMap.containsKey(text)) {
                    sendMessage(Bot.ADMIN, "What would you like to edit?\n1. Name\n2. Price\n3. Description\n4. Capacity\n5. Date");
                    stateMap.put(chatId, State.valueOf("EDIT_EVENT_FIELD"));
                    event = findEvent(text);
//                    userMap.get(chatId).getEventIds().add(text);
                } else {
                    sendMessage(Bot.ADMIN, "Invalid event ID");
                }
            }
            case DELETE_EVENT -> {
                if (eventMap.containsKey(text)) {
                    eventMap.remove(text);
                    sendMessage(Bot.ADMIN, "Event deleted successfully!", keyboard(Util.adminMain));
                    stateMap.put(chatId, State.MAIN_ADMIN);
                } else {
                    sendMessage(Bot.ADMIN, "Invalid event ID");
                }
            }
        }
    }

    private Event findEvent(String id) {

        return eventMap.get(id);
    }

    private String getLastEventId(Long chatId) {
        ArrayList<String> eventIds = userMap.get(chatId).getEventIds();
        return eventIds.get(eventIds.size() - 1);
    }

    public static String formatHistory() {
        StringBuilder result = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);

        historyMap.forEach((id, history) -> {
            result.append(index.getAndIncrement()).append(". 📜 History ID: `").append(history.getId()).append("`\n")
                    .append("   👤 Foydalanuvchi: ").append(history.getUserName()).append(" (ID: ").append(history.getUserId()).append(")\n")
                    .append("   📆 Sana: ").append(history.getDate()).append("\n")
                    .append("   🔄 Holat: ").append(history.getHistoryState()).append("\n")
                    .append("———————————————\n");
        });

        return result.toString();
    }

    public static String formatEvents() {
        StringBuilder result = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);

        eventMap.forEach((id, event) -> {
            result.append(index.getAndIncrement()).append(". 📅 Tadbir: ").append(event.getName()).append("\n")
                    .append("   💰 Narxi: ").append(event.getPrice()).append(" so'm\n")
                    .append("   📝 Tavsif: ").append(event.getDescription()).append("\n")
                    .append("   👥 Sig‘imi: ").append(event.getCapacity()).append("\n")
                    .append("   🎟 Mavjud joylar: ").append(event.getAvailableSpace()).append("\n")
                    .append("   📆 Sana: ").append(event.getDate()).append("\n")
                    .append("   🆔 ID: `").append(event.getId()).append("`\n")
                    .append("———————————————\n");
        });

        return result.toString();
    }

    private String formatEvent(Event event) {
        return "📅 Tadbir: " + event.getName() + "\n" +
                "💰 Narxi: " + event.getPrice() + " so'm\n" +
                "📝 Tavsif: " + event.getDescription() + "\n" +
                "👥 Sig‘imi: " + event.getCapacity() + "\n" +
                "🎟 Mavjud joylar: " + event.getAvailableSpace() + "\n" +
                "📆 Sana: " + event.getDate() + "\n" +
                "🆔 ID: `" + event.getId() + "`";
    }
}

