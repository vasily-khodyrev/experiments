package org.edu.mbt.test.model;

import org.edu.mbt.test.callflow;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.java.annotation.GraphWalker;

/**
 * Created by
 * User: vkhodyre
 * Date: 8/12/2015
 */
@GraphWalker(value = "random(edge_coverage(100))")
public class CallFlowModel extends ExecutionContext implements callflow {
    private Integer counter = 0;

    @Override
    public void v_CallInQueue() {
        processNode("CallInQueue:" + counter);
    }

    @Override
    public void v_OXE() {
        processNode("v_OXE:" + counter);
    }

    private static void processNode(String x) {
        System.out.println(x);
        /*try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            //
        }*/
    }

    @Override
    public void e_initialized() {
        processNode("e_initialized:" + counter);
    }

    @Override
    public void e_readyfornewcall() {
        processNode("e_readyfornewcall:" + counter);
    }

    @Override
    public void e_allbusy() {
        processNode("e_allbusy:" + counter);
    }

    @Override
    public void v_End() {
        processNode("v_End:" + counter);
    }

    @Override
    public void e_callendother() {
        processNode("e_callendother:" + counter);
    }

    @Override
    public void v_InitialState() {
        processNode("v_InitialState:" + counter);
    }

    @Override
    public void e_callendoxe() {
        processNode("e_callendoxe:" + counter);
    }

    @Override
    public void e_overflow() {
        processNode("e_overflow:" + counter);
    }

    @Override
    public void e_callendbyoperator() {
        processNode("e_callendbyoperator:" + counter);
    }

    @Override
    public void e_init() {
        processNode("CallInQueue:" + counter);
    }

    @Override
    public void e_routeoperator() {
        processNode("e_routeoperator:" + counter);
    }

    @Override
    public void v_Other() {
        processNode("v_Other:" + counter);
    }

    @Override
    public void v_Operator() {
        processNode("v_Operator:" + counter);
    }

    @Override
    public void v_Init() {
        processNode("v_Init:" + counter);
    }

    @Override
    public void e_call() {
        counter++;
        processNode("e_call:" + counter);
    }
}
