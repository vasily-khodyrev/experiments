package org.edu.mbt.test;

import org.edu.mbt.test.model.CallFlowModel;
import org.edu.mbt.test.runner.GraphStreamObserver;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphwalker.core.event.Observer;
import org.graphwalker.java.test.Executor;
import org.graphwalker.java.test.Result;
import org.graphwalker.java.test.TestExecutor;

public class TestMbt
{
    public static void main( String[] args )
    {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new SingleGraph("GraphWalker MeetUp");
        Viewer v = graph.display(true);

        Executor executor = new TestExecutor(CallFlowModel.class);
        Observer observer = new GraphStreamObserver(graph);
        executor.getMachine().addObserver(observer);
        Result result = executor.execute();
        System.out.println("Done: [" + result.getCompletedCount() + "," + result.getFailedCount() + "]");
        //graph.clear();
        //v.close();
    }
}
