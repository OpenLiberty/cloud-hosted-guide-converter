/*******************************************************************************
 * Copyright (c) 2020, 2022 IBM Corporation and others.
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
        String gitLab = args.length > 2 ? args[2] : "";
        convertToMD(guideName, branch, gitLab);
        System.out.println("Guide converted");
        System.out.println("Find markdown in " + guideName + ".md");

    }

    // Reads the adoc from github, and writes it to an arraylist
    public static void convertToMD(String guideName, String branch, String gitLab) throws IOException {
        Scanner scanner = null;
        FileInputStream lrpfis = null;
        FileInputStream rpfis = null;

        try {
            //read adoc file from the open liberty guide
            File guide = new File(guideName + "/README.adoc");
            scanner = new Scanner(guide);
            // ArrayList to hold the whole converted markdown format content
            ArrayList<String> mdContent = new ArrayList<>();
            
            int m = Functions.addSNLMetadata(mdContent, guideName, gitLab);

            String guideTitle = null;
            String guideDescription = null;

            Properties loopReplacementsProps = new Properties();
            Properties replacementsProps = new Properties();

            lrpfis = new FileInputStream("loopReplacements.properties");
            rpfis = new FileInputStream("replacements.properties");

            loopReplacementsProps.load(lrpfis);
            replacementsProps.load(rpfis);

            // write each line into the file
            while (scanner.hasNextLine()) {
                String inputLine = scanner.nextLine() + "\n";

                // process the guide title and description
                if (inputLine.startsWith("= ")) {
                    guideTitle = inputLine;
                    if (!scanner.nextLine().isEmpty() || !scanner.nextLine().isBlank()) {
                        scanner.nextLine();
                        scanner.nextLine();
                        guideDescription = scanner.nextLine();
                        guideDescription = guideDescription.substring(guideDescription.lastIndexOf(":") + 1, guideDescription.length());
                    }
                    continue;
                }

                // skip the Windows and Mac specific content
                if (inputLine.startsWith("[.tab_content.windows_section.mac_section]")) {
                    while (!scanner.nextLine().startsWith("[.tab_content.linux_section]")) {
                        continue;
                    }
                }

                // skip the Windows specific content
                if (inputLine.startsWith("[.tab_content.windows_section]")) {
                    while (!scanner.nextLine().startsWith("[.tab_content.mac_section")) {
                        continue;
                    }
                }

                // skip the static guide content that marked by ifndef::cloud-hosted
                if (inputLine.startsWith("ifndef::cloud-hosted[]")) {
                    while (!scanner.nextLine().startsWith("endif::[]")) {
                        continue;
                    }
                }

                mdContent.add(inputLine);
            }

            // Runs the src.main.java.Functions.class
            Functions.addPriorStep1(mdContent, m, guideName, guideTitle, guideDescription);
            Functions.ConditionsMethod(mdContent, guideName, branch, loopReplacementsProps, replacementsProps);
            Functions.end(mdContent, guideName, guideTitle);

            // Convert the listOfLines to StringBuilder
            StringBuilder builder = new StringBuilder();
            for (String value : mdContent) {
            	if (value.contains("{: codeblock}"))
            		continue;
                builder.append(value);
            }

            // Write the converted content to the file
            writeToFile(builder.toString(), guideName);

        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (scanner != null) {
                scanner.close();
                lrpfis.close();
                rpfis.close();
            }
        }
    }

    // append to md file
    public static void writeToFile(String str, String guideName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(guideName + ".md"));
        writer.append(str + "\n");
        writer.close();
    }
}
