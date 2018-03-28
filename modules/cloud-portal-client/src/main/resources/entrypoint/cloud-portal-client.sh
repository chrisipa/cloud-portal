#!/bin/bash

# include client library
source "${CLIENT_HOME}/lib.sh"


####################
# SCRIPT EXECUTION #
####################

echo ""
echo "###########################"
echo "# Cloud Portal CLI Client #"
echo "###########################"
echo ""

# check for environment variables
if [ "$CLOUD_PORTAL_URL" == "" ]
then
    log "ERROR" "Please specifiy CLOUD_PORTAL_URL as environment variable!"
    exit -1
fi
if [ "$CLOUD_PORTAL_USERNAME" == "" ]
then
    log "ERROR" "Please specifiy CLOUD_PORTAL_USERNAME as environment variable!"
    exit -1
fi
if [ "$CLOUD_PORTAL_PASSWORD" == "" ]
then
    log "ERROR" "Please specifiy CLOUD_PORTAL_PASSWORD as environment variable!"
    exit -1
fi
if [ "$CLOUD_PORTAL_PROVIDER" == "" ]
then
    log "ERROR" "Please specifiy CLOUD_PORTAL_PROVIDER as environment variable!"
    exit -1
fi
if [ "$CLOUD_PORTAL_ACTION" == "" ]
then
    log "ERROR" "Please specifiy CLOUD_PORTAL_ACTION (e.g. vm-create, vm-destroy) as environment variable!"
    exit -1
fi

# show variable information if nothing is entered
if [ "$#" ==  "0" ]
then
    variables    
fi  

# login 
login 

# execute action
case "$CLOUD_PORTAL_ACTION" in
    vm-create)
        # create vm 
        apply "$@"
    ;; 
    vm-destroy)
        # destroy vm 
        destroy "$@"
    ;;
    *)
        log "ERROR" "The Cloud Portal action '$CLOUD_PORTAL_ACTION' was not found!"
    ;;
esac    

# logout 
logout