package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;

/** Parses the proximity sensor log file. */
public class ProximitySensorLogParser
    extends SensorLogParser {

    /** Constructor that initialises the parsing. */
    public ProximitySensorLogParser( File logFile ) 
        throws IOException {
        super( logFile );
    }

    /** {@inheritDoc} */
    @Override
    protected void parseNextLine() 
        throws IOException {

        // Check if there is another log line available
        if( ! in.hasNextLine() ) {
            nextEvent = null;
            return;
        }

        // Parses the line
        String line = in.nextLine();
        String[] parts = line.split( "," );
        if( parts.length != 4 ) {
            throw new IOException( "Invalid line in log file \'" + 
                                   logFile.getName() + "\': " + line );
        }
        long timestamp = Long.parseLong( parts[1] );
        float proximity = Float.parseFloat( parts[2] );
        int accuracy = Integer.parseInt( parts[3] );

        // Create the next sensor event object
        nextEvent = new ProximityEvent( timestamp , proximity , accuracy );
    }
}
