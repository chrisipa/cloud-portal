output "host" {  
  value = "${azurerm_public_ip.pip.fqdn}"
}

output "username" {
  value = "${var.vm-username-string}"
}