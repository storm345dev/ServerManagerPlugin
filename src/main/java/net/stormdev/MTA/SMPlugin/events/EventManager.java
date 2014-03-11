package net.stormdev.MTA.SMPlugin.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
	private volatile Map<Class<Event>, List<Listener<?>>> handlers = new HashMap<Class<Event>, List<Listener<?>>>();
	
	public EventManager(){
		
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Event> void registerListener(T event, Listener<T> listener){
		List<Listener<?>> handles = new ArrayList<Listener<?>>();
		if(handlers.containsKey(event)){
			handles = handlers.get(event);
		}
		handles.add(listener);
		handlers.put((Class<Event>) event.getClass(), handles);
	}
	
	public <T extends Event> T callEvent(T event){
		List<Listener<?>> handles = new ArrayList<Listener<?>>();
		@SuppressWarnings("unchecked")
		Class<Event> c = (Class<Event>) event.getClass();
		
		if(handlers.containsKey(c)){
			handles = handlers.get(c);
		}
		
		for(Listener<?> l:handles){
			try {
				@SuppressWarnings("unchecked")
				Listener<T> list = (Listener<T>)l;
				list.onCall(event);
			} catch (Exception e) {
				//Wrong listener in list
				e.printStackTrace();
				continue;
			}
		}
		return event;
	}
}
