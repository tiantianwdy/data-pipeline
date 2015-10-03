package au.com.nicta.data.pipeline.console.controller;

import au.com.nicta.data.pipeline.console.viewobjects.ObjectUtils;
import au.com.nicta.data.pipeline.core.executor.AkkaCallbackEntry;
import au.com.nicta.data.pipeline.core.messages.QueryExecutionHistory;
import au.com.nicta.data.pipeline.core.messages.QueryExecutionHistoryResp;
import au.com.nicta.data.pipeline.core.view.ViewAdapters;
import au.com.nicta.data.pipeline.view.models.GraphVO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tiantian on 2/10/15.
 */
public class ExecutionDAGServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //return pipeline history in JSON
        String executionTag = req.getParameter("executionTag");
//        GraphVO mockGraph = ObjectUtils.getMockExecutionDAG(executionTag);
        QueryExecutionHistory msg = new QueryExecutionHistory(executionTag);
        QueryExecutionHistoryResp results = (QueryExecutionHistoryResp) AkkaCallbackEntry.sendCallBack(ObjectUtils.PIPELINE_SERVER, msg);
        String[][] traces = ViewAdapters.executionHistoryToArray(executionTag, results.results());
        String json = ObjectUtils.objectToJson(traces);
        resp.setContentType("application/json");
        resp.getWriter().write(json);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}