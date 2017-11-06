provider "aws" {
  profile = "default"
  access_key = "${var.credentials-access-key-string}"
  secret_key = "${var.credentials-secret-key-string}"
  region  = "${var.general-region-string}"
}

resource "aws_security_group" "nsg" {
  name        = "${var.general-name-string}-nsg"
}

resource "aws_security_group_rule" "incoming-ports" {
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "${var.network-incoming-port-start-string}"
  to_port           = "${var.network-incoming-port-end-string}"
}

resource "aws_security_group_rule" "outgoing-ports" {
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "egress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "all"
  from_port         = 0
  to_port           = 0
}

resource "aws_key_pair" "auth" {
  public_key = "${file("${var.bootstrap-public-key-file}")}"
}

resource "aws_instance" "vm" {

  ami = "${var.image-ami-string}"
  instance_type = "${var.vm-size-string}"
  availability_zone = "${var.general-availability-zone-string}"
  key_name = "${aws_key_pair.auth.id}"
  associate_public_ip_address = true
  vpc_security_group_ids = ["${aws_security_group.nsg.id}"]

  root_block_device = {
    "volume_type"           = "${var.storage-type-string}"
    "volume_size"           = "${var.storage-size-string}"
    "delete_on_termination" = true
  }

  tags {
    Name = "${var.general-name-string}"
  }
  
  connection {
    type = "ssh"
    agent = false  
    host = "${aws_instance.vm.public_dns}"
    user = "ubuntu"      
    private_key = "${file("${var.bootstrap-private-key-file}")}"
    timeout = "1m"      
  }

  provisioner "file" {
    source      = "${var.bootstrap-script-file}"
    destination = "/tmp/bootstrap.sh"                
  }

  provisioner "remote-exec" {
    inline = [
      "bash /tmp/bootstrap.sh",
      "rm /tmp/bootstrap.sh"
    ]
  }
}

output "username" {
  value = "ubuntu"
}

output "host" {
  value = "${aws_instance.vm.public_dns}"
}