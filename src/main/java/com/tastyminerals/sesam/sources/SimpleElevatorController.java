package com.tastyminerals.sesam.sources;
import java.io.IOException;
import java.util.ArrayList;


/*

Created on January 2, 2000
Ben Resner
benres@media.mit.edu

Modified for talking elevator by T.Liadal July 2007.


 */


public class SimpleElevatorController {    
	// these messages tell the elevator where to go, floorX, where X is the floor. 
	int[] floor0 = { 0x7e, 0x0, 0x3, 0x42, 0x1, 0xff, 0xff, 0x0, 0x20, 0x40, 0x0, 0x19, 0x9,  0x7e }; // floor0
	int[] floor0_5 = { 0x7e, 0x0, 0x3, 0x42, 0x1, 0xff, 0xff, 0x0, 0x20, 0x41, 0x1, 0x48, 0x1,  0x7e }; // floor 0.5
	int[] floor1 = { 0x7e, 0x0, 0x3, 0x42, 0x1, 0xff, 0xff, 0x0, 0x20, 0x42, 0x0, 0xa9, 0x3a, 0x7e }; // floor 1
	int[] floor1_5 ={ 0x7e, 0x0, 0x3, 0x42, 0x1, 0xff, 0xff, 0x0, 0x20, 0x43, 0x1, 0xf8, 0x32, 0x7e }; // floor 1.5
	int[] floor2 ={ 0x7e, 0x0, 0x3, 0x42, 0x1, 0xff, 0xff, 0x0, 0x20, 0x44, 0x0, 0x79, 0x6e, 0x7e }; // floor 2
	int[] floor3 = { 0x7e, 0x0, 0x3, 0x42, 0x1, 0xff, 0xff, 0x0, 0x20, 0x45, 0x0, 0xa1, 0x77, 0x7e }; //floor 3

	// this message will make the elevator reply with dynamic information, including current floor number
	int[] heartbeat1 = {0x7E, 0x00, 0x03, 0xe0, 0x01, 0xFF, 0xFF, 0x00, 0x20, 0xc3, 0x1e, 0x64, 0x02, 0x08, 0x3a, 0x7E };
	

	SimpleSerial            m_SerialPort = null;                                // Serial port
	int                     m_PortIndex  = 1 ;  //COMM1                         // Which comm port to use (1-based value -- there is no Comm0)
	


	// Init the serial port and associated Input/Output streams
	private void initSerialPort() throws IOException {    

		// If serial port was previously opened, close it now
		// Most applications open serial port, and never need to close it again.
		if (m_SerialPort != null) {
			m_SerialPort.close();     
			m_SerialPort = null;
		}
		
		// // New a serial port.  Similar to above, but allows greater user configuration
		//        SimpleSerialJava(int comPort, int baud, int dataBits, int stopBits, int parity) {
		//      _initPort(comPort, baud, dataBits, stopBits, parity);
		//

		// New instance of the serial port.
		m_SerialPort = new SimpleSerialJava(m_PortIndex, 38400, 8, 1, 0 );


		// If there's an error, throw an exception
		if (!m_SerialPort.isValid()) {
			throw (new IOException("Serial port not opened"));
		}
	}        

	// Constructor        
	public SimpleElevatorController() {      




		try {

			initSerialPort();
		}

		catch(Exception e) {
			System.out.println("initSerialPort threw an exception");


		}

	}
	/**
	 * Writes a message to the serial port, one byte after another. Encoded as ints so 
	 * a printout gives the right values. 
	 * @param message
	 */
	public void writeMessage(int[] message) {
		for(int i = 0; i<message.length ; i++) {
			m_SerialPort.writeByte((byte)message[i]);
		}
	}

	/**
	 * Reads all bytes that are being sent from the elevator, chops it up into strings that 
	 * start and end in 0x7E, and looks through these for messages with message-ID C9, 
	 * and checks what the floor byte is. The last one gets returned (might send several as
	 * the elevator is moving, we want the newest value.)
	 */
	public int whichFloor() {
		
		byte[] byteString = readMessages();
		
		ArrayList messages = findValidSubstrings(byteString); // fills the arraylist with valid substrings
		
		System.out.println("the arraylist now has "+messages.size()+" elements ");

		//returns -1 if no floor is set
		int floor = -1;
		for(int i=0; i<messages.size(); i++) {
			int temp = checkMessage((byte[])messages.get(i));
			if(temp!=-1){ floor = temp;}
		}
		return floor;
	}


	/**
	 * We are looking for messages with message ID 0xc9, the message ID is in 
	 * the 4th byte (indexed 3).
	 * If we have a message of the right kind, we check the 11th byte to see
	 * which floor we are in. It is anded with 00001111 because we don't care about the first part 
	 * of the byte (which tells us if buttons have been pressed from the outside or
	 * inside).
	 */
	public int checkMessage(byte[] message){
		if(message[3]==(byte)0xc9) { // -55 dec = 0xc9 hex
			int val =(int)(message[10]&0x0f);
			//System.out.println("found floor: " + val);
			return val;

		} 
		else {
			return -1;
		}

	}


	/**
	 * 
	 * Looks for the start and stop flag and copies them and the bytes between 
	 * to an ArrayList. 	
	 * If no such substring exist it returns an empty ArrayList, and other parts of the 
	 * program have to check the length of the returned message.
	 * @param byteString
	 * @return
	 */
 
	public ArrayList findValidSubstrings(byte[] byteString)
	{
		ArrayList validSubstrings = new ArrayList();

		int countingIndex = 0;
		int startFlagIndex = -1;
		int stopFlagIndex  = -1;

		boolean endOfByteString = false;

		while(!endOfByteString){
			boolean startFound = false;
			boolean stopFound = false;
			for(int i= countingIndex ; i< byteString.length ; i++) {
				if((byteString[i]==126)&& !startFound)
				{
					startFlagIndex = i;
					startFound = true;
				}
				else if((byteString[i]==126)&& startFound) {
					if(!stopFound) {
						stopFlagIndex = i;
						stopFound = true;
					}
					if(stopFound) {
						addByteStringToArrayList(byteString, startFlagIndex, stopFlagIndex, validSubstrings);
						countingIndex=i+1;
						if(countingIndex>=byteString.length) { endOfByteString = true;}
						break;
					}
				}

			}//end for 

		}// end while
		return validSubstrings;
	}
/**
 * Take the start and the stop index of an interval in the bytestring, and copies the contents into
 * a new bytestring of the same length as the interval. This new bytestring is added to the 
 * ArrayList. 
 * 
 * @param byteString
 * @param startFlagIndex
 * @param stopFlagIndex
 * @param validSubstrings
 */
	private void addByteStringToArrayList(byte[] byteString, int startFlagIndex, int stopFlagIndex, ArrayList validSubstrings) {
		//System.out.println("adding an byte array, start = "+startFlagIndex+" stop ="+stopFlagIndex);

		byte[] message = new byte[stopFlagIndex - startFlagIndex +1];
		int messageIndex = 0 ;
		for(int i = startFlagIndex; i< stopFlagIndex; i++) {
			message[messageIndex] = byteString[i];
			messageIndex++;
		}
		//insert crc check here if ever necessary
		validSubstrings.add(message);

	}
/**
 * Sends a heartbeat to the elevator, who writes status information back. We wait until we have gotten 
 * a long enough message, then this message is returned. 
 * 
 * @return
 */
	public byte[] readMessages() {

		writeMessage(heartbeat1); // have to write the heartbeat to get a reply
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}
		
		//reads all pending bytes being sent
		byte[] readByteString = m_SerialPort.readBytes();
		int waitingCounter = 0;

		//we try for a while until we get a long enough reply
		while(readByteString.length < 5 && waitingCounter<10) {
			try {
				Thread.sleep(1000);
				System.out.println("sleeping");
				readByteString = m_SerialPort.readBytes();
				waitingCounter++;
			} catch (InterruptedException e) {
				
				e.printStackTrace();
				waitingCounter++;
			}
		}
		return readByteString;




	}


	public void printMessage(int[] message) {
		for(int i = 0; i<message.length; i++ ) {
			System.out.println("message i : " + i + "  = " + message[i] );
		}
	}
	public void printMessage(byte[] message) {
		for(int i = 0; i<message.length; i++ ) {
			System.out.println("message i : " + i + "  = " +message[i]+"  (and hex:  "+ Integer.toHexString(message[i])+")");
		}
	}
	public byte[] readBytes() {
		return m_SerialPort.readBytes();
	}

	public void pushButton(int floor) {
		if(floor==0){ writeMessage(floor0);}
		else if(floor==1){ writeMessage(floor0_5);}
		else if(floor==2){ writeMessage(floor1);}
		else if(floor==3){ writeMessage(floor1_5);}
		else if(floor==4){ writeMessage(floor2);}
		else if(floor==5){ writeMessage(floor3);}
		else {System.out.println("out of elevator floor number range");}
	}

	public void pushButton(String floor) {
		if("0".equals(floor)){ writeMessage(floor0);}
		else if("0.5".equals(floor)){ writeMessage(floor0_5);}
		else if("1".equals(floor)){ writeMessage(floor1);}
		else if("1.5".equals(floor)){ writeMessage(floor1_5);}
		else if("2".equals(floor)){ writeMessage(floor2);}
		else if("3".equals(floor)){ writeMessage(floor3);}
		else {System.out.println("out of elevator floor number range");}
	}


// main code 
// for testing purposes
//	public static void main(String[] args) {
//
//		SimpleElevatorController simple = new SimpleElevatorController();
//		int currentFloor = -1;
//
//		simple.pushButton(5);
//try {
//	Thread.sleep(10000);
//} catch (InterruptedException e1) {
//	e1.printStackTrace();
//}
//
//		currentFloor = simple.whichFloor();
//		System.out.println("f0 = 0, f0.5 = 1, f1 = 2, f1.5 = 3, f2 = 4, f3 = 5");
//		System.out.println("current floor is : " + currentFloor);
//
//
//		System.out.println("end of main");
//	}            
}
