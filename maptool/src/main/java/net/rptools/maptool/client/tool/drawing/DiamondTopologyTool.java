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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DiamondTopologyTool extends AbstractDrawingTool implements MouseMotionListener {

	private static final long serialVersionUID = -1497583181619555786L;
	protected Shape diamond;
	protected ZonePoint originPoint;

	public DiamondTopologyTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/top-blue-diamond.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	// Override abstracttool to prevent color palette from
	// showing up
	protected void attachTo(ZoneRenderer renderer) {
		super.attachTo(renderer);
		// Hide the drawable color palette
		MapTool.getFrame().hideControlPanel();
	}

	@Override
	public boolean isAvailable() {
		return MapTool.getPlayer().isGM();
	}

	@Override
	public String getInstructions() {
		return "tool.recttopology.instructions";
	}

	@Override
	public String getTooltip() {
		return "tool.recttopology.tooltip";
	}

	@Override
	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
		if (MapTool.getPlayer().isGM()) {
			Zone zone = renderer.getZone();
			Area topology = zone.getTopology();

			Graphics2D g2 = (Graphics2D) g.create();
			g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
			g2.scale(renderer.getScale(), renderer.getScale());

			g2.setColor(AppStyle.topologyColor);
			g2.fill(topology);

			g2.dispose();
		}
		if (diamond != null) {
			Pen pen = new Pen();
			pen.setEraser(getPen().isEraser());
			pen.setOpacity(AppStyle.topologyRemoveColor.getAlpha() / 255.0f);
			pen.setBackgroundMode(Pen.MODE_SOLID);

			if (pen.isEraser()) {
				pen.setEraser(false);
			}
			if (isEraser()) {
				pen.setBackgroundPaint(new DrawableColorPaint(AppStyle.topologyRemoveColor));
			} else {
				pen.setBackgroundPaint(new DrawableColorPaint(AppStyle.topologyAddColor));
			}
			paintTransformed(g, renderer, new ShapeDrawable(diamond, false), pen);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			ZonePoint zp = getPoint(e);

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
		if (SwingUtilities.isLeftMouseButton(e)) {
			ZonePoint zp = getPoint(e);

			if (diamond != null) {
				diamond = createDiamond(originPoint, zp);

				if (diamond.getBounds().width == 0 || diamond.getBounds().height == 0) {
					diamond = null;
					renderer.repaint();
					return;
				}
				Area area = new ShapeDrawable(diamond, false).getArea();
				if (isEraser(e)) {
					renderer.getZone().removeTopology(area);
					MapTool.serverCommand().removeTopology(renderer.getZone().getId(), area);
				} else {
					renderer.getZone().addTopology(area);
					MapTool.serverCommand().addTopology(renderer.getZone().getId(), area);
				}
				renderer.repaint();
				// TODO: send this to the server

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
		setIsEraser(isEraser(e));

		ZonePoint zp = getPoint(e);
		if (diamond != null) {
			if (diamond != null) {
				diamond = createDiamond(originPoint, zp);
			}
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
			renderer.repaint();
		} else {
			super.resetTool();
		}
	}
}
