package uz.event.service;

import uz.event.bot.MainBot;
import uz.event.entity.State;
import uz.event.util.Util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.event.db.Datasource.*;
import static uz.event.util.Button.showEventUser;

public class UserService extends MainBot {
    public void service(Long chatId, String text) {
        stateMap.putIfAbsent(chatId, State.MAIN_USER);

        State currentState = stateMap.get(chatId);

        if (currentState == State.MAIN_USER) {
            switch (text) {
                case "/start" -> sendMessage(chatId, "Hello", keyboard(Util.userMain));
                case showEventUser -> sendMessage(chatId, formatEvents());
            }
        }
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


}