package org.edu.sample.telegram.botapi;

import org.apache.log4j.Logger;
import org.edu.sample.telegram.botapi.requests.*;
import org.edu.sample.telegram.botapi.types.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents a TelegramBot.
 * <p>
 * To use it, use any of the available subclasses or subclass it yourself.
 */
abstract public class TelegramBot {

    private static final Logger logger = Logger.getLogger(TelegramBot.class);

    private TelegramApi api;

    private Thread pollThread;
    private AtomicBoolean running = new AtomicBoolean();
    private volatile int lastUpdateId = 0;

    private ApiRequestExecutor requestExecutor;
    private ExecutorService executorService;

    private HandlerNotifier handlerNotifier;
    private boolean sendAsync = true;


    /**
     * Convenience constructor for {@code TelegramBot(botToken, true)}
     */
    public TelegramBot(String botToken) {
        this(botToken, true);
    }

    /**
     * Constructs a TelegramBot using the provided {@code botToken}, If {@code sendAsync} is {@code true},
     * the bot invokes all {@code sendXXX} methods asynchronously.
     *
     * @param botToken  The token provided by @BotFather
     * @param sendAsync Whether this bot should invoke {@code sendXXX} methods asynchronously.
     */
    public TelegramBot(String botToken, boolean sendAsync) {
        api = new TelegramApi(botToken);
        handlerNotifier = new HandlerNotifier(this);
        this.sendAsync = sendAsync;
    }

    /**
     * Starts the bot.
     * <p>
     * First, it instantiates a {@link ExecutorService} by calling {@link TelegramBot#provideExecutorService()}.
     * If this instance is constructed with {@code sendAsync} set to {@code true}, it instantiates a asynchronous {@link ApiRequestExecutor},
     * otherwise a synchronous version is used.
     * <p>
     * After this, a polling {@link Thread} is instantiated and the bot starts polling the Telegram API.
     */
    public final void start() {
        logger.info("Starting");

        executorService = provideExecutorService();
        requestExecutor = sendAsync ?
                ApiRequestExecutor.getAsynchronousExecutor() : ApiRequestExecutor.getSynchronousExecutor();

        running.set(true);

        pollThread = new Thread(new UpdatePoller());
        pollThread.start();
        onStart();
    }

    public final void start(int updateId) {
        this.lastUpdateId = updateId;
        start();
    }

    protected void onStart() {

    }

    /**
     * Stops the bot and joins the polling {@link Thread}.
     */
    public final void stop() {
        running.set(false);

        try {
            pollThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onStop();
    }

    protected void onStop() {
        logger.info("Last update id = " + lastUpdateId);
    }

    /**
     * Instantiates and returns an {@link ExecutorService}.
     * <p>
     * By default, {@link Executors#newCachedThreadPool()} is used.
     * This method can safely be overridden to adjust this behaviour.
     * This method can safely be overridden to return null, but if you decide to do so, {@link TelegramBot#notifyNewMessages(List)}
     * <b>must</b> be overridden to avoid a NPE.
     *
     * @return An instantiated {@link ExecutorService}
     */
    protected ExecutorService provideExecutorService() {
        return Executors.newCachedThreadPool();
    }

    /**
     * Forwards a message with ID {@code messageId} from {@code fromChatId} to {@code chatId}.
     *
     * @param chatId     Unique identifier for the message recipient � User or GroupChat id
     * @param fromChatId Unique identifier for the chat where the original message was sent � User or GroupChat id
     * @param messageId  Unique message identifier
     * @return An {@code ApiResponse} with the sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#forwardmessage">https://core.telegram.org/bots/api#forwardmessage</a>
     */
    public final ApiResponse<Message> forwardMessage(int chatId, int fromChatId, int messageId) {
        return requestExecutor.execute(api, new ForwardMessageRequest(chatId, fromChatId, messageId));
    }

    /**
     * A simple method for testing your bot's auth token. Requires no parameters. Returns basic information about the bot in form of a User object.
     *
     * @return this bot's information, in the form of a {@code User} wrapped in a {@code ApiResponse}
     * @see <a href="https://core.telegram.org/bots/api#getme">https://core.telegram.org/bots/api#getme</a>
     */
    public final ApiResponse<User> getMe() {
        return requestExecutor.execute(api, new GetMeRequest());
    }

    /**
     * @see TelegramBot#getUserProfilePhotos(int, OptionalArgs)
     */
    public final ApiResponse<UserProfilePhotos> getUserProfilePhotos(int userId) {
        return requestExecutor.execute(api, new GetUserProfilePhotosRequest(userId));
    }

    /**
     * Returns a {@link UserProfilePhotos} for a user.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param userId       Unique identifier of the target user
     * @param optionalArgs Any optional arguments for this method
     * @return The requested {@link UserProfilePhotos}
     * @see <a href="https://core.telegram.org/bots/api#getuserprofilephotos">https://core.telegram.org/bots/api#getuserprofilephotos</a>
     * @see OptionalArgs
     */
    public final ApiResponse<UserProfilePhotos> getUserProfilePhotos(int userId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new GetUserProfilePhotosRequest(userId, optionalArgs));
    }

    /**
     * @see TelegramBot#sendAudio(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendAudio(int chatId, File audioFile) {
        return requestExecutor.execute(api, new SendAudioRequest(chatId, audioFile));
    }

    /**
     * Use this method to send audio files, if you want Telegram clients to display them in the music player. Your audio must be in the .mp3 format. On success, the sent Message is returned. Bots can currently send audio files of up to 50 MB in size, this limit may be changed in the future.
     * <p>
     * For backward compatibility, when the fields title and performer are both empty and the mime-type of the file
     * to be sent is not audio/mpeg, the file will be sent as a playable voice message.
     * For this to work, the audio must be in an .ogg file encoded with OPUS.
     * This behavior will be phased out in the future. For sending voice messages, use the sendVoice method instead.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param audioFile    Audio {@link File} to send
     * @param optionalArgs Any optional arguments for this method
     * @return The sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#sendaudio">https://core.telegram.org/bots/api#sendaudio</a>
     */
    public final ApiResponse<Message> sendAudio(int chatId, File audioFile, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendAudioRequest(chatId, audioFile, optionalArgs));
    }

    /**
     * @see TelegramBot#sendAudio(int, String, OptionalArgs)
     */
    public final ApiResponse<Message> sendAudio(int chatId, String audioFileId) {
        return requestExecutor.execute(api, new SendAudioRequest(chatId, audioFileId));
    }

    /**
     * This version takes a file id instead of a {@link File}, as a {@code String} argument.
     *
     * @see TelegramBot#sendAudio(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendAudio(int chatId, String audioFileId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendAudioRequest(chatId, audioFileId, optionalArgs));
    }

    /**
     * Use this method when you need to tell the user that something is happening on the bot's side.
     * The status is set for 5 seconds or less (when a message arrives from your bot, Telegram clients clear its typing status).
     *
     * @param chatId     Unique identifier for the message recipient - {@link User} or {@link GroupChat} id.
     * @param chatAction The target {@link ChatAction}
     * @return True if the request was successful
     * @see <a href="https://core.telegram.org/bots/api#sendchataction">https://core.telegram.org/bots/api#sendchataction</a>
     */
    public final ApiResponse<Boolean> sendChatAction(int chatId, ChatAction chatAction) {
        return requestExecutor.execute(api, new SendChatActionRequest(chatId, chatAction));
    }

    /**
     * @see TelegramBot#sendAudio(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendDocument(int chatId, File documentFile) {
        return requestExecutor.execute(api, new SendDocumentRequest(chatId, documentFile));
    }

    /**
     * Use this method to send general files. On success, the sent Message is returned.
     * Bots can currently send files of any type of up to 50 MB in size, this limit may be changed in the future.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param documentFile File to send
     * @param optionalArgs Any optional arguments
     * @return The sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#senddocument">https://core.telegram.org/bots/api#senddocument</a>
     */
    public final ApiResponse<Message> sendDocument(int chatId, File documentFile, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendDocumentRequest(chatId, documentFile, optionalArgs));
    }

    /**
     * @see TelegramBot#sendDocument(int, String, OptionalArgs)
     */
    public final ApiResponse<Message> sendDocument(int chatId, String documentFileId) {
        return requestExecutor.execute(api, new SendDocumentRequest(chatId, documentFileId));
    }

    /**
     * This version takes a file id instead of a {@link File}.
     *
     * @see TelegramBot#sendDocument(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendDocument(int chatId, String documentFileId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendDocumentRequest(chatId, documentFileId, optionalArgs));
    }

    /**
     * @see TelegramBot#sendLocation(int, float, float, OptionalArgs)
     */
    public final ApiResponse<Message> sendLocation(int chatId, float latitude, float longitude) {
        return requestExecutor.execute(api, new SendLocationRequest(chatId, latitude, longitude));
    }

    /**
     * Use this method to send point on the map.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param latitude     Latitude of location
     * @param longitude    Longitude of location
     * @param optionalArgs Any optional arguments
     * @return the sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#sendlocation">https://core.telegram.org/bots/api#sendlocation</a>
     */
    public final ApiResponse<Message> sendLocation(int chatId, float latitude, float longitude, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendLocationRequest(chatId, latitude, longitude, optionalArgs));
    }

    /**
     * @see TelegramBot#sendMessage(int, String, OptionalArgs)
     */
    public final ApiResponse<Message> sendMessage(int chatId, String text) {
        return requestExecutor.execute(api, new SendMessageRequest(chatId, text));
    }

    /**
     * Use this method to send text messages.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param text         Text of the message to be sent
     * @param optionalArgs Any optional arguments
     * @return the sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#sendmessage">https://core.telegram.org/bots/api#sendmessage</a>
     */
    public final ApiResponse<Message> sendMessage(int chatId, String text, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendMessageRequest(chatId, text, optionalArgs));
    }

    /**
     * @see TelegramBot#sendPhoto(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendPhoto(int chatId, File photoFile) {
        return requestExecutor.execute(api, new SendPhotoRequest(chatId, photoFile));
    }

    /**
     * Use this method to send photos.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param photoFile    Photo to send
     * @param optionalArgs Any optional arguments
     * @return The sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#sendphoto">https://core.telegram.org/bots/api#sendphoto</a>
     */
    public final ApiResponse<Message> sendPhoto(int chatId, File photoFile, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendPhotoRequest(chatId, photoFile, optionalArgs));
    }

    /**
     * @see TelegramBot#sendPhoto(int, String, OptionalArgs)
     */
    public final ApiResponse<Message> sendPhoto(int chatId, String photoFileId) {
        return requestExecutor.execute(api, new SendPhotoRequest(chatId, photoFileId));
    }

    /**
     * This version takes a file id instead of a {@link File}
     *
     * @see TelegramBot#sendPhoto(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendPhoto(int chatId, String photoFileId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendPhotoRequest(chatId, photoFileId, optionalArgs));
    }

    /**
     * @see TelegramBot#sendSticker(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendSticker(int chatId, File stickerFile) {
        return requestExecutor.execute(api, new SendStickerRequest(chatId, stickerFile));
    }

    /**
     * Use this method to send .webp stickers.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param stickerFile  Sticker to send.
     * @param optionalArgs Any optional arguments
     * @return The sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#sendsticker">https://core.telegram.org/bots/api#sendsticker</a>
     */
    public final ApiResponse<Message> sendSticker(int chatId, File stickerFile, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendStickerRequest(chatId, stickerFile, optionalArgs));
    }

    /**
     * @see TelegramBot#sendSticker(int, String, OptionalArgs)
     */
    public final ApiResponse<Message> sendSticker(int chatId, String stickerFileId) {
        return requestExecutor.execute(api, new SendStickerRequest(chatId, stickerFileId));
    }

    /**
     * This version takes a file id instead of a {@link File}.
     *
     * @see TelegramBot#sendSticker(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendSticker(int chatId, String stickerFileId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendStickerRequest(chatId, stickerFileId, optionalArgs));
    }

    /**
     * @see TelegramBot#sendVideo(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendVideo(int chatId, File videoFile) {
        return requestExecutor.execute(api, new SendVideoRequest(chatId, videoFile));
    }

    /**
     * Use this method to send video files,
     * Telegram clients support mp4 videos (other formats may be sent as Document ({@link TelegramBot#sendDocument(int, File, OptionalArgs)})).
     * Bots can currently send video files of up to 50 MB in size, this limit may be changed in the future.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient - {@link User} or {@link GroupChat} id
     * @param videoFile    Video to send
     * @param optionalArgs Any optional arguments.
     * @return The sent {@link Message}
     * @see <a href="https://core.telegram.org/bots/api#sendvideo">https://core.telegram.org/bots/api#sendvideo</a>
     */
    public final ApiResponse<Message> sendVideo(int chatId, File videoFile, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendVideoRequest(chatId, videoFile, optionalArgs));
    }

    /**
     * @see TelegramBot#sendVideo(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendVideo(int chatId, String videoFileId) {
        return requestExecutor.execute(api, new SendVideoRequest(chatId, videoFileId));
    }

    /**
     * This version uses a file id rather than a {@link File}
     *
     * @see TelegramBot#sendVideo(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendVideo(int chatId, String videoFileId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendVideoRequest(chatId, videoFileId, optionalArgs));
    }

    /**
     * @see TelegramBot#sendVoice(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendVoice(int chatId, File voiceFile) {
        return requestExecutor.execute(api, new SendVoiceRequest(chatId, voiceFile));
    }

    /**
     * Use this method to send audio files, if you want Telegram clients to display the file as a playable voice message.
     * For this to work, your audio must be in an .ogg file encoded with OPUS (other formats may be sent as Audio or Document).
     * On success, the sent Message is returned.
     * Bots can currently send voice messages of up to 50 MB in size, this limit may be changed in the future.
     * <p>
     * For any optional arguments, refer to the Telegram documentation.
     *
     * @param chatId       Unique identifier for the message recipient � User or GroupChat id
     * @param voiceFile    Audio file to send. You can either pass a file_id as String to resend an audio
     *                     that is already on the Telegram servers, or upload a new audio file using multipart/form-data.
     * @param optionalArgs Any optional arguments
     * @return the sent {@link Message}
     */
    public final ApiResponse<Message> sendVoice(int chatId, File voiceFile, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendVoiceRequest(chatId, voiceFile, optionalArgs));
    }

    /**
     * This version uses a String rather than a File.
     *
     * @see TelegramBot#sendVoice(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendVoice(int chatId, String voiceFileId) {
        return requestExecutor.execute(api, new SendVoiceRequest(chatId, voiceFileId));
    }

    /**
     * This version uses a String rather than a File
     *
     * @see TelegramBot#sendVoice(int, File, OptionalArgs)
     */
    public final ApiResponse<Message> sendVoice(int chatId, String voiceFileId, OptionalArgs optionalArgs) {
        return requestExecutor.execute(api, new SendVoiceRequest(chatId, voiceFileId, optionalArgs));
    }

    /**
     * Convenience method for {@code sendMessage(message.getChat().getId(), text, new OptionalArgs().replyToMessageId(message.getMessageId()))}
     */
    public final ApiResponse<Message> replyTo(Message message, String text) {
        logger.info("Reply to " + message.getFrom().getFirstName() + "(" + message.getFrom().getId() + ")" + " : " + text);
        OptionalArgs optionalArgs = new OptionalArgs().replyToMessageId(message.getMessageId());
        return sendMessage(message.getChat().getId(), text, optionalArgs);
    }

    /**
     * This method is called when a new message has arrived.
     * It can safely be overridden by subclasses.
     *
     * @param message The newly arrived {@link Message}
     */
    protected void onMessage(Message message) {
        // Can be overridden by subclasses.
    }

    /**
     * This method is called by this class to process all new {@link Message}s asynchronously.
     * It <b>must</b> be overridden if {@link TelegramBot#provideExecutorService()} is overridden to return null, otherwise
     * a {@link NullPointerException} may be thrown.
     *
     * @param messages The newly arrived {@link Message}s
     */
    protected void notifyNewMessages(List<Message> messages) {
        for (final Message message : messages) {
            executorService.submit(new Runnable() {
                public void run() {
                    onMessage(message);
                }
            });
            executorService.submit(new Runnable() {
                public void run() {
                    handlerNotifier.notifyHandlers(message);
                }
            });
        }
    }

    private class UpdatePoller implements Runnable {
        private int countFailures = 0;

        public void run() {
            while (running.get()) {
                try {
                    poll();
                    if (countFailures > 0) {
                        logger.info("Connection issue resolved!");
                    }
                    countFailures = 0;
                    sleep(1);
                } catch (ApiException e) {
                    countFailures++;
                    logger.fatal("An exception occurred while polling Telegram.", e);
                    logger.warn("Wait for 10 min - maybe an issue will be solved soon...");
                    sleep(10 * 60);
                    if (countFailures > 10) {
                        running.set(false);
                    }
                }
            }
        }

        public void poll() {
            OptionalArgs optionalArgs = new OptionalArgs().offset(lastUpdateId + 1).timeout(3);
            GetUpdatesRequest request = new GetUpdatesRequest(optionalArgs);

            List<Update> updates = requestExecutor.execute(api, request).getResult();
            if (updates.size() > 0) {
                List<Message> newMessages = processUpdates(updates);
                notifyNewMessages(newMessages);
            } else {
                sleep(5);
            }
        }

        private List<Message> processUpdates(List<Update> updates) {
            List<Message> newMessages = new ArrayList<Message>();

            for (Update update : updates) {
                if (update.getUpdateId() > lastUpdateId)
                    lastUpdateId = update.getUpdateId();
                logger.info("update-id=" + lastUpdateId);
                newMessages.add(update.getMessage());
            }

            return newMessages;
        }

        private void sleep(int sec) {
            try {
                Thread.sleep(sec * 1000);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    running.set(false);
                }
            }
        }
    }

}
