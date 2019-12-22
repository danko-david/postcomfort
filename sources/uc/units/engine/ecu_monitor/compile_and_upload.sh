#!/bin/bash
set -e
compile_ub_app.sh atmega328p 16000000 ecu_monitor ecu_monitor.c
ub upload -t $1 -c ecu_monitor.hex
