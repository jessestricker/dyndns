# dyndns

Simple DynDNS server and client.

## Install dyndns-server

```shell
sudo unzip dyndns-server-0.3.1.zip -d /opt/
sudo nano /opt/dyndns-server-0.3.1/etc/config.yml
sudo systemctl link /opt/dyndns-server-0.3.1/etc/dyndns-server.service
```

## Uninstall dyndns-server

```shell
sudo systemctl stop dyndns-server.service
sudo systemctl disable dyndns-server.service
sudo rm -r /opt/dyndns-server-0.3.1/
```
