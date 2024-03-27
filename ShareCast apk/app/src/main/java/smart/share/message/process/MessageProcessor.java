package smart.share.message.process;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageProcessor {
	
	private static final String TAG = MessageProcessor.class.getSimpleName();
	/**
	 * The number of threads in thread pool.
	 */
	private static final int CORE_WORKER_THREAD_NUM = 4;
	
	private static MessageProcessor sInstance;
	
	private static final Object sMessageListSync = new Object();
	
	/**
	 * The looper thread that uses to receive message and
	 * handle the message.
	 */
	private HandlerThread messageReceiver;
	/**
	 * The handler of mHandlerThread.
	 */
	private static Handler sMessageHandler;
	
	/**
	 * This list uses to save the handle method, including PerformOnBackground
	 * and PerformOnForeground, that corresponds to certain message. Note that
	 * the message can be received in multiple activities and can be specified
	 * different handle method in different activities.
	 */
	private SparseArray<Map<Activity,List<Object>>> messageMappingList;
	
	/**
	 * The thread pool to handle the message which needs to do in background.
	 */
	private ExecutorService mWorkerThreadPool = Executors.newFixedThreadPool(CORE_WORKER_THREAD_NUM);
	
	private MessageProcessor() {
		messageMappingList = new SparseArray<Map<Activity,List<Object>>>();
		messageReceiver = new HandlerThread("Handler Thread");
		messageReceiver.start();
	}
	
	/**
	 * Interface definition for a callback to be invoked when receive the message.
	 * This callback will be performed in background.
	 */
	public interface PerformOnBackground {
		void doInBackground(Message msg);
	}
	
	/**
	 * Interface definition for a callback to be invoked when receive the message.
	 * This callback will be performed in main thread.
	 */
	public interface PerformOnForeground {
		void doInForeground(Message msg);
	}
	
	/**
	 * Sends a Message containing only the what value.
	 * 
	 * @param what The what value the message contains.
	 * 
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
	 */
	public boolean postEmptyMessage(int what) {
		return postEmptyMessageDelayed(what, 0);
	}
	
	/**
	 * Sends a Message containing only the what value, to be delivered
	 * delaying specific time.
	 * 
	 * @param what The what value the message contains.
	 * @param delayMillis Milliseconds to be delayed to push the message.
	 * 
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
	 */
	public boolean postEmptyMessageDelayed(int what, long delayMillis) {
		Message msg = Message.obtain();
		msg.what = what;
		return postMessageDelayed(msg, delayMillis);
	}
	
	/**
     * Sends a Message containing only the what value, to be delivered 
     * at a specific time.
     * @see #postMessageAtTime(Message, long)
     *  
     * @param what The what value the message contains.
     * @param uptimeMillis The absolute time at which the message should be
     *         delivered, using the
     *         {@link SystemClock#uptimeMillis} time-base.
     *         
     * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
     */
	public boolean postEmptyMessageAtTime(int what, long uptimeMillis) {
		Message msg = Message.obtain();
		msg.what = what;
		return postMessageAtTime(msg, uptimeMillis);
	}
	
	/**
	 * Pushes a message onto the end of the message queue after all pending messages
     * before the current time. It will be received in {@link #handleMessage},
     * in the thread attached to this handler.
	 * 
	 * @param msg The message to be pushed onto the message queue.
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
	 */
	public boolean postMessage(Message msg) {
		return postMessageDelayed(msg, 0);
	}
	
	/**
	 * Enqueue a message into the message queue after all pending messages
     * before (current time + delayMillis). You will receive it in
     * {@link #handleMessage}, in the thread attached to this handler.
	 * 
	 * @param msg The message to be pushed onto the message queue.
	 * @param delayMillis Milliseconds to be delayed to push the message.
	 * 
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.  Note that a
     *         result of true does not mean the message will be processed -- if
     *         the looper is quit before the delivery time of the message
     *         occurs then the message will be dropped.
	 */
	public boolean postMessageDelayed(final Message msg, long delayMillis) {
		if (delayMillis < 0) {
			delayMillis = 0;
		}
		return postMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
	}
	
	/**
	 * Enqueue a message into the message queue after all pending messages
     * before the absolute time (in milliseconds) <var>uptimeMillis</var>.
     * <b>The time-base is {@link SystemClock#uptimeMillis}.</b>
     * You will receive it in {@link #handleMessage}, in the thread attached
     * to this handler.
	 * 
	 * @param msg The message to be pushed onto the message queue.
	 * @param uptimeMillis The absolute time at which the message should be
     *         delivered, using the
     *         {@link SystemClock#uptimeMillis} time-base.
	 * 
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.  Note that a
     *         result of true does not mean the message will be processed -- if
     *         the looper is quit before the delivery time of the message
     *         occurs then the message will be dropped.
	 */
	public boolean postMessageAtTime(final Message msg, long uptimeMillis) {
		return postMessageAtTime(msg, null, uptimeMillis);
	}
	
	/**
	 * Enqueue a message into the message queue after all pending messages
     * before the absolute time (in milliseconds) <var>uptimeMillis</var>.
     * <b>The time-base is {@link SystemClock#uptimeMillis}.</b>
     * You will receive it in {@link #handleMessage}, in the thread attached
     * to this handler.
	 * 
	 * @param msg The message to be pushed onto the message queue.
	 * @param token Object.
	 * @param uptimeMillis uptimeMillis The absolute time at which the message should be
     *         delivered, using the
     *         {@link SystemClock#uptimeMillis} time-base.
     *         
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.  Note that a
     *         result of true does not mean the message will be processed -- if
     *         the looper is quit before the delivery time of the message
     *         occurs then the message will be dropped.
	 */
	public boolean postMessageAtTime(final Message msg, Object token, long uptimeMillis) {
		if (msg != null && messageReceiver != null) {
			if (sMessageHandler == null) {
				if (messageReceiver.getLooper() == null) {
					return false;
				}
				sMessageHandler = new Handler(messageReceiver.getLooper());
			}
			
			Message mMessage = Message.obtain(sMessageHandler, new Runnable() {
				
				@Override
				public void run() {
					dispatchMessage(msg);
				}
			});
			mMessage.what = msg.what;
			mMessage.arg1 = msg.arg1;
			mMessage.arg2 = msg.arg2;
			mMessage.obj = token;
			mMessage.replyTo = msg.replyTo;
			if (msg.getData() != null) {
	            mMessage.setData(new Bundle(msg.getData()));
	        }
			return sMessageHandler.sendMessageAtTime(mMessage, uptimeMillis);
		}
		return false;
	}
	
	/**
	 * Enqueue a message at the front of the message queue, to be processed on
     * the next iteration of the message loop.  You will receive it in
     * {@link #handleMessage}, in the thread attached to this handler.
     * <b>This method is only for use in very special circumstances -- it
     * can easily starve the message queue, cause ordering problems, or have
     * other unexpected side-effects.</b>
	 * 
	 * @param msg The message to be pushed onto the front of the message queue.
	 * 
	 * @return Returns true if the message was successfully placed in to the 
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
	 */
	public boolean postMessageAtFrontOfQueue(final Message msg) {
		if (msg != null && messageReceiver != null) {
			if (sMessageHandler == null) {
				if (messageReceiver.getLooper() == null) {
					return false;
				}
				sMessageHandler = new Handler(messageReceiver.getLooper());
			}
			
			Message mMessage = Message.obtain(sMessageHandler, new Runnable() {
				
				@Override
				public void run() {
					dispatchMessage(msg);
				}
			});
			mMessage.what = msg.what;
			mMessage.arg1 = msg.arg1;
			mMessage.arg2 = msg.arg2;
			mMessage.obj = msg.obj;
			mMessage.replyTo = msg.replyTo;
			if (msg.getData() != null) {
	            mMessage.setData(new Bundle(msg.getData()));
	        }
			return sMessageHandler.sendMessageAtFrontOfQueue(mMessage);
		}
		return false;
	}
	
	/**
	 * Remove any pending posts of messages with code 'what' that are in the
     * message queue.
	 * 
	 * @param what To identify which message to be removed.
	 * 
	 * @return Returns true if the pending post message was successfully
	 *         removed out from the message queue. Returns false on failure,
	 *         usually because the message is already enqueue in the message
	 *         queue.
	 */
	public boolean removeMessages(int what) {
		if (sMessageHandler != null && sMessageHandler.hasMessages(what)) {
			sMessageHandler.removeMessages(what);
			return true;
		}
		return false;
	}
	
	/**
	 * Remove any pending posts of messages with code 'what' that are in the
     * message queue.
	 * 
	 * @param what To identify which message to be removed.
	 * @param token Object.
	 * 
	 * @return Returns true if the pending post message was successfully
	 *         removed out from the message queue. Returns false on failure,
	 *         usually because the message is already enqueue in the message
	 *         queue.
	 */
	public boolean removeMessages(int what, Object token) {
		if (sMessageHandler != null && sMessageHandler.hasMessages(what, token)) {
			sMessageHandler.removeMessages(what, token);
			return true;
		}
		return false;
	}
	
	/**
	 * Handle the message. Put them to be handled on background or foreground.
	 * 
	 * @param msg The message to be handled.
	 */
	private void dispatchMessage(final Message msg) {
		synchronized (sMessageListSync) {
			if (messageMappingList == null) {
//				throw new NullPointerException("mMessageList is null");
				return;
			}
			if (messageMappingList.size() == 0) {
//				throw new RuntimeException("mMessageList is empty, please set message process before post message");
				return;
			}
			
			Map<Activity, List<Object>> activityPerformMap = messageMappingList.get(msg.what);
			if (activityPerformMap != null) {
				for (Activity activity : activityPerformMap.keySet()) {
					List<Object> list = activityPerformMap.get(activity);
					final PerformOnBackground pob = (PerformOnBackground) list.get(0);
					final PerformOnForeground pof = (PerformOnForeground) list.get(1);
					
					if (pob != null) {
						if (activity == null || (activity != null && !activity.isFinishing())) {
							mWorkerThreadPool.submit(new Runnable() {
								
								@Override
								public void run() {
									pob.doInBackground(msg);
								}
							});
						}
					}
					
					if (pof != null && activity != null && !activity.isFinishing()) {
						activity.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								pof.doInForeground(msg);
							}
						});
					}
				}
			}
		}
	}
	
	/**
	 * Define the handler to handle the specified message in specified activity.
	 * 
	 * @param what what To identify which message to be handle.
	 * @param pob The handler to handle the message on background.
	 */
	public void setOnMessageProcess(int what, PerformOnBackground pob) {
		setOnMessageProcess(what, null, pob);
	}
	
	/**
	 * Define the handler to handle the specified message in specified activity.
	 * 
	 * @param what To identify which message to be handle.
	 * @param activity To identify in which activity to handle the message.
	 * @param pob The handler to handle the message on background.
	 */
	public void setOnMessageProcess(int what, Activity activity, PerformOnBackground pob) {
		setOnMessageProcess(what, activity, pob, null);
	}
	
	/**
	 * Define the handler to handle the specified message in specified activity.
	 * 
	 * @param what To identify which message to be handle.
	 * @param activity To identify in which activity to handle the message.
	 * @param pof The handler to handle the message on main thread.
	 */
	public void setOnMessageProcess(int what, Activity activity, PerformOnForeground pof) {
		setOnMessageProcess(what, activity, null, pof);
	}
	
	/**
	 * Define the handler to handle the specified message in specified activity.
	 * 
	 * @param what To identify which message to be handle.
	 * @param activity To identify in which activity to handle the message.
	 * @param pob The handler to handle the message on background.
	 * @param pof The handler to handle the message on main thread.
	 */
	public void setOnMessageProcess(int what, Activity activity, PerformOnBackground pob, PerformOnForeground pof) {
		List<Object> performList = new ArrayList<Object>();
		Map<Activity, List<Object>> activityPerformMap = messageMappingList.get(what);
		
		if (performList.add(pob)) {
			if (performList.add(pof)) {
				if (activityPerformMap == null) {
					activityPerformMap = new HashMap<Activity, List<Object>>();
				}
				activityPerformMap.put(activity, performList);
				synchronized (sMessageListSync) {
					messageMappingList.put(what, activityPerformMap);
				}
			}
		}
//		Log.d(TAG, "messageMappingList.size = "+messageMappingList.size());
	}
	
	/**
	 * Obtain the instance of {@link #MessageProcessor}.
	 * 
	 * @return Returns the MessageProcessor object.
	 */
	public synchronized static MessageProcessor obtain() {
		if (sInstance == null) {
			sInstance = new MessageProcessor();
		}
		return sInstance;
	}
	
	/**
	 * Remove the finishing activity from the message map.
	 */
	private void recycleMessageMap() {
		int index;
		synchronized (sMessageListSync) {
			for (index = 0; index < messageMappingList.size(); index++) {
				int what = messageMappingList.keyAt(index);
				Map<Activity, List<Object>> activityPerformMap = messageMappingList.get(what);
				if (activityPerformMap != null) {
					List<Activity> removeItems = new ArrayList<Activity>();
					for (Activity activity : activityPerformMap.keySet()) {
						if (activity != null && activity.isFinishing()) {
							removeItems.add(activity);
						}
					}
					
					if (removeItems.size() != 0) {
						for (int i = 0; i < removeItems.size(); i++) {
							activityPerformMap.remove(removeItems.get(i));
						}
					}
					
					if (activityPerformMap.size() == 0) {
						messageMappingList.remove(what);
						--index;
					}
				}
			}
		}
	}

	/**
	 * Remove the special activity from the message map.
	 */
	public void recycleMessageMap(Activity activity) {
		int index;
		synchronized (sMessageListSync) {
			for (index = 0; index < messageMappingList.size(); index++) {
				int what = messageMappingList.keyAt(index);
				Map<Activity, List<Object>> activityPerformMap = messageMappingList.get(what);
				if (activityPerformMap != null) {
					activityPerformMap.remove(activity);
					if (activityPerformMap.size() == 0) {
						messageMappingList.remove(what);
					}
				}
			}
		}
	}
	
	/**
	 * Remove the finishing activity from the message map.<br>
	 * <b>CN</b>:只会回收那些已经结束的activity的callback，那些没有指定activity的后台callback无法回收．<br>
	 *  
	 */
	public void recycle() {
		if (sInstance != null) {
			recycleMessageMap();
		}
	}
	
	/**
	 * To quit the looper thread and thread pool in {@link #MessageProcessor}.
	 */
	public static void destroy() {
		if (sInstance != null) {
			sInstance.quitThread();
			synchronized (sMessageListSync) {
				sInstance.messageMappingList.clear();
				sInstance.messageMappingList = null;
			}
			sInstance = null;
		}
	}
	
	/**
	 * Quit the looper thread and thread pool.
	 */
	private void quitThread() {
		mWorkerThreadPool.shutdown();
		messageReceiver.quit();
		messageReceiver = null;
		sMessageHandler = null;
	}
	
	/**
	 * <b>EN</b>:remove activity which has register message callback <br>
	 * <b>CN</b>:移除注册过消息回调的activity, 避免内存泄漏<br> 
	 * 如果目标activity是null, 将会移除那些没有指定activity的后台callback
	 * 
	 * @param target
	 *            CN:目标activity, EN:target activity
	 */
	public void removeProcessCallback(Activity target) {
		int index;
		synchronized (sMessageListSync) {
			for (index = 0; index < messageMappingList.size(); index++) {
				int what = messageMappingList.keyAt(index);
				Map<Activity, List<Object>> activityPerformMap = messageMappingList.get(what);
				if (activityPerformMap != null) {
					List<Activity> removeItems = new ArrayList<Activity>();
					if (target != null) {
						for (Activity activity : activityPerformMap.keySet()) {
							if (activity != null && activity.equals(target)) {
								removeItems.add(activity);
							}
						}
					} else {
						removeItems.add(target);
					}

					for (int i = 0; i < removeItems.size(); i++) {
						activityPerformMap.remove(removeItems.get(i));
					}

					if (activityPerformMap.size() == 0) {
						messageMappingList.remove(what);
						--index;
					}
				} else {
					messageMappingList.remove(what);
					--index;
				}
			}
		}
	}

	/**
	 * <b>EN</b>:remove target message callback which has register target activity<br>
	 * <b>CN</b>:移除指定activity中指定的消息回调<br>
	 * 如果目标activity是null, 将会移除那些没有指定activity的后台callback<br>
	 * 
	 * @param target
	 *            CN:目标activity, EN:target activity
	 * @param what
	 *            CN:目标消息, EN:target message
	 */
	public void removeProcessCallback(Activity target, int what) {
		int index;
		synchronized (sMessageListSync) {
			for (index = 0; index < messageMappingList.size(); index++) {
				if (messageMappingList.keyAt(index) == what) {
					Map<Activity, List<Object>> activityPerformMap = messageMappingList.get(what);
					if (activityPerformMap != null) {
						List<Activity> removeItems = new ArrayList<Activity>();
						if (target != null) {
							for (Activity activity : activityPerformMap.keySet()) {
								if (activity != null && activity.equals(target)) {
									removeItems.add(activity);
								}
							}
						} else {
							removeItems.add(target);
						}

						for (int i = 0; i < removeItems.size(); i++) {
							activityPerformMap.remove(removeItems.get(i));
						}

						if (activityPerformMap.size() == 0) {
							messageMappingList.remove(what);
							--index;
						}
					} else {
						messageMappingList.remove(what);
						--index;
					}
				}
			}
		}
	}
}
