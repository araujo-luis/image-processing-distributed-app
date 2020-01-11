#!/bin/bash
mvn clean package
scp target/worker-app-1.0.jar twcam@10.50.0.12:opencv/

