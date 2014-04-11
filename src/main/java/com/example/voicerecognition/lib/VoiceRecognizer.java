package com.example.voicerecognition.lib;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by krzysztofwrobel on 10/04/14.
 */
public class VoiceRecognizer {
    private static SpeechRecognizer sr;
    private static MyRecognitionListener listener;
    private static Context context;
    private static HashMap<String,Method> voiceMethods;
    private static HashMap<String,Object> voiceReceivers;
    private static HashMap<String,ArrayList<String>> classToCommand;

    public static void init(Context context){
        VoiceRecognizer.context = context;
        sr = SpeechRecognizer.createSpeechRecognizer(VoiceRecognizer.context);
        listener = new MyRecognitionListener();
        sr.setRecognitionListener(listener);
        voiceMethods = new HashMap<String, Method>();
        voiceReceivers = new HashMap<String, Object>();
        classToCommand = new HashMap<String, ArrayList<String>>();
    }

    public static void startListening(){
        sr.startListening(RecognizerIntent.getVoiceDetailsIntent(context));
    }

    public static void registerVoiceMethods(Object receiver){
            Class<?> registeredClass = receiver.getClass();
            Method[] methods = registeredClass.getMethods();
            int pass = 0;
            int fail = 0;

            for (Method method : methods) {
                if (method.isAnnotationPresent(VoiceTriggered.class)) {
                    // this is how you access to the attributes
                    VoiceTriggered voiceTriggered = method.getAnnotation(VoiceTriggered.class);
                    String command = voiceTriggered.command();
                    voiceMethods.put(command,method);
                    voiceReceivers.put(command,receiver);
                    ArrayList<String> strings = classToCommand.get(registeredClass.getName());
                    if(strings == null){
                        strings = new ArrayList<String>();
                    }
                    strings.add(command);
                    classToCommand.put(registeredClass.getName(),strings);
//                    try {
//                        method.invoke(null);
//                        pass++;
//                    } catch (Exception e) {
//                        if (Exception.class != expected) {
//                            fail++;
//                        } else {
//                            pass++;
//                        }
//                    }
                }
            }
    }

    public static void unregisterVoiceMethods(Object object) {
        for (int i = 0; i < voiceReceivers.values().size(); i++) {

            ArrayList<String> commands = classToCommand.get(object.getClass().getName());
            for (String command : commands){
                voiceMethods.remove(command);
                voiceReceivers.remove(command);
                classToCommand.remove(object.getClass().getName());
            }
        }
    }

    static class MyRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("Speech", "onBeginningOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d("Speech", "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("Speech", "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
            Log.d("Speech", "onError");
            switch (error){
                case SpeechRecognizer.ERROR_NO_MATCH:
                    startListening();
                    break;
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d("Speech", "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d("Speech", "onPartialResults");
        }


        @Override
        public void onResults(Bundle results) {
            Log.d("Speech", "onResults");
            ArrayList<String> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < strlist.size();i++ ) {
                Log.d("Speech", "result=" + strlist.get(i));
                String recognizedSpeech = strlist.get(i).toLowerCase();
                Method invokeMethod = voiceMethods.get(recognizedSpeech);
                Object receiver = voiceReceivers.get(recognizedSpeech);
                if(invokeMethod != null){
                    try {
                        invokeMethod.invoke(receiver);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            startListening();

        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d("Speech", "onRmsChanged");
        }

    }
}
