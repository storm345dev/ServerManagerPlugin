package net.stormdev.MTA.SMPlugin.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
	private volatile Map<Class<Event>, List<Listener<?>>> handlers = new HashMap<Class<Event>, List<Listener<?>>>();
	
	public EventManager(){
		
	}
	
	public synchronized <T extends Event> void unregisterListener(Listener<T> listener){
		List<List<Listener<?>>> listeners =  new ArrayList<List<Listener<?>>>(handlers.values()); //Copy it
		for(List<Listener<?>> lists:listeners){ //For each event, get the listeners
			List<Listener<?>> registered = new ArrayList<Listener<?>>(lists);
			for(Listener<?> list:registered){ //For each listener in that event
				if(list.equals(listener)){
					//Remove it
					registered.remove(list);
				}
			}
			if(registered.isEmpty()){ //No more listeners for event, clear it
				listeners.remove(registered); //Remove event handling entirely
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Event> void registerListener(Class<T> event, Listener<T> listener){
		List<Listener<?>> handles = new ArrayList<Listener<?>>();
		if(handlers.containsKey(event)){
			handles = handlers.get(event);
		}
		handles.add(listener);
		handlers.put((Class<Event>) event, handles);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Event> void registerListener(T event, Listener<T> listener){
		List<Listener<?>> handles = new ArrayList<Listener<?>>();
		if(handlers.containsKey(event.getClass())){
			handles = handlers.get(event.getClass());
		}
		handles.add(listener);
		handlers.put(((Class<Event>) event.getClass()), handles);
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
