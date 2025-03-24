package uz.event.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import uz.event.bot.MainBot;
import uz.event.util.Bot;

import java.util.Objects;

public class AuthService extends MainBot {
    public void service(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (Objects.equals(chatId, Bot.ADMIN)) {
            new AdminService().service(chatId, text);
        } else {
            new UserService().service(chatId, text);
        }
    }
}
