_INSTALL_

1. Install SSH:
https://phoenixnap.com/kb/enable-ssh-raspberry-pi
2. Create new map, and insert oak-d-deploy.py file
3. [Edit Path in file oak-d-deploy.py](git clone https://github.com/AgroRobotlv/IoT_2_ZUZA.git)
https://code.visualstudio.com/download
4. Install pip environments:  
  -> sudo apt install python3-pip  
  -> sudo apt-get install python-opencv  
  -> python3 install_requirements.py  
  -> pip install --upgrade pip==19  
  -> https://linuxhint.com/update-python-raspberry-pi/

https://raspberrypi-guide.github.io/programming/install-opencv
--------------------------------------------------------------------
pythone 3.11 https://aruljohn.com/blog/python-raspberrypi/
1. pip install pip==23.2
2. sudo apt-get install -y  build-essential cmake pkg-config libjpeg-dev libtiff5-dev libjasper-dev libpng-dev libavcodec-dev libavformat-dev libswscale-dev libv4l-dev libxvidcore-dev libx264-dev libfontconfig1-dev libcairo2-dev libgdk-pixbuf2.0-dev libpango1.0-dev libgtk2.0-dev libgtk-3-dev libatlas-base-dev gfortran libhdf5-dev libhdf5-serial-dev libhdf5-103 python3-pyqt5 python3-dev -y
3. sudo apt-get install libopencv-dev python3-opencv

4. pip install numpy==1.25.2
5. pip install numpy --upgrade --ignore-installed
6. pip install pandas==2.0.2
7. sudo apt-get install libsdl2-image-2.0-0

8. pip install --upgrade pip setuptools wheel
9. pip install opencv-contrib-python==4.5.3.56
10. pip install opencv-python==4.6.0.66
11. pip install opencv-python-headless==4.7.0.72 -> pip install opencv-python-headless==4.5.3.56(old version rasp)

12. pip install depthai==2.21.2.0    
13. sudo apt-get install cmake libusb-1.0-0-dev libopenjp2-7 libopenjp2-7-dev    
14. pip install depthai-sdk==1.9.4.1    

15. pip install Kivy==2.2.0rc1  
    -> pip install Kivy==2.1.0    
    -> pip install --upgrade kivy    
16. pip install PyAutoGUI
17. -> echo 'SUBSYSTEM=="usb", ATTRS{idVendor}=="03e7", MODE="0666"' | sudo tee /etc/udev/rules.d/80-movidius.rules  
    -> sudo udevadm control --reload-rules && sudo udevadm trigger
    
*SCREEN*
sudo apt-get install xinput-calibrator 
sudo apt-get install update
sudo apt-get install matchbox-keyboard
sudo nano /usr/bin/toggle-matchbox-keyboard.sh
Button { id=/usr/local/share/applications/toggle-matchbox-keyboard.desktop }

pip install tkintertable==1.3.3
sudo apt-get install wmctrl
sudo apt-get install xdotool

