#!/bin/bash

# ---------------------------------------------------------------
# Function for printing the usage of this script
# ---------------------------------------------------------------
function usage() {

    # print help text
    cat <<USAGE

Usage:
  $0 [Options] <Args>

Options:
  -u <username>         SSH username
  -h <hostname>         SSH hostname
  -v <variable file>    Variable file path
  -a <ssl ca file>      SSL ca file path
  -c <ssl cert file>    SSL cert file path
  -k <ssl key file>     SSL key file path
  -l <ldap cert file>   LDAP cert file path

Example:
  $0 -u my-username -h my.server.host -v vars.json -a ca.crt -c server.crt -k server.key -l ldap.crt 

USAGE

    # exit with error
    exit -1
}

# get command line args
while getopts u:h:v:a:c:k:l: opt
do
    case $opt in
        u)
            username="$OPTARG"
        ;;
        h)
            hostname="$OPTARG"
        ;;
        v)
            variableFilePath="$OPTARG"
        ;;   
        a)
            sslCaFilePath="$OPTARG"
        ;;   
        c)
            sslCertFilePath="$OPTARG"
        ;;   
        k)
            sslKeyFilePath="$OPTARG"
        ;;   
        l)
            ldapCertFilePath="$OPTARG"
        ;;
        \?)
            log "ERROR" "Invalid option: -$OPTARG"
            exit -1
        ;;
    esac
done

# check command line args
if ([ "$username" == "" ] || [ "$hostname" == "" ] || [ "$variableFilePath" == "" ] || [ "$sslCaFilePath" == "" ] || [ "$sslCertFilePath" == "" ] || [ "$sslKeyFilePath" == "" ] || [ "$ldapCertFilePath" == "" ])
then 
    # print usage
    usage
fi

# read password from command line
read -s -p "Password: " password

# execute ansible playbook
ansible-playbook -u "$username" -i "$hostname," --extra-vars "@$variableFilePath" --extra-vars "ssl_ca_file_path=$sslCaFilePath" --extra-vars "ssl_cert_file_path=$sslCertFilePath" --extra-vars "ssl_key_file_path=$sslKeyFilePath" --extra-vars "ldap_cert_file_path=$ldapCertFilePath" --extra-vars "ansible_sudo_pass=$password" -e "ansible_ssh_pass=$password" -e "ansible_python_interpreter=/usr/bin/python3" "playbook.yml" 