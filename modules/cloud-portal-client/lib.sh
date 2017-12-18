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

# --------------------------------------------------------
# Function for creating a session in the cloud portal
# --------------------------------------------------------
# $1 - Username
# $2 - Password
# --------------------------------------------------------
function login() {

    # read parameters
    local username="$1"
    local password="$2"
    
    # get login url
    local loginUrl="$baseUrl/login"
    
    # logging
    log "INFO" "Creating a session with username '$username' and login url '$loginUrl'"
    
    # create session and save to cookie file
    curl -s -X POST -c "$cookieFile" -F "username=$username" -F "password=$password" "$loginUrl" >> /dev/null
}

# --------------------------------------------------------
# Function for destroying a session in the cloud portal
# --------------------------------------------------------
function logout() {

    # get logout url
    local logoutUrl="$baseUrl/logout"

    # logging
    log "INFO" "Destroying a session with logout url '$logoutUrl'"

    # destroy session
    curl -s -X GET -b "$cookieFile" "$logoutUrl" >> /dev/null
}

# --------------------------------------------------------
# Function for creating a vm for a cloud platform
# --------------------------------------------------------
# $1 - Provider (possible values: aws, azure, vsphere)
# --------------------------------------------------------
function apply() {

    # read parameters
    local provider="$1"
    
    # get apply url
    local applyUrl="$baseUrl/vm/create/action/apply"
    
    # logging
    log "INFO" "Creating virtual machine for cloud provider '$provider' and apply url '$applyUrl'"
    
    # create virtual machine for cloud provider
    curl -s -X POST -b "$cookieFile" -F "provider=$provider" "$applyUrl" | tee "$outputFile"            
}

# --------------------------------------------------------
# Function for destroying a vm for a cloud platform
# --------------------------------------------------------
# $1 - Provider (possible values: aws, azure, vsphere)
# --------------------------------------------------------
function destroy() {

    # read parameters
    local provider="$1"
    local id="$2"
    
    # get destroy url
    local destroyUrl="$baseUrl/vm/delete/action/$provider/$id"
    
    # logging
    log "INFO" "Destroying virtual machine for cloud provider '$provider' and id '$id'"
    
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
    local privateKeyUrl="$baseUrl/provision-log/private-key/$id"
    
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

# get base url from environment variables
if [ "$CLOUD_PORTAL_URL" != "" ]
then
    baseUrl="$CLOUD_PORTAL_URL"
else
    read -p "Base URL: " baseUrl     
fi

# get provider from environment variables
if [ "$CLOUD_PORTAL_PROVIDER" != "" ]
then
    provider="$CLOUD_PORTAL_PROVIDER"
else
    read -p "Provider: " provider 
fi

# get username from environment variables
if [ "$CLOUD_PORTAL_USERNAME" != "" ]
then
    username="$CLOUD_PORTAL_USERNAME"
else
    read -p "Username: " username    
fi

# get password from environment variables
if [ "$CLOUD_PORTAL_PASSWORD" != "" ]
then
    password="$CLOUD_PORTAL_PASSWORD"
else
    read -s -p "Password: " password
    echo ""        
fi