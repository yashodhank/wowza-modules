/**
 *
 */
package dk.statsbiblioteket.doms.wowza.plugin.model;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

/**
 * TODO javadoc
 *
 * @author heb + jrg
 */
public class DomsUriToFileMapper implements IMediaStreamFileMapper {

    private static SimpleDateFormat sdf
            = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private WMSLogger wmsLogger;

    // TODO: Remove default file mapper. Only for development purpose
    private IMediaStreamFileMapper defaultFileMapper;
    private String storageDir;

    /**
     * TODO javadoc
     *
     * @param storageDir
     * @param wmslogger
     * @param defaultFileMapper
     */
    public DomsUriToFileMapper(String storageDir, WMSLogger wmslogger,
                               IMediaStreamFileMapper defaultFileMapper) {
        this.wmsLogger = wmslogger;
        this.defaultFileMapper = defaultFileMapper;
        this.storageDir = storageDir;
        wmslogger.info("Creating mapper...");
        wmslogger.info("Creating mapper: StorageDir=" + storageDir);
    }

    /**
     * Get the file that should be streamed.
     *
     * TODO javadoc
     *
     * @param stream
     * @return
     */
    /* (non-Javadoc)
      * @see com.wowza.wms.stream.IMediaStreamFileMapper#streamToFileForRead(
      * com.wowza.wms.stream.IMediaStream)
      *
      * This method knows of the Wowza context, such as the content folder where
      * the files are placed
      */
    @Override
    public File streamToFileForRead(IMediaStream stream) {
        wmsLogger.info("***Entered streamToFileForRead(IMediaStream stream)");
        File fileToStream;

        try {
            // Extract filename from the query string
            String queryString = stream.getClient().getQueryStr();
            
            wmsLogger.info("queryString: '" + queryString + "'");
            String filename = extractFilename(queryString);
            wmsLogger.info("filename: '" + filename + "'");

            fileToStream = new File(storageDir + "/" + filename);
            wmsLogger.info("Got fileToStream");

            // Authorization check will throw InvalidURIException if not
            // authorized.
            checkAuthorization(stream);

        } catch (InvalidURIException e) {
            // No other means to signal Wowza that the request was wrong.
            // TODO Maybe here we wanna point to a video saying "access denied"
            fileToStream = null;
        }

        return fileToStream;
    }

    /**
     * Get the file that should be streamed.
     *
     * TODO javadoc
     *
     * @param stream
     * @param name
     * @param ext
     * @param query
     * @return
     */
    /* (non-Javadoc)
      * @see com.wowza.wms.stream.IMediaStreamFileMapper#streamToFileForRead(
      * com.wowza.wms.stream.IMediaStream, java.lang.String, java.lang.String,
      * java.lang.String)
      *
      * This method knows of the Wowza context, such as the content folder where
      * the files are placed
      */
    @Override
    public File streamToFileForRead(IMediaStream stream, String name,
                                    String ext, String query) {
        wmsLogger.info("***Entered streamToFileForRead(IMediaStream stream, "
                + "String name, String ext, String query)");

        return streamToFileForRead(stream);
    }

    @Override
    public File streamToFileForWrite(IMediaStream arg0) {
        wmsLogger.info("***Entered streamToFileForWrite(IMediaStream arg0)");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File streamToFileForWrite(IMediaStream arg0, String arg1,
                                     String arg2, String arg3) {
        wmsLogger.info("***Entered streamToFileForWrite(IMediaStream arg0, "
                + "String arg1, String arg2, String arg3");
        return null;
    }

    /**
     * This method knows how to extract a filename from a DOMS-PID in
     * a query string.
     *
     * @param queryString the query string of a stream, of the format:
     * "shard=http://www.statsbiblioteket.dk/doms/shard/<DOMS-PID>&ticket=<TICKET-ID>"
     * @return the filename of the movie clip that represents the program
     * @throws InvalidURIException if the URI is invalid
     */
    protected String extractFilename(String queryString)
            throws InvalidURIException {
        wmsLogger.info("***Entered extractFilename('" + queryString + "')");
        String shardId;

        // Create a pattern to match a correct query string
        Pattern queryPattern = Pattern.compile(
                "shard=http://www.statsbiblioteket.dk/doms/shard/uuid%3A([^&]*)"
                        + "&ticket=([^&]*)");

        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();

        // Extract
        if (matchFound) {
            shardId = matcher.group(1);
        } else {
            wmsLogger.info("Query string did not match required format, "
                    + "throwing exception");
            throw new InvalidURIException("Query string is not of the expected"
                    + " format.");
        }

        return shardId + ".mp4";
    }

    /**
     * Checks (with ticket issuer) that the ticket and shard url received from
     * player, along with the players ip, allow the stream to be played.
     *
     * @param stream The stream that we need to authorize for playing.
     * @throws InvalidURIException In case the stream was not allowed to be
     * played.
     */
    private void checkAuthorization(IMediaStream stream)
            throws InvalidURIException {
        wmsLogger.info("***Entered checkAuthorization(stream)");
        String queryString = stream.getClient().getQueryStr();
        String ipOfClientPlayer = stream.getClient().getIp();
        String ticket;
        String shardUrl;

        wmsLogger.info("queryString: " + queryString);

        ticket = getTicketFromQueryString(queryString);
        shardUrl = getShardUrlFromQueryString(queryString);

        
        //TODO ...

    }

    /**
     * TODO javadoc
     *
     * @param queryString
     * @return
     * @throws InvalidURIException
     */
    public String getTicketFromQueryString(String queryString)
            throws InvalidURIException {
        wmsLogger.info("***Entered getTicketFromQueryString('"
                + queryString + "')");
        String ticketId;

        // Create a pattern to match a correct query string
        Pattern queryPattern = Pattern.compile(
                "shard=(http://www.statsbiblioteket.dk/doms/shard/uuid%3A[^&]*)"
                        + "&ticket=([^&]*)");

        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();

        // Extract
        if (matchFound) {
            ticketId = matcher.group(2);
        } else {
            wmsLogger.info("Query string did not match required format, "
                    + "throwing exception");
            throw new InvalidURIException("Query string is not of the expected"
                    + " format.");
        }

        return ticketId;
    }

    /**
     * TODO javadoc
     *
     * @param queryString
     * @return
     * @throws InvalidURIException
     */
    public String getShardUrlFromQueryString(String queryString)
            throws InvalidURIException {
        wmsLogger.info("***Entered getShardUrlFromQueryString('"
                + queryString + "')");
        String shardPid;

        // Create a pattern to match a correct query string
        Pattern queryPattern = Pattern.compile(
                "shard=(http://www.statsbiblioteket.dk/doms/shard/uuid%3A[^&]*)"
                        + "&ticket=([^&]*)");

        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();

        // Extract
        if (matchFound) {
            shardPid = matcher.group(1);
        } else {
            wmsLogger.info("Query string did not match required format, "
                    + "throwing exception");
            throw new InvalidURIException("Query string is not of the expected"
                    + " format.");
        }

        return shardPid;
    }

    /**
     * TODO javadoc
     *
     * @param dateString
     * @throws ParseException
     */
    protected void validateStringAsDate(String dateString)
            throws ParseException {
        Date date = sdf.parse(dateString);
        String dateReturnString = sdf.format(date);
        if (!dateString.equals(dateReturnString)) {
            throw new ParseException("Date is not valid. Read: " + dateString
                    + ". " + "Interpreted: " + dateReturnString, 0);
        }
    }
}