output "host" {
  value = "${vmware_virtual_machine.vm.ip_address}"
}

output "username" {
  value = "${var.bootstrap-username-string}"
}