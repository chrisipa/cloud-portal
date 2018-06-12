provider "null" {
  version = "1.0.0"
}

locals {
  is_linux = "${replace(var.image_name, "Linux", "") != var.image_name ? 1 : 0}"
  linux_temp_folder_path = "/tmp"
  linux_script_folder_name = "linux_scripts"
  linux_script_folder_path = "${local.linux_temp_folder_path}/${local.linux_script_folder_name}"
  linux_prepare_script_path = "${local.linux_script_folder_path}/prepare.sh"
  linux_user_script_path = "${local.linux_script_folder_path}/user.sh"
  linux_cleanup_script_path = "${local.linux_script_folder_path}/cleanup.sh"
  
  is_windows = "${replace(var.image_name, "Windows", "") != var.image_name ? 1 : 0}"
  windows_temp_folder_path = "C:\\"
  windows_script_folder_name = "windows_scripts"      
  windows_script_folder_path = "${local.windows_temp_folder_path}\\${local.windows_script_folder_name}"
  windows_prepare_script_path = "${local.windows_script_folder_path}\\prepare.ps1"
  windows_user_script_path = "${local.windows_script_folder_path}\\user.ps1"
  windows_cleanup_script_path = "${local.windows_script_folder_path}\\cleanup.ps1"
  
  vm_template_path = "${lookup(local.image_templates_map, var.image_name)}"
  vm_guest_id = "${lookup(local.image_guest_id_map, var.image_name)}"
  vm_notes = "Title: ${var.title}\\nDescription: ${var.description}\\nCreationDate: ${var.creation_date}\\nOwnedBy: ${var.owner}\\nOwnerGroup: ${var.group}\\nProvisioningSystem: ${var.application_url}"
}

resource "null_resource" "vm_provisioning" {
  
  provisioner "local-exec" {
    when    = "create"
    command = "ansible-playbook -i \"${var.esxi_hostname},\" -e \"vm_guest_id='${local.vm_guest_id}'\" -e \"vm_template_path='${local.vm_template_path}'\" -e \"vm_notes='${local.vm_notes}'\" -e \"@parameters.yml\" apply.yml"
  } 
  
  provisioner "local-exec" {
    when    = "destroy"
    command = "ansible-playbook -i \"${var.esxi_hostname},\" -e \"@parameters.yml\" destroy.yml"
  }
}

data "local_file" "ipfile" {
    filename = "${path.module}/ip.txt"
    depends_on = ["null_resource.vm_provisioning"]
}

resource "null_resource" "linux_user_creation" {
  
  count = "${local.is_linux}"
  
  connection {
    type = "ssh"
    agent = false  
    host = "${data.local_file.ipfile.content}"
    user = "${local.linux_default_username}" 
    password = "${local.linux_default_password}"     
    timeout = "1m"      
  }
  
  provisioner "remote-exec" {
    inline = [
      "echo '${local.linux_default_password}' | sudo -S echo test",
      "sudo apt-get update",
      "sudo apt-get install -y whois",
      "sudo useradd -p \"$(mkpasswd --hash=md5 ${var.password})\" -s '/bin/bash' '${var.username}'",
      "sudo usermod -aG sudo '${var.username}'",
      "sudo mkdir -p '/home/${var.username}/.ssh'",
      "sudo bash -c \"echo '${file(var.public_key_file)}' >> '/home/${var.username}/.ssh/authorized_keys'\"",
      "sudo chown -R '${var.username}.${var.username}' '/home/${var.username}'"
    ]
  }  

  depends_on = ["null_resource.vm_provisioning"]
}

resource "null_resource" "linux_bootstrapping" {
  
  count = "${local.is_linux}"
  
  connection {
    type = "ssh"
    agent = false  
    host = "${data.local_file.ipfile.content}"
    user = "${var.username}" 
    password = "${var.password}"     
    timeout = "1m"      
  }

  provisioner "file" {
    source      = "${local.linux_script_folder_name}"
    destination = "${local.linux_temp_folder_path}"  
  } 

  provisioner "file" {
    source      = "${var.script_file}"
    destination = "${local.linux_user_script_path}"  
  }
  
  provisioner "remote-exec" {
    inline = [
      "echo '${var.password}' | sudo -S echo test",
      "bash '${local.linux_prepare_script_path}' '${var.random_id}'",
      "bash '${local.linux_user_script_path}'",
      "bash '${local.linux_cleanup_script_path}'",
      "rm -rf ${local.linux_script_folder_path}"
    ]
  } 
  
  depends_on = ["null_resource.linux_user_creation"]
}

resource "null_resource" "windows_user_creation" {
  
  count = "${local.is_windows}"
  
  connection {
    type = "winrm"
    host = "${data.local_file.ipfile.content}"
    user = "${local.windows_default_username}" 
    password = "${local.windows_default_password}"          
    timeout = "10m"      
  }
  
  provisioner "remote-exec" {
    inline = [
      "NET USER ${var.username} ${var.password} /add /y /expires:never",
      "NET LOCALGROUP Administrators ${var.username} /add",
      "WMIC USERACCOUNT WHERE \"Name='${var.username}'\" SET PasswordExpires=FALSE"
    ]
  }  

  depends_on = ["null_resource.vm_provisioning"]
}

resource "null_resource" "windows_bootstrapping" {
  
  count = "${local.is_windows}"
  
  connection {
    type = "winrm"
    host = "${data.local_file.ipfile.content}"
    user = "${var.username}" 
    password = "${var.password}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source      = "${local.windows_script_folder_name}"
    destination = "${local.windows_script_folder_path}"  
  }

  provisioner "file" {
    source = "${var.script_file}"
    destination = "${local.windows_user_script_path}" 
  } 
  
  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_prepare_script_path} ${var.random_id}",      
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_user_script_path}",
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_cleanup_script_path}",
      "Powershell.exe -ExecutionPolicy Unrestricted -Command Remove-Item ${local.windows_script_folder_path} -Force -Recurse"      
    ]
  } 
  
  depends_on = ["null_resource.windows_user_creation"]
}