#!/bin/bash

set -e

#############
# VARIABLES #
#############

tmpFolder="/tmp/cloud-portal-client/$(date +'%s%N')"
cookieFile="$tmpFolder/cookie.txt"
outputFile="$tmpFolder/output.txt"


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
# Function for cleaning up temporary files
# --------------------------------------------------------
function cleanup() {

    if [ -d "$tmpFolder" ]
    then
        # logging
        log "INFO" "Removing tmp folder '$tmpFolder'"
    
        # remove temp folder
        rm -rf "$tmpFolder"
    fi 
}

# --------------------------------------------------------
# Function for failing the script
# --------------------------------------------------------
# $1 - Message to show
# --------------------------------------------------------
function fail() {

    # get parameters
    message="$1"

    # logging
    log "ERROR" "$message"

    # logout
    logout
        
    # exit with error
    exit -1        
}

# --------------------------------------------------------
# Function for creating a session in the cloud portal
# --------------------------------------------------------
function login() {

    # create temp folder
    mkdir -p "$tmpFolder"

    # get login url
    local loginUrl="$CLOUD_PORTAL_URL/login"
    
    # logging
    log "INFO" "Creating a session with username '$CLOUD_PORTAL_USERNAME' and login url '$loginUrl'"
    
    # create session and save to cookie file
    redirectUrl=$(curl -N -w "%{redirect_url}" -s -X POST -c "$cookieFile" -F "username=$CLOUD_PORTAL_USERNAME" -F "password=$CLOUD_PORTAL_PASSWORD" "$loginUrl")
    
    # check if login failed
    if [[ "$redirectUrl" == *error ]]
    then
        fail "Login failed. Try with other credentials"
    fi 
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
    curl -N -s -X GET -b "$cookieFile" "$logoutUrl" > "$outputFile" >> /dev/null
    
    # cleanup
    cleanup
}

# --------------------------------------------------------
# Function for creating a use case for a cloud platform
# --------------------------------------------------------
function create() {

    # get create url
    local createUrl="$CLOUD_PORTAL_URL/usecase/create/action/$CLOUD_PORTAL_ACTION"
    
    # logging
    log "INFO" "Creating cloud portal use case '$CLOUD_PORTAL_USE_CASE' and url '$createUrl'"
    
    # create use case for cloud provider
    curl -N -w "\nstatus_code = %{http_code}" -s -X POST -b "$cookieFile" -F "id=$CLOUD_PORTAL_USE_CASE" "$@" "$createUrl" | tee "$outputFile"      
    
    # check if status code is valid
    statusCode=$(getVariableFromOutput "status_code")
    if [ "$statusCode" != "200" ]
    then
        fail "Invalid parameters. Try with other values." 
    fi     
}

# --------------------------------------------------------
# Function for destroying a use case for a cloud platform
# --------------------------------------------------------
# $1 - Provisioning ID
# --------------------------------------------------------
function destroy() {

    # read parameters
    local id="$1"
    
    # get destroy url
    local destroyUrl="$CLOUD_PORTAL_URL/usecase/destroy/action/$CLOUD_PORTAL_USE_CASE/$id"
    
    # logging
    log "INFO" "Destroying cloud portal use case '$CLOUD_PORTAL_USE_CASE' with id '$id'"
    
    # destroy use case for cloud provider
    curl -N -w "\nstatus_code = %{http_code}" -s -X GET -b "$cookieFile" "$destroyUrl" | tee "$outputFile"
    
    # check if status code is valid
    statusCode=$(getVariableFromOutput "status_code")
    if [ "$statusCode" != "200" ]
    then
        fail "Invalid parameters. Try with other values." 
    fi   
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
# Function for getting a list of possible variables
# --------------------------------------------------------
function variables() {

    # login
    login

    # get variables url
    local variablesUrl="$CLOUD_PORTAL_URL/usecase/variables/$CLOUD_PORTAL_USE_CASE"

    # logging
    log "INFO" "Getting variable information from url '$variablesUrl'"

    # get usage 
    curl -N -X GET -b "$cookieFile" "$variablesUrl"

    # logout
    login
    
    # stop
    exit 0
} 