/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.bus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.bus.annotation.EventSubscribe;
import android.content.BroadcastReceiver;
import android.util.Log;

class SubscriberMethodFinder {
	private static final String TAG = "SubscriberMethodFinder";

	/*
	 * In newer class files, compilers may add methods. Those are called bridge or synthetic methods. EventBus must
	 * ignore both. There modifiers are not public but defined in the Java class file format:
	 * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
	 */
	private static final int BRIDGE = 0x40;
	private static final int SYNTHETIC = 0x1000;

	private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;
	private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap<String, List<SubscriberMethod>>();

	/**
	 * 查找订阅的方法
	 * 
	 * 修改点: 1.使用注释来对注的方法解析; 2.不遍历super class,super class需要在父方法在中手动注册,避免重复注册;
	 * 
	 * @by handy
	 * 
	 * @param subscriberClass
	 * @return
	 */
	protected List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
		List<SubscriberMethod> subscriberMethods = null;

		if (subscriberClass != null) {
			Class<?> clazz = subscriberClass;

			String key = clazz.getName();

			synchronized (methodCache) {
				subscriberMethods = methodCache.get(key);
			}

			if (subscriberMethods == null) {
				subscriberMethods = new ArrayList<SubscriberMethod>();

				while (clazz != null) {
					// Starting with EventBus 2.2 we enforced methods to be public (might change with annotations again)
					Method[] methods = clazz.getDeclaredMethods();
					if (methods != null && methods.length > 0) {
						HashSet<String> eventTypesFound = new HashSet<String>();
						StringBuilder methodKeyBuilder = new StringBuilder();

						for (Method method : methods) {
							if (method != null) {
								int modifiers = method.getModifiers();
								if (Modifier.isPublic(modifiers)) {
									EventSubscribe subscribe = method.getAnnotation(EventSubscribe.class);
									if (subscribe != null) {
										Class<?>[] parameterTypes = method.getParameterTypes();
										if (parameterTypes.length == 1) {
											methodKeyBuilder.setLength(0);
											methodKeyBuilder.append(method.getName());
											methodKeyBuilder.append('>').append(parameterTypes[0].getName());
											String methodKey = methodKeyBuilder.toString();
											if (eventTypesFound.add(methodKey)) {
												// Only add if not already found in a sub class
												subscriberMethods.add(new SubscriberMethod(method, subscribe.tmode(),
														parameterTypes[0]));
											}
										}
									}
								}
							}
						}
					} else {
						Log.i(TAG, "EB: Class: method is null");
					}

					clazz = clazz.getSuperclass();
					if (clazz == Object.class || clazz == Activity.class || clazz == Service.class
							|| clazz == BroadcastReceiver.class || clazz == Application.class) {

						break;
					}
				}

				if (!subscriberMethods.isEmpty()) {
					synchronized (methodCache) {
						methodCache.put(key, subscriberMethods);
					}
				} else {
					Log.i(TAG, "EB: Class: subscriber methods is empty");
				}
			}
		}

		return subscriberMethods;
	}

	@Deprecated
	protected List<SubscriberMethod> __findSubscriberMethods(Class<?> subscriberClass) {
		final String ON_EVENT_METHOD_NAME = "onEvent";

		String key = subscriberClass.getName();
		List<SubscriberMethod> subscriberMethods;
		synchronized (methodCache) {
			subscriberMethods = methodCache.get(key);
		}
		if (subscriberMethods != null) {
			return subscriberMethods;
		}
		subscriberMethods = new ArrayList<SubscriberMethod>();
		Class<?> clazz = subscriberClass;
		HashSet<String> eventTypesFound = new HashSet<String>();
		StringBuilder methodKeyBuilder = new StringBuilder();
		while (clazz != null) {
			String name = clazz.getName();
			if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
				// Skip system classes, this just degrades performance
				break;
			}

			// Starting with EventBus 2.2 we enforced methods to be public (might change with annotations again)
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				if (methodName.startsWith(ON_EVENT_METHOD_NAME)) {
					int modifiers = method.getModifiers();
					if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length == 1) {
							String modifierString = methodName.substring(ON_EVENT_METHOD_NAME.length());
							ThreadMode threadMode = null;
							if (modifierString.length() == 0) {
								threadMode = ThreadMode.PostThread;
							} else if (modifierString.equals("MainThread")) {
								threadMode = ThreadMode.MainThread;
							} else if (modifierString.equals("BackgroundThread")) {
								threadMode = ThreadMode.BackgroundThread;
							} else if (modifierString.equals("Async")) {
								threadMode = ThreadMode.Async;
							}
							Class<?> eventType = parameterTypes[0];
							methodKeyBuilder.setLength(0);
							methodKeyBuilder.append(methodName);
							methodKeyBuilder.append('>').append(eventType.getName());
							String methodKey = methodKeyBuilder.toString();
							if (eventTypesFound.add(methodKey)) {
								// Only add if not already found in a sub class
								subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
							}
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}

		if (subscriberMethods.isEmpty()) {
			throw new EventBusException("Subscriber " + subscriberClass + " has no public methods called "
					+ ON_EVENT_METHOD_NAME);
		} else {
			synchronized (methodCache) {
				methodCache.put(key, subscriberMethods);
			}
			return subscriberMethods;
		}
	}

	static void clearCaches() {
		synchronized (methodCache) {
			methodCache.clear();
		}
	}

}
