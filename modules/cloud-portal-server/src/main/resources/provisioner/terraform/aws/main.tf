provider "random" {
  version = "1.1.0"
}

provider "aws" {
  profile    = "default"
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region     = "${var.region}"
  version    = "1.3.0"
}

locals {
  is_linux = "${replace(var.image_name, "Linux", "") != var.image_name ? 1 : 0}"
  is_windows = "${replace(var.image_name, "Windows", "") != var.image_name ? 1 : 0}"
  incoming_ports_list = "${split(",", var.incoming_ports)}"
  ami_name = "${lookup(var.image_names_map, var.image_name)}"
  ami_owner = "${lookup(var.image_owners_map, var.image_name)}"
}

resource "random_id" "id" {
  byte_length = 6
}

resource "aws_security_group" "nsg" {
  name        = "${random_id.id.hex}_nsg"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }  
}

resource "aws_security_group_rule" "remoting_ports_linux_ssh" {
  count             = "${local.is_linux}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "22"
  to_port           = "22"
}

resource "aws_security_group_rule" "remoting_ports_windows_rdp" {
  count             = "${local.is_windows}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "3389"
  to_port           = "3389"
}

resource "aws_security_group_rule" "remoting_ports_windows_rm" {
  count             = "${local.is_windows}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "5985"
  to_port           = "5985"
}

resource "aws_security_group_rule" "incoming_ports" {
  count             = "${length(local.incoming_ports_list)}"
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "ingress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "tcp"
  from_port         = "${element(local.incoming_ports_list, count.index)}"
  to_port           = "${element(local.incoming_ports_list, count.index)}"
}

resource "aws_security_group_rule" "outgoing_ports" {
  security_group_id = "${aws_security_group.nsg.id}"
  type              = "egress"
  cidr_blocks       = ["0.0.0.0/0"]
  protocol          = "all"
  from_port         = 0
  to_port           = 0
}

resource "aws_key_pair" "auth" {
  public_key = "${file("${var.public_key_file}")}"
}

data "aws_ami" "image" {
  most_recent = true
  filter {
      name   = "name"
      values = ["${local.ami_name}"]
  }
  filter {
      name   = "virtualization-type"
      values = ["hvm"]
  }  
  owners = ["${local.ami_owner}"]
}

resource "aws_instance" "linux" {

  count = "${local.is_linux}"
  ami = "${data.aws_ami.image.id}"
  instance_type = "${var.vm_size}"
  availability_zone = "${var.region}${var.availability_zone}"
  key_name = "${aws_key_pair.auth.id}"
  associate_public_ip_address = true
  vpc_security_group_ids = ["${aws_security_group.nsg.id}"]

  root_block_device = {
    "volume_type"           = "${var.storage_type}"
    "volume_size"           = "${var.storage_size}"
    "delete_on_termination" = true
  }

  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
  
  connection {
    type = "ssh"
    agent = false  
    host = "${aws_instance.linux.public_dns}"
    user = "ubuntu"      
    private_key = "${file("${var.private_key_file}")}"
    timeout = "1m"      
  }

  provisioner "file" {
    source      = "${var.script_file}"
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

  count = "${local.is_windows}"
  ami = "${data.aws_ami.image.id}"
  instance_type = "${var.vm_size}"
  availability_zone = "${var.region}${var.availability_zone}"
  key_name = "${aws_key_pair.auth.id}"
  associate_public_ip_address = true
  vpc_security_group_ids = ["${aws_security_group.nsg.id}"]

  root_block_device = {
    "volume_type"           = "${var.storage_type}"
    "volume_size"           = "${var.storage_size}"
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
    password = "${var.password}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source = "${var.script_file}"
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
  $admin.psbase.invoke("SetPassword", "${var.password}")
</powershell>
EOF
}