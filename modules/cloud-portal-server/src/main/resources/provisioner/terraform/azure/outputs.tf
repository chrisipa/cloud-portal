output "id" {
  value = "${var.random_id}"
}

output "host" {  
  value = "${azurerm_public_ip.pip.fqdn}"
}

output "username" {
  value = "${var.username}"
}