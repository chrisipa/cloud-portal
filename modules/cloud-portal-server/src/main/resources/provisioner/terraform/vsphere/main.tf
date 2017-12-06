provider "vmware" {
  vcenter_server = "${var.credentials-vcenter-hostname-string}"
  user = "${var.credentials-vcenter-username-string}"
  password = "${var.credentials-vcenter-password-string}"
  insecure_connection = "true"
  version = "1.2"
}

resource "vmware_virtual_machine" "vm" {

  name = "${var.vm-name-string}"
  image = "${var.vm-resource-pool-string}/${lookup(var.image-templates-map, var.vm-image-string)}"
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
    on_failure = "continue"              
  }

  provisioner "remote-exec" {
    inline = [
      "echo '${var.bootstrap-password-string}' | sudo -S bash /tmp/bootstrap.sh",
      "rm /tmp/bootstrap.sh"
    ]
    on_failure = "continue"
  }
}

output "host" {
  value = "${vmware_virtual_machine.vm.ip_address}"
}

output "username" {
  value = "${var.bootstrap-username-string}"
}