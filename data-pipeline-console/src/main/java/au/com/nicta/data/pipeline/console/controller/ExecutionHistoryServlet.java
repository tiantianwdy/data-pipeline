package au.com.nicta.data.pipeline.console.controller;

import au.com.nicta.data.pipeline.view.models.GraphVO;
import au.com.nicta.data.pipeline.console.viewobjects.ObjectUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tiantian on 24/09/15.
 */
public class ExecutionHistoryServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //return pipeline history in JSON
        String executionTag = req.getParameter("executionTag");
        GraphVO graph = ObjectUtils.getMockExecutionDAG(executionTag);
        String json = ObjectUtils.objectToJson(graph);
        resp.setContentType("application/json");
        resp.getWriter().write(json);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

}