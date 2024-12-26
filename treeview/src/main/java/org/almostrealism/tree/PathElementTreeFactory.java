package org.almostrealism.tree;

import javafx.scene.control.TreeItem;
import org.almostrealism.graph.PathElement;
import io.almostrealism.relation.Producer;
import io.almostrealism.relation.Factory;

public class PathElementTreeFactory implements Factory<TreeItem<PathElement>> {
	private PathElement root;

	public PathElementTreeFactory(PathElement root) {
		this.root = root;
	}

	@Override
	public TreeItem<PathElement> construct() {
		return buildTree(root);
	}

	private TreeItem<PathElement> buildTree(PathElement<?, ?> p) {
		TreeItem<PathElement> t = new TreeItem<>(p);

		for (Producer pe : p.getDependencies()) {
			if (pe instanceof PathElement) {
				t.getChildren().add(buildTree((PathElement) pe));
			} else {
				t.getChildren().add(new TreeItem(pe));
			}
		}

		return t;
	}
}
