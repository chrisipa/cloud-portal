#!/bin/bash

# include library
source "$(dirname $(readlink -f $0))/lib.sh"

# login 
login 

# create vm 
apply "$@"

# logout 
logout