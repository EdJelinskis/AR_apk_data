#! /usr/bin/env python3

import time
import serial
#from tfmini import TFmini
import sys
import socket
import bluetooth
import subprocess
import threading

#Variables
List =[]##kolekcioneju datus
MA1 = []## izrekinu videjo vertibu
LowerBound = 0.3 #apakseja robeza, kad ir parak tuvu, tad rada 0.3
UpperBound = 2.5 #augseja robeza, pie kuras mes izlaizam datus un neko nedaram
T = 1 #pozicija hidraulikai, sakuma lai sareizina reiz n
MoveTime = 0.2 #sec #kad kustina hidrauliku
n_move = 1 #m #ja uzreiz palielinasdistance vairak par 1m

Distance_a = 0.0 #distance kad izsledzas hidraulika, japiebida
Distance_b = 0.0 #distance kad izsledzas hidraulika, kad jaatbida
n = 0 #datu rinda ko kolekcionejam, parasti
MinH = 0.0 #0.35 #hidraulikas stoks ir iebidits
MaxH = 0.0  # 0.85 #hidraulikas stoka maksimali izbidits

#Functions
###TFmini read data
def getTFminiData(ser):
    while True:#1
        #print('getTF I am here')
        #time.sleep(0.1)
        count = ser.in_waiting
        if count > 8:
            recv = ser.read(9)
            ser.reset_input_buffer()
            if recv[0] == 0x59 and recv[1] == 0x59: #python3
                distance = recv[2] + recv[3] * 256
                strength = recv[4] + recv[5] * 256
                if ((distance/100)>12):
                    #print('(', distance, ',', strength, ')')
                    ser.reset_input_buffer()#read sensor one more time, not valid distance
                else:
                    #print('(', distance, ',', strength, ')')
                    ser.reset_input_buffer()
            break
    #print(distance)
    return distance

def TFmini(ser):
    try:
        if ser.is_open == False:
            ser.open()
        d = getTFminiData(ser)/100
        #print("Read data", d)
    except:
        print('fail to read')
        d = []
    return d

###SAVE to .txt
def write_txt(Distance_a,Distance_b,n,MinH,MaxH):
    i1 = float(Distance_a)*100
    i2 = float(Distance_b)*100
    i3 = n
    i4 = float(MinH)*100
    i5 = float(MaxH)*100
    param = [i1,i2,i3,i4,i5]
    #f = open('TFmini_data.txt', 'w')
    with open('TFmini_data.txt', 'w') as f:
        for line in param:
            f.write('%d' % line)
            f.write('\n')
    f.close()
    print('succesfully save data')


### GPIO control
def GPIO_control(position,status):
    if(position == True and status==True): #izbida hidrauliku
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(24, GPIO.OUT, initial=GPIO.LOW) #Pin 20 as an output
        GPIO.output(24, GPIO.HIGH) #Sets GPIO 20 to active (a 3.3v DC output)
    elif (position == True and status==False): #nostope izbidisanu
        GPIO.output(24, GPIO.LOW) #Deactivates GPIO 20 (sets to 0v DC output)
        GPIO.cleanup() # Standard cleanup routines for GPIO Library
    elif(position == False and status==True): #iebida cilindru
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(23, GPIO.OUT, initial=GPIO.LOW) #Pin 22 as an output
        GPIO.output(23, GPIO.HIGH) #Sets GPIO 22 to active (a 3.3v DC output)
    else: #nostope iebidisanu
        GPIO.output(23, GPIO.LOW) #Deactivates GPIO 20 (sets to 0v DC output)
        GPIO.cleanup() # Standard cleanup routines for GPIO Library

### Saglaba vertibas
def warehouse (data, List):
    if float(data)<=UpperBound and float(data)>LowerBound:
        #ja ir intervala, tad pievieno jaunu vertibu Listam
        if len(List)>=n:
            List = List[1:(n)] #save only latest values n-1
            List += [data] #add aditional value
        else:
            List = [data]*n
    else:
        # ja ir LowerBound vai parsniedz UpperBound, tad Lista nepievinojas dati un hidraulika nemainas
        List
    return List

### Average value calculation
def MA(List):
    return sum(List)/len(List)


### Control algorithm
def Move(Mean, Start,start_cilindr,T,List, client):
    global stop_processing, running,Distance_a,Distance_b,n,MinH,MaxH
    running=True
    #print(Distance_b,Distance_a)
    if ((Start - Mean)>=Distance_b and (Start - Mean) <= n_move): #atbida no krumiem
        if (start_cilindr<=MinH): #ja cilindrs ir tuvak neka iebidits cilindrs, tad neko nedara
            print('cilindrs ir iebidits')
            T1 = T
        else:
            T1 = 1
            #GPIO_control(position = False,status=True)
            while (Start - Mean) >= Distance_b:
                if stop_processing:
                    break

                if ((Start - Mean) >= n_move) or (start_cilindr<=MinH): #vai strauji mainas distance,  vai cilindrs ir iebidits
                    #GPIO_control(position = False,status=False)
                    break
                data = TFmini(ser) #tf.read()
                try:
                    start_cilindr = TFmini(ser2)# tf2.read()
                    client.send(bytes('cilindr:tf3'+str(start_cilindr) + ";",'UTF-8'))
                    # Signal that we are done sending data

                    time.sleep(0.5)
                except:
                    a = 'not work'
                List = warehouse(data,List)
                Mean = MA(List)
                client.send(bytes('mean:tf1'+str(Mean) + ";",'UTF-8'))
                time.sleep(MoveTime) #cik ilgi jakustina hidrauliku
                print('move h-', Mean , T)
            #GPIO_control(position = False,status=False)

    elif ((Mean-Start)>=Distance_a and (Mean - Start) <= n_move): #piebida krumiem
        if (start_cilindr>=MaxH): #ja cilindrs ir talak neka izbidits cilindrs, tad neko nedara
            print('cilindrs ir izbidits')
            T1 = T
        else:
            T1 = -1
            #GPIO_control(position = True,status=True)
            while (Mean - Start) >= Distance_a:
                if stop_processing:
                    break

                if ((Mean - Start) >= n_move) or (start_cilindr>=MaxH):#vai strauji mainas distance,  vai cilindrs ir izbidits
                    #GPIO_control(position = True,status=False)
                    break
                data = TFmini(ser)#tf.read()
                try:
                    start_cilindr =TFmini(ser2)# tf2.read()
                    client.send(bytes('cilindr:tf3'+str(start_cilindr)  + ";",'UTF-8'))
                    time.sleep(0.5)
                except:
                    a = 'not work'
                List = warehouse(data,List)
                Mean = MA(List)
                client.send(bytes('mean:tf1'+str(Mean) + ";",'UTF-8'))
                time.sleep(MoveTime) #cik ilgi jakustina hidrauliku
                print('move h+',Mean, T)
            #GPIO_control(position = True,status=False)
    else:
        T1 = 0
        ## nebidas nekur
    return T1


### Manualas hidraulikas bidisana
def hidraulic_move(message):
    if (message == 'HidManTrue'):
        print(1) #positive signal '+'
        tf = TFmini(ser)
        #print(tf)
        client.send(bytes('hidtrue:tf1'+str(tf) + ";",'UTF-8'))
        try:
            #start_cilindr = TFmini(ser2)# tf2.read()
            client.send(bytes('hidtrue:tf3'+str(start_cilindr) + ";",'UTF-8'))#nosuta zinu
        except:
            a = 'not work'
            print('not work')
        #GPIO_control(positio=True,status=True)
        #time.sleep(1) #signal led that everithing start work, after led is off
        #GPIO_control(positio=True,status=False)
    else:
        print(-1) #poga '-'
        tf = TFmini(ser)
        #print(tf)
        client.send(bytes('hidfalse:tf1'+str(tf) + ";",'UTF-8'))
        try:
            #start_cilindr = TFmini(ser2)# tf2.read()
            client.send(bytes('hidfalse:tf3'+str(start_cilindr) + ";",'UTF-8'))#nosuta zinu
        except:
            a = 'not work'
            print('not work')
        #GPIO_control(positio=False,status=True)
        #time.sleep(1) #signal led that everithing start work, after led is off
        #GPIO_control(positio=False,status=False)

    pass


### Received data
def read_data(d, client):
    #print(d)
    global Distance_a,Distance_b,n,MinH,MaxH
    #['RunningPrepar', 'errorPositive=10', 'errorNegative=5', 'average=3', 'cylinderIn=150.0', 'cylinderOut=190.0']
    data = d.split("|")

    Distance_a = float(data[1].split("=")[1])*0.01 #distance kad izsledzas hidraulika, japiebida
    Distance_b = float(data[2].split("=")[1]) *0.01#distance kad izsledzas hidraulika, kad jaatbida
    n = int(data[3].split("=")[1]) #datu rinda ko kolekcionejam, parasti
    MinH = float(data[4].split("=")[1])*0.01 #0.35 #hidraulikas stoks ir iebidits
    MaxH = float(data[5].split("=")[1])*0.01  # 0.85 #hidraulikas stoka maksimali izbidits

    #Distance_a = float(client.recv(size))*0.01 #distance kad izsledzas hidraulika, japiebida
    #Distance_b = float(client.recv(size)) *0.01#distance kad izsledzas hidraulika, kad jaatbida
    #n = int(client.recv(size)) #datu rinda ko kolekcionejam, parasti
    #MinH = float(client.recv(size))*0.01 #0.35 #hidraulikas stoks ir iebidits
    #MaxH = float(client.recv(size))*0.01  # 0.85 #hidraulikas stoka maksimali izbidits
    write_txt(Distance_a,Distance_b,n,MinH,MaxH)
    print(Distance_a,Distance_b,n,MinH,MaxH)

    #Save data to .txt on raspberry pico
    pass


###
def main_fun(client):
    global running, T, List, stop_processing, start_cilindr,Distance_a,Distance_b,n,MinH,MaxH
    running = True

    try: #ja nepievienojas, tad nosuta kludas pazinojumu
        data = TFmini(ser)
        print('tfwork',data)
    except:
        print('error')
        client.send(bytes('TFminiError','UTF-8'))
        print('data sent')
    try: #ja nepievienojas, tad nosuta kludas pazinojumu
        #tf2 = TFmini(ser2)
        print('tf2work')
    except:
        client.send(bytes('TFmini2Error','UTF-8'))

    print("Reading sensor data")
    try:
        print('='*25)
        Start = TFmini(ser)#tf.read()
        print('Start:',Start)

        while running:
            #print(stop_processing)
            data = TFmini(ser)#tf.read()
            try:
                #start_cilindr = (ser2)# tf2.read()
                client.send(bytes('cilindr:tf3'+str(start_cilindr) + ";",'UTF-8'))#nosuta zinu
            except:
                a = 'not work'
                start_cilindr = 50

            #time.sleep(0.5)
            if data:
                if T == 1 or T == -1:
                    List = [data]*n #aizvieto visu Listu ar jaunu d vertibu, pec hidraulikas kustiba
                List = warehouse(data,List)
                Mean = MA(List)
                print("New mean ", Mean, Start)
                T = Move(Mean, Start,start_cilindr, T, List, client)
                time.sleep(0.5)

                client.send(bytes('cilindr:tf1'+str(Mean) + ";",'UTF-8'))
                const=1

                if stop_processing:
                    break
                #print(T)
            else:
                print('No valid response')
                time.sleep(0.3) # Cik bieži lasa mērījumus, bija 0,3
        #tf.close()
        #tf2.close()
        print('bye!!')
    except KeyboardInterrupt:
        tf.close()
        tf2.close()
        print('bye!!')
    '''
        try:
            GPIO.output(23, GPIO.LOW)#Deactivates GPIO 22 (sets to 0v DC output)
        except:
            GPIO.output(24, GPIO.LOW)
        try:
            GPIO.output(24, GPIO.LOW)
        except:
            GPIO.output(23, GPIO.LOW)
        GPIO.cleanup()
    '''



### mfunctions which depend of message choose what to do
def handle_client(client, client_info):
    print(f"Accepted connection from {client_info}")
    global stop_processing, running

    while True:
        try:
            message = client.recv(size)
            if(str(message.decode('latin-1')) == "CloseBluetooth"):
                print('CloseBluetooth')
                client.close()
                #s.close()
                break
            if message:
                #client.send(data)
                d = str(message.decode('latin-1'))
                print("Incoming message", d)
                if (d =='HidManTrue'):
                    hidraulic_move(d) #atbida hidrauliku
                elif (d =='HidManFalse'):
                    hidraulic_move(d) #piebida hidrauliku
                elif ("RunningPrepar" in d):
                    stop_processing = False
                    print("Starting background process")
                    read_data(d, client)
                    process_job = threading.Thread(target=main_fun, args=(client,))
                    process_job.start()

                elif(d =='RunningStop'):
                    stop_processing = True
                    global running
                    running = False
                    print("Stopping background process")
                    process_job.cancel()

                elif(d == "CloseBluetooth"):
                    print('CloseBluetooth')
                    break
        except:
            #print('wait for start')
            pass


#Bluetooth connection
hostMACAddress = "E4:5F:01:AC:27:48" #bluetooth name
port =1
backlog = 1
size = 1024
s = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
s.bind((hostMACAddress,port))
s.listen(backlog)
print("Listening on port %d" % port)

stop_processing = False
active_clients = []
start_cilindr = 50 #second TFmini

try:
    sensors = False
    try:
        global ser,ser2
        print("Serial read")
        ser = serial.Serial("/dev/ttyUSB1",115200)
        try:
            #ser2 = serial.Serial("/dev/ttyUSB1",115200)#uz cilindru# nav obligats
            print('both good')
            sensors = True
        except:
            print("Something not working")
            a = 'not work'

    except Exception as ex:
        print(ex)
        print('can not connect to the port')
        #client.send(bytes('PortError','UTF-8'))

    while True:
        client_socket, client_info = s.accept()
        client_socket.settimeout(0.2)
        print('connected')

        # Start a new thread to handle the client communication
        if sensors:
            client_thread = threading.Thread(target=handle_client, args=(client_socket, client_info))
            client_thread.start()
        else:
            client_socket.send(bytes('PortError','UTF-8'))

except Exception as exept:
    print(exept)
    print('Closing socket')
    client_socket.close()

    s.close()
    ser.close()
    ser2.close()

ser.close()
ser2.close()












