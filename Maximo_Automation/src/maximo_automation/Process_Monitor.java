

/**
 * 
 * NOTE : THIS CLASS REPRESENTS AN OBJECT WITH FUNCTIONALITIES RELATED TO MONITOR THE AUTOMATED PROCESS
 * THE USER OBJECT IS USED TO EXECUTE ANY PROCESS RELATED CHECKS & COMMANDS
 * 
 */


package maximo_automation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Process_Monitor  {

    //CAPTURED OUTPUT
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream capturePrintStream = new PrintStream(outputStream);
    
 
    //CREATE PROCESS MONITOR
    public Process_Monitor() {
        
        
        // CUSTOM PRINTSTREAM TO CAPTURE OUTPUT BEFORE PRINTING IT OUT
        PrintStream customPrintStream = new PrintStream(System.out) {
            @Override
            public void println(String x) {
                super.println(x);
                capturePrintStream.println(x); // Capture the output
            }
        };

        // Replace System.out with the custom PrintStream
        System.setOut(customPrintStream);
        
    }

   
    //CREATE REPORT
    public  void generateProcessReport() {
        
         String folderPath = "process report";

        //RETRIEVING CAPTURED TEXT
        String capturedOutput = outputStream.toString();

        //CREATE A TIMESTAMP FOR THE REPORT FILE NAME
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm");
        String timestamp = dateFormat.format(new Date());

        //CREATE THE FILE NAME WITH TIMESTAMP
        String fileName = "Process_Report_" + timestamp + ".txt";

        //CREATE THE FULL FILE PATH
        String filePath = folderPath + File.separator + fileName;

        try {
            //CREATE THE REPORT FILE
            File reportFile = new File(filePath);
            reportFile.createNewFile();

            //WRITE THE CAPTURED OUTPUT TO THE REPORT FILE
            FileWriter fw = new FileWriter(reportFile);
            fw.write(capturedOutput);
            fw.close();

            System.out.println("Process report generated successfully at: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating process report: " + e.getMessage());
        }
    } //END CREATE REPORT
    
     
}