#!/bin/bash
mvn clean package
cp target/twcam-app-1.0.jar /var/web/dynamic/
