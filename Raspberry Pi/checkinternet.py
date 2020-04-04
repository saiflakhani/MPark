import time
import RPi.GPIO as GPIO
import socket

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


while True:
    if(is_connected("mpark.quicsolv.com")):
        print "WiFi Connected"
        blueOn()
        break;
    time.sleep(5.0)
            