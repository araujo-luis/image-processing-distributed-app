#!/bin/bash
ssh twcam@10.50.0.13 /home/twcam/.sdkman/candidates/java/current/bin/java -Djava.library.path=/home/twcam/opencv/. -classpath /home/twcam/opencv/opencv-2413.jar:/home/twcam/opencv/worker-app-1.0.jar:. AppOpenCV WORKER2
