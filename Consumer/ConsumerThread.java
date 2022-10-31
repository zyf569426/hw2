import java.util.concurrent.ConcurrentHashMap;

public class ConsumerThread implements Runnable{
	private final ConcurrentHashMap<String,String> map;
	private final String message;

	public ConsumerThread(ConcurrentHashMap<String,String> map, String message) {
		this.map = map;
		this.message = message;
	}

	@Override
	public void run() {
		//message format: /skiers/8/seasons/2022/days/1/skiers/98776/body/{"time":357,"liftID":12}
//		System.out.println(message);
		String[] info = message.split("/");
		String skierID = info[7];
		map.put(skierID, message);
	}
}
