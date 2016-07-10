package com.almostrealism.google.ui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jgraph.JGraph;
import org.jgraph.example.GraphEd;
import org.jgraph.example.JGraphIconView;
import org.jgraph.example.IconExample.CustomCell;
import org.jgraph.example.IconExample.InvisiblePortRenderer;
import org.jgraph.example.IconExample.InvisiblePortView;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.almostrealism.google.YouTubeVideo;
import com.google.gdata.util.ServiceException;

public class RelatedVideosPanel extends JPanel {
	private VideoListModel videoListModel;
	private JList resultsList;

	private JGraph graphDisplay;
	private JGraphModelAdapter graphModel;
	private DefaultListenableGraph graph;

	private int maxRelatedPerVideo = 5;

	private Hashtable vel;

	public class CustomCell extends DefaultGraphCell {
		private ImageIcon icon;

		private String description;

		public CustomCell(ImageIcon icon, String description) {
			this.icon = icon;
			this.description = description;
		}

		public String getDescription() { return description; }
		public ImageIcon getIcon() { return icon; }

		/**
		 * Sets the description on a cell. This is called from the multi-lined
		 * editor.
		 */
		public void setUserObject(Object obj) {
			if (obj != null && obj instanceof String) {
				this.description = obj.toString();
			}
		}

		/**
		 * Return the description of the cell so that it will be the initial
		 * value of the in-graph editor.
		 */
		public String toString() {
			return description;
		}
	}

	public RelatedVideosPanel() {
		super(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();

		this.videoListModel = new VideoListModel();
		this.resultsList = new JList(this.videoListModel);
		this.videoListModel.setVideos(new ArrayList());
		this.resultsList.setCellRenderer(this.videoListModel);

		this.graph = new ListenableDirectedGraph(DefaultEdge.class);
		this.graphModel = new JGraphModelAdapter(this.graph);
		this.graphDisplay = new GraphEd.MyGraph(this.graphModel);
//		this.graphDisplay.setGraphLayoutCache(new GraphLayoutCache(this.graphModel,
//		new DefaultCellViewFactory(), true));
		this.graphDisplay.getGraphLayoutCache().setAutoSizeOnValueChange(true);
		this.graphDisplay.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {
			public CellView createView(GraphModel model, Object c) {
				CellView view = null;

//				if (c instanceof DefaultGraphCell) {
//				Object u = ((DefaultGraphCell)c).getUserObject();

//				if (u instanceof YouTubeVideo == false) {
//				if (u != null) System.out.println(u.getClass());
//				return super.createView(model, c);
//				}

//				try {
//				return new JGraphIconView(new CustomCell(((YouTubeVideo)u).getIcon(), ((YouTubeVideo)u).toString()));
//				} catch (MalformedURLException e) {
//				return super.createView(model, c);
//				}
//				} else {
				view = super.createView(model, c);
//				}

				return view;
			}
		});

		tabs.addTab("List", new JScrollPane(this.resultsList));
		tabs.addTab("Graph", new JScrollPane(this.graphDisplay));

		super.add(tabs, BorderLayout.CENTER);
	}

	public void setVideo(final YouTubeVideo video) throws MalformedURLException, IOException, ServiceException {
		List l = video.getRelatedVideos();
		this.videoListModel.setVideos(l);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					doRelated(video, 1);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					e.printStackTrace();
				}
				
				vel = new Hashtable();
				Iterator itr = graph.vertexSet().iterator();
				while (itr.hasNext()) vel.put(itr.next(), new double[] {0.0, 0.0});
				
				// TODO Swing Utilities invoke later.
			}
		});
	}

	private void doRelated(YouTubeVideo video, int i) throws MalformedURLException, IOException, ServiceException {
		doRelated(video, -1, -1, i);
	}

	private void doRelated(YouTubeVideo video, int x, int y, int i) throws MalformedURLException, IOException, ServiceException {
		List l = video.getRelatedVideos();
		
		if (this.graph.vertexSet().isEmpty()) {
			x = 300;
			y = 300;
		}
		
		if (!this.graph.containsVertex(video)) this.graph.addVertex(video);

		Rectangle2D r = GraphConstants.getBounds(this.graphModel.getVertexCell(video).getAttributes());
		
		if (x != -1) positionVertexAt(video, (int) x, (int) y);
		
		x = (int) r.getX();
		y = (int) r.getY();

		Iterator itr = l.iterator();

		int it = 0;

		while (itr.hasNext()) {
			YouTubeVideo v = (YouTubeVideo) itr.next();

			if (!this.graph.containsVertex(v))
				this.graph.addVertex(v);
			if (!this.graph.containsEdge(video, v))
				this.graph.addEdge(video, v);

			positionVertexAt(v, (int) (x + 700 * (Math.random() - 0.5)),
								(int) (y + 700 * (Math.random() - 0.5)));

			int nx = (int) (x + 700 * (Math.random() - 0.5));
			int ny = (int) (y + 700 * (Math.random() - 0.5));

			if (i > 0) this.doRelated(v, nx, ny, i - 1);
			if (it++ > maxRelatedPerVideo) return;
		}
	}

	private void positionVertexAt(Object vertex, int x, int y) {
		DefaultGraphCell cell = this.graphModel.getVertexCell(vertex);
		Map attr = cell.getAttributes();
		Rectangle2D b = GraphConstants.getBounds( attr );

		GraphConstants.setBounds(attr, new Rectangle2D.Double(x, y, b.getWidth(), b.getHeight()));

		Map cellAttr = new HashMap();
		cellAttr.put(cell, attr);
		this.graphModel.edit(cellAttr, this.graphModel.getConnectionSet(), null, null);
	}

	public double[] getPosition(Object vertex) {
		DefaultGraphCell cell = this.graphModel.getVertexCell(vertex);
		Map attr = cell.getAttributes();
		Rectangle2D b = GraphConstants.getBounds(attr);
		return new double[] { b.getCenterX(), b.getCenterY() };
	}
	
	public void iterateSpaceDistributor(int index) {
		double mass = 100.0;
		
		Iterator itr = this.vel.entrySet().iterator();
		
		while (itr.hasNext()) {
			Map.Entry ent = (Map.Entry) itr.next();
			Object vertex = ent.getKey();
			double vel[] = (double[]) ent.getValue();
			double pos[] = getPosition(vertex);
			positionVertexAt(vertex, (int) (pos[0] + vel[0]), (int) (pos[1] + vel[1]));
		}
		
		itr = this.vel.entrySet().iterator();
		
		while (itr.hasNext()) {
			Map.Entry ent = (Map.Entry) itr.next();
			Object vertex = ent.getKey();
			double vel[] = (double[]) ent.getValue();
			double pos[] = getPosition(vertex);
			
			Set edges = new HashSet();
			edges.addAll(this.graph.edgesOf(vertex));
			edges.addAll(this.graph.incomingEdgesOf(vertex));
			
			Iterator edgeItr = edges.iterator();
			
			while (edgeItr.hasNext()) {
				Edge e = (Edge) edgeItr.next();
				Object target = e.getTarget();
				if (target == vertex) target = e.getSource();
				
				double tpos[] = getPosition(target);
				double dx = pos[0] - tpos[0];
				double dy = pos[1] - tpos[1];
				double r = dx * dx + dy * dy;
				r = Math.sqrt(r);
				double d = r / mass;
				
				vel[0] += dx * d / r;
				vel[1] += dy * d / r;
			}
		}
		
		if (index > 0) this.iterateSpaceDistributor(index - 1);
	}
}
