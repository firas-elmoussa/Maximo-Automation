
package maximo_automation;

public class Maximo_Automation {


    public static void main(String[] args) throws InterruptedException {
        
        //CREATE A PROCESS MONITOR
        Process_Monitor processAdmin = new Process_Monitor(); //ALWAYS EXECUTE AT THE BEGINNING OF THE PROCESS
        
        //CREATING A USER OBJECT FOR ANY MAXIMO ENV
        User maxadmin = new User();
        
        //LOGGING IN
        maxadmin.login();

        //NAVIGATE TO ANY APP
        maxadmin.searchNavigate("Work Order Tracking");
       
        //QUERY RECORDS
        maxadmin.queryRecords("Description = 'This WO was created automatically.'");
        
        //DOWNLOAD RESULTS
        maxadmin.downloadRecords();
        
        //CREATE NEW RECORD WITH A GIVEN DESC
        //maxadmin.createRecord("This WO was created automatically.");
        
        //QUERY RECORDS
        //maxadmin.queryRecords("Description = 'This WO was created automatically.'");
        
        //NAVIGATE TO ANY APP
        //maxadmin.searchNavigate("Service Request");
        
        
       //LOGGING OUT
       maxadmin.logout();
       
       
       //GENERATE A REPORT AFTER THE END OF THE PROCESS
       processAdmin.generateProcessReport(); //ALWAYS EXECUTE AT THE END OF THE PROCESS
       
    }
    
}
