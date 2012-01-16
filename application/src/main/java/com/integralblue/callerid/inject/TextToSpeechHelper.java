package com.integralblue.callerid.inject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.google.inject.Inject;

import android.app.Application;
import android.speech.tts.TextToSpeech;

public class TextToSpeechHelper {
	
	@Inject Application application;
	
	private class QueuedSpeak implements Runnable {
		final String text;
		final int queueMode;
		final HashMap<String, String> params;
		
		public QueuedSpeak(final String text, final int queueMode,
				final HashMap<String, String> params) {
			this.text = text;
			this.queueMode = queueMode;
			this.params = params;
		}

		public void run() {
			textToSpeech.speak(text, queueMode, params);
		}
		
	}
	
	TextToSpeech textToSpeech;
	final Object startedLock = new Object();
	boolean started = false;
	final Queue<QueuedSpeak> queuedSpeaks = new LinkedList<TextToSpeechHelper.QueuedSpeak>();
	
	public void speak(String text, int queueMode, HashMap<String, String> params){
		synchronized (startedLock) {
			if(started){
				textToSpeech.speak(text, queueMode, params);
			}else{
				queuedSpeaks.add(new QueuedSpeak(text, queueMode, params));
				if(textToSpeech==null){
					textToSpeech = new TextToSpeech(application,new TextToSpeech.OnInitListener() {
						public void onInit(int status) {
							synchronized (startedLock) {
								started = true;
								for (QueuedSpeak queuedSpeak; (queuedSpeak = queuedSpeaks.poll()) != null;){
									queuedSpeak.run();
								}
							}
						}
					});
				}
			}
		}
	}

	public void stop(){
		synchronized (startedLock) {
			if(started){
				textToSpeech.stop();
			}
			queuedSpeaks.clear();
		}
	}
	
	public void shutdown(){
		synchronized (startedLock) {
			if(started){
				started = false;
				textToSpeech.shutdown();
			}
			textToSpeech = null;
			queuedSpeaks.clear();
		}
	}
}
