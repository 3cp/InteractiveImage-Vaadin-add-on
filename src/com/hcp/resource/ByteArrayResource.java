package com.hcp.resource;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;

public class ByteArrayResource extends StreamResource
{
    private static final long serialVersionUID = -4107181119840303378L;

    /**
     * 
     * @param ba
     * @param enableGzip
     * @param filename
     * @param application
     */
    public ByteArrayResource(byte[] ba, boolean enableGzip, String filename,
            Application application)
    {
        super(new ByteArraySource(ba, enableGzip), filename
                + (enableGzip ? ".gz" : ""), application);
        this.setCacheTime(0);
    }
    
    /**
     * default disable gzip compression
     * @param ba
     * @param filename
     * @param application
     */
    public ByteArrayResource(byte[] ba, String filename,
            Application application)
    {
        this(ba, false, filename, application);
    }
}
