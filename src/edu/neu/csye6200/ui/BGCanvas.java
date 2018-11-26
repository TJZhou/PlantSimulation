package edu.neu.csye6200.ui;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import edu.neu.csye6200.bg.*;

/**
 * A sample canvas that draws a rainbow of lines
 * 
 * @author MMUNSON
 */
public class BGCanvas extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(BGCanvas.class.getName());
	private long counter = 0L;
	private double width = 1000;
	private double height = 750;
	
	/**
	 * CellAutCanvas constructor
	 */
	public BGCanvas() {
		// this.setBackground(Color.GRAY);
	}

	/**
	 * The UI thread calls this method when the screen changes, or in response to a
	 * user initiated call to repaint();
	 */
	public void paint(Graphics g) {
		super.paint(g);
		this.setBackground(Color.GRAY);
		drawBG(g); // Our Added-on drawing
	}

	/**
	 * Draw the CA graphics panel
	 * 
	 * @param g
	 */
	public void drawBG(Graphics g) {
		log.info("Drawing BG " + counter++);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (BGApp.isSimComplete == false) {
			new Thread(new Runnable() {
				public void run() {
					drawPlant(g2d);
				}
			}).start();
		}

		// when the jpanel is repainted and do not need thread to control
		// in this way, we can remain the picture we paint
		if (BGApp.isSimComplete == true) {
			for (int i = 0; i < BGApp.bgs.getBgSet().get(0).getBgs().size(); i++) {
				BGStem st = BGApp.bgs.getBgSet().get(0).getBgs().get(i);
				paintLine(g2d, BGApp.color, st);
			}
		}

/*		//if the size of jrame is resized
		BGApp.frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if(BGApp.isSimComplete == true) {
					if((width/getSize().getWidth()) >= (height/getSize().getHeight())) {
						BGApp.midLengthGrow = BGApp.midLengthGrow*(width/getSize().getWidth());
						BGApp.sideLengthGrow = BGApp.sideLengthGrow*(width/getSize().getWidth());
					}
					
					else {
						BGApp.midLengthGrow = BGApp.midLengthGrow*(height/getSize().getHeight());
						BGApp.sideLengthGrow = BGApp.sideLengthGrow*(height/getSize().getHeight());
					}
					BGApp.bgs.genrationSet(BGApp.rule); // generate stems according to rules
					drawPlant(g2d);
				}
			}
		});*/
	}

	/**
	 *  extra data from bgSet and draw stem based on the data
	 *  if the isStop param is true the stop the thread
	 *  when the draw process is done,
	 *  set isStop false, set setResizable true, set isSimComplete true
	 * @param g2d
	 */
	private void drawPlant(Graphics2D g2d) {
		try {
			// the first time, the canvas is initialized without stem data
			if (BGApp.bgs.getBgSet().isEmpty() == false) {
				for (int i = 0; i < BGApp.bgs.getBgSet().get(0).getBgs().size(); i++) {
					BGStem st = BGApp.bgs.getBgSet().get(0).getBgs().get(i); // get the current BGStem;
					paintLine(g2d, BGApp.color, st); // paint on the canvas
					// show growth process
					Thread.sleep(BGApp.growthRate);
					// if the flag isStop is true; then stop the thread
					synchronized(this) {
						while (BGApp.isStop == true) {
							BGApp.isSimComplete = true;
							wait();
						}
					}
				}
				BGApp.isStop = false;
				BGApp.frame.setResizable(true);
				BGApp.isSimComplete = true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Stop the thread.
	synchronized void mystop() {
		BGApp.isStop = true;
	}
	
	//continue the thread
	synchronized void myresume() {
		BGApp.isStop = false;
		notifyAll();
	}

	/**
	 * A convenience routine to set the color and draw a line
	 * 
	 * @param g2d   the 2D Graphics context
	 * @param st    the instance of BGstem which we need to draw
	 * @param color the line color
	 */
	private void paintLine(Graphics2D g2d, Color color, BGStem st) {
		Dimension size = getSize();
		g2d.setColor(BGApp.color);
		Line2D line;
		line = new Line2D.Double(st.getLocationX() + size.getWidth() / 2, -st.getLocationY() + size.getHeight(),
				(st.getLocationX() + st.getLength() * Math.cos(st.getRadians()) + size.getWidth() / 2),
				-(st.getLocationY() + st.getLength() * Math.sin(st.getRadians())) + size.getHeight());
		g2d.draw(line);
	}

	@Override
	public void update(Observable o, Object arg) {
	}
}
