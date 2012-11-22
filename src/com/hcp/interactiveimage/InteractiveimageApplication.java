package com.hcp.interactiveimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.vaadin.Application;
import com.vaadin.ui.*;

public class InteractiveimageApplication extends Application implements
		InteractiveImage.InteractiveImageListener {
	private static final long serialVersionUID = -984559736698995896L;
	private Label info;
	private InteractiveImage imgWidget;
	private Window mainWindow;

	@Override
	public void init() {
		mainWindow = new Window("Interactiveimage Application");
		
		imgWidget = new InteractiveImage();
		// set an optional InteractiveImageListener to handler locate_event,
		// clickEvent and boxEvent.
		imgWidget.setListener(this);
		BufferedImage image = new BufferedImage(400, 300,
				BufferedImage.TYPE_INT_RGB);
		imgWidget.setImage(image);

		mainWindow.addComponent(imgWidget);

		info = new Label("<h2>Mouse location:</h2>", Label.CONTENT_XHTML);
		mainWindow.addComponent(info);
		setMainWindow(mainWindow);
	}

	public void onMouseMove(int x, int y) {
		info.setValue("<h2>Mouse location: x=" + x + " y=" + y + "</h2>");
	}

	public void onClick(int x, int y) {
		mainWindow.addComponent(new Label("Clicked at x=" + x + " y=" + y));
		BufferedImage image = imgWidget.getImage();
		if (image == null)
			return;
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.red);
		g2d.drawRoundRect(x - 5, y - 5, 10, 10, 10, 10);
		g2d.dispose();
		imgWidget.setImage(image);
	}

	public void onBoxSelect(int x, int y, int w, int h) {
		mainWindow.addComponent(new Label("Selected at x=" + x + " y=" + y
				+ " width=" + w + " height=" + h));
		BufferedImage image = imgWidget.getImage();
		if (image == null)
			return;
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.yellow);
		g2d.drawRect(x, y, w, h);
		g2d.dispose();
		imgWidget.setImage(image);
	}
}
