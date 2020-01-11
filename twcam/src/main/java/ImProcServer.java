import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import cs.edu.uv.http.dynamicresponse.ResponseClass;
import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;
import cs.edu.uv.http.dynamicresponse.MultipartUtils;

public class ImProcServer extends ResponseClass{
    // Directorio donde guardaremos las imágenes que nos envían
    private static final String PATH_SRC="/tmp/images/";
    // Directorio donde se deben guardar las imágenes procesadas
    private static final String PATH_DST="/var/web/resources/images";
	
    
    
    public ImProcServer(){}
    
    public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception{
        
        try {
			if (MultipartUtils.isMultipartFormData(req.getHeaders())) {
				MultipartUtils multipartUtils = new MultipartUtils(req);		
				
				// This is a HashMap to store params from multipart body
				HashMap<String,String> params = new HashMap<String,String>();
				// This is a HashMap to store info about files saved from multipart body
				HashMap<String,String> files = new HashMap<String,String>();
				// This is the path where files will be stored
				String path="/tmp";

				multipartUtils.parseMultipart(params, files, path);
	
				System.out.println(params.get("ACTION"));
				System.out.println(files.get("IMAGE"));
				Channel c = ConexionRabbitMQ.getChannel();

				System.out.println("CONEXION ESTABLECIDA");
				
				resp.flushResponseHeaders();
				

				PrintWriter pw = resp.getWriter();
				ImageJob ij = new ImageJob();
				ij.setTsCreationMessage(System.currentTimeMillis());
				pw.println("Message creation: "+ ij.getTsCreationMessage());
				ij.setPathSrc(path);
				
				ij.setImageSrc(files.get("IMAGE"));
				ij.setAction(params.get("ACTION"));
				ij.setPathDst("/var/web/resources");
				ij.setImageDst(files.get("IMAGE"));
				
				
                Gson gson = new Gson();
                String json = gson.toJson(ij);  
                
                // Usamos el canal para definir: el exchange, la cola y la asociación exchange-cola
                c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
                c.queueDeclare(RabbitMQStuff.COLA_TRABAJOS, true, false, false, null);
                c.queueBind(RabbitMQStuff.COLA_TRABAJOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS);
                
                // Obtención del cuerpo del mensaje
                byte[] messageBody = json.getBytes();
                // Publicar el mensaje con el trabajo a realizar
                c.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS, null, messageBody);
                
				pw.println("{\"status\":\"ok\"}");
				pw.flush();
				pw.close();
			}
		} catch (Exception ex) {
			resp.setStatus(500);
			resp.flushResponseHeaders();
			PrintWriter pw = resp.getWriter();
			pw.println("<html><body>");
			StringWriter sw = new StringWriter();
			PrintWriter pwt = new PrintWriter(sw);
			ex.printStackTrace(pwt);
			pw.println(sw.toString());
			pw.println("</body></html>");
			pw.flush();
			pw.close();
		}
        
        
	}
	
	
}
