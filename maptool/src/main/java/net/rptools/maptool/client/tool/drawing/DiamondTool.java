/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.tool.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DiamondTool extends AbstractDrawingTool implements MouseMotionListener {

	private static final long serialVersionUID = 8239333601131612106L;
	protected Shape diamond;
	protected ZonePoint originPoint;

	public DiamondTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/draw-blue-diamond.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public String getInstructions() {
		return "tool.rect.instructions";
	}

	@Override
	public String getTooltip() {
		return "tool.diamond.tooltip";
	}

	@Override
	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
		if (diamond != null) {
			Pen pen = getPen();
			if (pen.isEraser()) {
				pen = new Pen(pen);
				pen.setEraser(false);
				pen.setPaint(new DrawableColorPaint(Color.white));
				pen.setBackgroundPaint(new DrawableColorPaint(Color.white));
			}
			paintTransformed(g, renderer, new ShapeDrawable(diamond, false), pen);
			ToolHelper.drawDiamondMeasurement(renderer, g, diamond);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		ZonePoint zp = getPoint(e);
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (diamond == null) {
				originPoint = zp;
				diamond = createDiamond(originPoint, originPoint);
			}
			setIsEraser(isEraser(e));
		}
		super.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		ZonePoint zp = getPoint(e);
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (diamond != null) {
				diamond = createDiamond(originPoint, zp);

				if (diamond.getBounds().width == 0 || diamond.getBounds().height == 0) {
					diamond = null;
					renderer.repaint();
					return;
				}
				//ToolHelper.drawDiamondMeasurement(renderer, null, diamond);
				completeDrawable(renderer.getZone().getId(), getPen(), new ShapeDrawable(diamond, false));
				diamond = null;
			}
			setIsEraser(isEraser(e));
		}
		super.mouseReleased(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (diamond == null) {
			super.mouseDragged(e);
		}

		if (diamond != null) {
			ZonePoint p = getPoint(e);
			diamond = createDiamond(originPoint, p);
			renderer.repaint();
		}
	}

	/**
	 * Stop drawing a rectangle and repaint the zone.
	 */
	@Override
	public void resetTool() {
		if (diamond != null) {
			diamond = null;
			originPoint = null;
			renderer.repaint();
		} else {
			super.resetTool();
		}
	}
}
