package android.bus;

public abstract class EventBusProxy {

	private final EventBus mEventBus;

	protected EventBusProxy() {
		mEventBus = new EventBus();
	}

	public final void register(Object subscriber) {
		mEventBus.register(subscriber);
	}

	public final void register(Object subscriber, int priority) {
		mEventBus.register(subscriber, priority);
	}

	public final void registerSticky(Object subscriber) {
		mEventBus.registerSticky(subscriber);
	}

	public final void registerSticky(Object subscriber, int priority) {
		mEventBus.registerSticky(subscriber, priority);
	}

	public final boolean isRegistered(Object subscriber) {

		return mEventBus.isRegistered(subscriber);
	}

	public final void unregister(Object subscriber) {
		mEventBus.unregister(subscriber);
	}

	public final void post(Object event) {
		mEventBus.post(event);
	}

	public final void postSticky(Object event) {
		mEventBus.postSticky(event);
	}

	public final <T> T getStickyEvent(Class<T> eventType) {

		return mEventBus.getStickyEvent(eventType);
	}

	public final <T> T removeStickyEvent(Class<T> eventType) {

		return mEventBus.removeStickyEvent(eventType);
	}

	public final boolean removeStickyEvent(Object event) {

		return mEventBus.removeStickyEvent(event);
	}

	public final void removeAllStickyEvents() {
		mEventBus.removeAllStickyEvents();
	}

	public final boolean hasSubscriberForEvent(Class<?> eventClass) {

		return mEventBus.hasSubscriberForEvent(eventClass);
	}
}
