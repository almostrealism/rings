package org.almostrealism.tree.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.almostrealism.graph.PathElement;
import org.almostrealism.tree.PathElementTreeFactory;
import io.almostrealism.relation.Evaluable;

import java.util.ArrayList;
import java.util.List;

public class TreeTest extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Tree View Sample");

		PathElement p = buildTestTree();
		TreeView<PathElement> tree = new TreeView<>(new PathElementTreeFactory(p).construct());
		StackPane root = new StackPane();
		root.getChildren().add(tree);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.show();
	}

	protected PathElement buildTestTree() {
		List<Evaluable> c = new ArrayList<>();
		c.add(new TestPathElement());
		c.add(new TestPathElement());
		c.add(new TestPathElement());
		c.add(new TestPathElement());

		return new TestPathElement(c);
	}
}
