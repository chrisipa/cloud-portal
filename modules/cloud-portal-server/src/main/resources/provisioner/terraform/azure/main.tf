provider "null" {
  version = "1.0.0"
}

provider "random" {
  version = "1.1.0"
}

provider "azurerm" {
  subscription_id = "${var.credentials-subscription-id-string}"
  tenant_id = "${var.credentials-tenant-id-string}"
  client_id = "${var.credentials-client-id-string}"
  client_secret = "${var.credentials-client-secret-string}"
  version = "0.3.0"
}

resource "random_id" "id" {
  byte_length = 6
}

resource "azurerm_resource_group" "rg" {
  name = "${random_id.id.hex}rg"
  location = "${var.general-region-string}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_virtual_network" "vnet" {
  name = "${random_id.id.hex}vnet"
  location = "${var.general-region-string}"
  address_space = ["${var.network-vnet-address-space-string}"]
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
  address_prefix = "${var.network-subnet-address-space-string}"
}

resource "azurerm_network_security_group" "nsg" {
  name = "${random_id.id.hex}nsg"
  location = "${var.general-region-string}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_network_security_rule" "rulessh" {
  count = "${var.vm-image-string == "Ubuntu Server 16.04" ? 1 : 0}"
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
  count = "${var.vm-image-string == "Windows Server 2016" ? 1 : 0}"
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
  count = "${var.vm-image-string == "Windows Server 2016" ? 1 : 0}"
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
  count = "${length(split(",", var.network-incoming-ports-string))}"
  name = "${random_id.id.hex}rulecustom"
  priority = "${103 + count.index}"
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "${element(split(",", var.network-incoming-ports-string), count.index)}"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_interface" "nic" {
  name = "${random_id.id.hex}nic"
  location = "${var.general-region-string}"
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
  location = "${var.general-region-string}"
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
  location = "${var.general-region-string}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  account_tier = "${var.storage-account-tier-string}"
  account_replication_type = "${var.storage-replication-type-string}"  
  
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

resource "azurerm_virtual_machine" "ubuntu" {

  count = "${var.vm-image-string == "Ubuntu Server 16.04" ? 1 : 0}"
  name = "${random_id.id.hex}vm"
  location = "${var.general-region-string}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  vm_size = "${var.vm-size-string}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 0)}"
    offer = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 1)}"
    sku = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 2)}"
    version = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 3)}"
  }

  storage_os_disk {
    name = "${random_id.id.hex}osdisk"
    vhd_uri = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${random_id.id.hex}osdisk.vhd"
    caching = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name = "${random_id.id.hex}"
    admin_username = "${var.vm-username-string}"
    admin_password = "${var.vm-password-string}"
  }

  os_profile_linux_config {
    disable_password_authentication = "true"

    ssh_keys = [{
      path = "/home/${var.vm-username-string}/.ssh/authorized_keys"
      key_data = "${file("${var.bootstrap-public-key-file}")}"
    }]
  }

  connection {
    type = "ssh"
    agent = false  
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.vm-username-string}"      
    private_key = "${file("${var.bootstrap-private-key-file}")}"
    timeout = "1m"      
  }

  provisioner "file" {
    source = "${var.bootstrap-script-file}"
    destination = "/tmp/bootstrap.sh"         
  }

  provisioner "remote-exec" {
    inline = [
      "bash /tmp/bootstrap.sh",
      "rm /tmp/bootstrap.sh"
    ]
  }
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }

  depends_on = ["azurerm_storage_account.stor"]
}

resource "azurerm_virtual_machine" "windows" {

  count = "${var.vm-image-string == "Windows Server 2016" ? 1 : 0}"
  name = "${random_id.id.hex}vm"
  location = "${var.general-region-string}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  vm_size = "${var.vm-size-string}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 0)}"
    offer = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 1)}"
    sku = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 2)}"
    version = "${element(split(":", lookup(var.image-names-map, var.vm-image-string)), 3)}"
  }

  storage_os_disk {
    name = "${random_id.id.hex}osdisk"
    vhd_uri = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${random_id.id.hex}osdisk.vhd"
    caching = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name = "${random_id.id.hex}"
    admin_username = "${var.vm-username-string}"
    admin_password = "${var.vm-password-string}"
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
  
  count = "${var.vm-image-string == "Windows Server 2016" ? 1 : 0}"
  name = "${random_id.id.hex}vmext"
  location = "${var.general-region-string}"
  resource_group_name = "${azurerm_resource_group.rg.name}"  
  virtual_machine_name = "${azurerm_virtual_machine.windows.name}"  
  publisher = "Microsoft.Compute"
  type = "CustomScriptExtension"
  type_handler_version = "1.8"
  
  settings = <<SETTINGS
  {
    "fileUris": [
      "https://raw.githubusercontent.com/chrisipa/cloud-portal/master/public/bootstrap/allow-winrm.cmd"
    ],
    "commandToExecute": "cmd.exe /c allow-winrm.cmd"
  }
SETTINGS

  depends_on = ["azurerm_virtual_machine.windows"]
}

resource "null_resource" "windowsprovisioning" {
  
  count = "${var.vm-image-string == "Windows Server 2016" ? 1 : 0}"
  
  connection {
    type = "winrm"
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.vm-username-string}"
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
  
  depends_on = ["azurerm_virtual_machine_extension.windowsvmext"]
}