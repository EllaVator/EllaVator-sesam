package com.tastyminerals.sesam.sources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class PropertiesWriter {
    
    public static void createProperties(){
        String archivoConf = 
            System.getProperty("java.home") + File.separator + "lib" + 
            File.separator + "javax.comm.properties";
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(archivoConf));
            
            out.write(
                "#"+ "\n" + 
                "# Drivers loaded by the Java Communications API standard extension"+ "\n" + 
                "# at initialization time "+ "\n" + 
                "# "+ "\n" + 
                "# Format: "+ "\n" + 
                "#   Each line must contain ONE driver definition only "+ "\n" + 
                "#   Each line must be of the form: "+ "\n" + 
                "#           driver=<ClassName> "+ "\n" + 
                "#       No spaces or tabs in the line. "+ "\n" +
                "#       ClassName must implement the interface javax.comm.CommDriver "+ "\n" + 
                "#           example: driver=Win32Serial"+ "\n" + 
                "#   "+ "\n" + 
                "#"+ "\n" + 
                "# The hash(#) character indicates comment till end of line."+ "\n" + 
                "#"+ "\n" + 
                "# Windows Serial Driver"+ "\n" + 
                "Driver=gnu.io.RXTXCommDriver" + "\n"); 
            
            out.close();
        } catch (IOException e) {
            System.err.println("No se pudo crear el archivo:" + "javax.comm.properties");
            System.err.println("Ruta completa:" + archivoConf);
        }

    }
}