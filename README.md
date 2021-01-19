# cloud-hosted-guide-converter

This repository contains the source code for the guide converter application. This source code once built is used to convert our guides on the openliberty.io/guides page from AsciiDoc to markdown for use on the Skills Network Environment.


# Explaining the Cloud-hosted-guide-converter

The CloudHostedGuideConverter has 4 classes:
  1. CloudHostedGuideConverter
  2. Functions
  3. ImportFunctions
  4. OverridingEquals
  
1\. This is the main class that runs the whole Converter. In this function you can find 3 functions:
   
   * `main`- this function makes the user input the guide name and the branch name. This is then passed to the next function.
   
   
   * `getMD` - This functions reads the guide from the raw github .adoc file and adds it to a Array list. It then kicks off the next class called `Functions`.        Next it uses a String Builder to build the array list and uses the next function to write to a file.
   
   * `writeToFile` - this function writes the built array list into a file.
  
 2\. This function is where all the functionality is. In this class the Array list gets converted using different methods to create a .md format of the Guide. Each functions name is self explainatory (there are also comments to explain each method if the name isn't enough).
 
 3\. This class reads in `guides-commons` files from github. This is because the original guides use `guides-common` to get pre set parts of guides imported. This means that the SN guides will stay up to date with the original guides.
 
 4\. This function allows us to over ride the `.equals()` method of java. When trying to use `.equals()` on a Array List it allows you to compare if a line equals a different line in that list, however we are trying to do `.equals(STRING||INTEGER)`. Because we have over ridden the original function we are now capable of doing so. 

There are also 2 Test files: `TestMain` and `TestConversion`.

1\. `TestMain` - This file holds a few simple JUnit tests that are used to test the actual code of the Cloud Hosted Guide Converter. This test file is run every time someone creates a PR to the `main` branch of the converter.

2\. `TestConversion` - This file holds tests to test the converted guide in the `Cloud-Hosted-Guides` repo. It's results can be found in each one of the PRs created by the GuideConverter action. 
 
 

# Updating the Cloud-hosted-guide-converter

When updating/maintaing the Cloud-hosted-guide-converter:
1. Pull this repository to your local machine and do the changes there. Once the changes are done, try running the guide converter with some guides. Do this by running the following commands from your terminal (make sure you are in the same directory as the GuideConverter files) :
   * `mvn compiler:compile`
  
   * `mvn exec:java -Dexec.args="GUIDE_NAME BRANCH_NAME" `(e.g - ` mvn exec:java -Dexec.args="${{ github.event.inputs.guide_name }} ${branchName:11}"`) 
  
2. Review the converted guides and make sure there is nothing wrong with them and everything has been converted properly.
  
3. If there is something that hasn't converted properly, Look within the `ConditionMethods` in `Functions.java` to see if a method to convert this already      exists. If it does exists looks for reason as to why it might have not converted, this could potentially be something like the CloudHostedGuideConverter has not been set to take under consideration the extra space in the text, majority of the reasons behind incorrect the conversions are very simple. If there is bigger bug then it is recommened to have a look at the original guides `.adoc` file and checking out the text around the part that has not been converted properly. If it is something to do with the `guides-common` please look in `ImportFunctions.java`.
   
4. After the changes to CloudHostedGuideConverter have been tested locally and everything is in working order, create a PR to this repositories `main` branch.
