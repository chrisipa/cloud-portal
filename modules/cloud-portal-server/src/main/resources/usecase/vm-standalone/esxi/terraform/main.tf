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
}

resource "null_resource" "vmprovisioning" {
  
  provisioner "local-exec" {
    when    = "create"
    command = "ansible-playbook -i '${var.esxi_hostname},' -e 'is_linux=${local.is_linux}' -e 'is_windows=${local.is_windows}' -e '@parameters.yml' apply.yml"
  } 
  
  provisioner "local-exec" {
    when    = "destroy"
    command = "ansible-playbook -i '${var.esxi_hostname},' -e '@parameters.yml' destroy.yml"
  }
}

data "local_file" "ipfile" {
    filename = "${path.module}/ip.txt"
    depends_on = ["null_resource.vmprovisioning"]
}

resource "null_resource" "userprovisioning" {
  
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

  depends_on = ["null_resource.vmprovisioning"]
}

resource "null_resource" "linuxprovisioning" {
  
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
      "bash '${local.linux_prepare_script_path}'",
      "bash '${local.linux_user_script_path}'",
      "bash '${local.linux_cleanup_script_path}'",
      "rm -rf ${local.linux_script_folder_path}"
    ]
  } 
  
  depends_on = ["null_resource.userprovisioning"]
}