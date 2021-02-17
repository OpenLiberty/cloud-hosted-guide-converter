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

class LinkSets {
    String linkName;
    String link;
}

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
    private static ArrayList<String> linksForNextGuides;

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


    public static void replacePreSetURL(ArrayList<String> listOfLines, int i) {

        LinkSets LinkSets = new LinkSets();

        String inputLine = listOfLines.get(i);

        int length = inputLine.length();

        LinkSets.link = inputLine.substring(inputLine.indexOf(":", +1) + 2, length);

        LinkSets.linkName = inputLine.substring(inputLine.indexOf(":") + 1, inputLine.indexOf(":", +1));


        for (int x = 0; x < listOfLines.size(); x++) {
            if (listOfLines.get(x).contains(LinkSets.linkName)) {
//                System.out.println(LinkSets.linkName);
//                System.out.println(LinkSets.link);

                String fullLine = listOfLines.get(x).replaceAll("\\{" + LinkSets.linkName + "\\}", LinkSets.link);
                fullLine = fullLine.replaceAll("\n", "");
//                System.out.println(fullLine);


                listOfLines.set(x, fullLine);
//                System.out.println(listOfLines.get(x));
            }
        }

//        link(listOfLines, i);
    }

    public static void ifAdminLink(ArrayList<String> listOfLines, int x, String AdminLink) {
        for (int i = 0; i < x; i++) {
            if (listOfLines.get(i).contains(AdminLink)) {

                if (listOfLines.get(i).contains("^]")) {
                    link(listOfLines, i);
                    listOfLines.set(i, listOfLines.get(i).replaceAll("(?m)^curl(.*?)$", "curl -k -u admin " + AdminLink));
                }
            }
        }
    }

    public static ArrayList<String> relatedGuides(ArrayList<String> listOfLines, int i) {
        ArrayList<String> visitLinks = new ArrayList<String>();
        String line = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[") + 1, listOfLines.get(i).indexOf("]"));
        String[] relatedGuides = line.trim().split("\\s*,\\s*");
        for (String e : relatedGuides) {
            String relatedGuidesName = e.substring(1, e.length() - 1);
            String getTitle = null;
            try {
                URL url = new URL("https://raw.githubusercontent.com/openliberty/guide-" + relatedGuidesName + "/master/README.adoc");
                Scanner s = new Scanner(url.openStream());
                String inputLine = null;
                while (s.hasNextLine()) {
                    inputLine = s.nextLine() + "\n";

                    if (inputLine.startsWith("= ")) {
                        getTitle = inputLine;
                    }
                }
            } catch (IOException ex) {

                System.out.println(ex);
            }
            if (getTitle != null) {
                getTitle = getTitle.substring(+2, getTitle.length() - 1);
                String fullLinks = "https://openliberty.io/guides/" + relatedGuidesName + ".html";
                String fullGuidePlus = "[" + getTitle + "](" + fullLinks + ")";
                visitLinks.add(fullGuidePlus);
            } else {
                String fullGuidePlus = "";
                visitLinks.add(fullGuidePlus);
            }
        }
        return visitLinks;
    }

    public static void Next(ArrayList<String> listOfLines) {

        StringBuilder builder = new StringBuilder();
        for (String value : linksForNextGuides) {
            builder.append("- " + value + "\n");
        }

        String text = builder.toString();

        String whereToNext = "\n\n\n# Where to next? \n\n" + text;

        int End = listOfLines.size();

        listOfLines.add(End, whereToNext);

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
        int counters = 0;
        for (int x = 0; x < 6; x++) {
            int y = x + i;
            if (listOfLines.get(y).startsWith("--")) {
                counters++;
            }
            if (counters != 2) {
                listOfLines.set(y, "");
            }
        }
    }


    public static void CheckTWYB(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        ArrayList<String> tempList = new ArrayList<>();
        for (int x = i; !listOfLines.get(x).startsWith("# "); x++) {
//                System.out.println(x);

            if (listOfLines.get(x).startsWith("#Update")) {
                position = "finishUpdate";
                codeInsert(listOfLines.get(x), listOfLines, guideName, branch, x, position);
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
        if (atIndex.startsWith("#Update") && position != "finishUpdate") {
            update(listOfLines, guideName, branch, g, position);
        } else if (atIndex.startsWith("#Replace")) {
            replace(listOfLines, guideName, branch, g, position);
        } else if (atIndex.startsWith("#Update") && position == "finishUpdate") {
            updateFinish(listOfLines, guideName, branch, g, position);
        }
    }

    // Removes the "Additional pre-reqs" section
    public static void removeAdditionalpres(ArrayList<String> listOfLines, int i) {
        while (!listOfLines.get(i).startsWith("[role=")) {
//            System.out.println(listOfLines.get(i));
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

    public static void addPriorStep1(ArrayList<String> listOfLines, int i, String guideName) {
        String GuidesCommon = "before-start-information.md";

        ImportFunctions.beforeStart(listOfLines, i, GuidesCommon, guideName);
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
        String Summery = "# Summary\n\n## Nice Work!\n\n" + lastLine;
        listOfLines.set(i, Summery);
    }

    public static void end(ArrayList<String> listOfLines, String guideName) {
        listOfLines.add("\n\n## Clean up your environment\n\nClean up your online environment so that it is ready to be used with the next guide:\n\nDelete the **" + guideName + "** project by running the following commands:\n\n```\ncd /home/project\nrm -fr " + guideName + "\n```\n{: codeblock}\n\nLog out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.");
    }

    //configures instructions to replace file
    public static String replace(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        String str = listOfLines.get(i).replaceAll("`", "");
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> From the menu of the IDE, select \n **File** > **Open** > " + guideName + "/start/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
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
        listOfLines.set(i, "\n> From the menu of the IDE, select \n **File** > **Open** > " + guideName + "/start/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        codeSnippet(listOfLines, guideName, branch, i + 2, str);
        position = "main";
        return position;
    }

    public static String updateFinish(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        String str = listOfLines.get(i).replaceAll("`", "");
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> From the menu of the IDE, select \n **File** > **Open** > " + guideName + "/finish/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        codeSnippet(listOfLines, guideName, branch, i + 2, str);
        position = "main";
        return position;
    }

    //configures instructions to create file
    public static String touch(ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        String str = listOfLines.get(i).replaceAll("`", "");
        listOfLines.set(i, "```\n" + "touch " + str + "```" + "\n{: codeblock}\n\n\n");
        listOfLines.set(i, "\n> Run the following touch command in your terminal\n" + "```\ntouch /home/project/" + guideName + "/start/" + str +  "```\n{: codeblock}\n\n" + "\n> Then from the menue of the IDE, select **File** > **Open** > " + guideName + "/start/" + str + "\n\n\n");
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
                String fullText = listOfLines.get(i);
                fullText = fullText.replaceAll(link + "\\[(.*?)\\^\\]", link);
                listOfLines.set(i, listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", ""));
                if (listOfLines.get(i).contains("admin")) {
                    localhostSplit[0] = localhostSplit[0].replaceAll("\\[(.*?)\\^\\]", "");
                    listOfLines.set(i, "\n" + fullText + ("\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```\ncurl -k -u admin " + link + "\n```\n{: codeblock}\n\n\n"));
                    ifAdminLink(listOfLines, listOfLines.size(), link);
                } else if (localhostSplit.length >= 2) {
                    listOfLines.set(i, "\n" + fullText + ("\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```\ncurl " + link + "\n```\n{: codeblock}\n\n\n"));
                } else {
                    listOfLines.set(i, "\n" + fullText + ("\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```\ncurl " + link + "\n```\n{: codeblock}\n\n\n"));
                }
                return;
            } else {
                if (!listOfLines.get(i).contains("curl")) {
                    listOfLines.set(i, "\n" + listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", link) + "\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```\ncurl " + link + "\n```\n{: codeblock}\n\n\n");
                }
            }
        }
        if (listOfLines.get(i).contains("http")) {
            formattedLink = "[" + description + "](" + link + ")";
            listOfLines.set(i, listOfLines.get(i).replaceAll("http(.*?)\\^\\]", formattedLink));
        }
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
                    if (listOfLines.get(i).startsWith("--\n")) {
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
            code.add("\n");
            code.add("```\n");
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                if (inputLine.contains("// tag::copyright[]")) {
                    while (!s.nextLine().startsWith("// end::copyright[]")) {
                        continue;
                    }
                }

                if (inputLine.startsWith("/******")) {
                    inputLine = "";
                    while (!s.nextLine().endsWith("**/")) {
                        continue;
                    }
                }


                if (!inputLine.replaceAll(" ", "").startsWith("//")) {
                    if (!inputLine.startsWith("#")) {
                        code.add(inputLine);
                    }
                }
            }

            for (int x = 0; x < code.size(); x++) {
                String pattern5 = "(.*?)<!--(.*?)-->";
                String pattern6 = "//(.*?)::";

                Pattern r5 = Pattern.compile(pattern5);
                Pattern r6 = Pattern.compile(pattern6);

                Matcher m5 = r5.matcher(code.get(x));
                Matcher m6 = r6.matcher(code.get(x));
                if (m5.find() || m6.find()) {
                    code.remove(x);
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
            int counters = 0;
            String line = listOfLines.get(i + 1);
            for (int n = 0; n < line.length(); n++) {
                if (line.charAt(n) == '|') {
                    counters++;
                }
            }
            if (counters == 2) {
                listOfLines.set(i + 1, line + props.getProperty("2") + "\n");
            } else if (counters == 3) {
                listOfLines.set(i + 1, line + props.getProperty("3") + "\n");
            } else if (counters == 4) {
                listOfLines.set(i + 1, line + props.getProperty("4") + "\n");
            } else if (counters == 5) {
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
    public static void ConditionsMethod(ArrayList<String> listOfLines, String guideName, String
            branch, Properties prop, Properties props) throws IOException {

        Boolean flag = false;
        int counter = 0;
        String position = "";
        final String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "start/", "finish/", "system/", "inventory/", "ifndef::cloud-hosted[]", "ifdef::cloud-hosted[]", "endif::[]"};
        // Main for loop
        for (int i = 0; i < listOfLines.size(); i++) {

            // Function to add related Guides. (Not Completed)
            if (listOfLines.get(i).startsWith(":page-related-guides:")) {
                linksForNextGuides = relatedGuides(listOfLines, i);
            }

            if (listOfLines.get(i).startsWith(":")) {
                if (listOfLines.get(i).contains("-url")) {
                    replacePreSetURL(listOfLines, i);
//                    link(listOfLines, i);
                }
            }

            String pattern2 = "`(.*?)(\\w)(.*?)(\\w)(.*?)`";
            String pattern5 = "(?m)^[```]$";

            Pattern r2 = Pattern.compile(pattern2);
            Pattern r5 = Pattern.compile(pattern5);

            Matcher m2 = r2.matcher(listOfLines.get(i));
            Matcher m5 = r5.matcher(listOfLines.get(i));

            if (m2.find() && !m5.find()) {
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

//            //Removes references to images
//            if (listOfLines.get(i).indexOf("diagram") != -1) {
//                removeDiagramReference(listOfLines, i);
//            }

            if (listOfLines.get(i).startsWith("image::")) {
//                if (listOfLines.get(i + 1).startsWith("*")) {
//                    listOfLines.remove(i + 1);
//                } else if (listOfLines.get(i + 2).startsWith("*")) {
//                    listOfLines.remove(i + 2);
//                }

                String imageRepoLink = "https://raw.githubusercontent.com/OpenLiberty/" + guideName + "/master/assets";

                String imageName = listOfLines.get(i).substring(listOfLines.get(i).indexOf("::") + 2, listOfLines.get(i).indexOf("["));

                String imageDesc = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[") + 1, listOfLines.get(i).indexOf(","));

                String imageLink = imageRepoLink + "/" + imageName;

                if (listOfLines.get(i + 1).contains("{empty} +")) {
                    listOfLines.set(i + 1, "");
                }

                listOfLines.set(i, "![" + imageDesc + "]" + "(" + imageLink + ")\n\n");
            }

            //Removes Additional prerequisites section
            if (listOfLines.get(i).startsWith("## Additional prerequisites") || listOfLines.get(i).startsWith("# Additional prerequisites")) {
                removeAdditionalpres(listOfLines, i);
            }

            // Identifies an instruction for windows only and skips the current line
//             if (listOfLines.get(i).startsWith("[.tab_content.windows_section]") || listOfLines.get(i).startsWith("[.tab_content.windows_section.mac_section]")) {
//                 removeWindowsCommand(listOfLines, i);
//             }


            // Identifies that line is the start of a table
            if (listOfLines.get(i).startsWith("|===")) {
                table(listOfLines, i, props);
                listOfLines.set(i + 1, listOfLines.get(i + 1).replaceAll("^[\\*]", ""));
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

            if (listOfLines.get(i).contains("\\http")) {
                listOfLines.set(i, listOfLines.get(i).replaceAll("\\\\", ""));
            }

            if (listOfLines.get(i).indexOf("^]") > 1) {
                int counters = 0;
                char letter = '^';
                if (listOfLines.get(i).startsWith("- ")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("- ", ""));
                }
                if (listOfLines.get(i).startsWith("* ")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("^[\\*]", ""));
                }
                if (!listOfLines.get(i).contains("localhost")) {
                    for (int x = 0; x < listOfLines.get(i).length(); x++) {
                        if (listOfLines.get(i).charAt(x) == letter) {
                            counters++;
                        }
                    }

                    String[] moreThen = new String[0];
                    if (counters == 2) {
                        moreThen = listOfLines.get(i).split("]");

                        moreThen[0] = moreThen[0] + "]";
                        moreThen[1] = moreThen[1] + "]";
                    }

                    if (moreThen.length == 3) {
                        String link = moreThen[0].substring(moreThen[0].indexOf("http"), moreThen[0].indexOf("["));
                        String link2 = moreThen[1].substring(moreThen[1].indexOf("http"), moreThen[1].indexOf("["));
                        String linkDesc = moreThen[0].substring(moreThen[0].indexOf("[") + 1, moreThen[0].indexOf("^"));
                        if (linkDesc.isEmpty()) {
                            linkDesc = link;
                        }
                        String linkDesc2 = moreThen[1].substring(moreThen[1].indexOf("[") + 1, moreThen[1].indexOf("^"));
                        if (linkDesc2.isEmpty()) {
                            linkDesc2 = link2;
                        }
                        String fullLink = "[" + linkDesc + "]" + "(" + link + ")";
                        String fullLink2 = "[" + linkDesc2 + "]" + "(" + link2 + ")";
                        String wholeLine = listOfLines.get(i);
                        String begginingOfLine = listOfLines.get(i).substring(0, listOfLines.get(i).indexOf("http"));
                        wholeLine = wholeLine.replace(begginingOfLine, "");
                        String endOfLine = listOfLines.get(i).substring(listOfLines.get(i).lastIndexOf("]") + 1, listOfLines.get(i).length());
                        wholeLine = wholeLine.replace(endOfLine, "");
                        wholeLine = wholeLine.replace(link, "");
                        String middleOfLine = wholeLine.substring(wholeLine.indexOf("]") + 1, wholeLine.indexOf("http"));
                        String check2 = begginingOfLine + fullLink + middleOfLine + fullLink2 + endOfLine;
                        listOfLines.set(i, check2);
                    }
                } else {
                    if (listOfLines.get(i).contains("^]")) {
                        if (listOfLines.get(i).startsWith("-")) {
                            listOfLines.set(i, listOfLines.get(i).replaceAll("-", ""));
                        }
                        if (listOfLines.get(i).startsWith("* ")) {
                            listOfLines.set(i, listOfLines.get(i).replaceAll("^[\\*]", ""));
                        }
                        if (listOfLines.get(i).indexOf("http://") != -1) {
                            if (!listOfLines.get(i).contains("localhost")) {
                                String link = listOfLines.get(i).substring(listOfLines.get(i).indexOf("http://"), listOfLines.get(i).indexOf("["));
                                String linkDesc = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[") + 1, listOfLines.get(i).indexOf("^"));
                                if (linkDesc.isEmpty()) {
                                    linkDesc = link;
                                }
                                String fullLink = "[" + linkDesc + "]" + "(" + link + ")";
                                String check = listOfLines.get(i).replaceAll("http:(.*?)]", fullLink);
                                listOfLines.set(i, check);
                            }
                        }
                    }
                }
            }

            if (listOfLines.get(i).contains("^]")) {
                link(listOfLines, i);
                if (listOfLines.get(i).contains("localhost")) {
                    counter++;
                }
                if (listOfLines.get(i).startsWith("-")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("-", ""));
                }
                if (listOfLines.get(i).startsWith("*")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("^[\\*]", ""));
                }

                if (counter == 1) {
                    flag = true;
                }

                if (flag == true) {
                    String GuidesCommon = "new-terminal.md";

                    listOfLines.add(i, "");
                    listOfLines.add(i, "");
                    ImportFunctions.newTerminal(listOfLines, i - 1, GuidesCommon);
                    listOfLines.add(i + 12, "");
                    flag = false;
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
                    listOfLines.set(i, listOfLines.get(i).substring(+1, listOfLines.get(i).length()));
                }
            }

            String pattern3 = "\\*\\*((?:(?!\\*\\*)[^_])*)_(.*?)\\*\\*";

            Pattern r3 = Pattern.compile(pattern3);

            Matcher m3 = r3.matcher(listOfLines.get(i));

            if (m3.find()) {
                if (m3.group().contains("_")) {
                    String s = m3.group();
                    s = s.substring(s.indexOf("**") + 2, s.lastIndexOf("**"));
                    s = "**`" + s + "`**";
                    if ((s.length() < 70)) {
                        listOfLines.set(i, listOfLines.get(i).replaceAll("\\*\\*((?:(?!\\*\\*)[^_])*)_(.*?)\\*\\*", s));
                    } else {
                        listOfLines.set(i, listOfLines.get(i));
                    }
                }
            }

            if (listOfLines.get(i).startsWith("docker")) {
                if (!listOfLines.get(i + 1).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                    listOfLines.add(i + 2, "");
                    listOfLines.set(i + 2, "{: codeblock}\n\n\n");
                }
            }

            String pattern7 = "(?m)^(.\s)$";
            String pattern8 = "(?m)^(.)$";

            Pattern r7 = Pattern.compile(pattern7);
            Pattern r8 = Pattern.compile(pattern8);

            Matcher m7 = r7.matcher(listOfLines.get(i));
            Matcher m8 = r8.matcher(listOfLines.get(i));

            if (m7.find()) {
                if (!listOfLines.get(i).contains("{") || !listOfLines.get(i).contains("}") || !listOfLines.get(i).contains("(") || !listOfLines.get(i).contains(")")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("(?m)^(.\s)$", ""));
                }
            }

            if (m8.find()) {
                if (!listOfLines.get(i).contains("{") || !listOfLines.get(i).contains("}") || !listOfLines.get(i).contains("(") || !listOfLines.get(i).contains(")")) {
                    listOfLines.set(i, listOfLines.get(i).replaceAll("(?m)^(.\s)$", ""));
                }
            }

            if (listOfLines.get(i).startsWith("mvn")) {
                if (!listOfLines.get(i + 2).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                    if (!listOfLines.get(i + 3).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                        listOfLines.add(i + 2, "");
                        listOfLines.set(i + 1, "```\n{: codeblock}\n\n\n");
                    }
                }
            }

            String pattern9 = "^mvn(.*?)";
            String pattern10 = "[A-Za-z]+";

            Pattern r9 = Pattern.compile(pattern9);
            Pattern r10 = Pattern.compile(pattern10);

            Matcher m9 = r9.matcher(listOfLines.get(i));
            Matcher m10 = null;
            if (i < listOfLines.size() - 2) {
                m10 = r10.matcher(listOfLines.get(i + 2));
            }

            if (m9.find()) {
                if (m10 != null) {
                    if (m10.find()) {
                        if (!m10.group().contains("codeblock")) {
                            if (!listOfLines.get(i + 2).startsWith("mvn")) {
                                listOfLines.set(i + 2, "{: codeblock}\n\n" + listOfLines.get(i + 2));
                            }
                        }
                    }
                }
            }

            if (listOfLines.get(i).contains("^]")) {
                link(listOfLines, i);
            }

            if (listOfLines.get(i).startsWith("### Try what you'll build")) {
                int g = i + 1;
                Functions.CheckTWYB(listOfLines, guideName, branch, g, position);
            }
            if (listOfLines.get(i).contains(": codeblock")) {
                String pattern6 = "(?m)^: codeblock$";

                Pattern r6 = Pattern.compile(pattern6);

                Matcher m6 = r6.matcher(listOfLines.get(i));

                if (m6.find()) {
                    if (!listOfLines.get(i).startsWith("{: codeblock}")) {

                        listOfLines.set(i, listOfLines.get(i).replaceAll("(?m)^(.*?)codeblock(.*?)$", "{: codeblock}"));
                    }
                }
            }
        }
    }
}
