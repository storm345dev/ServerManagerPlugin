package net.stormdev.MTA.SMPlugin.events;

public interface Listener<T extends Event> {
	public void onCall(T event);
}
