/**
 * Copyright (C) 2015 ADITYA T 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
 
package Rpi_Server;

import java.net.*;
import java.util.*;
import java.io.*;
import Rpi_Server.Transfer_Video.Input_Received;
import javax.swing.*;
import processing.core.*;
import processing.video.*;
public class Rpi_Server_Java
{
    /* Basically handles the request from the client to connect to the 
       Raspberry Pi, has a capacity of handling 100 clients, There
       is still an issue about buffer overflow when the android
       tablet is tilted for a long duration in anyone direction.
       It will be rectified at the earliest*/
    Rpi_Server_Java() throws Exception
    {
    	/* Entry class for accepting clients. Bifurcation of the
    	   part accepting the clients and the part handling the clients,
    	   helps in better performance and service providability to 
    	   individual clients without hampering the connectivity
    	   to new clients*/
    	ServerSocket soc=new ServerSocket(9000,100);
        System.out.println("Waiting for connection .....");
        while(true)
        {    
            Socket CSoc=soc.accept();
            /* On being connected to a new client, new instace of 
               the class providing the service ,which is 'AcceptClient'
               is created*/
            new AcceptClient(CSoc);
        }
    }
    public static void main(String args[]) throws Exception
    {
        new Rpi_Server_Java();
    }
}

class AcceptClient extends JFrame implements Runnable
{
    Socket ClientSocket; // socket to hold the object passed by the entry class
    static BufferedReader br; // read from the input stream, i.e from the client
    static BufferedWriter pr; // write to the output stream, i.e to the client
    Thread t; // define new thread object
    static ArrayList<Socket> cliSoc; // To hold the lsit of clients connected 
    static ArrayList<String>Log; // Log the messages sent by individual clients
    /*static 
    {
	System.loadLibrary("cprogstart");
	System.loadLibrary("cprogsearch");
	System.loadLibrary("cprogfront");
	System.loadLibrary("cprogback");
	System.loadLibrary("cprogleft");
	System.loadLibrary("cprogright");
	System.loadLibrary("cprogcam");
    }
    public static  native void start1();
    public static native void search(String h);
    public static native void front();
    public static native void back();
    public static native void left();  
    public static native void right();
    public static native void cam();*/
    
    AcceptClient (Socket CSoc) throws Exception
    {
    	
        cliSoc=new ArrayList<Socket>();
        Log=new ArrayList<String>();
        ClientSocket=CSoc;
        setSize(600,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        br=new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
        pr=new BufferedWriter(new OutputStreamWriter(ClientSocket.getOutputStream()));
        
        String LoginName=br.readLine();
        pr.write("Connected");
        pr.newLine();
        pr.flush();
        System.out.println("User Logged In :" + LoginName);
        Log.add(LoginName);
        cliSoc.add(ClientSocket); 
        t=new Thread(this); 
        t.start
        /* Encapsulate reading from the client , in 'run' block nd let it run along its
           own thread due to the unpredictability of receiving data from the client. 
           In simple words, the program has no idea when its going to receive data from 
           the client and so instead of using loops which suspend the execution of the current thread
           which involves accepting clients, make it run as a new thread thus the program now
           independently and separately processes connection request and data sent through by
           the client */
        
    }

    public void run()
    {
        while(true)
        {
            
            try
            {
                String msgFromClient=new String();
                msgFromClient=br.readLine();
				String f=msgFromClient.substring(msgFromClient.lastIndexOf(' ')+1,msgFromClient.length());
				System.out.println(f);
				f.trim();
				if(f.equalsIgnoreCase("start"))
				{
						pr.write("Starting");
						pr.newLine();
						pr.flush();
						//start1();
		         	}
				else if(f.startsWith("search"))
				{
						
						pr.write("Searching");
						pr.newLine();
						pr.flush();
						f=f+" ";
						String g=f.substring(f.indexOf("-"),f.length()-1);
						//search(g);
				}
				else if(f.equalsIgnoreCase("front"))
				{
						pr.write("Moving front");
						pr.newLine();
                        			pr.flush();
						//front();
				}
				else if(f.equalsIgnoreCase("back"))
				{
						pr.write("Moving back");
						pr.newLine();
						pr.flush();
						//back();
				}
				else if(f.equalsIgnoreCase("left"))
				{
						pr.write("Moving left");
						pr.newLine();
						pr.flush();
						//left();
				}
				else if(f.equalsIgnoreCase("right"))
				{
						pr.write("Moving Right");
						pr.newLine();
						pr.flush();
						//right();
				}
				else if (f.equalsIgnoreCase("cam"))
				{
						pr.write("Initializing cam");
						pr.newLine();
						pr.flush();
						setVisible(true);
						processing.core.PApplet s=new Transfer_Video();
						add(s);
						s.init();
				}
				else if(!(Transfer_Video.set) && Character.isDigit(f.charAt(0)))
				{
					System.out.println("check 1  "+f);
					new Input_Received(f).start();
				}

                StringTokenizer st=new StringTokenizer(msgFromClient);
                String Sendto=st.nextToken();                
                String MsgType=st.nextToken();
                int i=0;
                
                if(MsgType.equals("LOGOUT"))
                {
                    for(i=0;i<Log.size();i++)
                    {
                        if(Log.get(i).equals(Sendto))
                        {
                            Log.remove(i);
                            cliSoc.remove(i);
                            //Transfer_Video.v.stop();
                           // Transfer_Video.set=false;
                            System.out.println("User " + Sendto +" Logged Out ...");
                            break;
                        }
                    }
    
                }
                else
                {
                    String msg="";
                    while(st.hasMoreTokens())
                    {
                        msg=msg+" " +st.nextToken();
                    }
                    for(i=0;i<Log.size();i++)
                    {
                        if(Log.get(i).equals(Sendto))
                        {    
                            Socket tSoc=(Socket)cliSoc.get(i);                            
                            BufferedWriter tdout=new BufferedWriter(new OutputStreamWriter(tSoc.getOutputStream()));
                            tdout.write(msg);
                            tdout.newLine();
                            tdout.flush();
                            break;
                        }
                    }
                    
                }
                if(MsgType.equals("LOGOUT"))
                {
                    break;
                }

            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            
        }        
    }
}



