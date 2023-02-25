package tutorial;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "ninjatimeslotbot";
    }

    @Override
    public String getBotToken() {
        return REDACTED;
    }

    private String[] timings = new String[0];
    int timingSize;
    //the timings are initialised when the bot is created
    public void addTimings(String[] timings) {
        this.timings = timings;
        timingSize = timings.length;
    }

    //date time format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL yyyy");
    @Override
    public void onUpdateReceived(Update update) {
        // var msg = update.getMessage();
        // var user = msg.getFrom();
        //obtain the telegram ID from the person ordering the parcel
        long userId = REDACTED;
        System.out.println(update);
        String trackingNumber = "a04812894214E";
        String product = "Prism flat screen TV";
        if (update.hasMessage() && update.getMessage().isCommand() && update.getMessage().getText().equals("/start")) {
            String message1 = String.format("Dear customer, your parcel has arrived. Order no: %s", trackingNumber);
            sendMsg(message1, userId);
            String message2 = String.format("Would you like to specify a preferred timeslot? Additional charges will apply.");
            sendMsg(message2, userId);
            timingVote(userId);
        } else if (update.hasCallbackQuery()) {
            //user pressed a button
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            String chat_id = update.getCallbackQuery().getMessage().getChatId().toString();

            if (call_data.equals("Yes")) {
                DeleteMessage deleteMsg = new DeleteMessage(chat_id, (int) message_id);
                //if consignee chooses yes, show them all the available timeslots

                try {
                    execute(deleteMsg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                //display buttons
                List<List<InlineKeyboardButton>> someButtons = new ArrayList<List<InlineKeyboardButton>>();
                for (int i = 0; i < timingSize; i++) {
                    //display each button
                    var builtButton = InlineKeyboardButton.builder().text(timings[i]).callbackData(timings[i]).build();
                    someButtons.add(List.of(builtButton));
                }
                InlineKeyboardMarkup keyboard2 = new InlineKeyboardMarkup(someButtons);

                Long wrappedUserId = userId;
                SendMessage sm = SendMessage.builder().chatId(wrappedUserId.toString())
                        .parseMode("HTML").text("Please select your preferred timeslot:")
                        .replyMarkup(keyboard2).build();
                try {
                    execute(sm);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            } else if (call_data.equals("No")) {
                EditMessageText new_message = new EditMessageText();
                new_message.setChatId(chat_id);
                new_message.setMessageId((int) message_id);
                LocalDate localDate = LocalDate.now().plusDays(2);
                //returns the date in 2 days in the format eg 25 Feb 2023
                new_message.setText("Your parcel will arrive on " + localDate.format(formatter));
                try {
                    execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sendMsg("Invalid command or text :(", userId);
        }
    }

    //template to send a message to the consignee
    public void sendMsg(String message, Long userId){
        //todo: find tracking number;
        SendMessage sm = SendMessage.builder()
                .chatId(userId.toString())
                .text(message).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void timingVote(Long userId){
        //todo: find tracking number;

        var yes = InlineKeyboardButton.builder().text("Yes").callbackData("Yes").build();
        var no = InlineKeyboardButton.builder().text("No").callbackData("No").build();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(yes);
        rowInline.add(no);
        InlineKeyboardMarkup keyboard1 = InlineKeyboardMarkup.builder().keyboardRow(rowInline).build();
        SendMessage sm = SendMessage.builder().chatId(userId.toString())
                .parseMode("HTML").text("Do you want to choose a timeslot?")
                .replyMarkup(keyboard1).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
