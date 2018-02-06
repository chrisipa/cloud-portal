output "random_id" {
  value = "${var.random_id}"
}

output "host" {
  value = "${element(flatten(list(vsphere_virtual_machine.linux.*.guest_ip_addresses.0, vsphere_virtual_machine.windows.*.guest_ip_addresses.0)), 0)}"  
}

output "username" {
  value = "${var.username}"
}