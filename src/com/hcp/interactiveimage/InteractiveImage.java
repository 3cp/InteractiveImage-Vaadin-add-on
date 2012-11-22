package com.hcp.interactiveimage;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.hcp.interactiveimage.client.ui.VInteractiveImage;
import com.hcp.resource.BufferedImageResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VInteractiveImage widget.
 */
@com.vaadin.ui.ClientWidget(com.hcp.interactiveimage.client.ui.VInteractiveImage.class)
public class InteractiveImage extends AbstractComponent {
	private static final long serialVersionUID = -8635965737478695640L;
	public static final int UpdateIntervalMin = 50, UpdateIntervalMax = 10000;
	private BufferedImageResource imageResource;
	private transient BufferedImage image;
	private int updateInterval = 500;
	private Rectangle highlightBox = new Rectangle();
	private InteractiveImageListener listener;

	public InteractiveImage() {
		super();
	}

	@Override
	public void setWidth(float width, int unit) {
		throw new UnsupportedOperationException(
				"setWidth(...) on "
						+ this.getClass().getName()
						+ " is not supported.\nsetImage(image) automatically updates the size of this component with the size of image.");
	}

	@Override
	public void setHeight(float height, int unit) {
		throw new UnsupportedOperationException(
				"setheight(...) on "
						+ this.getClass().getName()
						+ " is not supported.\nsetImage(image) automatically updates the size of this component with the size of image.");
	}
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		if (imageResource != null)
			target.addAttribute("image", imageResource);

		target.addAttribute("box", highlightBox.x + " " + highlightBox.y + " "
				+ highlightBox.width + " " + highlightBox.height);

		target.addAttribute("updateInterval", updateInterval);
	}

	public interface InteractiveImageListener {
		public void onMouseMove(int x, int y);
		public void onClick(int x, int y);
		public void onBoxSelect(int x, int y, int w, int h);
	}

	/**
	 * set the optional InteractiveImageListener to handler MouseMove, Click and BoxSelect.
	 * @param l
	 */
	public void setListener(InteractiveImageListener l) {
		listener = l;
	}

	public void unsetListener() {
		listener = null;
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		if (variables.containsKey(VInteractiveImage.CLICK_EVENT)) {
			String[] t = ((String) variables.get(VInteractiveImage.CLICK_EVENT))
					.split(" ");
			fireClickEvent(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
		}
		if (variables.containsKey(VInteractiveImage.MOUSE_MOVE_EVENT)) {
			String[] t = ((String) variables
					.get(VInteractiveImage.MOUSE_MOVE_EVENT)).split(" ");
			fireLocateEvent(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
		}
		if (variables.containsKey(VInteractiveImage.BOX_SELECT_EVENT)) {
			String[] t = ((String) variables.get(VInteractiveImage.BOX_SELECT_EVENT))
					.split(" ");
			fireBoxEvent(Integer.parseInt(t[0]), Integer.parseInt(t[1]),
					Integer.parseInt(t[2]), Integer.parseInt(t[3]));
		}
		this.requestRepaint();
	}

	private void fireBoxEvent(int x, int y, int w, int h) {
		if (listener != null)
			listener.onBoxSelect(x, y, w, h);
	}

	private void fireClickEvent(int x, int y) {
		if (listener != null)
			listener.onClick(x, y);
	}

	private void fireLocateEvent(int x, int y) {
		if (listener != null)
			listener.onMouseMove(x, y);
	}

	/**
	 * Set the BufferedImage. (Null is acceptable)
	 * @param image
	 */
	public void setImage(BufferedImage image) {

		this.image = image;
		this.updateImage();
	}

	/**
	 * Get the current BufferedImage
	 * @return
	 */
	public BufferedImage getImage() {
		//after server restarted, for the same session, imageResource is persistent, image is not.
		if (image == null && imageResource != null) {
			image = imageResource.getImage();
		}
		return image;
	}

	@Override
	public void attach() {
		super.attach();
		if (imageResource != null) {
			this.getApplication().removeResource(imageResource);
			imageResource = null;
		}
		this.updateImage();
	}

	private void updateImage() {
		if (image == null) {
			super.setWidth(1, UNITS_PIXELS);
			super.setHeight(1, UNITS_PIXELS);
		} else {

			super.setWidth(image.getWidth(), UNITS_PIXELS);
			super.setHeight(image.getHeight(), UNITS_PIXELS);
		}
		if (this.getApplication() == null)
			return;

		if (imageResource == null) {
			imageResource = new BufferedImageResource(image,
					this.getApplication());
		} else {
			imageResource.refresh(image);
		}

		this.requestRepaint();
	}

	@Override
	public void detach() {
		if (imageResource != null) {
			this.getApplication().removeResource(imageResource);
			imageResource = null;
		}
		super.detach();
	}

	/**
	 * get the current highlight box rectangle
	 * @return
	 */
	public Rectangle getHighlightBox() {
		return highlightBox;
	}

	/**
	 * set the highlight box rectangle.
	 * @param x
	 * @param y
	 * @param w width
	 * @param h height
	 */
	public void setHighlightBox(int x, int y, int w, int h) {
		highlightBox.setBounds(x, y, w, h);
		this.requestRepaint();
	}

	/**
	 * 
	 * @return the current mouse move update interval in milliseconds. (return value -1 means MouseMove event has been disabled.)
	 */
	public int getMouseMoveUpdateInterval() {
		return updateInterval;
	}

	/**
	 * Set the update interval for MouseMove event in milliseconds.
	 * Pass a negative integer or 0 to disable it.
	 * Pass an integer between 50 and 10000 to set new update interval.
	 * The default value is 500ms, you may set a proper value based on your client/server latency.
	 * @param updateInterval the new update interval in milliseconds.
	 */
	public void setMouseMoveUpdateInterval(int updateInterval) {
		if (updateInterval <= 0) {
			updateInterval = -1;
		}
		else if (updateInterval < UpdateIntervalMin
				|| updateInterval > UpdateIntervalMax) {
			throw new RuntimeException("UpdateInterval: " + updateInterval
					+ " is not acceptable. Please use an int between "
					+ UpdateIntervalMin + " and " + UpdateIntervalMax);
		}
		this.updateInterval = updateInterval;
		this.requestRepaint();
	}
}
