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
}

output "address" {
  value = "${vmware_virtual_machine.vm.ip_address}"
}