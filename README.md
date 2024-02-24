# Maximo-Automation
Maximo Automation Testing Tool is a java package used to test maximo web applications automatically using selenium.


## Libraries & Tools
- Selenium WebDriver
- Chrome Driver


## Package Breakdown
This package is made of the following classes & files with their listed functionality.


#### Maximo_Automation.java
> This is the main method file where the automation test process is designed and executed.
<br>
> Note : Every process MUST start by creating a Process Monitor and end by calling the "generateProcessReport()" method.


#### User.java
> This file contains the funcionalities that mimics the user's behaviour and allows designing testing processes accordingly.


#### Process_Monitor.java
> This file contains  funcionalities that monitor the testing process from start to end and reports accordingly.

#### config.properties
> This file contains properties used to lead the process accordingly.