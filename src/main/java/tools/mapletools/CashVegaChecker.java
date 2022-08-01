package tools.mapletools;

import provider.wz.WZFiles;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author RonanLana
 * <p>
 * This application main objective is to read Vega-related information from
 * the item's description report back missing nodes for these items.
 * <p>
 * Estimated parse time: 10 seconds
 */
public class CashVegaChecker {
    private static final Path OUTPUT_FILE = ToolConstants.getOutputFile("vega_checker_report.txt");
    private static final int INITIAL_STRING_LENGTH = 1000;
    private static final Set<Integer> vegaItems = new HashSet<>();

    private static PrintWriter printWriter = null;
    private static InputStreamReader fileReader = null;
    private static BufferedReader bufferedReader = null;
    private static int currentItem;
    private static byte status = 0;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[INITIAL_STRING_LENGTH];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return (d.trim());
    }

    private static String getValue(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("value=");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[INITIAL_STRING_LENGTH];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return (d.trim());
    }

    private static void forwardCursor(int st) {
        String line = null;

        try {
            while (status >= st && (line = bufferedReader.readLine()) != null) {
                simpleToken(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simpleToken(String token) {
        if (token.contains("/imgdir")) {
            status -= 1;
        } else if (token.contains("imgdir")) {
            status += 1;
        }
    }

    private static void translateItemToken(String token) {
        if (token.contains("/imgdir")) {
            status -= 1;
        } else if (token.contains("imgdir")) {
            status += 1;

            if (status == 2) {
                currentItem = Integer.parseInt(getName(token));
            }
        } else {
            if (status == 2) {
                if (getValue(token).endsWith("Vega&apos;s Spell.")) {
                    vegaItems.add(currentItem);
                }
            }
        }
    }

    private static void translateVegaToken(String token) {
        if (token.contains("/imgdir")) {
            status -= 1;
        } else if (token.contains("imgdir")) {
            status += 1;
        } else {
            if (status == 2) {
                if (getName(token).contentEquals("item")) {
                    vegaItems.remove(Integer.valueOf(getValue(token)));
                }
            }
        }
    }

    private static void readItemDescriptionFile(File f) {
        System.out.print("Reading String.wz... ");
        try {
            fileReader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                translateItemToken(line);
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(vegaItems.size() + " Vega Scroll items found");
    }

    private static void readVegaDescriptionFile(File f) {
        System.out.println("Reading Etc.wz...");
        try {
            fileReader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                translateVegaToken(line);
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleCashVegaChecker feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }

    private static void reportMissingVegaItems() {
    	System.out.println("Reporting results ...");

        try (PrintWriter pw = new PrintWriter(Files.newOutputStream(OUTPUT_FILE))) {
            printWriter = pw;
            printReportFileHeader();

            for (Integer itemid : vegaItems) {
                printWriter.println("  " + itemid);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    public static void main(String[] args) {

        readItemDescriptionFile(new File(WZFiles.STRING.getFilePath() + "/Consume.img.xml"));
        readVegaDescriptionFile(new File(WZFiles.ETC.getFilePath() + "/VegaSpell.img.xml"));

        reportMissingVegaItems();
    }

}
