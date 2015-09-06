#!/bin/bash
nohup sbt -mem 512 "run --irc" 2>&1 >/dev/null &
