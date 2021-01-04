 /*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/


import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class CloudHostedGuideConverter {

    //asks user for the guide name
    public static void main(String[] args) throws Exception {

        String guideName = args[0];
        String branch = args[1];
        getMD(guideName, branch);
        System.out.println("Guide converted");
        System.out.println("Find markdown in " + guideName + ".md");

    }

    // Reads the adoc from github, and writes it to an arraylist
    public static void getMD( String guideName, String branch) throws IOException {
        Scanner s = null;
        FileInputStream ip = null;
        FileInputStream ips = null;

        try {
            //read adoc file from the open liberty guide
            File guide = new File("README.adoc");
//            URL url = new URL("https://raw.githubusercontent.com/openliberty/" + guideName + "/" + branch + "/README.adoc");
            s = new Scanner(guide);
//          ArrayList for whole text file
            ArrayList<String> listOfLines = new ArrayList<>();

            Properties prop = new Properties();
            Properties props = new Properties();

            ip = new FileInputStream("loopReplacements.properties");
            ips = new FileInputStream("replacements.properties");

            prop.load(ip);
            props.load(ips);


//          write each line into the file
            while (s.hasNextLine()) {
                String inputLine = s.nextLine() + "\n";

                if (inputLine.startsWith("[.tab_content.windows_section.mac_section]")) {

                    while (!s.nextLine().startsWith("[.tab_content.linux_section]")) {
                        continue;
                    }
                }


                listOfLines.add(inputLine);
            }


            // Runs the src.main.java.Functions.class
            Functions.ConditionsMethod(listOfLines, guideName, branch, prop, props);

            //String builder to format the arraylist
            StringBuilder builder = new StringBuilder();
            for (String value : listOfLines) {
                builder.append(value);
            }

            String text = builder.toString();

            writeToFile(text, guideName);

        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (s != null){
                s.close();
                ip.close();
                ips.close();
            }
        }
    }

    // append to md file
    public static void writeToFile(String str, String guideName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(guideName + ".md"));
        writer.append("\n" + str);
        writer.close();
    }
}
