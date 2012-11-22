package com.hcp.resource;

import java.awt.image.BufferedImage;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;

public class BufferedImageResource extends StreamResource
{
    private static final long serialVersionUID = 5380980244376358019L;
    private int refreshCnt = 0;

    /**
     * 
     * @param img
     * @param application
     * @param disableBrowserCache
     */
    public BufferedImageResource(BufferedImage img, Application application,
            boolean disableBrowserCache)
    {
        super(new BufferedImageSource(img), "0.png", application);
        if (disableBrowserCache) this.setCacheTime(0);
    }

    /**
     * default constructor disables browser cache.
     * @param img
     * @param application
     */
    public BufferedImageResource(BufferedImage img, Application application)
    {
        this(img, application, true);
    }

    /**
     * Only works for BufferedImageResource created with disableBrowserCache=true
     * @param img BufferedImage to redraw
     */
    public void refresh(BufferedImage img)
    {
        refreshCnt++;
        this.setStreamSource(new BufferedImageSource(img));
        this.setFilename(refreshCnt + ".png");
    }
    
    @Override
    public BufferedImageSource getStreamSource() {
    	return (BufferedImageSource) super.getStreamSource();
    }
    
    /**
     * 
     * @return the BufferedImage underhood.
     */
    public BufferedImage getImage() {
    	return this.getStreamSource().getImage();
    }
}
