provider "random" {
  version = "1.1.0"
}

provider "vmware" {
  vcenter_server = "${var.vcenter_hostname}"
  user = "${var.vcenter_username}"
  password = "${var.vcenter_password}"
  insecure_connection = "true"
  version = "1.2.0"
}

locals {
  is_linux = "${replace(var.image_name, "Linux", "") != var.image_name ? 1 : 0}"
  is_windows = "${replace(var.image_name, "Windows", "") != var.image_name ? 1 : 0}"
  linux_script_path = "/tmp/bootstrap.sh"
  windows_script_path = "C:\\bootstrap.ps1"  
}

resource "random_id" "id" {
  byte_length = 6
}

resource "vmware_virtual_machine" "linux" {

  count = "${local.is_linux}"
  name = "${random_id.id.hex}"
  image = "${var.vcenter_image_folder}/${lookup(var.image_templates_map, var.image_name)}"
  folder = "${var.vcenter_target_folder}"
  cpus = "${var.vm_cores}"
  memory = "${var.vm_ram}"
  
  connection {
    type = "ssh"
    agent = false  
    host = "${vmware_virtual_machine.linux.ip_address}"
    user = "${var.username}" 
    password = "${var.password}"     
    timeout = "1m"      
  }
  
  provisioner "file" {
    source      = "${var.script_file}"
    destination = "${local.linux_script_path}"  
  }

  provisioner "remote-exec" {
    inline = [
      "echo '${var.password}' | sudo -S bash ${local.linux_script_path}",
      "rm ${local.linux_script_path}"
    ]
  }
}

resource "vmware_virtual_machine" "windows" {

  count = "${local.is_windows}"
  name = "${random_id.id.hex}"
  image = "${var.vcenter_image_folder}/${lookup(var.image_templates_map, var.image_name)}"
  folder = "${var.vcenter_target_folder}"
  cpus = "${var.vm_cores}"
  memory = "${var.vm_ram}"
  
  connection {
    type = "winrm"
    host = "${vmware_virtual_machine.windows.ip_address}"
    user = "${var.username}"
    password = "${var.password}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source = "${var.script_file}"
    destination = "${local.windows_script_path}" 
  }  

  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_script_path}",
      "del ${local.windows_script_path}"      
    ]
  }
}