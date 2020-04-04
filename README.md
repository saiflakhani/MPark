# MPark
Allows for Gate In / Gate Out of a mall with a gateway system, at the simple click of a button. No more bending out of car windows to grab tickets at gates.

## How to Use

### Gateway
- Take a raspberry pi
- Run the python script given in the raspberry pi folder
- In the GPIO.BOARD format, attach:
  - Red LED at GPIO 11
  - Green LED at GPIO 15
  - Blue LED at GPIO 13
  - Buzzer at GPIO 19
- If you would like to run the script at startup, copy the ```rc.local``` file into ```/etc/```
  
 ### Android
 - Walk close to the raspberry pi
 - Click Enter
 - Buzzer should beep, and you're in!
 - Counter should begin and you'll be charged according to the time you take
 - Repeat the process for stepping out.
 
## Credits
#### Ideation:
- Riyaz Lakhani
#### Development
- Vikas Somani
  
  
