provider "random" {
  version = "1.1.0"
}

provider "aws" {
  profile    = "default"
  access_key = "${var.credentials-access-key-string}"
  secret_key = "${var.credentials-secret-key-string}"
  region     = "${var.general-region-string}"
  version    = "1.3.0"
}

resource "random_id" "id" {
  byte_length = 6
}

resource "aws_security_group" "nsg" {
  name        = "${random_id.id.hex}-nsg"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }  
}

resource "aws_security_group_rule" "remoting-ports-ubuntu-ssh" {
  count             = "${var.image-ami-string == "Ubuntu Server 16.04" ? 1 : 0}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "22"
  to_port           = "22"
}

resource "aws_security_group_rule" "remoting-ports-windows-rdp" {
  count             = "${var.image-ami-string == "Windows Server 2016" ? 1 : 0}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "3389"
  to_port           = "3389"
}

resource "aws_security_group_rule" "remoting-ports-windows-rm" {
  count             = "${var.image-ami-string == "Windows Server 2016" ? 1 : 0}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "5985"
  to_port           = "5985"
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

data "aws_ami" "image" {
  most_recent = true
  filter {
      name   = "name"
      values = ["${lookup(var.image-names-map, var.image-ami-string)}"]
  }
  filter {
      name   = "virtualization-type"
      values = ["hvm"]
  }  
  owners = ["${lookup(var.image-owners-map, var.image-ami-string)}"]
}

resource "aws_instance" "ubuntu" {

  count = "${var.image-ami-string == "Ubuntu Server 16.04" ? 1 : 0}"
  ami = "${data.aws_ami.image.id}"
  instance_type = "${var.vm-size-string}"
  availability_zone = "${var.general-region-string}${var.general-availability-zone-string}"
  key_name = "${aws_key_pair.auth.id}"
  associate_public_ip_address = true
  vpc_security_group_ids = ["${aws_security_group.nsg.id}"]

  root_block_device = {
    "volume_type"           = "${var.storage-type-string}"
    "volume_size"           = "${var.storage-size-string}"
    "delete_on_termination" = true
  }

  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
  
  connection {
    type = "ssh"
    agent = false  
    host = "${aws_instance.ubuntu.public_dns}"
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

resource "aws_instance" "windows" {

  count = "${var.image-ami-string == "Windows Server 2016" ? 1 : 0}"
  ami = "${data.aws_ami.image.id}"
  instance_type = "${var.vm-size-string}"
  availability_zone = "${var.general-region-string}${var.general-availability-zone-string}"
  key_name = "${aws_key_pair.auth.id}"
  associate_public_ip_address = true
  vpc_security_group_ids = ["${aws_security_group.nsg.id}"]

  root_block_device = {
    "volume_type"           = "${var.storage-type-string}"
    "volume_size"           = "${var.storage-size-string}"
    "delete_on_termination" = true
  }

  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
  
  connection {
    type = "winrm"
    host = "${aws_instance.windows.public_dns}"
    user = "Administrator"
    password = "${var.vm-password-string}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source = "${var.bootstrap-script-file}"
    destination = "C:\\bootstrap.ps1" 
  }  

  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File C:\\bootstrap.ps1",
      "del C:\\bootstrap.ps1"      
    ]
  }
  
  user_data = <<EOF
<script>
  winrm quickconfig -q & winrm set winrm/config @{MaxTimeoutms="1800000"} & winrm set winrm/config/service @{AllowUnencrypted="true"} & winrm set winrm/config/service/auth @{Basic="true"}
</script>
<powershell>
  netsh advfirewall firewall add rule name="WinRM in" protocol=TCP dir=in profile=any localport=5985 remoteip=any localip=any action=allow
  # Set Administrator password
  $admin = [adsi]("WinNT://./administrator, user")
  $admin.psbase.invoke("SetPassword", "${var.vm-password-string}")
</powershell>
EOF
}