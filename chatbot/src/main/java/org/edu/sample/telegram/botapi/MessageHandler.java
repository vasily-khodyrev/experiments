package org.edu.sample.telegram.botapi;

import org.edu.sample.telegram.botapi.types.Message;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageHandler {
    Message.Type[] contentTypes() default Message.Type.TEXT;
}