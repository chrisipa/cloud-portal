#!/bin/bash

# include library
source "$(dirname $(readlink -f $0))/lib.sh"

# login 
login 

# create vm 
apply "$@"

# get variables from output
provisioningId="$(getVariableFromOutput 'provisioning_id')"

# destroy vm if user decides
if confirm "Do you want to remove this VM?"
then
    # destroy vm 
    destroy "$provisioningId"
fi

# logout 
logout