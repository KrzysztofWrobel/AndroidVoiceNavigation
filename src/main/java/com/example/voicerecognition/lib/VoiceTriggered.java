package com.example.voicerecognition.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by krzysztofwrobel on 10/04/14.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VoiceTriggered {
    String command();

    String[] keywords() default {};
    String[] arguments() default {};
}
