#!/bin/bash

# read ssh user
read -p "SSH Host: " hostname
read -p "SSH User: " user
read -p "Time Server: " timeServer

ansible-playbook -e "ansible_python_interpreter=/usr/bin/python3" -K -u "$user" -i "$hostname," --extra-vars "time_server=$timeServer" playbook.yml