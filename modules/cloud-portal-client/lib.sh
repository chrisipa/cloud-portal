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
    redirectUrl=$(curl -w "%{redirect_url}" -s -X POST -c "$cookieFile" -F "username=$CLOUD_PORTAL_USERNAME" -F "password=$CLOUD_PORTAL_PASSWORD" "$loginUrl")
    
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
    curl -s -X GET -b "$cookieFile" "$logoutUrl" > "$outputFile" >> /dev/null
    
    # cleanup
    cleanup
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
    curl -w "\nstatus_code = %{http_code}" -s -X POST -b "$cookieFile" -F "provider=$CLOUD_PORTAL_PROVIDER" "$@" "$applyUrl" | tee "$outputFile"      
    
    # check if status code is valid
    statusCode=$(getVariableFromOutput "status_code")
    if [ "$statusCode" != "200" ]
    then
        fail "Invalid parameters. Try with other values." 
    fi     
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
    curl -w "\nstatus_code = %{http_code}" -s -X GET -b "$cookieFile" "$destroyUrl" | tee "$outputFile"
    
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
    local variablesUrl="$CLOUD_PORTAL_URL/vm/variables/$CLOUD_PORTAL_PROVIDER"

    # logging
    log "INFO" "Getting variable information from url '$variablesUrl'"

    # get usage 
    curl -X GET -b "$cookieFile" "$variablesUrl"

    # logout
    login
    
    # stop
    exit 0
}


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

# show variable information if nothing is entered
if [ "$#" ==  "0" ]
then
    variables    
fi    