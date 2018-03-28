#!/bin/bash

# include client library
source "${CLIENT_HOME}/lib.sh"

# login 
login 

# create vm 
apply "$@"

# logout 
logout