package uz.event.service;

import lombok.SneakyThrows;
import uz.event.bot.MainBot;
import uz.event.entity.Event;
import uz.event.entity.State;
import uz.event.util.Bot;
import uz.event.util.Util;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.*;

public class AdminService extends MainBot {
    @SneakyThrows
    public void service(Long chatId, String text) {
        stateMap.putIfAbsent(chatId, State.MAIN_ADMIN);
        State currentState = stateMap.get(chatId);

        if (currentState == State.MAIN_ADMIN) {
            switch (text) {
                case "/start" ->
                        sendMessage(Bot.ADMIN, "Welcome to our bot", keyboard(Util.adminMain));
                case addEvent -> {
                    sendMessage(Bot.ADMIN, "Enter event name");
                    stateMap.put(chatId, State.valueOf("ADD_EVENT_NAME"));
                }
                case showEvent ->
                        sendMessage(Bot.ADMIN, formatEvents());
                case editEvent -> {
                    sendMessage(Bot.ADMIN, "Enter event ID to edit:\n\n" + formatEvents());
                    stateMap.put(chatId, State.valueOf("EDIT_EVENT_ID"));
                }
                case deleteEvent -> {
                    sendMessage(Bot.ADMIN, "Enter event ID to delete:\n\n" + formatEvents());
                    stateMap.put(chatId, State.valueOf("DELETE_EVENT"));
                }
                case history ->
                        sendMessage(Bot.ADMIN, "History feature coming soon!");
                default ->
                        handleStateInput(chatId, text);
            }
        }else if (currentState==State.ADD_EVENT_NAME){
            Event newEvent = Event.builder().name(text).build();
            userMap.get(chatId).getEventIds().add(newEvent.getId());
            sendMessage(Bot.ADMIN, "Enter event price");
            stateMap.put(chatId, State.valueOf("ADD_EVENT_PRICE"));
            eventMap.put(newEvent.getId(), newEvent);
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
                    userMap.get(chatId).getEventIds().add(text);
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

    private String getLastEventId(Long chatId) {
        ArrayList<String> eventIds = userMap.get(chatId).getEventIds();
        return eventIds.get(eventIds.size() - 1);
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
