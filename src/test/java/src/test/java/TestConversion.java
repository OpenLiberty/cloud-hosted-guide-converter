import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class TestConversion {

    String inputLine = null;
    final String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "image::", "start/", "finish/", "system/", "inventory/"};
    public int h = 0;

    @Before
    public void main() {

        String guideName = System.getProperty("guideName");

        Scanner s = null;

        try {
            File guideToLoad = new File(guideName + ".md");
            s = new Scanner(guideToLoad);




//          write each line into the file
            while (s.hasNextLine()) {

               inputLine = s.nextLine() + "\n";

                testTab();

                testHotspot();

                testDash();

//                for (h = 0; h < startingPhrases.length; h++) {
//                    testPhrases();
//                }

                testDiagram();

                testWindowsCommand();

                System.out.println(inputLine);
            }


        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    @Test
    public void testTab() {
        Assert.assertTrue(!inputLine.startsWith("[.tab_content.linux_section]"));
    }

    @Test
    public void testHotspot() {
        Assert.assertTrue(!inputLine.contains("[hotspot"));
    }

    @Test
    public void testDash() {
        Assert.assertTrue(!inputLine.equals("--"));
    }

    @Test
    public void testPhrases() {
        Assert.assertTrue(!inputLine.startsWith(startingPhrases[h]));
    }

    @Test
    public void testDiagram() {
        Assert.assertTrue(!inputLine.contains("Diagram") || !inputLine.contains("diagram"));
    }

    @Test
    public void testWindowsCommand() {
        Assert.assertTrue(!inputLine.contains("[.tab_content"));
    }
}
