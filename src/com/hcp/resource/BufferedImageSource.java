package com.hcp.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.vaadin.terminal.StreamResource.StreamSource;

public class BufferedImageSource implements StreamSource {
	private static final long serialVersionUID = -1127952577695988570L;
	private byte[] data;

	public BufferedImageSource(BufferedImage image) {
		if (image == null) {
			data = null;
		} else {
			ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "png", imagebuffer);
				data = imagebuffer.toByteArray();
			} catch (IOException e) {
			}
		}
	}

	public InputStream getStream() {
		if (data == null)
			return null;
		else
			return new ByteArrayInputStream(data);
	}

	public BufferedImage getImage() {
		if (data == null) {
			return null;
		}
		else {
			ByteArrayInputStream imagebuffer = new ByteArrayInputStream(data);
			try {
				return ImageIO.read(imagebuffer);
			} catch (IOException e) {
				return null;
			}
		}
	}
}
