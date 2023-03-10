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
    private static Properties guidesProperties = null;
    private static boolean addedPasteCmd = false;

    // Replaces the dashes which stand for a codeblock in adoc with backticks which are codeblocks in md
    public static void replaceCodeBlocks(ArrayList<String> listOfLines, int i) {
        listOfLines.get(i).replaceAll("----", "```");
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
                String fullLine = listOfLines.get(x).replaceAll("\\{" + LinkSets.linkName + "\\}", LinkSets.link);
                fullLine = fullLine.replaceAll("\n", "");
                listOfLines.set(x, fullLine);
            }
        }
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

    public static void addCodeblockAfterMVN(ArrayList<String> listOfLines, int i) {
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
                        for (int s = -10; s < 7; s++) {
                            if (listOfLines.get(i + s).contains("NO_COPY")) {
                                listOfLines.set(i + s, "");
                                return;
                            }
                            return;
                        }
                        // To be removed: - no need to add {: cdodeblock}
                        /*
                        if (!listOfLines.get(i + 2).startsWith("mvn")) {
                            for (int l = i; l < listOfLines.size(); l++) {
                                if (listOfLines.get(l).contains("----")) {
                                    listOfLines.set(l + 1, "{: codeblock}\n\n");
                                }
                            }
                        }
                        */
                    }
                }
            }
        }
    }


    public static void relatedLinksMove(ArrayList<String> listOfLines, int i) {

        int l = i;
        for (int x = l; x < listOfLines.size() - 1; x++) {
            if (!listOfLines.get(x).startsWith("# Related Links")) {
                if (listOfLines.get(x).startsWith("http")) {
                    if (!listOfLines.get(x).isBlank()) {
                        if (!listOfLines.get(x).isEmpty()) {
                            if (listOfLines.get(x).startsWith("http")) {
                                link(listOfLines, x);
                            }
                            linksForNextGuides.add(listOfLines.get(x));
                        }
                    }
                }
                if(listOfLines.get(x).startsWith("Learn more about")){
                    linksForNextGuides.add(listOfLines.get(x));
                }
            }
            listOfLines.set(x, "");
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
                URL url = new URL("https://raw.githubusercontent.com/openliberty/guide-" + relatedGuidesName + "/prod/README.adoc");
                Scanner s = new Scanner(url.openStream());
                String inputLine = null;
                while (s.hasNextLine()) {
                    inputLine = s.nextLine() + "\n";

                    if (inputLine.startsWith("= ")) {
                        getTitle = inputLine;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Guide contains iGuide reference");
            }
            if (getTitle != null) {
                getTitle = getTitle.substring(+2, getTitle.length() - 1);
                String fullLinks = "https://openliberty.io/guides/" + relatedGuidesName + ".html";
                String fullGuidePlus = "[" + getTitle + "](" + fullLinks + ")";
                visitLinks.add(fullGuidePlus);
            } else {
                try {
                    URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/iguide-" + relatedGuidesName + "/prod/html/" + relatedGuidesName + "-guide.html");
                    Scanner s = new Scanner(url.openStream());
                    String inputLine = null;
                    while (s.hasNextLine()) {
                        inputLine = s.nextLine() + "\n";

                        if (inputLine.startsWith("title: ")) {
                            getTitle = inputLine;
                        }
                    }
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                getTitle = getTitle.substring(+8, getTitle.length() - 2);
                String fullLinks = "https://openliberty.io/guides/" + relatedGuidesName + ".html";
                String fullGuidePlus = "[" + getTitle + "](" + fullLinks + ")";
                visitLinks.add(fullGuidePlus);
            }
        }
        return visitLinks;
    }

    public static String Next(ArrayList<String> listOfLines) {

        StringBuilder builder = new StringBuilder();
        for (String value : linksForNextGuides) {
            if(value.startsWith("[")){
                builder.append("* " + value);
                if (!value.endsWith("\n"))
                    builder.append("\n");
            } else {
                builder.append("\n**" + value.replaceAll("\\.", "**"));
            }
        }

        String text = builder.toString();

        String whereToNext = "\n\n### Where to next?\n\n" + text;

        return whereToNext;

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

    // To be removed - no need to add {: cdodeblock}
    // This is a function that inserts {: cdodeblock} after a codeblock
    /*
    public static void insertCopyButton(ArrayList<String> listOfLines, int i) {
        ArrayList<String> check = new ArrayList<>();
        int y = 0;
        boolean addCodeblock = true; // prevent duplicate to add codeblock within 15 lines
        for (int x = 0; x <= 15; x++) {
            y = i + x;
            if (y < listOfLines.size()) {
                check.add(listOfLines.get(y));
                if (check.get(x).startsWith("```")) {
                    if (listOfLines.get(y + 1).isBlank()) {
                        if (addCodeblock) {
                            listOfLines.set(y + 1, "{: codeblock}\n\n\n");
                            addCodeblock = false;
                        }
                    }
                } else if (check.get(x).contains("no_copy")) {
                    addCodeblock = false;
                }
            }
        }
    }
    */
    
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
 
        for (int x = i; !listOfLines.get(x).startsWith("# "); x++) {

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

    public static void lowercaseKeyword(String keyword, ArrayList<String> listOfLines, int i){
        String lowerCase = keyword.toLowerCase();
        String prohibited = "[]-=";
        int v = i-2;
        char lastChar = 0;
        do{
            if(listOfLines.get(v).isBlank()){
                lastChar = 0;
            }else{
                lastChar = listOfLines.get(v).charAt(listOfLines.get(v).length()-2);
            }
            if(lastChar == ','){
                listOfLines.set(i-1, listOfLines.get(i-1).replaceAll(keyword, lowerCase));
            }
            v--;
        } while (prohibited.indexOf(lastChar) != -1 || lastChar == 0);
    }


    // Inserts code snippets
    public static void codeInsert(String atIndex, ArrayList<String> listOfLines, String guideName, String branch, int i, String position) {
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        String hideTags[] = new String[0];

        if (listOfLines.get(i - 1).startsWith("```") || listOfLines.get(i - 1).startsWith("----")) {
            listOfLines.set(i - 1, "");
        }
        if (listOfLines.get(i + 2).startsWith("----")) {
            listOfLines.set(i + 2, "");
        }

		String fromDir = "start";
        if (guidesProperties == null) {
    		guidesProperties = new Properties();
    		try {
    			guidesProperties.load(new FileInputStream("guides.properties"));
    		} catch (Exception ex) {
    			System.out.println(ex);
    		}
        }
		fromDir = guidesProperties.getProperty(guideName + ".start-dir","start");
		
        int g = i + 1;
        if (atIndex.startsWith("#Create")) {

            // String fileName = listOfLines.get(i + 1).substring(1, listOfLines.get(i + 1).length() - 2);

            int nextSectionHeading = 0;

            for (int x = i; x < listOfLines.size(); x++) {
                if (listOfLines.get(x).startsWith("#")) {
                    nextSectionHeading = x;
                    break;
                }
            }

            for (int x = i; x < nextSectionHeading; x++) {
                if (listOfLines.get(x).startsWith("[source") && listOfLines.get(x).contains("hide_tags")) {
                    hideTags = (listOfLines.get(x).substring(listOfLines.get(x).indexOf("hide_tags") + 10, listOfLines.get(x).length() - 3)).split(",");
                    break;
                }
            }

            // To be removed - hide_tags should be handled by the above for loop
            /*
            for (int x = i; x < listOfLines.size(); x++) {
                String pattern20 = fileName;
                Pattern r20 = Pattern.compile(pattern20);
                Matcher m20 = r20.matcher(listOfLines.get(x));
                if (m20.find()) {
                    if (x < nextSectionHeading) {
                        if (listOfLines.get(x - 2).contains("hide_tags")) {
                            hideTags = (listOfLines.get(x - 2).substring(listOfLines.get(x - 2).indexOf("hide_tags") + 10, listOfLines.get(x - 2).length() - 3)).split(",");
                        }
                    }
                }
            }
            */

            touch(listOfLines, guideName, branch, fromDir, g, position, hideTags);
        } else if (atIndex.startsWith("#Update") && position != "finishUpdate") {

            // String fileName = listOfLines.get(i + 1).substring(1, listOfLines.get(i + 1).length() - 2);

            int nextSectionHeading = 0;

            for (int x = i; x < nextSectionHeading; x++) {
                if (listOfLines.get(x).startsWith("#")) {
                    nextSectionHeading = x;
                    break;
                }
            }

            for (int x = i; x < listOfLines.size(); x++) {
                if (listOfLines.get(x).startsWith("[source") && listOfLines.get(x).contains("hide_tags")) {
                    hideTags = (listOfLines.get(x).substring(listOfLines.get(x).indexOf("hide_tags") + 10, listOfLines.get(x).length() - 3)).split(",");
                    break;
                }
            }

            // To be removed - hide_tags should be handled by the above for loop
            /*
            for (int x = i; x < listOfLines.size(); x++) {
                String pattern20 = fileName;
                Pattern r20 = Pattern.compile(pattern20);
                Matcher m20 = r20.matcher(listOfLines.get(x));
                if (m20.find()) {
                    if (x < nextSectionHeading) {
                        if (listOfLines.get(x - 2).contains("hide_tags")) {
                            hideTags = (listOfLines.get(x - 2).substring(listOfLines.get(x - 2).indexOf("hide_tags") + 10, listOfLines.get(x - 2).length() - 3)).split(",");
                        }
                    }
                }
            }
            */

            openFile("Update", fromDir, listOfLines, guideName, branch, g, position, hideTags);

        } else if (atIndex.startsWith("#Replace")) {

            // String fileName = listOfLines.get(i + 1).substring(1, listOfLines.get(i + 1).length() - 2);

            int nextSectionHeading = 0;

            for (int x = i; x < listOfLines.size(); x++) {
                if (listOfLines.get(x).startsWith("#")) {
                    nextSectionHeading = x;
                    break;
                }
            }
            for (int x = i; x < nextSectionHeading; x++) {
                if (listOfLines.get(x).startsWith("[source") && listOfLines.get(x).contains("hide_tags")) {
                    hideTags = (listOfLines.get(x).substring(listOfLines.get(x).indexOf("hide_tags") + 10, listOfLines.get(x).length() - 3)).split(",");
                    break;
                }
            }

            // To be removed - hide_tags should be handled by the above for loop
            /*
            for (int x = i; x < listOfLines.size(); x++) {
                String pattern20 = fileName;
                Pattern r20 = Pattern.compile(pattern20);
                Matcher m20 = r20.matcher(listOfLines.get(x));
                if (m20.find()) {
                    if (x < nextSectionHeading) {
                        if (listOfLines.get(x - 2).contains("hide_tags")) {
                            hideTags = (listOfLines.get(x - 2).substring(listOfLines.get(x - 2).indexOf("hide_tags") + 10, listOfLines.get(x - 2).length() - 3)).split(",");
                        }
                    }
                }
            }
            */
            
            openFile("Replace", fromDir, listOfLines, guideName, branch, g, position, hideTags);

        } else if (atIndex.startsWith("#Update") && position == "finishUpdate") {

            // String fileName = listOfLines.get(i + 1).substring(1, listOfLines.get(i + 1).length() - 2);

            int nextSectionHeading = 0;

            for (int x = i; x < listOfLines.size(); x++) {
                if (listOfLines.get(x).startsWith("#")) {
                    nextSectionHeading = x;
                    break;
                }
            }

            for (int x = i; x < nextSectionHeading; x++) {
                if (listOfLines.get(x).startsWith("[source") && listOfLines.get(x).contains("hide_tags")) {
                    hideTags = (listOfLines.get(x).substring(listOfLines.get(x).indexOf("hide_tags") + 10, listOfLines.get(x).length() - 3)).split(",");
                    break;
                }
            }

            // To be removed - hide_tags should be handled by the above for loop
            /*
            for (int x = i; x < listOfLines.size(); x++) {
                String pattern20 = fileName;
                Pattern r20 = Pattern.compile(pattern20);
                Matcher m20 = r20.matcher(listOfLines.get(x));
                if (m20.find()) {
                    if (x < nextSectionHeading) {
                        if (listOfLines.get(x - 2).contains("hide_tags")) {
                            hideTags = (listOfLines.get(x - 2).substring(listOfLines.get(x - 2).indexOf("hide_tags") + 10, listOfLines.get(x - 2).length() - 3)).split(",");
                        }
                    }
                }
            }
            */

            openFile("Update", "finish", listOfLines, guideName, branch, g, position, hideTags);
        }

    }

    // Removes the "Additional pre-reqs" section
    public static void removeAdditionalpres(ArrayList<String> listOfLines, int i) {
    	listOfLines.remove(i);
        while (!listOfLines.get(i).startsWith("[role=") && !listOfLines.get(i).startsWith("# ")) {
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
            file.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
	public static int addSNLMetadata(ArrayList<String> listOfLines, String guideName, String gitLab) {
		ArrayList<String> temp = new ArrayList<>();
		Properties vhsdProperties = new Properties();
		try {
			vhsdProperties.load(new FileInputStream("version-history-start-date.properties"));
		} catch (Exception ex) {
			System.out.println(ex);
		}
		temp.add("---\n");
		temp.add("markdown-version: v1\n");
		String gitlabTitle = vhsdProperties.getProperty(gitLab + ".title");
		if (gitlabTitle == null) {
			gitlabTitle = "instructions";
		}
		temp.add("title: " + gitlabTitle + "\n");
		String gitlabBranch = vhsdProperties.getProperty(gitLab + ".branch");
		if (gitlabBranch == null) {
			gitlabBranch =  vhsdProperties.getProperty(gitLab.replaceAll("draft-", "") + ".branch","lab-204-instruction");
		}
		temp.add("branch: " + gitlabBranch + "\n");
		String gitlabStartDate = vhsdProperties.getProperty(gitLab + ".start-date");
		if (gitlabStartDate == null) {
			gitlabStartDate = vhsdProperties.getProperty(gitLab.replaceAll("draft-", "") + ".start-date", "2022-02-09T14:19:17.000Z");
		}
		temp.add("version-history-start-date: " + gitlabStartDate + "\n");
		String gitlabToolType = vhsdProperties.getProperty(gitLab + ".tool-type");
		if (gitlabToolType != null) {
			temp.add("tool-type: " + gitlabToolType + "\n");
		}
		temp.add("---\n");
		listOfLines.addAll(temp);
		return temp.size();
	}

    public static void addPriorStep1(ArrayList<String> listOfLines, int i, String guideName, String
            GuideTitle, String GuideDescription) {
        String GuidesCommon = "before-start-information.md";

        ImportFunctions.beforeStart(listOfLines, i, GuidesCommon, guideName, GuideTitle, GuideDescription);
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

        if (listOfLines.get(i).startsWith("include::{common-includes}/") && !listOfLines.get(i).startsWith("include::{common-includes}/attribution.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/attribution.adoc[subs=\"attributes\"]") && !listOfLines.get(i).startsWith("include::{common-includes}/gitclone.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/os-tabs.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/kube-prereq.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/kube-start.adoc[]") && !listOfLines.get(i).startsWith("include::{common-includes}/kube-minikube-teardown.adoc[]")) {

            GuidesCommon = listOfLines.get(i).substring(27, listOfLines.get(i).length() - 3);

            CommonURL = CommonURL + GuidesCommon;

            ImportFunctions.OtherGuidesCommon(listOfLines, guideName, i, GuidesCommon);
        }
    }

    // This function adds in the last steps of a guide.
    public static void finish(ArrayList<String> listOfLines, String lastLine, String guideName, int i) {
        //String summary = "::page{title=\"Summary\"}\n\n## **Nice Work!**\n\n" + lastLine;
        String summary = "# **Summary**\n\n### Nice Work!\n\n" + lastLine;
        listOfLines.set(i, summary);
    }

    public static void end(ArrayList<String> listOfLines, String guideName, String GuideTitle) {

        String GuideNameFormatted = (GuideTitle.replaceAll("= ","")).replaceAll(" ", "%20").trim();

        System.out.println(GuideNameFormatted);

        String FeedbackLink = "https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=" + GuideNameFormatted + "&guide-id=cloud-hosted-" + guideName;

        System.out.println(FeedbackLink);

        listOfLines.add("\n### Clean up your environment\n\n\nClean up your online environment so that it is ready to be used with the next guide:\n\nDelete the ***" + guideName + "*** project by running the following commands:\n\n```bash\ncd /home/project\nrm -fr " + guideName + "\n```\n\n" +
                "### What did you think of this guide?\n\nWe want to hear from you. To provide feedback, click the following link.\n\n" + "* [Give us feedback](" +  FeedbackLink + ")" + "\n\nOr, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.\n\n" +
                "### What could make this guide better?\n\nYou can also provide feedback or contribute to this guide from GitHub.\n* [Raise an issue to share feedback.](https://github.com/OpenLiberty/" + guideName + "/issues)\n" + "* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/" + guideName + "/pulls)\n\n" +
                Next(listOfLines) + "\n\n" +
                "### Log out of the session\n\nLog out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.");
    }

    private static String getFilePath(ArrayList<String> listOfLines, int i) {
        String filePath = null;
        for (int x = i; x <= i + 10; x++) {
            if (listOfLines.get(x).startsWith("include") && !listOfLines.get(x).startsWith("include::{common-includes}")) {
            	filePath = listOfLines.get(x);
                filePath = filePath.substring(9, filePath.length() - 3);
            }
        }
        return filePath == null ? "unknown" : listOfLines.get(i).replaceAll("`", "").trim();
    }
    
    private static String getIncludeFile(ArrayList<String> listOfLines, int i) {
        String filePath = null;
        for (int x = i; x <= i + 10; x++) {
            if (listOfLines.get(x).startsWith("include") && !listOfLines.get(x).startsWith("include::{common-includes}")) {
            	filePath = listOfLines.get(x);
                filePath = filePath.substring(9, filePath.length() - 3);
                return filePath.trim();
            }
        }
        return "unknown";
    }
    
    private static String openFile(String guideName, String filePath, String fromDir) {
    	File f = new File(filePath);
		return "\n> To open the " + f .getName() + " file in your IDE, select\n" + 
                "> **File** > **Open** > " + guideName + "/" + fromDir + "/" + filePath.replaceAll("\\*\\*", "") +
                ", or click the following button\n\n" + 
                "::openFile{path=\"/home/project/" + guideName + "/" + fromDir + "/" + filePath + "\"}" +
                "\n\n\n";
    }
    
    //configures instructions to open file for replace and update
    public static String openFile(String instruction, String fromDir, ArrayList<String> listOfLines, String guideName, String branch, int i, String
            position, String[] hideTags) {
        String filePath = getFilePath(listOfLines, i);
        String includeFile = getIncludeFile(listOfLines, i);
        lowercaseKeyword(instruction, listOfLines, i);
        listOfLines.set(i, openFile(guideName, filePath, fromDir));
        codeSnippet(listOfLines, guideName, branch, i + 2, includeFile, hideTags, "replace");
        position = "main";
        return position;
    }

    /* To be removed
    //configures instructions to replace file
    public static String replace(ArrayList<String> listOfLines, String guideName, String branch, int i, String
            position, ArrayList<String> hideList) {
        String str = null;
        for (int x = i; x <= i + 10; x++) {
            if (listOfLines.get(x).startsWith("include") && !listOfLines.get(x).startsWith("include::{common-includes}")) {
                str = listOfLines.get(x);
                str = str.substring(9, str.length() - 3);
            }
        }
        if (str == null) {
            str = "finish/" + listOfLines.get(i).replaceAll("`", "");
        }

        lowercaseKeyword("Replace", listOfLines, i);

        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> From the menu of the IDE, select\n" + "> **File** > **Open** > " + guideName + "/start/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        codeSnippet(listOfLines, guideName, branch, i + 2, str, hideList);
        position = "main";
        return position;
    }

    //configures instructions to update file
    public static String update(ArrayList<String> listOfLines, String guideName, String branch, int i, String
            position, ArrayList<String> hideList) {
        String str = null;
        for (int x = i; x <= i + 10; x++) {
            if (listOfLines.get(x).startsWith("include") && !listOfLines.get(x).startsWith("include::{common-includes}")) {

                str = listOfLines.get(x);
                str = str.substring(9, str.length() - 3);

            }
        }

        lowercaseKeyword("Update", listOfLines, i);

        if (str == null) {
            str = "finish/" + listOfLines.get(i).replaceAll("`", "");
        }
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> From the menu of the IDE, select\n" + "> **File** > **Open** > " + guideName + "/start/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        codeSnippet(listOfLines, guideName, branch, i + 2, str, hideList);
        position = "main";
        return position;
    }

    public static String updateFinish(ArrayList<String> listOfLines, String guideName, String branch,
                                      int i, String position, ArrayList<String> hideList) {
        String str = null;
        for (int x = i; x <= i + 10; x++) {
            if (listOfLines.get(x).startsWith("include") && !listOfLines.get(x).startsWith("include::{common-includes}")) {

                str = listOfLines.get(x);
                str = str.substring(9, str.length() - 3);

            }
        }

        lowercaseKeyword("Update", listOfLines, i);

        if (str == null) {
            str = "finish/" + listOfLines.get(i).replaceAll("`", "");
        }
        listOfLines.set(i, listOfLines.get(i).replaceAll("#", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("`", "**"));
        listOfLines.set(i, "\n> From the menu of the IDE, select\n" + "> **File** > **Open** > " + guideName + "/finish/" + listOfLines.get(i).replaceAll("\\*\\*", "") + "\n\n\n");
        listOfLines.set(i, listOfLines.get(i).replaceAll("touch ", ""));
        codeSnippet(listOfLines, guideName, branch, i + 2, str, hideList);
        position = "main";
        return position;
    }
    */

    //configures instructions to create file
    public static String touch(ArrayList<String> listOfLines, String guideName, String branch, String fromDir, int i, String
            position, String[] hideTags) {
 
        String filePath = getFilePath(listOfLines, i);
        String includeFile = getIncludeFile(listOfLines, i);
        File f = new File(filePath);

        lowercaseKeyword("Create", listOfLines, i);

        listOfLines.set(i,
            "\n> Run the following touch command in your terminal\n" + 
            "```bash\ntouch /home/project/" + guideName + "/" + fromDir + "/" + listOfLines.get(i).replaceAll("`", "") + "```\n\n" +
            "\n> Then, to open the " + f.getName() + " file in your IDE, select" +
            "\n> **File** > **Open** > " + guideName + "/" + fromDir + "/" + filePath + 
            ", or click the following button\n\n" + 
            "::openFile{path=\"/home/project/" + guideName + "/" + fromDir + "/" + filePath + "\"}" +
            "\n\n\n");
        codeSnippet(listOfLines, guideName, branch, i + 2, includeFile, hideTags, "add");
        position = "main";
        return position;
    }

    // configures link
    public static void link(ArrayList<String> listOfLines, int i) {
        listOfLines.set(i, listOfLines.get(i).replaceAll("\\{", ""));
        listOfLines.set(i, listOfLines.get(i).replaceAll("\\}", ""));
        String[] linkParts = listOfLines.get(i).split("\\[");
        String[] findLink = linkParts[0].split(" ");
        String link = findLink[findLink.length - 1];
        if (link.contains("localhost")) {
            // cloudLink makes the localhost links not accessible
            String cloudLink = "***" + link.replace("://", "\\\\://") + "***";
            String findDescription[] = linkParts[1].split("\\^");
            String description = findDescription[0];
            if (listOfLines.get(i).contains(".")) {
            	String localhostSplit[] = listOfLines.get(i).split("\\.");
                String fullText = listOfLines.get(i);
                fullText = fullText.replaceAll(link + "\\[(.*?)\\^\\]", cloudLink);
                listOfLines.set(i, listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", ""));
                if (listOfLines.get(i).contains("admin")) {
                    localhostSplit[0] = localhostSplit[0].replaceAll("\\[(.*?)\\^\\]", "");
                    listOfLines.set(i, "\n" + fullText + ("\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```bash\ncurl -k -u admin " + appendJQ(link) + "\n```\n\n\n"));
                    ifAdminLink(listOfLines, listOfLines.size(), link);
                } else if (localhostSplit.length >= 2) {
                    listOfLines.set(i, "\n" + fullText + ("\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```bash\ncurl " + appendJQ(link) + "\n```\n\n\n"));
                } else {
                    listOfLines.set(i, "\n" + fullText + ("\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```bash\ncurl " + appendJQ(link) + "\n```\n\n\n"));
                }
                return;
            } else {
                if (!listOfLines.get(i).contains("curl")) {
                    listOfLines.set(i, "\n" + listOfLines.get(i).replaceAll(link + "\\[" + description + "\\^\\]", cloudLink) + "\n\n_To see the output for this URL in the IDE, run the following command at a terminal:_\n\n```bash\ncurl " + appendJQ(link) + "\n```\n\n");
                    return;
                }
            }
        }
        if (listOfLines.get(i).contains("http")) {
            int r = 0;
            for (int l = 1; l < linkParts.length; l++) {
                String[] findDescription = linkParts[l].split("\\^");
                String description = findDescription[0];
                findLink = linkParts[l-1].split(" ");
                link = findLink[findLink.length - 1];
                String formattedLink = "[" + description + "](" + link + ")";
                int s = listOfLines.get(i).indexOf("http", r);
                int e = listOfLines.get(i).indexOf("]", r);
                if (e > s) {
                    String replaceStr = listOfLines.get(i).substring(s,e+1);
                    listOfLines.set(i, listOfLines.get(i).replace(replaceStr, formattedLink));
                    r = s + formattedLink.length();
                }
            }
        }
    }

    private static String appendJQ(String link) {
        String pattern[] = new String[] { 
            ".*/system/properties$",
            ".*/inventory/systems$",
            ".*/inventory/systems/localhost$",
            ".*/LibertyProject/System/properties$",
            ".*/health$",
            ".*/health/ready$",
            ".*/health/live$" };
        for (String p : pattern) {
            if (link.matches(p)) {
                return "-s " + link + " | jq";
            }
        }
        return link;
    }

    // general text configuration
    public static ArrayList<String> mains(ArrayList<String> listOfLines, Properties prop, Properties props) {

        for (int i = 0; i < listOfLines.size(); i++) {
            if (!listOfLines.get(i).startsWith("[.hidden]")) {
                if (listOfLines.get(i).startsWith("----")) {
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

                    if (listOfLines.get(i).startsWith("== What you'll learn")) {
                        listOfLines.set(i, listOfLines.get(i).replaceAll("==", "#"));
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
    public static ArrayList<String> codeSnippet(ArrayList<String> listOfLines, String guideName, String branch,
                                                int i, String path, String[] hideTags, String pasteFor) {
    	String p = path.trim();
        try {
            ArrayList<String> code = new ArrayList<String>();
            URL url = new URL("https://raw.githubusercontent.com/openliberty/" + guideName + "/" + branch + "/" + p);
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            code.add("\n");
            if (p.endsWith(".java")) {
                code.add("```java\n");
            } else if((p.endsWith(".js"))) {
                code.add("```javascript\n");
            } else if((p.endsWith(".json"))) {
                code.add("```json\n");
            } else if((p.endsWith(".xml"))) {
                code.add("```xml\n");
            } else if((p.endsWith(".yaml"))) {
                code.add("```yaml\n");
            } else if((p.endsWith(".html"))) {
                code.add("```html\n");
            } else {
                code.add("```\n");
            }

            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                ArrayList<String> newList = new ArrayList<>();

                // Traverse through the first list
                for (String element : hideTags) {

                    // If this element is not present in newList
                    // then add it
                    if (!newList.contains(element)) {
                        newList.add(element);
                    }
                }

                if (hideTags != null) {
                    for (String e : newList) {
                        if (inputLine.contains("tag::" + e)) {
                            while (!s.nextLine().contains("end::" + e)) {
                                continue;
                            }
                        }
                    }
                }

                if (inputLine.startsWith("/******")) {
                    inputLine = "";
                    while (!s.nextLine().endsWith("**/")) {
                        continue;
                    }
                }

                if (inputLine.contains("# tag::")) {
                    inputLine = "";
                }

                if (inputLine.contains("# end::")) {
                    inputLine = "";
                }
                
                if (inputLine.contains("<!-- tag::")) {
                    inputLine = "";
                }
                if (inputLine.contains("<!-- end::")) {
                    inputLine = "";
                }

                if (!inputLine.replaceAll(" ", "").startsWith("//")) {
                    if (!inputLine.startsWith("#")) {
                        code.add(inputLine);
                    }
                }
            }

            /* TODO: remove the following code
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
            */
            
            for (int x = 0; x < code.size(); x++) {
                String pattern6 = "//(.*?)::";
                Pattern r6 = Pattern.compile(pattern6);
                Matcher m6 = r6.matcher(code.get(x));
                if (m6.find()) {
                    code.remove(x);
                }
            }

            code.add("```\n\n\n");
            if (!addedPasteCmd) {
            	code.add("Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to " +
    					 pasteFor + " the code to the file.\n\n");
            	addedPasteCmd = true;
    		}

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

            boolean ignoreMacros = false;

            if (listOfLines.get(i).startsWith("ifdef::cloud-hosted[]")) {
                listOfLines.set(i, "");
                while (!listOfLines.get(i).startsWith("endif::[]")) {
                    i++;
                    ignoreMacros = true;
                }
                if (listOfLines.get(i).startsWith("endif::[]")) {
                    listOfLines.set(i, "");
                }
            }

            if (listOfLines.get(i).contains("no_copy")) {
                listOfLines.set(i, "NO_COPY\n");
            }

            //Removes Additional prerequisites section
            if (listOfLines.get(i).startsWith("## Additional prerequisites") || listOfLines.get(i).startsWith("# Additional prerequisites")) {
                removeAdditionalpres(listOfLines, i);
            }

            if (ignoreMacros == false) {
                // Function to add related Guides. (Not Completed)
                if (listOfLines.get(i).startsWith(":page-related-guides:")) {
                    linksForNextGuides = relatedGuides(listOfLines, i);
                }

                if (listOfLines.get(i).startsWith(":")) {
                    if (listOfLines.get(i).contains("-url")) {
                        replacePreSetURL(listOfLines, i);
                    }
                }


                String pattern2 = "`(.*?)(\\w)(.*?)(\\w)(.*?)`";
                String pattern5 = "(?m)^[```]$";

                Pattern r2 = Pattern.compile(pattern2);
                Pattern r5 = Pattern.compile(pattern5);

                Matcher m2 = r2.matcher(listOfLines.get(i));
                Matcher m5 = r5.matcher(listOfLines.get(i));

                if (m2.find() && !m5.find()) {

                	String s = listOfLines.get(i).replaceAll("`", "***");

                	// special handle "***<"
                    if (s.contains("***") && s.contains("<")) {
                    	s = s.replace("<", "\\<");
                    	s = s.replace(">", "\\>");
                    }
                	
                    // special handle "Ctrl"
                	if (s.contains("***Ctrl+C***"))
                		s = s.replace("***Ctrl+C***", "`Ctrl+C`");

                	if (s.contains("***CTRL+C***"))
                		s = s.replace("***CTRL+C***", "`Ctrl+C`");

                	if (s.contains("***Ctrl+V***"))
                		s = s.replace("***Ctrl+V***", "`Ctrl+V`");

                	if (s.contains("***Command+V***"))
                		s = s.replace("***Command+V***", "`Command+V`");

                    
                	listOfLines.set(i, s);
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

//             Replaces left over ----
                if (listOfLines.get(i).startsWith("----")) {
                    replaceDashes(listOfLines, i);
                }

                if (listOfLines.get(i).startsWith("{empty} +")) {
                    listOfLines.set(i, "\n\n");
                }


                //For parts of text that need to be copied
                if (listOfLines.get(i).startsWith("[role='command']") || 
                    listOfLines.get(i).startsWith("[role=\"command\"") || 
                    listOfLines.get(i).startsWith("[role=command]")) {
                    // To be removed - no need to add {: cdodeblock}
                    //insertCopyButton(listOfLines, i);
                	if (listOfLines.get(i+1).startsWith("```")) {
                		listOfLines.set(i+1, listOfLines.get(i+1).replace("```", "```bash"));
                	} else if (listOfLines.get(i+1).startsWith("----")) {
                		listOfLines.set(i+1, listOfLines.get(i+1).replace("----", "```bash"));
                	}
                }
                

                //User is instructed to replace a file
                if (listOfLines.get(i).startsWith("#Replace") || listOfLines.get(i).startsWith("#Create") || listOfLines.get(i).startsWith("#Update")) {
                    final String atIndex = listOfLines.get(i);
                    codeInsert(atIndex, listOfLines, guideName, branch, i, position);
                }

                if (listOfLines.get(i).startsWith("image::")) {

                    String imageRepoLink = "https://raw.githubusercontent.com/OpenLiberty/" + guideName + "/" + (guideName.startsWith("draft") ? "draft" : "prod") + "/assets";

                    String imageName = listOfLines.get(i).substring(listOfLines.get(i).indexOf("::") + 2, listOfLines.get(i).indexOf("["));

                    String imageDesc = listOfLines.get(i).substring(listOfLines.get(i).indexOf("[") + 1, listOfLines.get(i).indexOf(","));

                    String imageLink = imageRepoLink + "/" + imageName;

                    if (listOfLines.get(i + 1).contains("{empty} +")) {
                        listOfLines.set(i + 1, "");
                    }

                    listOfLines.set(i, "![" + imageDesc + "]" + "(" + imageLink + ")\n\n");
                }


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

                // make the localhost links not accessible
                if (listOfLines.get(i).contains(" http://localhost:") ||
                    listOfLines.get(i).contains(" https://localhost:")
                   ) {
                    String s = listOfLines.get(i);
                    if (!s.contains("curl") && !s.contains("- url:") && 
                    	!s.contains("^]") && !s.contains("9292") &&
                    	!s.startsWith("[")
                       ) {
                        int h1 = s.indexOf("http://localhost:");
                        int l = "http://localhost:".length();
                        if (h1 < 0) {
                            h1 = s.indexOf("https://localhost:");
                            l++;
                        }
                        int h2 = s.indexOf(" ", h1+l);
                        if (h2 < 0) {
                            s = s.substring(0,h1) + "***" + s.substring(h1,s.length()-1) + "***\n";
                        } else {
                            s = s.substring(0,h1) + "***" + s.substring(h1,h2) + "***" + s.substring(h2);
                            h1 = s.indexOf("http://localhost:", h2);
                            l = "http://localhost:".length();
                            if (h1 < 0) {
                                h1 = s.indexOf("https://localhost:", h2);
                                l++;
                            }
                            if (h1 > 0) {
                                h2 = s.indexOf(" ", h1+l);
                                if (h2 < 0) {
                                    s = s.substring(0,h1) + "***" + s.substring(h1,s.length()-1) + "***\n";
                                } else {
                                    s = s.substring(0,h1) + "***" + s.substring(h1,h2) + "***" + s.substring(h2);
                                }
                            }
                        }
                        s = s.replaceAll("://localhost", "\\\\://localhost");
                        listOfLines.set(i, s);
                    }
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
//                        listOfLines.add(i + 12, "");
                        flag = false;
                    }
                }


                // end of guide
                if (listOfLines.get(i).startsWith("# Great work! You're done!")) {
                    String lastLine = listOfLines.get(i + 2);
                    if (lastLine.contains("`")) {
                        lastLine = lastLine.replaceAll("`", "**");
                    }
                    listOfLines.set(i + 2, "");
                    finish(listOfLines, lastLine, guideName, i);
                }

               //Identifies the start of a table
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

                // To be removed - no need to use backtick for string contain underscore character
                /*
                String pattern3 = "\\*\\*((?:(?!\\*\\*)[^_])*)_(.*?)\\*\\*";

                Pattern r3 = Pattern.compile(pattern3);

                Matcher m3 = r3.matcher(listOfLines.get(i));

                if (m3.find()) {
                    if (m3.group().contains("_")) {
                        String s = m3.group();
                        s = s.substring(s.indexOf("**") + 2, s.lastIndexOf("**"));
                        s = "**`" + s + "`**";
                        if ((s.length() < 90)) {
                            listOfLines.set(i, listOfLines.get(i).replaceAll("\\*\\*((?:(?!\\*\\*)[^_])*)_(.*?)\\*\\*", s));
                        } else {
                            listOfLines.set(i, listOfLines.get(i));
                        }
                    }
                }
                */

                // To be removed - no need to use backtick for string contain < or > characters
                /*
                String pattern11 = "\\*\\*<(.*?)>\\*\\*|\\*\\*\\[(.*?)<(.*?)>(.*?)\\]\\*\\*";

                Pattern r11 = Pattern.compile(pattern11);

                Matcher m11 = r11.matcher(listOfLines.get(i));

                if (m11.find()) {
                    if (m11.group().contains("<") && m11.group().contains(">")) {
                        String s = m11.group();
                        s = s.substring(s.indexOf("**") + 2, s.lastIndexOf("**"));
                        s = "**`" + s + "`**";
                        listOfLines.set(i, listOfLines.get(i).replaceAll("\\*\\*\\<", "**`<"));
                        listOfLines.set(i, listOfLines.get(i).replaceAll("\\>\\*\\*", ">`**"));
                        listOfLines.set(i, listOfLines.get(i).replaceAll("\\*\\*\\[(.*?)<(.*?)>(.*?)\\]\\*\\*", s));
                    }
                }
                */

                String pattern12 = "\\*\\*`((?:(?!\\*\\*))*)(.*?)(?!`)\\*\\*\\s";

                Pattern r12 = Pattern.compile(pattern12);

                Matcher m12 = r12.matcher(listOfLines.get(i));

                if (m12.find()) {
                    if (m12.group().contains("<") && m12.group().contains(">")) {
                        String s = m12.group();
                        s = s.substring(s.indexOf("**") + 3, s.lastIndexOf("**"));
                        s = "**`" + s + "`**";
                        if (s.contains("``**")) {
                            s = s.substring(s.indexOf("**`") + 3, s.lastIndexOf("``**"));
                            s = "**`" + s + "`**";
                        }
                        if (!s.contains("$")) {
                            listOfLines.set(i, listOfLines.get(i).replaceFirst("\\*\\*`((?:(?!\\*\\*))*)(.*?)(?!`)\\*\\*", s));
                        }
                    }
                }

                // To be removed - no need to add {: codeblock}
                /*
                if (listOfLines.get(i).startsWith("docker")) {
                    if (!listOfLines.get(i + 1).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                        listOfLines.add(i + 2, "");
                        listOfLines.set(i + 2, "{: codeblock}\n\n\n");
                    }
                }
                */

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

                // To be removed - no need to add {: codeblock}
                /*
                if (listOfLines.get(i).startsWith("mvn")) {
                    if (!listOfLines.get(i + 2).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                        if (!listOfLines.get(i + 3).startsWith("{: codeblock}") && listOfLines.get(i + 2).isBlank()) {
                            listOfLines.add(i + 2, "");
                            listOfLines.set(i + 1, "```\n{: codeblock}\n\n\n");
                        }
                    }
                }
                */

                addCodeblockAfterMVN(listOfLines, i);

                if (listOfLines.get(i).contains("^]")) {
                    link(listOfLines, i);
                }

                if (listOfLines.get(i).contains("# Related Links")) {
                    relatedLinksMove(listOfLines, i);
                }

                if (listOfLines.get(i).startsWith("### Try what you'll build")) {
                    int g = i + 1;
                    Functions.CheckTWYB(listOfLines, guideName, branch, g, position);
                }

                if (listOfLines.get(i).startsWith("#")) {
                    if (!listOfLines.get(i).contains("**")) {
                    	if (listOfLines.get(i).startsWith("# ") &&
                    		!listOfLines.get(i).startsWith("# TYPE ") &&  // for mp-metric guide
                    		!listOfLines.get(i).startsWith("# HELP ")     // for mp-metric guide
                    	   ) {
                    		String heading = listOfLines.get(i);
                    		heading = heading.replace("# ", "::page{title=\"");
                            heading = heading.replace("\n",  "\"}\n");
                            listOfLines.set(i, heading);
                    	} 
                    	// To be removed - no need to bold the section title
                    	/*
                    	else {
                            String HSize = listOfLines.get(i).substring(0, listOfLines.get(i).lastIndexOf("#") + 1);
                            String heading = listOfLines.get(i).substring(listOfLines.get(i).lastIndexOf("#") + 2, listOfLines.get(i).length() - 1);
                            heading = HSize + " **" + heading + "**\n";
                            listOfLines.set(i, heading);
                    	}
                    	*/
                    } else if (listOfLines.get(i).contains("# **Summary**")) {
                    	String heading = listOfLines.get(i);
                    	heading = heading.replace("# **Summary**", "::page{title=\"Summary\"}");
                    	listOfLines.set(i, heading);
                    }
                }

                if (listOfLines.get(i).startsWith("NO_COPY")) {
                    listOfLines.set(i, "");
// To be removed when SNL fix no copy button block
/*
                    // Reformat the no copy code block
                    boolean change = false;
                    for (int j = i + 1; j < i + 20; j++) {
                    	if (!change && listOfLines.get(j).startsWith("----")) {
                    		change = true;
                    		listOfLines.set(j, ">\n");
                    		continue;
                    	}
                    	if (change) {
                        	if (listOfLines.get(j).startsWith("----")) {
                        		change = false;
                        		listOfLines.set(j, ">\n");
                        		break;
                        	}
                    		String changed = ">" + listOfLines.get(j);
                    		listOfLines.set(j, changed);
                    	}
                    }
 */
                }

                // To be removed
                /*
                if (listOfLines.get(i).startsWith("##")) {
                    listOfLines.set(i, "<br/>\n" + listOfLines.get(i));
                }
                */

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
                if (listOfLines.get(i).startsWith("NO_COPY")) {
                    listOfLines.set(i, "");
                }
            }
        }
    }
}
