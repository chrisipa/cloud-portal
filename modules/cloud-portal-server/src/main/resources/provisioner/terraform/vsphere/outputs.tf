output "host" {
  value = "${element(flatten(list(vmware_virtual_machine.ubuntu.*.ip_address, vmware_virtual_machine.windows.*.ip_address)), 0)}"  
}

output "username" {
  value = "${var.bootstrap-username-string}"
}