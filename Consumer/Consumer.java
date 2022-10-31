import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Consumer {
	private final static String QUEUE_NAME = "skiersQueue";
	private final static String RABBITMQ_URL = "localhost";
//	private final static String RABBITMQ_URL = "35.89.191.176";

	private final static Integer N_CONSUMER_THREAD = 100;
	private static ConcurrentHashMap<String,String> map;


	public static void main(String[] argv) throws Exception {
		map = new ConcurrentHashMap<>(N_CONSUMER_THREAD);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(RABBITMQ_URL);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		ThreadPoolExecutor executor =
			(ThreadPoolExecutor) Executors.newFixedThreadPool(N_CONSUMER_THREAD);

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//			System.out.println(" [x] Received '" + message + "'");
			executor.execute(new ConsumerThread(map,message));
		};

		channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
	}
}
