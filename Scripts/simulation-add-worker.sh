#!/bin/bash
nohup ./run-worker1.sh &
sleep 37
nohup ./run-worker2.sh &
