
import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import com.google.gson.Gson;

import java.io.*;



class Statistics{
    public static void main(String[] args){
        
        
        try {
            final Channel c = ConexionRabbitMQ.getChannel();
            System.out.println("CONEXION ESTABLECIDA");
            
            c.basicConsume(RabbitMQStuff.COLA_TIEMPOS, false, "Statistics", new DefaultConsumer(c){
                
                @Override
                public void handleDelivery(String consumetTAg, Envelope e, AMQP.BasicProperties p, byte[] body) throws IOException {
                    try {
                        File file = new File("/home/twcam/pls/statistics.csv");
                        boolean fileExists = file.exists();
                        FileWriter fw = new FileWriter(file, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter pw = new PrintWriter(bw);
                        if(!fileExists){
                            pw.println("Worker;Image;Creation Message;Reception Worker;Finalization Worker");
                        }                        
                        Gson gson = new Gson();
                        
                        long deliveryTag = e.getDeliveryTag();
                        
                        String cuerpo = new String(body);
                        //OBTENGO EL MENSAJE
                        System.out.println("RECIBIDO" + cuerpo);
                        JobCompletion jc = gson.fromJson(cuerpo, JobCompletion.class);
                        
                        pw.println(jc.getWorker() +";" + jc.getImage() + ";"+jc.getTsCreationMessage() + ";" + jc.getTsReceptionWorker() + ";" + jc.getTsFinalizationWorker());
                        
                        pw.close();
                        c.basicAck(deliveryTag, false);
                        
                    } catch (Exception e1) {
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
    
}

