package uz.event.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.event.entity.Event;
import uz.event.entity.History;
import uz.event.entity.HistoryState;
import uz.event.entity.User;
import uz.event.service.AuthService;
import uz.event.util.Bot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static uz.event.db.Datasource.*;

public class MainBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            new AuthService().service(update);
        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().startsWith("event:")) {
                getEventInfo(update.getCallbackQuery().getFrom().getId(), update.getCallbackQuery().getData().substring(6));
            } else if (update.getCallbackQuery().getData().startsWith("buy:")) {
                buyEvent(update.getCallbackQuery().getFrom().getId(), update.getCallbackQuery().getData().substring(4), update.getCallbackQuery().getMessage().getDate(), update.getCallbackQuery().getFrom());
            } else if (update.getCallbackQuery().getData().startsWith("cancel:")) {
                cancelEvent(update.getCallbackQuery().getFrom().getId(), update.getCallbackQuery().getData().substring(7), update.getCallbackQuery().getMessage().getDate(), update.getCallbackQuery().getFrom());
            }
        }
    }

    private void cancelEvent(Long chatId, String id, int unixTimestamp, org.telegram.telegrambots.meta.api.objects.User from) {
        Event event = eventMap.get(id);
        User user = userMap.get(chatId);
        User admin = userMap.get(Bot.ADMIN);

        user.getEventIds().remove(event.getId());

        event.setAvailableSpace(event.getAvailableSpace() + 1);

        double discountedPrice = event.getPrice() * 0.8;

        admin.setBalance(admin.getBalance() - discountedPrice);
        user.setBalance(user.getBalance() + discountedPrice);

        sendMessage(chatId, "20% o'zimizga olib qoldik rozi bo'ling");

        String userName = from.getUserName() != null ? from.getUserName() : from.getFirstName();

        Date date = new Date(unixTimestamp * 1000L);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(date);


        History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.CANCEL).build();

        historyMap.put(build.getId(), build);

    }

    private void buyEvent(Long chatId, String id, int unixTimestamp, org.telegram.telegrambots.meta.api.objects.User from) {
        User user = userMap.get(chatId);
        Event event = eventMap.get(id);

        if (user.getBalance() >= event.getPrice()) {
            if (event.getAvailableSpace() > 0) {
                event.setAvailableSpace(event.getAvailableSpace() - 1);
                user.setBalance(user.getBalance() - event.getPrice());
                user.getEventIds().add(event.getId());
                User admin = userMap.get(Bot.ADMIN);
                admin.setBalance(admin.getBalance() + event.getPrice());
                sendMessage(chatId, "Sotib olindi");


                String userName = from.getUserName() != null ? from.getUserName() : from.getFirstName();

                Date date = new Date(unixTimestamp * 1000L);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatter.format(date);


                History build = History.builder().id(UUID.randomUUID().toString()).userId(chatId).userName(userName).date(formattedDate).historyState(HistoryState.BUY).build();

                historyMap.put(build.getId(), build);
            } else {
                sendMessage(chatId, "Sizga joy yo'q");
            }

        } else {
            sendMessage(chatId, "Pul yetmaydi");
        }
    }

    private void getEventInfo(Long chatId, String id) {
        Event event = eventMap.get(id);

        String result = "📅 Tadbir: " + event.getName() + "\n" +
                "💰 Narxi: " + event.getPrice() + " so'm\n" +
                "📝 Tavsif: " + event.getDescription() + "\n" +
                "👥 Sig‘imi: " + event.getCapacity() + "\n" +
                "🎟 Mavjud joylar: " + event.getAvailableSpace() + "\n" +
                "📆 Sana: " + event.getDate() + "\n";

        sendMessage(chatId, result);
    }

    @Override
    public String getBotUsername() {
        return Bot.USERNAME;
    }

    @Override
    public String getBotToken() {
        return Bot.TOKEN;
    }

    public void sendMessage(Long chatId, String message) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(message).build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long chatId, String message, Integer messageId) {
        try {
            execute(SendMessage.builder().chatId(chatId).replyToMessageId(messageId).text(message).build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long chatId, String caption, File path) {
        try {
            execute(SendPhoto.builder().chatId(chatId).photo(new InputFile(path)).caption(caption).build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long chatId, String message, ReplyKeyboard replyKeyboard) {
        try {
            execute(SendMessage.builder().chatId(chatId).replyMarkup(replyKeyboard).text(message).build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long chatId, String caption, File path, ReplyKeyboard replyKeyboard) {
        try {
            execute(SendPhoto.builder().chatId(chatId).photo(new InputFile(path)).caption(caption).replyMarkup(replyKeyboard).build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPhoto(Long chatId, String caption, List<PhotoSize> photoSizes) {
        try {
            if (!photoSizes.isEmpty()) {
                PhotoSize photo = photoSizes.stream()
                        .max((p1, p2) -> Long.compare(p1.getFileSize() != null ? p1.getFileSize() : 0,
                                p2.getFileSize() != null ? p2.getFileSize() : 0))
                        .orElse(photoSizes.get(0));

                execute(SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(photo.getFileId()))
                        .caption(caption)
                        .build());
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
