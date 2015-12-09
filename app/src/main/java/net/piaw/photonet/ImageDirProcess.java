/**
 * Created by xiaoqin on 11/12/2015.
 */
package net.piaw.photonet;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;
import android.view.ViewDebug;

public class ImageDirProcess {


    public static void readImageInfoList(File infoFileName, ImageInfoList info_list, int num) {

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(infoFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            String line;
            DateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);

            line = bufferedReader.readLine();
            if (line == null)
                return;

            info_list.recentTime = format.parse(line);

            String[] tokens;
            ImageInfo info;
            while ((line = bufferedReader.readLine()) != null) {
                tokens = line.split(";");
                info = new ImageInfo(format.parse(tokens[0]), Double.parseDouble(tokens[1]),
                        Double.parseDouble(tokens[2]), tokens[3], tokens[4]);
                Log.d("info: " + line,  " " + info.toString());
                info_list.add_one_image_info(info);
                if ((num != 0) && (info_list.infoList.size() == num))
                    break;
            }

            Log.d("read infofile till index: ", " " +info_list.infoList.size());
            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            infoFileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + infoFileName + "'");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void writeImageInfoList(ImageInfoList imageInfoList, File infoFile)
            throws IOException {
        ArrayList<ImageInfo> infoList = imageInfoList.infoList;
        Collections.sort(infoList, new ImageInfoComparator());
        FileWriter fw = new FileWriter(infoFile.getAbsoluteFile(), false);

        String content = imageInfoList.recentTime.toString() + "\n";
        fw.write(content);

        for (ImageInfo info : infoList) {
            content = info.date.toString() + ";" + info.lat + ";" + info.lon + ";" +
                    info.addrStr + ";" + info.fileName + "\n";
            fw.write(content);
        }

        fw.flush();
        fw.close();
    }
}