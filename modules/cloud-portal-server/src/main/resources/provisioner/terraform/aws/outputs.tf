output "id" {
  value = "${random_id.id.hex}"
}

output "host" {
  value = "${element(flatten(list(aws_instance.ubuntu.*.public_dns, aws_instance.windows.*.public_dns)), 0)}"  
}

output "username" {
  value = "${var.image-ami-string == "Ubuntu Server 16.04" ? "ubuntu" : "Administrator"}"
}