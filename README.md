# image-processing-distributed-app

Ditributed app which process images. It has three nodes which implement the work.

- RabbitMQ
- Worker
- Server

The dynamic server is configured to listen on port 8080. The application accepts POST requests with the image to be processed and the type of operation to be performed. The image will be stored locally on this machine and a message will be generated that will be sent to the broker with the work that the worker must perform.

The worker's application consumes messages and performs the actions requested in the message. The image is obtained from the server to be processed in the worker. When the processing is finished and the image is uploaded, a message is sent to a queue to notify that the work is finished.


