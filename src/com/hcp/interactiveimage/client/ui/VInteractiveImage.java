package com.hcp.interactiveimage.client.ui;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class VInteractiveImage extends AbsolutePanel implements Paintable
{
    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-imagewidget";
    public static final String BOX_CLASSNAME = CLASSNAME + "-box";
    public static final String COVER_CLASSNAME = CLASSNAME + "-cover";

    public static final String MOUSE_MOVE_EVENT = "ii_mouse_move_event";
    public static final String CLICK_EVENT = "ii_click_event";
    public static final String BOX_SELECT_EVENT = "ii_box_select_event";
    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    ApplicationConnection client;

    private final Poller poller;
    private int updateInterval = 500;

    private int canvasSizeX = 0, canvasSizeY = 0;
    private int bx, by, bw, bh, currentx, currenty;
    protected Image image = null;
    protected MousePanel coverPanel, boxPanel;
    private boolean running = false;

    private enum MouseStat
    {
        NONE, DRAGING, START
    };

    private MouseStat mouseStat = MouseStat.NONE;

    private native JavaScriptObject applyDisableTextSelectionHack()/*-{ 
                                                                   return function(){ return false; };
                                                                   }-*/;

    public VInteractiveImage()
    {
        super();
        this.setStyleName(CLASSNAME);
        this.setSize("1px", "1px");
        
        image = new Image();
        image.addLoadHandler(new LoadHandler()
        {
            public void onLoad(LoadEvent event)
            {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width != canvasSizeX || height != canvasSizeY)
                {
                    // Window.alert("DEBUG# new size");
                    canvasSizeX = width;
                    canvasSizeY = height;
                    VInteractiveImage.this.setSize(canvasSizeX + "px",
                            canvasSizeY + "px");
                    coverPanel.setSize(canvasSizeX + "px", canvasSizeY + "px");
                    coverPanel.setVisible(true);
                    Util.notifyParentOfSizeChange(VInteractiveImage.this, true);

                }
                // boxPanel.setVisible(true);
                running = true;
            }
        });
        this.add(image, 0, 0);

        boxPanel = new MousePanel();
        boxPanel.setStyleName(BOX_CLASSNAME);
        boxPanel.setSize("0px", "0px");
        boxPanel.setVisible(false);
        this.add(boxPanel, 0, 0);

        coverPanel = new MousePanel();
        coverPanel.setStyleName(COVER_CLASSNAME);
        coverPanel.getElement().setPropertyJSO("onselectstart",
                applyDisableTextSelectionHack());
        coverPanel.setSize("0px", "0px");
        this.add(coverPanel, 0, 0);
        coverPanel.setVisible(false);
        initHandlers();

        poller = new Poller();
        poller.scheduleRepeating(updateInterval);
    }

    private int moveStartX, moveStartY;

    private void initHandlers()
    {
        coverPanel.addMouseDownHandler(new MouseDownHandler()
        {
            public void onMouseDown(MouseDownEvent event)
            {
                if (!running)
                    return;
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT)
                {
                    moveStartX = event.getX();
                    moveStartY = event.getY();
                    if (mouseStat == MouseStat.NONE)
                        mouseStat = MouseStat.START;
                }
            }
        });
        coverPanel.addMouseMoveHandler(new MouseMoveHandler()
        {
            public void onMouseMove(MouseMoveEvent event)
            {
                if (!running)
                    return;
                if (mouseStat == MouseStat.START)
                    mouseStat = MouseStat.DRAGING;
                else if (mouseStat == MouseStat.DRAGING)
                {
                    int x = event.getX();
                    int y = event.getY();
                    int newx = (x < moveStartX) ? x : moveStartX;
                    int newy = (y < moveStartY) ? y : moveStartY;
                    int neww = Math.abs(x - moveStartX);
                    int newh = Math.abs(y - moveStartY);
                    VInteractiveImage.this.setWidgetPosition(boxPanel, newx,
                            newy);
                    boxPanel.setSize(neww + "px", newh + "px");
                    boxPanel.setVisible(true);
                } else
                {
                    currentx = event.getX();
                    currenty = event.getY();
                }
            }
        });

        coverPanel.addMouseOutHandler(new MouseOutHandler()
        {
            public void onMouseOut(MouseOutEvent event)
            {
                if (!running)
                    return;
                if (mouseStat != MouseStat.NONE)
                {
                    mouseStat = MouseStat.NONE;
                    VInteractiveImage.this.setWidgetPosition(boxPanel, bx, by);
                    boxPanel.setSize(bw + "px", bh + "px");
                }
            }
        });

        coverPanel.addMouseUpHandler(new MouseUpHandler()
        {
            public void onMouseUp(MouseUpEvent event)
            {
                if (!running)
                    return;
                if (mouseStat == MouseStat.DRAGING)
                {
                    mouseStat = MouseStat.NONE;
                    int x = event.getX();
                    int y = event.getY();
                    int newx = (x < moveStartX) ? x : moveStartX;
                    int newy = (y < moveStartY) ? y : moveStartY;
                    int neww = Math.abs(x - moveStartX);
                    int newh = Math.abs(y - moveStartY);
                    VInteractiveImage.this.setWidgetPosition(boxPanel, newx,
                            newy);
                    boxPanel.setSize(neww + "px", newh + "px");
                    running = false;
                    client.updateVariable(paintableId, BOX_SELECT_EVENT, newx + " "
                            + newy + " " + neww + " " + newh, true);

                } else if (mouseStat == MouseStat.START)
                {
                    mouseStat = MouseStat.NONE;
                    running = false;
                    client.updateVariable(paintableId, CLICK_EVENT,
                            event.getX() + " " + event.getY(), true);
                }
            }
        });
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
    {
        if (client.updateComponent(this, uidl, true))
        {
            return;
        }
        this.client = client;
        paintableId = uidl.getId();

        if (uidl.hasAttribute("box"))
        {
            String[] t = uidl.getStringAttribute("box").split(" ");
            bx = Integer.parseInt(t[0]);
            by = Integer.parseInt(t[1]);
            bw = Integer.parseInt(t[2]);
            bh = Integer.parseInt(t[3]);
            VInteractiveImage.this.setWidgetPosition(boxPanel, bx, by);
            boxPanel.setSize(bw + "px", bh + "px");
            if (bw == 0 || bh == 0)
            {
                boxPanel.setVisible(false);
            }
            else {
                boxPanel.setVisible(true);
            }
        }
        if (uidl.hasAttribute("updateInterval"))
        {
        	int newInterval = Integer.parseInt(uidl.getStringAttribute("updateInterval"));
        	if(newInterval > 0) {
        		updateInterval = newInterval;
        		poller.scheduleRepeating(updateInterval);
        	}
        	else {
        		updateInterval = -1;
        		poller.cancel();
        	}
        }
        if (uidl.hasAttribute("image"))
        {
            String url = client.translateVaadinUri(uidl
                    .getStringAttribute("image"));
            if (!image.getUrl().endsWith(url))
            {
                // update Image
                running = false;
                image.setUrl(url);
            } else
                running = true;
        } else
        {
            running = true;
        }
    }

    private class Poller extends Timer
    {
        private int newx, newy, lastx, lasty;

        @Override
        public void run()
        {
            if (!running)
                return;
            if (mouseStat == MouseStat.NONE)
            {
                newx = currentx;
                newy = currenty;
                if (newx != lastx || newy != lasty)
                {
                    lastx = newx;
                    lasty = newy;
                    client.updateVariable(paintableId, MOUSE_MOVE_EVENT, newx + " "
                            + newy, true);
                }
            }
        }
    }
}
