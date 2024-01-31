package com.shoppingbot.SpringBot.Service;

import com.shoppingbot.SpringBot.config.BotConfig;
import com.shoppingbot.SpringBot.model.Ads;
import com.shoppingbot.SpringBot.model.AdsRepository;
import com.shoppingbot.SpringBot.model.User;
import com.shoppingbot.SpringBot.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdsRepository adsRepository;

    private String linkedUser;
    private String waitingForUserInput = null;

    final BotConfig config;
    static final String HELP_TEXT = "Help text";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String YES_BUTTON = "YES_BUTTON";
    static final String CANCEL_BUTTON = "CANCEL_BUTTON";

    public TelegramBot(BotConfig botConfig) {
        this.config = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start the bot"));
        listOfCommands.add(new BotCommand("/mydata", "operations with your data"));
        listOfCommands.add(new BotCommand("/help", "how to use this bot"));
        listOfCommands.add(new BotCommand("/create_new_list", "create a new list"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) { // Main logic

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Processing send
            if (messageText.contains("/send") && chatId == config.getOwnerId()) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    prepareAndSendMessage(user.getChatId(), textToSend);
                }
            }

            // ads
            /* if (messageText.contains("/send_ad") && chatId == config.getOwnerId()) {

            } */

            // Processing other commands
            else {
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChatId(), update.getMessage().getChat().getFirstName());
                        break;
                    case "/mydata":
                        break;
                    case "/help":
                        prepareAndSendMessage(chatId, HELP_TEXT);
                        break;
                    case "/create_new_list":
                        createNewShoppingList(chatId);
                        break;
                    default:
                        if (waitingForUserInput != null && update.hasMessage() && update.getMessage().hasText()) {
                            String userInput = update.getMessage().getText();
                            // Processing the user input
                            processUserInput(chatId, userInput);
                            log.info("waitingForUserInput: " + waitingForUserInput);
                            // Reset the state after processing user input
                            waitingForUserInput = null;
                        } else {
                            prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                        }
                        break;
                }
            }
        }

        // Processing buttons
        else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();

            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            // Processing the 'Yes' button
            if (callBackData.equals(YES_BUTTON)) {
                String text = "You pressed yes button. Enter the nickname of the person for whom the list will be created";
                executeEditMessageText(text, chatId, messageId);
                waitingForUserInput = YES_BUTTON;

            }
            // Processing the 'Cancel' button
            else if (callBackData.equals(CANCEL_BUTTON)) {
                String text = "You pressed cancel";
                executeEditMessageText(text, chatId, messageId);
            }

            // Processing user input if expected
            if (waitingForUserInput != null && update.hasMessage() && update.getMessage().hasText()) {
                String userInput = update.getMessage().getText();
                processUserInput(chatId, userInput);
                log.info("waitingForUserInput: " + waitingForUserInput);

                // Resetting the state after processing user input
                waitingForUserInput = null;
            }
            switch (callBackData) {
                case "exit":
                    break;
                case "createNewList":
                    break;
                case "myList":
                    break;
            }

        }
    }

    // Processing the user input
    private void processUserInput(long chatId, String userInput) {
        prepareAndSendMessage(chatId, "User entered: " + userInput);
    }


    private void createNewShoppingList(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you want to create new shopping list?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Yes");
        cancelButton.setCallbackData(YES_BUTTON);

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("No");
        yesButton.setCallbackData(CANCEL_BUTTON);
        rowInLine.add(cancelButton);
        rowInLine.add(yesButton);
        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        executeMessage(message);
    }


    private void getUserData() {

    }

    private void removeUserData() {

    }

    // Method /start
    private void startCommandReceived(Long chatId, Long userId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you! :wink:\nWhat do you want to do?");

        // The keyboard
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // Buttons
        InlineKeyboardButton myListButton  = new InlineKeyboardButton();
        myListButton.setText("My list");
        myListButton.setCallbackData("/mylist");
        row.add(myListButton);

        InlineKeyboardButton createNewListButton  = new InlineKeyboardButton();
        createNewListButton.setText("Create new list");
        createNewListButton.setCallbackData("/createnewlist");
        row.add(createNewListButton);

        InlineKeyboardButton exitButton = new InlineKeyboardButton();
        exitButton.setText("Exit");
        exitButton.setCallbackData("/exit");
        row.add(exitButton);

        // Add on a keyboard
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        log.info("Replied to user: " + userId);


        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    // Use this method if you want to send simple message.
    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    // Use this method if you want to send message with keyboard.
    private void sendMessageWithKeyboard(long chatId, String textToSend, KeyboardRow keyboardRow) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(getKeyboard(keyboardRow));
        executeMessage(message);
    }

    // Use this method if you want to send editable message
    private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    // Registration
    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(message.getChat().getFirstName());
            user.setLastName(message.getChat().getLastName());
            user.setUserName(message.getChat().getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }

    private ReplyKeyboardMarkup getKeyboard(KeyboardRow keyboardRow) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(keyboardRow);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Scheduled(cron = "${cron.scheduled}")
    private void sendAds () {
        var ads = adsRepository.findAll();
        var users = userRepository.findAll();
        for (Ads ad: ads) {
            for (User user: users) {
                prepareAndSendMessage(user.getChatId(), ad.getMessage());
            }
        }
    }



}
