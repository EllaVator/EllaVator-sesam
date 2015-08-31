package com.tastyminerals.sesam.sources;

// simple runner for sesam sources
import java.util.Scanner;

public class ElevatorRunner {
    public static void main(String[] args) {
        //PropertiesWriter.createProperties();
        final SimpleElevatorController sec = new SimpleElevatorController();
        final Scanner scan = new Scanner(System.in);
        String message = "";
        System.out.println("Welcome to Elevator control demo based on sesam source files!");
        while (true) {
            System.out.println("Choose your floor number [0, 0.5, 1, 1.5, 2, 3] or send a hearbeat [-1]:");
            System.out.println("Hit ENTER to call whichFloor() method.");
            message = scan.next();
            switch(message) {
                case "0": 
                    sec.pushButton(message);
                    break;
                case "0.5":
                    sec.pushButton(message);
                    break;
                case "1":
                    sec.pushButton(message);
                    break;
                case "1.5":
                    sec.pushButton(message);
                    break;
                case "2":
                    sec.pushButton(message);
                    break;
                case "3":
                    sec.pushButton(message);
                    break;
                case "-1":
                    sec.readMessages();
                    break;
                case "exit":
                    scan.close();
                    System.exit(0);
                    break;
                default:
                    sec.whichFloor();
                    break;
            }
        }
    }
}
