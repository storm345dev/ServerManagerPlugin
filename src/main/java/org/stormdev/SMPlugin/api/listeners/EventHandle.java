package org.stormdev.SMPlugin.api.listeners;

import java.lang.reflect.Method;

import org.stormdev.servermanager.api.listeners.SMEventHandler.Priority;
import org.stormdev.servermanager.api.listeners.SMListener;

public class EventHandle {
	private Priority priority;
	private SMListener listener;
	private Method method;
	
	public EventHandle(Priority priority, SMListener listener, Method method){
		this.setPriority(priority);
		this.setListener(listener);
		this.setMethod(method);
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public SMListener getListener() {
		return listener;
	}

	public void setListener(SMListener listener) {
		this.listener = listener;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
