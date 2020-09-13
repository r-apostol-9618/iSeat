# iSeat 👶

iSeat is an intelligent anti-abandonment booster seat, is one of my first application I ever made, started in 2017, with this application I started to have a soft spot for with mobile programming, and even if this application is not perfect (it remains an alpha project) I learned a lot and it made me live some amazing experiences.

### Project idea

By following the news, I asked myself how important and fundamental is the creation of new safety systems for children transported in vehicles. I decided to develop a child booster seat designed to prevent the child from being unintentionally left inside the car by the parent causing, in many cases, the death of the child.

### Components

For the components I used an Arduino mega, a load cell, bluetooth model and a power supply.  I asked a friend of mine to help me with the project and he made the project box (with plexiglass and wood)

<img src="iSeat1.png" width="300" />

### User Journey

When the app start the user need to enter the Mac address of the bluetooth module to be able to connect with the Arduino. If is the first time that the user open the app, he will be required to enter a number by pressing the appropriate button, this number will be saved in an sqlite database. After that he can connect to the device with the bluetooth, this connection will be used for confirm or not the presence of the parent in the car

<img src="iSeat2.png" width="300" />

In fact if the parent were to move away from the car the bluetooth signal would drop and at this point the app will verify through the last message received from the arduino, the presence of the child and if so it would start the alarm systems and send the emergency message.

<img src="iSeat3.png" width="300" />

### :books: Technologies I used 
- Bluetooth adapter 
- Sqlite database
- Notifications 
- Google Maps (removed in this version)
