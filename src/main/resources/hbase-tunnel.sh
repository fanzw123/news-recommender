#!/bin/sh

#lsof -i | grep LISTEN

##kill `ps ax | grep 'ssh \-f' | awk '{print $1}'`
#xmolnarm2
ssh -f -L 2181:147.175.146.112:2181 molnar@147.175.146.112 -N 
ssh -f -L 59848:147.175.146.112:59848 molnar@147.175.146.112 -N 
ssh -f -L 34063:147.175.146.112:34063 molnar@147.175.146.112 -N 
ssh -f -L 60030:147.175.146.112:60030 molnar@147.175.146.112 -N 
ssh -f -L 60010:147.175.146.112:60010 molnar@147.175.146.112 -N 
