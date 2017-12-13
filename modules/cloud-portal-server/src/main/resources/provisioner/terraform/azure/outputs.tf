output "id" {
  value = "${random_id.id.hex}"
}

output "host" {  
  value = "${azurerm_public_ip.pip.fqdn}"
}

output "username" {
  value = "${var.username}"
}