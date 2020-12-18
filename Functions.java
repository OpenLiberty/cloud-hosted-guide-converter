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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class OverridingEquals {

    private String inArray;

    public OverridingEquals(String inArray) {
        this.inArray = inArray;
    }


    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof OverridingEquals)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        OverridingEquals c = (OverridingEquals) o;

        // Compare the data members and return accordingly
        return CharSequence.compare(inArray, c.inArray) == 1;
    }
}

public class Functions {

    public static final String codes = "----";

    // Replaces the dashes which stand for a codeblock in adoc with backticks which are codeblocks in md
    public static void replaceCodeBlocks(ArrayList<String> listOfLines, int i) {
        listOfLines.get(i).replaceAll("----", "```");
//        listOfLines.set(i,"```\n");
    }

    // This function uses a for loop to remove pre-set lines/words from the guide
    public static void removingIrrelevant(ArrayList<String> listOfLines, int i, String[] str, int h) {

        if (listOfLines.get(i).startsWith(str[h])) {
            listOfLines.set(i, "");
        }
    }

    // This function removes the reference to any diagrams that were in the guide. This is becasue we do not convert images/diagrams, we remove them.
    public static void removeDiagramReference(ArrayList<String> listOfLines, int i) {
        ArrayList<String> list = new ArrayList<String>();
        for (int x = 0; x <= 7; x++) {
            String temp = listOfLines.get(i + x);

            list.add(temp);
        }

        for (String e : list) {
            if (e.startsWith("image::")) {
                if (listOfLines.get(i).indexOf("diagram") != -1) {
                    String noDiagram = listOfLines.get(i).substring(0, listOfLines.get(i).indexOf(".")) + ".";
                    listOfLines.set(i, listOfLines.get(i).replace(listOfLines.get(i), noDiagram));
                    listOfLines.remove(i + 1);
                }
            }
        }
    }

    // This is a function that inserts {: cdodeblock} after a codeblock
    public static void insertCopyButton(ArrayList<String> listOfLines, int i) {
        ArrayList<String> check = new ArrayList<>();
        int y = 0;
        for (int x = 0; x <= 7; x++) {
            y = i + x;
            check.add(listOfLines.get(y));
            if (check.get(x).startsWith("```")) {
                if (listOfLines.get(y + 1).isBlank()) {
                    listOfLines.set(y + 1, "{: codeblock}\n\n\n");
                }
            }
        }
    }

    // This removes any windows commands that are use in the guides. This is because we use a pre installed environment given to the users online. This environment is a linux OS so there for windwows commands are not required.
    public static void removeWindowsCommand(ArrayList<String> listOfLines, int i) {
        int counter = 0;
        for (int x = 0; x < 6; x++) {
            int y = x + i;
            if (listOfLines.get(y).startsWith("--")) {
                counter++;
            }
            if (counter != 2) {
                listOfLines.set(y, "");
                listOfLines.set(y + 1, "");
            }
        }
    }

    // This line replaces any dashes as long as they are not part of the testblock.
    public static void replaceDashes(ArrayList<String> listOfLines, int i) {
        if (!listOfLines.get(i).startsWith("--------")) {
            listOfLines.set(i, "```\n");
        }
    }

    // Inserts code snippets
    public static void codeInsert(String atIndex, ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        for (int x = 0; x < 10; x++) {
            int y = x + i;
            if (listOfLines.get(y).startsWith("----")) {
                listOfLines.set(y, "");
            }
        }
        int g = i + 1;
        if (atIndex.startsWith("#Create")) {
            touch(listOfLines, guideName, branch, g, position);
        }
        if (atIndex.startsWith("#Update")) {
            update(listOfLines, guideName, branch, g, position);
        } else if (atIndex.startsWith("#Replace")) {
            replace(listOfLines, guideName, branch, g, position);
        }
    }

    // Removes the "Additional pre-reqs" section
    public static void removeAdditionalpres(ArrayList<String> listOfLines, int i) {
        while (!listOfLines.get(i).startsWith("[role='command']")) {
            listOfLines.remove(i);
        }
    }

    //Don't know what this does
    public static void removeLast(String guideName) {
        try {
            java.io.RandomAccessFile file = new java.io.RandomAccessFile(guideName + ".md", "rw");
            byte b = 0;
            long pos = file.length();
            while (b != '\n' && --pos >= 0) {
                file.seek(pos);
                b = file.readByte();
            }
            file.seek(++pos);
            file.setLength(pos);
            file.write("".getBytes());
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    // Inserts all the Guides-common
    public static void commons(ArrayList<String> listOfLines, String guideName, int i) {
        // The 2 following if statements are used to get from Guides-Common
        String CommonURL = "https://raw.githubusercontent.com/OpenLiberty/guides-common/dev/";
        String GuidesCommon = null;


        if (listOfLines.get(i).startsWith("include::{common-includes}/gitclone.adoc[]")) {

            GuidesCommon = listOfLines.get(i).substring(27, listOfLines.get(i).length() - 3);

            CommonURL = CommonURL + GuidesCommon;

            ImportFunctions.clone(listOfLines, guideName, i, GuidesCommon);
        }

        if (listOfLines.get(i).startsWith("include::{common-includes}/kube-start.adoc[]") || listOfLines.get(i).startsWith("include::{common-includes}/kube-minikube-teardown.adoc[]")) {

            GuidesCommon = listOfLines.get(i).substring(27, listOfLines.get(i).length() - 3);

            CommonURL = CommonURL + GuidesCommon;

            ImportFunctions.KubeStart(listOfLines, guideName, i, GuidesCommon);
        }

        if (listOfLines.get(i).startsWith("include::{common-includes}/") && !listOfLines.get(i).startsWith("include::{common-includes}/attribution.adoc[subs=\"attributes\"]") && !listOfLines.get(i).startsWith("include::{common-includes}/gitclone.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/os-tabs.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/kube-prereq.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/kube-start.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/kube-minikube-teardown.adoc[]")) {

            GuidesCommon = listOfLines.get(i).substring(27, listOfLines.get(i).length() - 3);

            CommonURL = CommonURL + GuidesCommon;

            ImportFunctions.OtherGuidesCommon(listOfLines, guideName, i, GuidesCommon);
        }
    }

    // This function adds in the last steps of a guide.
    public static void finish(ArrayList<String> listOfLines, String lastLine, String guideName, int i) {
        String finish = "## Nice work!\n\n" + lastLine + "\n# Summary\n\n## Clean up your environment\n\nClean up your online environment so that it is ready to be used with the next guide!\n\nYou can clean up the environment by doing the following:\n\nDelete the **" + guideName + "** project by navigating to the **/home/project/** directory\n\n```\ncd /home/project\nrm -fr " + guideName + "\n```\n{: codeblock}\n\nNow Log out by navigating to: \n\n> [Account -> Logout]\n\n";
        listOfLines.set(i,finish);
    }

    //configures instructions to replace file
    public static String replace(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        String str = listOfLines.get(i).replaceAll("`", "");
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> [File -> Open...]  \n> " + guideName + "/start/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.add(i, "\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        codeSnippet(listOfLines, guideName, branch, i + 2, str);
        position = "main";
        return position;
    }

    //configures instructions to update file
    public static String update(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        String str = listOfLines.get(i).replaceAll("`", "");
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> [File -> Open...]  \n> " + guideName + "/start/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        listOfLines.add(i, "\n");
        codeSnippet(listOfLines, guideName, branch, i + 2, str);
        position = "main";
        return position;
    }

    //configures instructions to create file
    public static String touch(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        String str = listOfLines.get(i).replaceAll("`", "");
        listOfLines.set(i, "```\n" + "touch " + str + "```" + "\n{: codeblock}\n\n\n");
        listOfLines.set(i, "\n> [File -> New File]  \n> " + guideName + "/start/" + str + "\n\n\n");
        listOfLines.add(i, "\n");
        codeSnippet(listOfLines, guideName, branch, i + 2, str);
        position = "main";
        return position;
    }

    //configures link
    public static void link(ArrayList<String> listOfLines, int i) {
        String linkParts[] = new String[2];
        String findLink[];
        String link;
        String description;
        String formattedLink;
        String localhostSplit[];
        String findDescription[];
        listOfLines.set(i, listOfLines.get(i).replaceAll("\\{", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("\\}", ""));
        linkParts = listOfLines.get(i).split("\\[");
        findDescription = linkParts[1].split("\\^");
        description = findDescription[0];
        findLink = linkParts[0].split(" ");
        link = findLink[findLink.length - 1];
        if (link.contains("localhost")) {
            if (listOfLines.get(i).contains(".")) {
                localhostSplit = listOfLines.get(i).split("\\.");
                listOfLines.set(i, listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", ""));
                if (localhostSplit.length == 2) {
                    listOfLines.set(i, localhostSplit[0] + localhostSplit[1] + ("\n```\ncurl " + link + "\n```\n{: codeblock}\n\n\n"));
                } else {
                    listOfLines.set(i, localhostSplit[0] + ("\n```\ncurl " + link + "\n```\n{: codeblock}\n\n\n"));
                }
                return;
            } else {
                listOfLines.set(i, listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", ("\n```\ncurl " + link + "\n```\n{: codeblock}\n\n\n")));
            }
        }
        formattedLink = "[" + description + "](" + link + ")";
        listOfLines.set(i, listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", formattedLink));
    }

    // general text configuration
    public static ArrayList<String> mains(ArrayList<String> listOfLines, Properties prop, Properties props) {

        for (int i = 0; i < listOfLines.size(); i++) {
            if (!listOfLines.get(i).startsWith("[.hidden]")) {
                if (listOfLines.get(i).startsWith("----")) {
//                    listOfLines.set(i, "" + "\n");
                } else {
                    //For loop that changes all the properties in the text that match with the ones in the loopReplacements.properties file
                    for (String key : prop.stringPropertyNames()) {
                        String value = prop.getProperty(key);
                        listOfLines.set(i, listOfLines.get(i).replaceAll(key, value));
                    }

                    // Removes --
                    if (listOfLines.get(i).startsWith("--")) {
                        listOfLines.set(i, "");
                    }

                    //Uses replacements.properties to change some more properties in the text
                    if (listOfLines.get(i).startsWith("== ")) {
                        if (!listOfLines.get(i).startsWith("== What you'll learn")) {
                            listOfLines.set(i, listOfLines.get(i).replaceAll("==", props.getProperty("==")));
                        }
                    }

                    //Uses replacements.properties to change some more properties in the text
                    if (listOfLines.get(i).startsWith("= ")) {
                        listOfLines.set(i, listOfLines.get(i).replaceAll("=", props.getProperty("=")));
                    }

                    //Uses replacements.properties to change some more properties in the text
                    if (listOfLines.get(i).startsWith("==")) {
                        listOfLines.set(i, listOfLines.get(i).replaceAll("=", props.getProperty("=")));
                    }
                }
            }
        }
        return listOfLines;
    }


    //inserts code snippet (Finds the right code snippet and inserts it into the text
    public static ArrayList<String> codeSnippet(ArrayList<String> listOfLines, String guideName, String branch, int i, String path) {
        try {
            ArrayList<String> code = new ArrayList<String>();
            URL url = new URL("https://raw.githubusercontent.com/openliberty/" + guideName + "/" + branch + "/finish/" + path);
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            code.add("```\n");
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                if (!inputLine.replaceAll(" ", "").startsWith("/")) {
                    if (!inputLine.startsWith("*")) {
                        if (!inputLine.startsWith(" *")) {
                            if (!inputLine.startsWith("#")) {

                                code.add(inputLine);
                            }
                        }
                    }
                }
            }
            code.add("```\n{: codeblock}\n\n\n");
            listOfLines.addAll(i, code);
        } catch (IOException ex) {

            System.out.println(ex);
        }
        return listOfLines;
    }


    //    // Configures tables
    public static String table(ArrayList<String> listOfLines, int i, Properties props) {
        if (listOfLines.get(i).startsWith("|===")) {
            listOfLines.set(i, "");
            int counter = 0;
            String line = listOfLines.get(i + 1);
            for (int n = 0; n < line.length(); n++) {
                if (line.charAt(n) == '|') {
                    counter++;
                    System.out.println(counter);
                }
            }
            if (counter == 2) {
                listOfLines.set(i + 1, line + props.getProperty("2") + "\n");
            } else if (counter == 3) {
                listOfLines.set(i + 1, line + props.getProperty("3") + "\n");
            } else if (counter == 4) {
                listOfLines.set(i + 1, line + props.getProperty("4") + "\n");
            } else if (counter == 5) {
                listOfLines.set(i + 1, line + props.getProperty("5") + "\n");
            }
            return "main";
        }
        if (listOfLines.get(i).startsWith("     ")) {
            listOfLines.set(i, listOfLines.get(i).replaceAll("                            ", ""));
            return "main";
        }
        if (listOfLines.get(i).startsWith("| *")) {
            int count = 0;
            for (int x = 0; x < listOfLines.get(i).length(); x++) {
                if (listOfLines.get(i).charAt(i) == '*') {
                    count++;
                }
            }
            String split = "";
            for (int j = 0; j < count / 2; j++) {
                split = split + "|---";
            }
            listOfLines.set(i, split);
            return "table";
        }
        return "table";
    }


    //Main function that runs all the methods
    public static void ConditionsMethod(ArrayList<String> listOfLines, String guideName, String branch, Properties prop, Properties props) throws IOException {

        String position = "";
        final String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "image::", "start/", "finish/", "system/", "inventory/"};
        // Main for loop
        for (int i = 0; i < listOfLines.size(); i++) {

            // Function to add related Guides. (Not Completed)
//            if (listOfLines.get(i).startsWith(":page-related-guides:")) {
//                String line = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[")+1,listOfLines.get(i).indexOf("]"));
//                String[] relatedGuides = line.trim().split("\\s*,\\s*");
//                for (String e:relatedGuides){
//                    String relatedGuidesName = e.substring(1, e.length()-1);
//                    String[] fullLinks = {"https://openliberty.io/guides/" + relatedGuidesName + ".html"};
//                    System.out.println(Arrays.toString(fullLinks));
//                    if (i == listOfLines.size()) {
//
//                    }
//                }
//            }

            String pattern2 = "`(.*?)`";

            Pattern r2 = Pattern.compile(pattern2);

            Matcher m2 = r2.matcher(listOfLines.get(i));

            if (m2.find() && !listOfLines.get(i).startsWith("```")) {
                listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
            }


            OverridingEquals c1 = new OverridingEquals(listOfLines.get(i));
            OverridingEquals c2 = new OverridingEquals(codes);

            // Runs the commons Functions
            commons(listOfLines, guideName, i);

            // Changes ---- to ```
            if (c1.equals(c2)) {
                replaceCodeBlocks(listOfLines, i);
            }

            // removes un-used code blocks
            if (listOfLines.get(i).startsWith("[role=\"code_command")) {
                listOfLines.set(i + 1, "");
                listOfLines.set(i + 4, "");
                listOfLines.set(i + 5, "");
            }

            String pattern = "\\[source(.*?)linenums";

            Pattern r = Pattern.compile(pattern);

            Matcher m = r.matcher(listOfLines.get(i));

            // removes code examples
//            if (listOfLines.get(i).startsWith("[source, Java, linenums") || listOfLines.get(i).startsWith("[source,java,linenums") || listOfLines.get(i).startsWith("[source, java, linenums,") || listOfLines.get(i).startsWith("[source, xml, linenums,") || listOfLines.get(i).startsWith("[source , xml, linenums,")|| listOfLines.get(i).startsWith("[source,xml,linenums")|| listOfLines.get(i).startsWith("[source, XML, linenums,") || listOfLines.get(i).startsWith("[source, Text, linenums") || listOfLines.get(i).startsWith("[source, text, linenums,") || listOfLines.get(i).startsWith("[source, json, linenums,") || listOfLines.get(i).startsWith("[source, JSON, linenums,")) {

            if (m.find()) {
                listOfLines.set(i - 1, "");
                listOfLines.set(i + 1, "");
                listOfLines.set(i + 3, "");
            }

            // Replaces left over ----
            if (listOfLines.get(i).startsWith("----")) {
                replaceDashes(listOfLines, i);
            }


            //For parts of text that need to be copied
            if (listOfLines.get(i).startsWith("[role='command']") || listOfLines.get(i).startsWith("[role=command]")) {
                insertCopyButton(listOfLines, i);
            }

            //User is instructed to replace a file
            if (listOfLines.get(i).startsWith("#Replace") || listOfLines.get(i).startsWith("#Create") || listOfLines.get(i).startsWith("#Update")) {
                final String atIndex = listOfLines.get(i);

                codeInsert(atIndex, listOfLines, guideName, branch, i, position);
            }

            //Removes references to images
            if (listOfLines.get(i).indexOf("diagram") != -1) {
                removeDiagramReference(listOfLines, i);
            }

            if (listOfLines.get(i).startsWith("image::")) {
                if (listOfLines.get(i + 1).startsWith("*")) {
                    listOfLines.remove(i + 1);
                } else if (listOfLines.get(i + 2).startsWith("*")) {
                    listOfLines.remove(i + 2);
                }
            }

            //Removes Additional prerequisites section
            if (listOfLines.get(i).startsWith("## Additional prerequisites") || listOfLines.get(i).startsWith("# Additional prerequisites")) {
                removeAdditionalpres(listOfLines, i);
            }


            //Current line is an example output of a mvn test
//            if (position.equals("testBlock")) {
//                if (!listOfLines.get(i).startsWith("[INFO]")) {
//                    listOfLines.set(i, "```\n");
//                    position = "main";
//                } else {
//                    listOfLines.set(i, listOfLines.get(i));
//                }
//            }

            //Identifies an instruction for windows only and skips the current line
            if (listOfLines.get(i).startsWith("[.tab_content.windows_section]")) {
                removeWindowsCommand(listOfLines, i);
            }


            //Identifies that line is the start of an example output of a mvn test
//            if (listOfLines.get(i).startsWith("[INFO]")) {
//                position = "testBlock";
//                listOfLines.set(i, "```\n" + listOfLines.get(i));
//            }


//            //Identifies that line is the start of a table
            if (listOfLines.get(i).startsWith("|===")) {
                table(listOfLines, i, props);
                listOfLines.set(i + 1, listOfLines.get(i + 1).replaceAll("\\*", ""));
            }

            //Finds title so we skip over irrelevant lines
            if (listOfLines.get(i).startsWith("= ")) {
                listOfLines.set(i, listOfLines.get(i).replaceAll("=", "#"));
            }

            if (listOfLines.get(i).startsWith("[.h")) {
                listOfLines.set(i, "");
                if (!listOfLines.get(i + 1).isBlank()) {
                    listOfLines.set(i + 1, "");
                    if (!listOfLines.get(i + 2).isBlank()) {
                        listOfLines.set(i + 2, "");
                    }
                }

            }

            //Identifies another heading after the intro so we stop skipping over lines
            if (listOfLines.get(i).startsWith("== ")) {
                position = "main";
            }

            if (listOfLines.get(i).startsWith("[source")) {
                removeLast(guideName);
            }

            if (listOfLines.get(i).contains("^]")) {
                if (listOfLines.get(i).contains("https://")) {
                    String link = listOfLines.get(i).substring(listOfLines.get(i).indexOf("https://"), listOfLines.get(i).indexOf("["));
                    String linkDesc = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[") + 1, listOfLines.get(i).indexOf("^"));
                    String fullLink = "[" + linkDesc + "]" + "(" + link + ")";
                    if (listOfLines.get(i).indexOf("https://") != -1) {
                        String check = listOfLines.get(i).replaceAll("https:(.*?)]", fullLink);
                        listOfLines.set(i, check);
                    }
                }
            }

            if (listOfLines.get(i).contains("^]")) {
                if (listOfLines.get(i).startsWith("* ")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("\\*", ""));
                }
                link(listOfLines, i);
            }


//            Identifies a link in the file line and configures it
            if (listOfLines.get(i).contains("^]")) {
                if (listOfLines.get(i).indexOf("http://") != -1) {
                    String link = listOfLines.get(i).substring(listOfLines.get(i).indexOf("http:"), listOfLines.get(i).indexOf("["));
                    String linkDesc = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[") + 1, listOfLines.get(i).indexOf("^"));
                    String fullLink = "[" + linkDesc + "]" + "(" + link + ")";
                    if (listOfLines.get(i).indexOf("http://") != -1) {
                        String check = listOfLines.get(i).replaceAll("http:(.*?)]", fullLink);
                        listOfLines.set(i, check);
                    }
                }
            }

            // end of guide
            if (listOfLines.get(i).startsWith("# Great work! You're done!")) {
                String lastLine = listOfLines.get(i + 2);
                listOfLines.set(i + 2, "");
                finish(listOfLines, lastLine, guideName, i);
            }

//            //Identifies the start of a table
            if (listOfLines.get(i).startsWith("[cols")) {
                listOfLines.set(i, "");
            }

            //compares line with the irrelevant ones in startingPhrases
            for (int h = 0; h < startingPhrases.length; h++) {
                removingIrrelevant(listOfLines, i, startingPhrases, h);
                position = "main";
            }

            // element contains info that needs general configuration and is not a special case
            if (position.equals("main")) {
                mains(listOfLines, prop, props);
            }

            if (listOfLines.get(i).startsWith("Add the")) {
                listOfLines.set(i, "");
            }

            if (listOfLines.get(i).startsWith("- ")) {
                if (listOfLines.get(i + 1).isBlank()) {
                    listOfLines.set(i, "");
                }
            }

            if (listOfLines.get(i).contains("^]")) {
                String desc = null;
                String link = null;
            }

            if (listOfLines.get(i).startsWith("mvn")) {
                if (!listOfLines.get(i + 2).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                    if (!listOfLines.get(i + 3).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                        listOfLines.add(i + 2, "");
                        listOfLines.set(i + 1, "```\n{: codeblock}\n\n\n");
                    }
                }
            }
        }
    }
}