winrm create winrm/config/listener?Address=*+Transport=HTTP 
netsh advfirewall firewall add rule name="WinRM in" protocol=TCP dir=in profile=any localport=5985 remoteip=any localip=any action=allow