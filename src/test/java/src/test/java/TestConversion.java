import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class TestConversion {

    @Test
    public void LoadFile() {

        String guideName = System.getProperty("guideName");

        Scanner s = null;

        try {
            //read adoc file from the open liberty guide
            File guideToLoad = new File(guideName + ".md");
            s = new Scanner(guideToLoad);
//          ArrayList for whole text file
            ArrayList<String> listOfLines = new ArrayList<>();

            final String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "image::", "start/", "finish/", "system/", "inventory/"};


//          write each line into the file
            while (s.hasNextLine()) {
                String inputLine = s.nextLine() + "\n";

                System.out.println(inputLine);

                Assert.assertTrue(!inputLine.startsWith("[.tab_content.linux_section]"));
                Assert.assertTrue(!inputLine.contains("[hotspot"));
                Assert.assertTrue(!inputLine.equals("--"));
                for (int h = 0; h < startingPhrases.length; h++) {
                    Assert.assertTrue(!inputLine.startsWith(startingPhrases[h]));
                }
                Assert.assertTrue(!inputLine.contains("Diagram") || !inputLine.contains("diagram"));
                Assert.assertTrue(!inputLine.contains("[.tab_content"));


            }

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
