
import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Consumer;
import com.google.gson.Gson;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui; 


class OpenCVUtils{
    public static Mat readFile(String fileName){
        Mat img = Highgui.imread(fileName);
        return img;
    }   
    
    public static void writeImage(Mat mat, String dest){
        Highgui.imwrite(dest, mat);
    }
    
    public static Mat blur(Mat input){
        Mat destImage = input.clone();
        Imgproc.blur(input, destImage, new Size(3.0, 3.0));
        return destImage;
    }
    
    public static Mat gray(Mat input){
        Mat gray = new Mat(input.rows(),input.cols(),CvType.CV_8UC1);
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
        return gray;
    }
    
    public static Mat canny(Mat input){
        Mat gray = gray(input);
        Imgproc.blur(gray, input, new Size(3, 3));
        int threshold=2;
        Imgproc.Canny(input, input, threshold, threshold * 3, 3, false);
        Mat dest = input.clone();
        Core.add(dest, Scalar.all(0), dest);
        dest.copyTo(dest, input);
        return dest;
    }
}

class AppOpenCV{
    public static void main(String[] args){
        
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); // Esto es para trabajar con OpenCV
        try {
            final String workerID = args[0];
            final Channel c = ConexionRabbitMQ.getChannel();
            System.out.println("CONEXION ESTABLECIDA "+ workerID);            
            c.basicQos(1);
            c.basicConsume(RabbitMQStuff.COLA_TRABAJOS, false, workerID, new DefaultConsumer(c){
                @Override
                public void handleDelivery(String consumetTAg, Envelope e, AMQP.BasicProperties p , byte[] body) throws IOException {
                    try {
                        long t0 = System.currentTimeMillis();
                        Gson gson = new Gson();
                        long deliveryTag = e.getDeliveryTag();
                        
                        String cuerpo = new String(body);
                        //OBTENGO EL MENSAJE
                        
                        ImageJob ij = gson.fromJson(cuerpo, ImageJob.class);
                        JobCompletion jc = new JobCompletion();
                        jc.setTsCreationMessage(ij.getTsCreationMessage());
                        jc.setTsReceptionWorker(t0);
                        jc.setWorker(workerID);
                        jc.setImage(ij.getImageSrc());
                        System.out.println(workerID +" imagen procesada: " + jc.getImage());
                        
                        String fullImagePath = ij.getPathSrc()+"/"+ij.getImageSrc();
                        
                        //SCP DE IMAGEN DE TWCAM
                        ProcessBuilder pb = new ProcessBuilder("scp", "twcam@" + RabbitMQStuff.HOST_TWCAM+":"+fullImagePath, ij.getPathSrc()+"/");
                        
                        Process process = pb.start();
                        process.waitFor();
                        
                        //PROCESAMIENTO DE IMAGEN
                        String action = ij.getAction();
                        Mat img;
                        if(action.equals("blur")){
                             img = OpenCVUtils.blur(OpenCVUtils.readFile(fullImagePath));
                        }else if(action.equals("gray")){
                            img = OpenCVUtils.gray(OpenCVUtils.readFile(fullImagePath));
                        }else{
                            img = OpenCVUtils.canny(OpenCVUtils.readFile(fullImagePath));
                        }
                        
                        OpenCVUtils.writeImage(img, "/home/twcam/images/"+ij.getImageSrc());
                        
                        ProcessBuilder pb2 = new ProcessBuilder("scp","/home/twcam/images/"+ij.getImageSrc(), "twcam@" + RabbitMQStuff.HOST_TWCAM+":/var/web/resources/images");
                        Process process2 = pb2.start();
                        process2.waitFor();

                        c.basicAck(deliveryTag, false);
                        jc.setTsFinalizationWorker(System.currentTimeMillis());
                        createTimeQueue(c, jc);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    
                }
            });             
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
       
    }

    public static void createTimeQueue(Channel c, JobCompletion jc){
		
        try {
            c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, true, false, false, null);
            c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS);
            Gson gson = new Gson();
            String json = gson.toJson(jc); 
            byte[] messageBody = json.getBytes();
            c.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS, null, messageBody);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
	}
}

