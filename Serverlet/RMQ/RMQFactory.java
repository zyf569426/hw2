package RMQ;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RMQFactory extends BasePooledObjectFactory<Channel> {

	private final Connection connection;

	public RMQFactory(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Channel create() throws Exception {
		return connection.createChannel();
	}

	@Override
	public PooledObject<Channel> wrap(Channel channel) {
		return new DefaultPooledObject<>(channel);
	}

}
