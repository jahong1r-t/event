package uz.event.db;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.event.entity.Event;
import uz.event.entity.State;
import uz.event.entity.User;

import java.util.*;

public class Datasource {

    public static Map<Long, State> stateMap = new HashMap<>();

    public static Map<String, Event> eventMap = new HashMap<>();

    public static Map<Long, User> userMap = new HashMap<>();

    static {
        Event event1 = Event.builder()
                .id(UUID.randomUUID().toString())
                .name("Standup")
                .price(100)
                .description("Zo'r")
                .capacity(10)
                .availableSpace(10)
                .date("2020-10-10")
                .build();

        Event event2 = Event.builder()
                .id(UUID.randomUUID().toString())
                .name("Tech Conference")
                .price(200)
                .description("Innovatsion g'oyalar")
                .capacity(50)
                .availableSpace(50)
                .date("2025-06-15")
                .build();

        Event event3 = Event.builder()
                .id(UUID.randomUUID().toString())
                .name("Music Festival")
                .price(150)
                .description("Jonli musiqa kechasi")
                .capacity(500)
                .availableSpace(500)
                .date("2025-07-22")
                .build();

        Event event4 = Event.builder()
                .id(UUID.randomUUID().toString())
                .name("Startup Pitch")
                .price(50)
                .description("Yangi startaplar taqdimoti")
                .capacity(20)
                .availableSpace(20)
                .date("2025-04-05")
                .build();

        Event event5 = Event.builder()
                .id(UUID.randomUUID().toString())
                .name("Coding Bootcamp")
                .price(300)
                .description("Intensiv dasturlash kursi")
                .capacity(30)
                .availableSpace(30)
                .date("2025-09-10")
                .build();

        eventMap.put(event1.getId(), event1);
        eventMap.put(event2.getId(), event2);
        eventMap.put(event3.getId(), event3);
        eventMap.put(event4.getId(), event4);
        eventMap.put(event5.getId(), event5);
    }


    public static ReplyKeyboardMarkup keyboard(String[][] buttons) {
        List<KeyboardRow> rows = new ArrayList<>();

        for (String[] button : buttons) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (String s : button) {
                keyboardRow.add(s);
            }
            rows.add(keyboardRow);
        }
        ReplyKeyboardMarkup reply = new ReplyKeyboardMarkup();
        reply.setResizeKeyboard(true);
        reply.setKeyboard(rows);

        return reply;
    }

    public static ReplyKeyboard inlineKeyboard(String[][] buttons, String[][] data) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < buttons.length; i++) {
            List<InlineKeyboardButton> buttonRow = new ArrayList<>();
            for (int j = 0; j < buttons[i].length; j++) {
                buttonRow.add(InlineKeyboardButton.builder().callbackData(data[i][j]).text(buttons[i][j]).build());
            }
            rows.add(buttonRow);
        }

        markup.setKeyboard(rows);

        return markup;
    }
}
