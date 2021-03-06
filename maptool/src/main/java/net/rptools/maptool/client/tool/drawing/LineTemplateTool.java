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
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;
import net.rptools.maptool.model.drawing.LineTemplate;
import net.rptools.maptool.model.drawing.Pen;

/**
 * Draw the effected area of a spell area type of line.
 * 
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author: azhrei_fje $
 */
public class LineTemplateTool extends RadiusTemplateTool implements PropertyChangeListener {

	/*---------------------------------------------------------------------------------------------
	 * Instance Variables
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Has the anchoring point been set? When false, the anchor point is being placed. When true, the area of effect is
	 * being drawn on the display.
	 */
	private boolean pathAnchorSet;

	/*---------------------------------------------------------------------------------------------
	 * Constructor 
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Add the icon to the toggle button.
	 */
	public LineTemplateTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"net/rptools/maptool/client/image/tool/temp-blue-line.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} // endtry
		AppState.addPropertyChangeListener(AppState.USE_DOUBLE_WIDE_PROP_NAME, this);
	}

	/*---------------------------------------------------------------------------------------------
	 * Overidden RadiusTemplateTool Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return "tool.linetemplate.tooltip";
	}

	/**
	 * @see net.rptools.maptool.client.ui.Tool#getInstructions()
	 */
	@Override
	public String getInstructions() {
		return "tool.linetemplate.instructions";
	}

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#createBaseTemplate()
	 */
	@Override
	protected AbstractTemplate createBaseTemplate() {
		return new LineTemplate();
	}

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#resetTool(net.rptools.maptool.model.ZonePoint)
	 */
	@Override
	protected void resetTool(ZonePoint aVertex) {
		super.resetTool(aVertex);	
		((LineTemplate) template).setDoubleWide(AppState.useDoubleWideLine());
	}

	/*---------------------------------------------------------------------------------------------
	 * Overridden AbstractDrawingTool Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see net.rptools.maptool.client.ui.zone.ZoneOverlay#paintOverlay(net.rptools.maptool.client.ui.zone.ZoneRenderer,
	 *      java.awt.Graphics2D)
	 */
	@Override
	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
		if (/*painting &&*/ renderer != null) {
			Pen pen = getPenForOverlay();
			AffineTransform old = g.getTransform();
			g.setTransform(getPaintTransform(renderer));
			ZonePoint vertex = template.getVertex();
			ZonePoint pathVertex = ((LineTemplate) template).getPathVertex();
			template.draw(g, pen);
			Paint paint = pen.getPaint() != null ? pen.getPaint().getPaint() : null;
			paintCursor(g, paint, pen.getThickness(), vertex);
			/*if (pathVertex != null) {
				paintCursor(g, paint, pen.getThickness(), pathVertex);
			}*/
			g.setTransform(old);
			if (pathVertex != null) {
				paintRadius(g, vertex);
			}
		}
	}

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#getRadiusAtMouse(java.awt.event.MouseEvent)
	 */
	@Override
	protected int getRadiusAtMouse(MouseEvent aE) {
		int radius = super.getRadiusAtMouse(aE);
		return Math.max(0, radius - 3);
	}

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		/*if (!painting)
			return;*/

		if (SwingUtilities.isLeftMouseButton(e)) {

			// Set the eraser
			setIsEraser(isEraser(e));

			// Need to set the anchor?
			controlOffset = null;
			if (!anchorSet) {
				LineTemplate lt = (LineTemplate) template;
				ZonePoint pathVertex = lt.getPathVertex();
				ZonePoint vertex = lt.getVertex();
				setCellAtMouse(e, vertex);
				anchorSet = true;
				return;
			} // endif
			if (!pathAnchorSet) {
				LineTemplate lt = (LineTemplate) template;
				ZonePoint pathVertex = lt.getPathVertex();
				ZonePoint vertex = lt.getVertex();
				// If the anchor vertex and path anchor vertex are the same, the line is invalid, so do not allow.
				if ((vertex != null) && !vertex.equals(pathVertex)) {
					pathAnchorSet = true;
				}
				return;
			} // endif
		} // endif

		// Let the radius code finish the template
		super.mousePressed(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// Set the eraser
		setIsEraser(isEraser(e));
		super.mouseReleased(e);
	}

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#handleMouseMovement(java.awt.event.MouseEvent)
	 */
	@Override
	protected void handleMouseMovement(MouseEvent e) {
		// Setting anchor point?
		LineTemplate lt = (LineTemplate) template;
		ZonePoint pathVertex = lt.getPathVertex();
		ZonePoint vertex = lt.getVertex();
		
		if (!anchorSet) {
			setCellAtMouse(e, vertex);
			controlOffset = null;
			anchorSet = true;
			return;
		}
		
		if (SwingUtil.isControlDown(e)) {
			handleControlOffset(e, vertex);
		} 
		else
		{		
			controlOffset = null;
		}
		
		template.setRadius(getRadiusAtMouse(e));

		// The path vertex remains null until it is set the first time.
		if (pathVertex == null) {
			pathVertex = new ZonePoint(vertex.x, vertex.y);
			lt.setPathVertex(pathVertex);
		} // endif
		if (pathVertex != null && setCellAtMouse(e, pathVertex))
			lt.clearPath();

		if (pathVertex != null) {
			// Determine which of the extra squares are used on diagonals
			double dx = pathVertex.x - vertex.x;
			double dy = pathVertex.y - vertex.y;
			if (dx != 0 && dy != 0) { // Ignore straight lines
				boolean mouseSlopeGreater = false;
				double m = Math.abs(dy / dx);
				double edx = e.getX() - vertex.x;
				double edy = e.getY() - vertex.y;
				if (edx != 0 && edy != 0) { // Handle straight lines differently
					double em = Math.abs(edy / edx);
					mouseSlopeGreater = em > m;
				} else if (edx == 0) {
					mouseSlopeGreater = true;
				} // endif
				if (mouseSlopeGreater != lt.isMouseSlopeGreater()) {
					lt.setMouseSlopeGreater(mouseSlopeGreater);
					renderer.repaint();
				} // endif
			} // endif

			// Quadrant change?
			ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
			int rdx = mouse.x - vertex.x;
			int rdy = mouse.y - vertex.y;
			AbstractTemplate.Quadrant quadrant = (rdx < 0) ? (rdy < 0 ? Quadrant.NORTH_WEST : Quadrant.SOUTH_WEST)
					: (rdy < 0 ? Quadrant.NORTH_EAST : Quadrant.SOUTH_EAST);
			if (quadrant != lt.getQuadrant()) {
				lt.setQuadrant(quadrant);
				renderer.repaint();
			} // endif
		} // endif
	}

	/*---------------------------------------------------------------------------------------------
	 * PropertyChangeListener Interface Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent aEvt) {
		((LineTemplate) template).setDoubleWide(((Boolean) aEvt.getNewValue()).booleanValue());
	}
}
