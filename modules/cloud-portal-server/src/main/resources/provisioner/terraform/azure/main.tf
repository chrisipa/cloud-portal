provider "null" {
  version = "1.0.0"
}

provider "random" {
  version = "1.1.0"
}

provider "azurerm" {
  subscription_id = "${var.subscription_id}"
  tenant_id = "${var.tenant_id}"
  client_id = "${var.client_id}"
  client_secret = "${var.client_secret}"
  version = "0.3.0"
}

locals {

  image_names_map = {
    "Ubuntu Server Linux 16.04" = "Canonical:UbuntuServer:16.04-LTS:latest"
    "Windows Server 2016" = "MicrosoftWindowsServer:WindowsServer:2016-Datacenter:latest"
  }

  is_linux = "${replace(var.image_name, "Linux", "") != var.image_name ? 1 : 0}"
  is_windows = "${replace(var.image_name, "Windows", "") != var.image_name ? 1 : 0}"
  incoming_ports_list = "${split(",", var.incoming_ports)}"
  image_publisher = "${element(split(":", lookup(local.image_names_map, var.image_name)), 0)}"
  image_offer = "${element(split(":", lookup(local.image_names_map, var.image_name)), 1)}"
  image_sku = "${element(split(":", lookup(local.image_names_map, var.image_name)), 2)}"
  image_version = "${element(split(":", lookup(local.image_names_map, var.image_name)), 3)}"
  linux_script_path = "/tmp/bootstrap.sh"
  windows_script_path = "C:\\bootstrap.ps1"  
  allow_win_rm_file = "allow-winrm.cmd"
  allow_win_rm_url = "https://raw.githubusercontent.com/chrisipa/cloud-portal/master/public/bootstrap/${local.allow_win_rm_file}"
}

resource "random_id" "id" {
  byte_length = 6
}

resource "azurerm_resource_group" "rg" {
  name = "${random_id.id.hex}rg"
  location = "${var.region}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_virtual_network" "vnet" {
  name = "${random_id.id.hex}vnet"
  location = "${var.region}"
  address_space = ["${var.vnet_address_space}"]
  resource_group_name = "${azurerm_resource_group.rg.name}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_subnet" "subnet" {
  name = "${random_id.id.hex}subnet"
  virtual_network_name = "${azurerm_virtual_network.vnet.name}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  address_prefix = "${var.subnet_address_space}"
}

resource "azurerm_network_security_group" "nsg" {
  name = "${random_id.id.hex}nsg"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_network_security_rule" "rulessh" {
  count = "${local.is_linux}"
  name = "${random_id.id.hex}rulessh"
  priority = 100
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "22"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_security_rule" "rulerdp" {
  count = "${local.is_windows}"
  name = "${random_id.id.hex}rulerdp"
  priority = 101
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "3389"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_security_rule" "rulerm" {
  count = "${local.is_windows}"
  name = "${random_id.id.hex}rulerm"
  priority = 102
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "5985"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_security_rule" "rulecustom" {
  count = "${var.incoming_ports != -1 ? length(local.incoming_ports_list) : 0}"
  name = "${random_id.id.hex}rulecustom"
  priority = "${103 + count.index}"
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "${element(local.incoming_ports_list, count.index)}"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_interface" "nic" {
  name = "${random_id.id.hex}nic"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_id = "${azurerm_network_security_group.nsg.id}"

  ip_configuration {
    name = "${random_id.id.hex}ipconfig"
    subnet_id = "${azurerm_subnet.subnet.id}"
    private_ip_address_allocation = "dynamic"
    public_ip_address_id = "${azurerm_public_ip.pip.id}"
  }
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }

  depends_on = ["azurerm_network_security_group.nsg"]
}

resource "azurerm_public_ip" "pip" {
  name = "${random_id.id.hex}ip"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  public_ip_address_allocation = "dynamic"
  domain_name_label = "d${random_id.id.hex}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_storage_account" "stor" {
  name = "${random_id.id.hex}stor"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  account_tier = "${var.storage_account_tier}"
  account_replication_type = "${var.storage_account_replication_type}"  
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_storage_container" "storc" {
  name = "${random_id.id.hex}vhds"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  storage_account_name = "${azurerm_storage_account.stor.name}"
  container_access_type = "private"
}

resource "azurerm_virtual_machine" "linux" {

  count = "${local.is_linux}"
  name = "${random_id.id.hex}vm"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  vm_size = "${var.vm_size}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${local.image_publisher}"
    offer = "${local.image_offer}"
    sku = "${local.image_sku}"
    version = "${local.image_version}"
  }

  storage_os_disk {
    name = "${random_id.id.hex}osdisk"
    vhd_uri = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${random_id.id.hex}osdisk.vhd"
    caching = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name = "${random_id.id.hex}"
    admin_username = "${var.username}"
    admin_password = "${var.password}"
  }

  os_profile_linux_config {
    disable_password_authentication = "true"

    ssh_keys = [{
      path = "/home/${var.username}/.ssh/authorized_keys"
      key_data = "${file("${var.public_key_file}")}"
    }]
  }

  connection {
    type = "ssh"
    agent = false  
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.username}"      
    private_key = "${file("${var.private_key_file}")}"
    timeout = "1m"      
  }

  provisioner "file" {
    source = "${var.script_file}"
    destination = "${local.linux_script_path}"         
  }

  provisioner "remote-exec" {
    inline = [
      "bash ${local.linux_script_path}",
      "rm ${local.linux_script_path}"
    ]
  }
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }

  depends_on = ["azurerm_storage_account.stor"]
}

resource "azurerm_virtual_machine" "windows" {

  count = "${local.is_windows}"
  name = "${random_id.id.hex}vm"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  vm_size = "${var.vm_size}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${local.image_publisher}"
    offer = "${local.image_offer}"
    sku = "${local.image_sku}"
    version = "${local.image_version}"
  }

  storage_os_disk {
    name = "${random_id.id.hex}osdisk"
    vhd_uri = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${random_id.id.hex}osdisk.vhd"
    caching = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name = "${random_id.id.hex}"
    admin_username = "${var.username}"
    admin_password = "${var.password}"
  }
  
  os_profile_windows_config {
    enable_automatic_upgrades = true
    provision_vm_agent = true
  }
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }  

  depends_on = ["azurerm_storage_account.stor"]
}

resource "azurerm_virtual_machine_extension" "windowsvmext" {
  
  count = "${local.is_windows}"
  name = "${random_id.id.hex}vmext"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"  
  virtual_machine_name = "${azurerm_virtual_machine.windows.name}"  
  publisher = "Microsoft.Compute"
  type = "CustomScriptExtension"
  type_handler_version = "1.8"
  
  settings = <<SETTINGS
  {
    "fileUris": [
      "${local.allow_win_rm_url}"
    ],
    "commandToExecute": "cmd.exe /c ${local.allow_win_rm_file}"
  }
SETTINGS

  depends_on = ["azurerm_virtual_machine.windows"]
}

resource "null_resource" "windowsprovisioning" {
  
  count = "${local.is_windows}"
  
  connection {
    type = "winrm"
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.username}"
    password = "${var.password}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source = "${var.script_file}"
    destination = "${local.windows_script_path}" 
  }  

  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_script_path}",
      "del ${local.windows_script_path}"      
    ]
  }
  
  depends_on = ["azurerm_virtual_machine_extension.windowsvmext"]
}