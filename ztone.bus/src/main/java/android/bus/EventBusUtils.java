package android.bus;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

public class EventBusUtils {
    private static volatile EventBus mDefaultEventBus;

    private static final ArrayMap<String, EventBus> mEventBusMap = new ArrayMap<String, EventBus>();

    /**
     * Convenience singleton for apps using a process-wide EventBus instance.
     */
    public static EventBus obtain() {
        if (mDefaultEventBus == null) {
            synchronized (EventBus.class) {
                if (mDefaultEventBus == null) {
                    mDefaultEventBus = new EventBus();
                }
            }
        }

        return mDefaultEventBus;
    }

    public static EventBus obtain(String tag) {
        EventBus eventBus = null;
        if (!TextUtils.isEmpty(tag)) {
            eventBus = mEventBusMap.get(tag);
            if (eventBus == null) {
                synchronized (EventBusUtils.class) {
                    if (eventBus == null) {
                        eventBus = new EventBus();

                        mEventBusMap.put(tag, eventBus);
                    }
                }
            }
        }

        if (eventBus == null) {
            eventBus = obtain();
        }

        return eventBus;
    }

    public static void register(Object subscriber) {
        obtain().register(subscriber);
    }

    public static void register(String tag, Object subscriber) {
        obtain(tag).register(subscriber);
    }

    public static void register(Object subscriber, int priority) {
        obtain().register(subscriber, priority);
    }

    public static void register(String tag, Object subscriber, int priority) {
        obtain(tag).register(subscriber, priority);
    }

    public static void registerSticky(Object subscriber) {
        obtain().registerSticky(subscriber);
    }

    public static void registerSticky(String tag, Object subscriber) {
        obtain(tag).registerSticky(subscriber);
    }

    public static void registerSticky(Object subscriber, int priority) {
        obtain().registerSticky(subscriber, priority);
    }

    public static void registerSticky(String tag, Object subscriber, int priority) {
        obtain(tag).registerSticky(subscriber, priority);
    }

    public static boolean isRegistered(Object subscriber) {

        return obtain().isRegistered(subscriber);
    }

    public static boolean isRegistered(String tag, Object subscriber) {

        return obtain(tag).isRegistered(subscriber);
    }

    public static void unregister(Object subscriber) {
        obtain().unregister(subscriber);
    }

    public static void unregister(String tag, Object subscriber) {
        obtain(tag).unregister(subscriber);
    }

    public static <O> void post(O o) {
        obtain().post(o);
    }

    public static <O> void post(String tag, O o) {
        obtain(tag).post(o);
    }

    public static void postSticky(Object event) {
        obtain().postSticky(event);
    }

    public static void postSticky(String tag, Object event) {
        obtain(tag).postSticky(event);
    }

    public static <T> T getStickyEvent(Class<T> eventType) {

        return obtain().getStickyEvent(eventType);
    }

    public static <T> T getStickyEvent(String tag, Class<T> eventType) {

        return obtain(tag).getStickyEvent(eventType);
    }

    public static <T> T removeStickyEvent(Class<T> eventType) {

        return obtain().removeStickyEvent(eventType);
    }

    public static <T> T removeStickyEvent(String tag, Class<T> eventType) {

        return obtain(tag).removeStickyEvent(eventType);
    }

    public static boolean removeStickyEvent(Object event) {

        return obtain().removeStickyEvent(event);
    }

    public static boolean removeStickyEvent(String tag, Object event) {

        return obtain(tag).removeStickyEvent(event);
    }

    public static void removeAllStickyEvents() {

        obtain().removeAllStickyEvents();
    }

    public static void removeAllStickyEvents(String tag) {

        obtain(tag).removeAllStickyEvents();
    }

    public static boolean hasSubscriberForEvent(Class<?> eventClass) {

        return obtain().hasSubscriberForEvent(eventClass);
    }

    public static boolean hasSubscriberForEvent(String tag, Class<?> eventClass) {

        return obtain(tag).hasSubscriberForEvent(eventClass);
    }
}
