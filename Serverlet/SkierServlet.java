import Models.LiftData;
import Models.LiftRide;
import RMQ.RMQFactory;
import RMQ.RMQPool;
import com.google.gson.Gson;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class SkierServlet extends HttpServlet {
    private static final int DAY_MIN = 1;
    private static final int DAY_MAX = 366;

    private final static String RMQ_URL = "localhost";
//    private final static String RMQ_URL = "35.89.191.176";

    private final static int MAX_CHANNEL_SIZE = 110;
    private final static String QUEUE_NAME = "skiersQueue";
    private RMQPool rmqPool;

    private Gson gson  = new Gson();


    @Override
    public void init() throws  ServletException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(RMQ_URL);
        RMQFactory rmqFactory;
        try {
            rmqFactory = new RMQFactory(connectionFactory.newConnection());
        } catch (Exception e) {
            throw new RuntimeException("Error: failed to create new connection." + e.toString());
        }
        this.rmqPool = new RMQPool(MAX_CHANNEL_SIZE, rmqFactory);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("incorrect parameters");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("200 It works!");
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        return urlPath.length == 8 && isNumeric(urlPath[1]) && urlPath[2].equals("seasons") &&
            isNumeric(urlPath[3]) && urlPath[3].length() == 4 && urlPath[4].equals("days") &&
            isNumeric(urlPath[5]) &&
            Integer.parseInt(urlPath[5]) >= DAY_MIN &&
            Integer.parseInt(urlPath[5]) <= DAY_MAX &&
            urlPath[6].equals("skiers") && isNumeric(urlPath[7]);
    }

    private boolean isNumeric(String s) {
        if(s == null || s.equals("")) {
            return false;
        }
        for(char ch : s.toCharArray()){
            if(!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().println("incorrect parameters");
        } else {
            StringBuilder sb = new StringBuilder();
            String s = request.getReader().readLine();
            while (s != null) {
                sb.append(s);
                s = request.getReader().readLine();
            }
            LiftData liftData = gson.fromJson(sb.toString(), LiftData.class);
            String payload = request.getPathInfo() + "/body/" + sb.toString();
            this.publish(payload);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getOutputStream().println(gson.toJson(liftData));
            response.getOutputStream().println("201 It works!");
            response.getOutputStream().flush();
        }
    }

    protected void publish(String msg) {
        Channel channel = this.rmqPool.poll();
        try {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error: failed to operation on channel." + e.toString());
        }
        this.rmqPool.offer(channel);
//        System.out.println("[x] Sent '" + msg + "'");
    }

}
