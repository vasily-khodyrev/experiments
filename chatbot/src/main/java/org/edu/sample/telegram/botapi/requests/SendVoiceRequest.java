package org.edu.sample.telegram.botapi.requests;

import org.edu.sample.telegram.botapi.types.Message;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SendVoiceRequest implements ApiRequest<Message> {

    private Map<String, String> args = new HashMap<>();
    private RequestStrategy requestStrategy;

    public SendVoiceRequest(int chatId, File audioFile) {
        this(chatId, audioFile, null);
    }

    public SendVoiceRequest(int chatId, File audioFile, OptionalArgs optionalArgs) {
        args.put("chat_id", String.valueOf(chatId));

        if (optionalArgs != null)
            args.putAll(optionalArgs.options());

        requestStrategy = new MultipartStrategy(audioFile, "voice");
    }

    public SendVoiceRequest(int chatId, String audioString) {
        this(chatId, audioString, null);
    }

    public SendVoiceRequest(int chatId, String audioString, OptionalArgs optionalArgs) {
        args.put("chat_id", String.valueOf(chatId));
        args.put("voice", audioString);

        if (optionalArgs != null)
            args.putAll(optionalArgs.options());

        requestStrategy = new PostStrategy();
    }

    @Override
    public String getMethodName() {
        return "sendVoice";
    }

    @Override
    public ResultTypes getResultType() {
        return ResultTypes.MESSAGE;
    }

    @Override
    public Map<String, String> getArgs() {
        return args;
    }

    @Override
    public RequestStrategy getRequestStrategy() {
        return requestStrategy;
    }
}
