package RMQ;

import com.rabbitmq.client.Channel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RMQPool {
	private final BlockingQueue<Channel> pool;
	private final RMQFactory factory;

	public RMQPool(int maxSize, RMQFactory factory) {
		this.factory = factory;
		pool = new LinkedBlockingQueue<>(maxSize);
		for (int i = 0; i < maxSize; i++) {
			Channel channel;
			try {
				channel = this.factory.create();
				pool.put(channel);
			} catch (Exception e) {
				throw new RuntimeException("Error: RMQ pool create failed");
			}
		}
	}

	public Channel poll() {
		try {
			return pool.take();
		} catch (Exception e) {
			throw new RuntimeException("Error: Channel poll failed");
		}
	}

	public void offer(Channel channel) {
		if (channel != null) {
			pool.add(channel);
		}
	}
}
