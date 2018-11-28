package parlamentdl;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jsoup.Connection.Response;

/**
 *
 * @author Fodi
 */
public class ParlamentDL
{

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1";
    public static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public static String defaultRequestedVideoResolution = "1280x720";

    public static Date parseDateStamp(String dateToParse)
    {
        if (dateToParse == null)
        {
            return null;
        }

        simpleDateFormatter.setLenient(false);

        Date date = null;

        try
        {
            date = simpleDateFormatter.parse(dateToParse);
        } catch (ParseException e)
        {
            return null;
        }
        return date;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Date date = null;
        String videoPlayerURL = null, videoPlaylistURL = null, videoChunklistURL = null, videoChunklistRootURL = null;
        String videoPlaylistContent = null;
        String[] videoChunklistContent = null;
        List<String> videoChunkURLs = new ArrayList<String>();

        // try to parse date stamp from first argument
        if (args.length > 0)
        {
            date = parseDateStamp(args[0]);
            if (date == null)
            {
                System.err.format("ERROR: unable to parse date '%s'\n", args[0]);
                System.err.println("EXITING. Error code: -1");
                System.exit(-1);
            } else
            {
                System.out.println("INFO: Date stamp argument was specified: " + simpleDateFormatter.format(date));
            }
        } else
        {
            date = Calendar.getInstance().getTime();
            System.out.println("INFO: Date stamp argument not specified, today's date will be used: " + simpleDateFormatter.format(date));
        }

        // get video archive page
        try
        {
            System.out.println("INFO: Attempting to download video archive page.");
            Document doc = Jsoup.connect("http://www.parlament.hu/videoarchivum").userAgent(DEFAULT_USER_AGENT).get();
            System.out.println("INFO: Successfully downloaded video archive page.");

            simpleDateFormatter = new SimpleDateFormat("yyyy.MM.dd.");

            for (Element row : doc.select("table tr")) // iterate through table rows
            {
                if (row.select("td:first-of-type").text().startsWith(simpleDateFormatter.format(date))) // check if right date is in first column
                {
                    for (Element link : row.select("td a")) // iterate through links in row
                    {
                        if (link.absUrl("href").startsWith("http://sgis.parlament.hu/archive/playseq.php")) // check if link is a player link
                        {
                            videoPlayerURL = link.absUrl("href");
                            System.out.println("INFO: Video player page URL: " + videoPlayerURL);
                        }
                    }
                }
            }

        } catch (IOException ex)
        {
            System.err.println("ERROR: Unable to download video archive page: " + ex.toString());
            System.err.println("EXITING. Error code: -2");
            System.exit(-2);
        }

        if (videoPlayerURL == null)
        {
            System.err.println("ERROR: Unable to find video for date.");
            System.err.println("EXITING. Error code: -3");
            System.exit(-3);
        }

        // get video playlist
        try
        {
            System.out.println("INFO: Attempting to download video playlist.");
            Document doc = Jsoup.connect(videoPlayerURL).userAgent(DEFAULT_USER_AGENT).ignoreContentType(true).get(); // server will redirect from video player page to a m3u8 playlist because of the iPhone user string
            System.out.println("INFO: Successfully downloaded video playlist.");

            Response response = Jsoup.connect(videoPlayerURL).userAgent(DEFAULT_USER_AGENT).ignoreContentType(true).followRedirects(true).execute(); // get final redirect URL
            videoPlaylistURL = response.url().toString();
            System.out.println("INFO: Video playlist URL: " + videoPlaylistURL);

            videoPlaylistContent = doc.wholeText();
            videoChunklistRootURL = videoPlaylistURL.substring(0, videoPlaylistURL.lastIndexOf('/') + 1);

            String requestedVideoResolution = (args.length > 1 ? args[1] : defaultRequestedVideoResolution);

            // construct video chunklist URL for requested resolution stream
            if (videoPlaylistContent.indexOf("RESOLUTION=" + requestedVideoResolution) > 0)
            {
                videoChunklistURL = videoPlaylistContent.substring(videoPlaylistContent.indexOf("RESOLUTION=" + requestedVideoResolution) + (12 + requestedVideoResolution.length()));
                videoChunklistURL = videoChunklistURL.substring(0, videoChunklistURL.indexOf(".m3u8") + 5);
                videoChunklistURL = videoChunklistRootURL + videoChunklistURL;
                System.out.println("INFO: Video chunklist URL: " + videoChunklistURL);
            }
        } catch (IOException ex)
        {
            System.err.println("ERROR: Unable to download video playlist: " + ex.toString());
            System.err.println("EXITING. Error code: -5");
            System.exit(-5);
        }

        if (videoChunklistURL == null)
        {
            System.err.println("ERROR: Video playlist does not contain a chunklist for the requested video resolution (" + defaultRequestedVideoResolution + ").");
            System.err.println("EXITING. Error code: -6");
            System.exit(-6);
        }

        // get video chunklist
        try
        {
            System.out.println("INFO: Attempting to download video chunklist.");
            Document doc = Jsoup.connect(videoChunklistURL).userAgent(DEFAULT_USER_AGENT).ignoreContentType(true).get();
            System.out.println("INFO: Successfully downloaded video chunklist.");

            videoChunklistContent = doc.wholeText().split("\n");

            for (String videoChunklistContentLine : videoChunklistContent)
            {
                if (videoChunklistContentLine.endsWith(".ts"))
                {
                    System.out.println(videoChunklistRootURL + videoChunklistContentLine);
                    videoChunkURLs.add(videoChunklistRootURL + videoChunklistContentLine);
                }
            }

        } catch (IOException ex)
        {
            System.err.println("ERROR: Unable to download video chunklist: " + ex.toString());
            System.err.println("EXITING. Error code: -7");
            System.exit(-7);
        }
    }
}
