provider "vmware" {
  vcenter_server = "${var.credentials-vcenter-hostname-string}"
  user = "${var.credentials-vcenter-username-string}"
  password = "${var.credentials-vcenter-password-string}"
  insecure_connection = "true"
  version = "1.2.0"
}

resource "vmware_virtual_machine" "ubuntu" {

  count = "${var.vm-image-string == "Ubuntu Server 16.04" ? 1 : 0}"
  name = "${var.vm-name-string}"
  image = "${var.credentials-vcenter-image-folder-string}/${lookup(var.image-templates-map, var.vm-image-string)}"
  folder = "${var.credentials-vcenter-target-folder-string}"
  cpus = "${var.vm-vcores-string}"
  memory = "${var.vm-ram-string}"
  
  connection {
    type = "ssh"
    agent = false  
    host = "${vmware_virtual_machine.ubuntu.ip_address}"
    user = "${var.bootstrap-username-string}" 
    password = "${var.bootstrap-password-string}"     
    timeout = "1m"      
  }
  
  provisioner "file" {
    source      = "${var.bootstrap-script-file}"
    destination = "/tmp/bootstrap.sh"  
  }

  provisioner "remote-exec" {
    inline = [
      "echo '${var.bootstrap-password-string}' | sudo -S bash /tmp/bootstrap.sh",
      "rm /tmp/bootstrap.sh"
    ]
  }
}

resource "vmware_virtual_machine" "windows" {

  count = "${var.vm-image-string == "Windows Server 2016" ? 1 : 0}"
  name = "${var.vm-name-string}"
  image = "${var.credentials-vcenter-image-folder-string}/${lookup(var.image-templates-map, var.vm-image-string)}"
  folder = "${var.credentials-vcenter-target-folder-string}"
  cpus = "${var.vm-vcores-string}"
  memory = "${var.vm-ram-string}"
  
  connection {
    type = "winrm"
    host = "${vmware_virtual_machine.windows.ip_address}"
    user = "${var.bootstrap-username-string}"
    password = "${var.bootstrap-password-string}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source = "${var.bootstrap-script-file}"
    destination = "C:\\bootstrap.ps1" 
  }  

  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File C:\\bootstrap.ps1",
      "del C:\\bootstrap.ps1"      
    ]
  }
}