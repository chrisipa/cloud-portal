output "id" {
  value = "${random_id.id.hex}"
}

output "host" {
  value = "${element(flatten(list(vmware_virtual_machine.linux.*.ip_address, vmware_virtual_machine.windows.*.ip_address)), 0)}"  
}

output "username" {
  value = "${var.username}"
}