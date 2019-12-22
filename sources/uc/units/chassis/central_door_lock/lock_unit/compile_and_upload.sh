#!/bin/bash
set -e

compile_ub_app.sh atmega328p 16000000 lock_unit lock_unit.c
ub upload -t $1 -c lock_unit.hex
