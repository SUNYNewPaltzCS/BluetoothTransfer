
import sys
import bluetooth

try:
	file = open("sample.txt")
	content = file.read()
except IOError: 
	content = "error, couldn't find file "
bdaddr="4C:A5:6D:5A:06:32"
name="Bluetooth Transfer"
uuid = "f1239387-98b2-4dce-a7d5-635ce03572a0"

service_matches = bluetooth.find_service(uuid=uuid,address=bdaddr)

if len(service_matches) == 0:
    print "couldn't find the Bluetooth service"
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "connecting to \"%s\" on %s" % (name, host)

sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((host, port))
sock.send(content)
sock.close()

