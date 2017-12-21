#!/bin/bash

# include library
source "$(dirname $(readlink -f $0))/lib.sh"

# login 
login 

# destroy vm 
destroy "$@"

# logout 
logout