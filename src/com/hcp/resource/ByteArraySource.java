package com.hcp.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import com.vaadin.terminal.StreamResource.StreamSource;

public class ByteArraySource implements StreamSource
{
    private static final long serialVersionUID = 3996616105233032145L;
    private byte[] ba;

    public ByteArraySource(byte[] ba, boolean enableGzip)
    {
        if (enableGzip)
        {
            ByteArrayOutputStream bb = new ByteArrayOutputStream();
            try
            {
                GZIPOutputStream zo = new GZIPOutputStream(bb);
                zo.write(ba);
                zo.finish();
                zo.close();
                this.ba = bb.toByteArray();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } else
        {
            this.ba = ba;
        }
    }

    public ByteArraySource(byte[] ba)
    {
        this(ba, false);
    }

    public InputStream getStream()
    {
        if (ba == null)
            return null;
        else
            return new ByteArrayInputStream(ba);
    }

}
