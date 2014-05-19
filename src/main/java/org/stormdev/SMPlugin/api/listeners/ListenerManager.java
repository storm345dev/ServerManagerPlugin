package org.stormdev.SMPlugin.api.listeners;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.stormdev.MTA.SMPlugin.core.Core;

import org.stormdev.servermanager.api.listeners.SMEvent;
import org.stormdev.servermanager.api.listeners.SMEventHandler;
import org.stormdev.servermanager.api.listeners.SMEventHandler.Priority;
import org.stormdev.servermanager.api.listeners.SMListener;

public class ListenerManager implements org.stormdev.servermanager.api.listeners.ListenerManager {
	private Map<Class<? extends SMEvent>, Map<Priority, List<EventHandle>>> registered = new HashMap<Class<? extends SMEvent>, Map<Priority, List<EventHandle>>>();
	
	
	@Override
	public void registerListener(SMListener listener) {
		Class<? extends SMListener> cls = listener.getClass();
					
		for(Method method:cls.getDeclaredMethods()){
			method.setAccessible(true);
			if(method.isAnnotationPresent(SMEventHandler.class)){
				Annotation annot = method.getAnnotation(SMEventHandler.class);
				SMEventHandler handler = (SMEventHandler) annot;
				
				Priority p = handler.priority();
				
				EventHandle handle = new EventHandle(p, listener, method);
				register(handle);
			}
		}
		
	}
	
	private synchronized void register(EventHandle handle){
		Method method = handle.getMethod();
		Class<?>[] params = method.getParameterTypes();
		if(params.length < 1 || params.length > 1){
			Core.logger.info("Error in "+handle.getListener().getClass().getName()+" - Event handled method can only take ONE parameter, an event!");
			return;
		}
		
		Class<?> param = params[0];
		if(param == null){
			return;
		}
		if(!SMEvent.class.isAssignableFrom(param)){
			Core.logger.info("Error in "+handle.getListener().getClass().getName()+" - Event handled method parameter MUST be an SMEvent!");
			return;
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends SMEvent> evt = (Class<? extends SMEvent>) param;
		
		Priority priority = handle.getPriority();
		
		Map<Priority, List<EventHandle>> evts = new HashMap<Priority, List<EventHandle>>();
		if(registered.containsKey(evt)){
			evts = registered.get(evt);
		}
		
		List<EventHandle> events = new ArrayList<EventHandle>();
		if(evts.containsKey(priority)){
			events = evts.get(priority);
		}
		
		events.add(handle);
		
		evts.put(priority, events);
		
		registered.put(evt, evts);
		
		Core.logger.debug("Registered a listener to the API!");
	}

	@Override
	public void callEvent(SMEvent event) {
		if(!registered.containsKey(event.getEventClass())){
			return;
		}
		Map<Priority, List<EventHandle>> handles = registered.get(event.getEventClass());
		
		attemptCall(handles, Priority.LOWEST, event);
		attemptCall(handles, Priority.LOW, event);
		attemptCall(handles, Priority.NORMAL, event);
		attemptCall(handles, Priority.HIGH, event);
		attemptCall(handles, Priority.HIGHEST, event);
		attemptCall(handles, Priority.MONITOR, event);
	}
	
	private void attemptCall(Map<Priority, List<EventHandle>> handles, Priority priority, SMEvent event){
		if(handles.containsKey(priority)){
			List<EventHandle> lowest = handles.get(priority);
			for(EventHandle handle:lowest){
				try {
					handle.getMethod().invoke(handle.getListener(), event);
				} catch (Exception e) {
					//Something went wrong
				}
			}
		}
	}
	
}
