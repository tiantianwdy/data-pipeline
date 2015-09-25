package au.com.nicta.data.pipeline.console.controller;

import au.com.nicta.data.pipeline.console.viewobjects.ObjectUtils;
import au.com.nicta.data.pipeline.core.executor.AkkaCallbackEntry;
import au.com.nicta.data.pipeline.core.messages.QueryPipelineHistory;
import au.com.nicta.data.pipeline.core.messages.QueryPipelineHistoryResp;
import au.com.nicta.data.pipeline.core.messages.QueryPipelineList;
import au.com.nicta.data.pipeline.core.messages.QueryPipelineListResp;
import au.com.nicta.data.pipeline.core.view.ViewAdapters;
import au.com.nicta.data.pipeline.view.models.TreeVO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tiantian on 24/09/15.
 */
public class PipelineHistoryServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //return pipeline history in JSON
//        TreeVO tree = ObjectUtils.getMockPipelineTreeVO();
        QueryPipelineList msg = new QueryPipelineList();
        QueryPipelineListResp results = (QueryPipelineListResp) AkkaCallbackEntry.sendCallBack(ObjectUtils.PIPELINE_SERVER, msg);
        TreeVO tree = ViewAdapters.pipelineListToTreeVO(results.results());

        String json = ObjectUtils.objectToJson(tree);
        resp.setContentType("application/json");
        resp.getWriter().write(json);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
