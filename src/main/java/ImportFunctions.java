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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class ImportFunctions {

    // inserts gitclone.aoc from https://github.com/OpenLiberty/guides-common
    public static void clone(ArrayList<String> listOfLines, String guideName, int i, String CommonURL) {
        ArrayList<String> temp = new ArrayList<>();
        File common = new File("guides-common/cloud-hosted/" + CommonURL);
        Scanner scanner = null;
        try {
            scanner = new Scanner(common);
            String inputLine = null;
            int counter = 0;
            while (scanner.hasNextLine()) {
                inputLine = scanner.nextLine() + "\n";
                if (inputLine.contains("{: codeblock}")) {
                	continue;
                }
                if (inputLine.startsWith("----")) {
                    counter++;
                }
                if (counter == 1) {
                    inputLine = inputLine.replaceAll("----", "```");
                }
                if (counter == 2) {
                    inputLine = inputLine.replaceAll("----", "```\n");
                    counter = 0;
                }
                inputLine = inputLine.replace("guide-{projectid}", guideName);
                temp.add(inputLine);
            }
            temp.subList(0, 7).clear();
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
        	if (scanner != null)
        		scanner.close();
        }
    }

    public static void KubeStart(ArrayList<String> listOfLines, String guideName, int i, String CommonURL) {
        ArrayList<String> temp = new ArrayList<>();
        FileInputStream ips = null;
        try {
            ips = new FileInputStream("replacements.properties");

            Properties props = new Properties();

            props.load(ips);
            File common = new File("Guides-common/" + CommonURL);
//            URL url = new URL(CommonURL);
//            Scanner s = new Scanner(url.openStream());
            Scanner s = new Scanner(common);
            String inputLine = null;

            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                if (inputLine.startsWith("////")) {
                    continue;
                }

                if (inputLine.startsWith("[.tab_content.windows_section.mac_section]")) {
                    while (!s.nextLine().startsWith("[.tab")) {
                        continue;
                    }
                }

                if (inputLine.startsWith("[.") || inputLine.startsWith("`*") || inputLine.startsWith("[subs=\"") || inputLine.startsWith("ifdef::") || inputLine.startsWith("{") || inputLine.startsWith("endif::") || inputLine.startsWith("ifndef::")) {
                    continue;
                }

//                inputLine = inputLine.replace("guide-{projectid}", guideName);
                temp.add(inputLine);
            }

            for (int n = 0; n < temp.size(); n++) {
                if (temp.get(n).startsWith("```")) {
                    if (temp.get(n - 1).isBlank()) {
                        temp.set(n - 1, "[role='command']");
                    }
                }
            }
            temp.subList(0, 7).clear();
            listOfLines.addAll(i + 1, temp);

            s.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }


    //inserts try what you build-intro from  https://github.com/OpenLiberty/guides-common
    public static void OtherGuidesCommon(ArrayList<String> listOfLines, String guideName, int i, String CommonURL) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            File common = new File("guides-common/" + CommonURL);
//            URL url = new URL(CommonURL);
//            Scanner s = new Scanner(url.openStream());
            Scanner s = new Scanner(common);
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
            s.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts try what you build-intro from  https://github.com/OpenLiberty/guides-common
    public static void newTerminal(ArrayList<String> listOfLines, int i, String CommonURL) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            File common = new File("guides-common/cloud-hosted/" + CommonURL);
            int x = i;
            Scanner s = new Scanner(common);
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                temp.add(inputLine);
            }
            temp.add(0, "\n");
            temp.add(0, "\n");
            temp.add("\n");
            listOfLines.addAll(x + 1, temp);
            temp.add(0, "");
            s.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static void beforeStart(ArrayList<String> listOfLines, int i, String CommonURL, String guideName, String GuideTitle , String GuideDescription) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            File common = new File("guides-common/cloud-hosted/" + CommonURL);
            int x = i;
            Scanner s = new Scanner(common);
            String inputLine = null;

            GuideTitle = GuideTitle.substring(2, GuideTitle.length());
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                if(inputLine.startsWith("# Welcome to the cloud-hosted guide!")) {
                	inputLine = inputLine.replaceAll("cloud-hosted guide!", GuideTitle.trim() + " guide!");
                    temp.add(inputLine);
                    temp.add("\n");
                    temp.add(GuideDescription);
                    temp.add("\n");
                } else {
                    temp.add(inputLine);
                }
            }
            temp.add("\n");
            temp.add("\n");
            listOfLines.addAll(x + 1, temp);
            s.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
