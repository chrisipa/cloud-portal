provider "vmware" {
  vcenter_server = "${var.credentials-vcenter-hostname-string}"
  user = "${var.credentials-vcenter-username-string}"
  password = "${var.credentials-vcenter-password-string}"
  insecure_connection = "true"
}

resource "vmware_virtual_machine" "vm" {

  name = "${var.vm-name-string}"
  image = "${var.vm-image-string}"
  cpus = "${var.vm-vcores-string}"
  memory = "${var.vm-ram-string}"
  
  connection {
    type = "ssh"
    agent = false  
    host = "${vmware_virtual_machine.vm.ip_address}"
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
      "bash /tmp/bootstrap.sh",
      "rm /tmp/bootstrap.sh"
    ]
  }
}

output "host" {
  value = "${vmware_virtual_machine.vm.ip_address}"
}

output "username" {
  value = "${var.bootstrap-username-string}"
}