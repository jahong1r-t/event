package uz.event.service;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.event.bot.MainBot;
import uz.event.entity.User;
import uz.event.util.Bot;

import java.util.ArrayList;
import java.util.Objects;

import static uz.event.db.Datasource.*;

public class AuthService extends MainBot {
    @SneakyThrows
    public void service(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        Message message = update.getMessage();
        String userName = update.getMessage().getFrom().getUserName() != null ? update.getMessage().getFrom().getUserName() : update.getMessage().getFrom().getFirstName();

        userMap.putIfAbsent(chatId, User.builder().chatId(chatId).userName(userName).balance(0.0d).eventIds(new ArrayList<>()).build());

        if (Objects.equals(chatId, Bot.ADMIN)) {
            new AdminService().service(chatId, text, message);
        } else {
            new UserService().service(chatId, text, message);
        }
    }
}
