from bluepy.btle import Scanner, DefaultDelegate, Peripheral
import subprocess
import struct
import RPi.GPIO as GPIO
import time
import hmac
import hashlib
import time
import sys
import socket
import requests,json


#For encryption
from hashlib import md5
from base64 import b64decode
from base64 import b64encode
from Crypto import Random
from Crypto.Cipher import AES


#Setting up LEDs

redPin = 11   #Set to appropriate GPIO
greenPin = 15 #Should be set in the 
bluePin = 13  #GPIO.BOARD format
buzzerPin = 19

GPIO.setwarnings(False)



def turnOff(pin):
    GPIO.setmode(GPIO.BOARD)
    
    GPIO.setup(pin, GPIO.OUT)
    GPIO.output(pin, GPIO.LOW)
    
def beep():
    GPIO.setmode(GPIO.BOARD)
    
    GPIO.setup(buzzerPin, GPIO.OUT)
    GPIO.output(buzzerPin, GPIO.HIGH)
    time.sleep(1.2)
    GPIO.output(buzzerPin, GPIO.LOW)
    
    
def blink():
    GPIO.setmode(GPIO.BOARD)
    GPIO.setup(redPin, GPIO.OUT)
    GPIO.output(redPin, GPIO.HIGH)
    
    GPIO.setup(greenPin, GPIO.OUT)
    GPIO.output(greenPin, GPIO.HIGH)
    
    GPIO.setup(bluePin, GPIO.OUT)
    GPIO.output(bluePin, GPIO.HIGH)
    

def redOn():
    blink()
    turnOff(redPin)

def greenOn():
    blink()
    turnOff(greenPin)
    
def blueOn():
    blink()
    turnOff(bluePin)

def yellowOn():
    blink()
    turnOff(redPin)
    turnOff(greenPin)

#For TOTP
timestep = 30
T0 = 0

#Get MAC
hwaddr = subprocess.check_output(['hcitool','dev']).decode('utf-8').replace('\n','')[-17:]

#-------TOTP Libraries--------#
def HOTP(K, C, digits=6):
    """HTOP:
    K is the shared key
    C is the counter value
    digits control the response length
    """
    K_bytes = K.encode()
    C_bytes = struct.pack(">Q", C)
    hmac_sha512 = hmac.new(key = K_bytes, msg=C_bytes, digestmod=hashlib.sha512).hexdigest()
    return Truncate(hmac_sha512)[-digits:]

def Truncate(hmac_sha512):
    """truncate sha512 value"""
    offset = int(hmac_sha512[-1], 16)
    binary = int(hmac_sha512[(offset *2):((offset*2)+8)], 16) & 0x7FFFFFFF
    return str(binary)

def TOTP(K, digits=6, timeref = 0, timestep = 30,timereq = 1111111111111):
    """TOTP, time-based variant of HOTP
    digits control the response length
    the C in HOTP is replaced by ( (currentTime - timeref) / timestep )
    """
    C = int ( timereq - timeref ) // timestep
    return HOTP(K, C, digits = digits)


def retrieve_data():
        r = requests.get("http://mpark.quicsolv.com/api/wifi_config.php",timeout=180)
        if r.status_code == requests.codes.ok:
            data =  r.json()
            file = open("/etc/wpa_supplicant/wpa_supplicant.conf",'w')
            file.write('ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev\n')
            file.write('update_config=1\n')
            file.write('country=GB\n\n')
            for element in data["list"]:
                file.write("network={\n")
                file.write('\tssid="'+element["ssid"]+'"\n')
                file.write('\tpsk="'+element["psk"]+'"\n')
                file.write('}\n')

#------------- Encrypted Connection to Server -------------#
def sendDataToServer(otp="0",uuid="0",phone="0",timestamp="0",flag="-1",gwma="0",dma="0"):
    TCP_IP = '188.166.247.93'
    TCP_PORT = 22335
    BUFFER_SIZE = 1024
    data = '{"suuid":"'+uuid+'","ph":"'+phone+'","pf":"'+flag+'","otp":"'+otp+'","t":"'+timestamp+'","gwma":"'+gwma+'","dma":"'+dma+'"}'
    #print data
    password = "bhokaalbhokaalbh"
    cipher = AESCipher(password).encrypt(data)
    jsonOfencryptedData = '{"encryptedData":'+'"'+cipher+'"'+'}'
    #print "Encrypted Data : "+jsonOfencryptedData
    finalData = json.loads(jsonOfencryptedData)
    try:
        r = requests.post("http://mpark.quicsolv.com/api/parking.php",finalData,timeout=3)
        if r.status_code == requests.codes.ok:
            print "Sent data to server : "+str(r.status_code)
            retrieve_data()
    except Exception:
        print "Error connecting to server"
    
    
#------------ BluePy Class ----------#
class ScanDelegate(DefaultDelegate):
    lastDetectedUUID = ""
    def __init__(self):
        DefaultDelegate.__init__(self)

    #-------- Handle Device when Connection occurs -------#
    def handleDiscovery(self, dev, isNewDev, isNewData):
        if isNewDev or isNewData:
            for (adtype, desc, value) in dev.getScanData():
                if value == self.lastDetectedUUID:
                    print "Duplicate UUID, will not open"
                    break
                if adtype == 7:
                    print "Found a new device. Broadcasted values are :"
                   #Getting all values here
                    uuid = ""
                    i=0
                    #Calculate UUID
                    while i<=30:
                        uuid = value[i:i+2]+uuid
                        i+=2
                    timestamp = long(uuid[8:21])
                    otp = uuid[0:6]
                    flag = uuid[6]
                    phone = uuid[22:]
                    genotp =  TOTP("meowmeowmeowmeowmeowmeowmeowmeowmeowmeowmeowmeowmeowmeowmeowmeow", 6, T0, timestep,timestamp).zfill(6)
                    print "Phone = " + phone
                    print "Timestamp = " + str(timestamp)
                    print "Flag = " + flag
                    print "OTP = " + otp
                    print "Generated OTP " + genotp
                    if(otp != genotp):
                        print "Unauthorized user. Connection will not proceed"
                        break
                    print "FOUND AN MPARK UUID! --> " + uuid
                    p = Peripheral(dev.addr,"random")
                    services=p.getServices()
                    characteristics = p.getCharacteristics()
                    
                    for ch in characteristics:
                        if "00002a00-0000-1000-8000-00805f9b34aa" == str(ch.uuid):
                            charvalue = ch.read()
                            print "Seems this is an Apple device."
                            break
 
                    greenOn()
                    print "Initiated a connection"
                    p.disconnect()
                    beep()
                    blueOn()
                    #SEND POST REQUEST HERE
                    sendDataToServer(otp,uuid,phone,str(timestamp),flag,hwaddr,dev.addr)
                    print "Gate opened. Disconnecting"
                    self.lastDetectedUUID = value
                    break



#--------- Encryption Classes --------- #
BLOCK_SIZE = 16  # Bytes
pad = lambda s: s + (BLOCK_SIZE - len(s) % BLOCK_SIZE) * \
                chr(BLOCK_SIZE - len(s) % BLOCK_SIZE)
unpad = lambda s: s[:-ord(s[len(s) - 1:])]


class AESCipher:
    
    def __init__(self, key):
        self.key = key

    def encrypt(self, raw):
        raw = pad(raw)
        iv = "HELLOWORLD123456"
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return b64encode(cipher.encrypt(raw))

    def decrypt(self, enc):
        enc = b64decode(enc)
        iv = enc[:16]
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return unpad(cipher.decrypt(enc[16:])).decode('utf8')
    
    
    
#-----------INTERNET CONNECTION METHODS---------#
def is_connected(hostname):
  try:
    # see if we can resolve the host name -- tells us if there is
    # a DNS listening
    host = socket.gethostbyname(hostname)
    # connect to the host -- tells us if the host is actually
    # reachable
    s = socket.create_connection((host, 80), 2)
    return True
  except:
     print "Error in connection"
  return False
    

# ----------- MAIN METHOD ----------- #

scanner = Scanner().withDelegate(ScanDelegate())

while True:
    print "Scanning..."
   
    try:
        if(is_connected("mpark.quicsolv.com")):
            blueOn()
        scanner.scan(0.0)
        
    except Exception:
        print "Error connecting to device"
        redOn()
        time.sleep(1)
        
        


    
    
