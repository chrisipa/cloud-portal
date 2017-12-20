#!/bin/bash

set -e

#############
# VARIABLES #
#############

tmpFolder="/tmp"
cookieFile="$tmpFolder/cloud-portal-cookie.txt"
outputFile="$tmpFolder/cloud-portal-output.txt"
privateKeyFile="$tmpFolder/cloud-portal-private-key.txt"


#############
# FUNCTIONS #
#############

# --------------------------------------------------------
# Function for creating log entries on the console
# --------------------------------------------------------
# $1 - Log level
# $2 - Log text
# --------------------------------------------------------
function log() {

    # read parameters
    local level="$1"
    local text="$2"

    # create log message
    local now=$(date +"%d-%m-%Y %H:%M:%S")
    echo -e "\n$now [$level] $text\n"
}

# ---------------------------------------------------------------
# Function for confirming a message on the console
# ---------------------------------------------------------------
# $1 - Confirm message to show
# ---------------------------------------------------------------
function confirm() {

    # read parameters
    local question="$1"

    echo ""
    read -r -p "$question [Y/n] " answer
    echo ""

    case "$answer" in
        [yY][eE][sS]|[yY])
            return 0
        ;;
        *)
            return 1
        ;;
    esac
}

# --------------------------------------------------------
# Function for creating a session in the cloud portal
# --------------------------------------------------------
function login() {

    # get login url
    local loginUrl="$CLOUD_PORTAL_URL/login"
    
    # logging
    log "INFO" "Creating a session with username '$CLOUD_PORTAL_USERNAME' and login url '$loginUrl'"
    
    # create session and save to cookie file
    curl -s -X POST -c "$cookieFile" -F "username=$CLOUD_PORTAL_USERNAME" -F "password=$CLOUD_PORTAL_PASSWORD" "$loginUrl" >> /dev/null
}

# --------------------------------------------------------
# Function for destroying a session in the cloud portal
# --------------------------------------------------------
function logout() {

    # get logout url
    local logoutUrl="$CLOUD_PORTAL_URL/logout"

    # logging
    log "INFO" "Destroying a session with logout url '$logoutUrl'"

    # destroy session
    curl -s -X GET -b "$cookieFile" "$logoutUrl" >> /dev/null
}

# --------------------------------------------------------
# Function for creating a vm for a cloud platform
# --------------------------------------------------------
function apply() {

    # get apply url
    local applyUrl="$CLOUD_PORTAL_URL/vm/create/action/apply"
    
    # logging
    log "INFO" "Creating virtual machine for cloud provider '$CLOUD_PORTAL_PROVIDER' and apply url '$applyUrl'"
    
    # create virtual machine for cloud provider
    curl -s -X POST -b "$cookieFile" -F "provider=$CLOUD_PORTAL_PROVIDER" "$@" "$applyUrl" | tee "$outputFile"            
}

# --------------------------------------------------------
# Function for destroying a vm for a cloud platform
# --------------------------------------------------------
# $1 - Provisioning ID
# --------------------------------------------------------
function destroy() {

    # read parameters
    local id="$1"
    
    # get destroy url
    local destroyUrl="$CLOUD_PORTAL_URL/vm/delete/action/$CLOUD_PORTAL_PROVIDER/$id"
    
    # logging
    log "INFO" "Destroying virtual machine for cloud provider '$CLOUD_PORTAL_PROVIDER' and id '$id'"
    
    # destroy virtual machine for cloud provider
    curl -X GET -b "$cookieFile" "$destroyUrl"  
}

# --------------------------------------------------------
# Function for getting a variable from the output
# --------------------------------------------------------
# $1 - Variable Name
# --------------------------------------------------------
function getVariableFromOutput() {

    # read parameters
    local variableName="$1"

    # get variable value from output file
    local variableValue="$(cat "$outputFile" | grep "$variableName = " | awk -F ' = ' '{print $2}')"
    
    # return variable value
    echo "$variableValue"
}

# --------------------------------------------------------
# Function for executing a command on a server
# --------------------------------------------------------
# $1 - Username
# $2 - Host
# $3 - Command
# $4 - Provisioning ID
# --------------------------------------------------------
function execute() {
    
    # read parameters
    local username="$1"
    local host="$2"
    local command="$3"
    local id="$4"
    
    # get private key url
    local privateKeyUrl="$CLOUD_PORTAL_URL/provision-log/private-key/$id"
    
    # download private key
    log "INFO" "Downloading private key file '$privateKeyUrl'"
    curl -s -X GET -b "$cookieFile" -o "$privateKeyFile" "$privateKeyUrl" >> /dev/null 
    
    # change file rights
    log "INFO" "Changing permissions of private key file '$privateKeyFile'"
    chmod 600 "$privateKeyFile"
    
    # execute command
    log "INFO" "Executing command '$command' on server '$host' with username '$username'"
    ssh -i "$privateKeyFile" -o StrictHostKeyChecking=no "$username"@"$host" "$command"    
}

# --------------------------------------------------------
# Function for cleaning up temporary files
# --------------------------------------------------------
# $1 - Username
# $2 - Host
# $3 - Command
# $4 - Provisioning ID
# --------------------------------------------------------
function cleanup() {

    # remove cookie file
    rm -f "$cookieFile"
    
    # remove output file
    rm -f "$outputFile"
    
    # remove private key file
    rm -f "$privateKeyFile"
}

####################
# SCRIPT EXECUTION #
####################

echo ""
echo "###########################"
echo "# Cloud Portal CLI Client #"
echo "###########################"
echo ""

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