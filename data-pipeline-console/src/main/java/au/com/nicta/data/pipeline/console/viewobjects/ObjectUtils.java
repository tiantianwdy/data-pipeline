package au.com.nicta.data.pipeline.console.viewobjects;

import au.com.nicta.data.pipeline.core.executor.PipeExecutionContext;
import au.com.nicta.data.pipeline.core.models.PipelineContext;
import au.com.nicta.data.pipeline.view.models.GraphVO;
import au.com.nicta.data.pipeline.view.models.Link;
import au.com.nicta.data.pipeline.view.models.PipeNode;
import au.com.nicta.data.pipeline.view.models.TreeVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by tiantian on 24/09/15.
 */
public class ObjectUtils {

    public static String PIPELINE_SERVER = PipeExecutionContext.DEFAULT_PIPELINE_SERVER();

    public static ObjectMapper mapper = new ObjectMapper();

    public static String objectToJson(Object obj) throws JsonProcessingException {

        String res = mapper.writeValueAsString(obj);
        return res;
    }

    public static <T> T  getObject(String jsonStr, Class<T> cls) throws IOException {
        T obj = mapper.readValue(jsonStr, cls);
        return obj;
    }

    public static GraphVO getMockExecutionDAG(String execTag){
        GraphVO graph = new GraphVO();
        graph.addNodes(new PipeNode("csvMapper", "0.0.1"))
                .addNodes(new PipeNode("textMapper", "0.0.1"))
                .addNodes(new PipeNode("jsonMapper", "0.0.1"))
                .addNodes(new PipeNode("dataJoiner", "0.0.1"))
                .addNodes(new PipeNode("featureExtractor1", "0.0.1"))
                .addNodes(new PipeNode("featureExtractor2", "0.0.1"))
                .addNodes(new PipeNode("analysis1", "0.0.1"))
                .addNodes(new PipeNode("analysis2", "0.0.1"));

        graph.addLink(new Link(0, 3))
                .addLink(new Link(1, 3))
                .addLink(new Link(2, 3))
                .addLink(new Link(3, 4))
                .addLink(new Link(3, 5))
                .addLink(new Link(4, 6))
                .addLink(new Link(5, 7))
        ;
        return graph;
    }

    public static TreeVO getMockPipelineTreeVO() {
        TreeVO root = new TreeVO("Pipeline List");
        TreeVO pipeline1 = new TreeVO("pipeline1");
        TreeVO pipeline2 = new TreeVO("pipeline2");
        TreeVO execution_1 = new TreeVO("execution_1");
        TreeVO execution_2 = new TreeVO("execution_2");
        TreeVO execution_3 = new TreeVO("execution_3");
        TreeVO execution_4 = new TreeVO("execution_4");
        TreeVO execution_5 = new TreeVO("execution_5");
        TreeVO execution_6 = new TreeVO("execution_6");
        pipeline1.addChild(execution_1).addChild(execution_2).addChild(execution_3);
        pipeline2.addChild(execution_4).addChild(execution_5).addChild(execution_6);
        root.addChild(pipeline1).addChild(pipeline2);
        return root;
    }

    public static TreeVO getMockPipeHistoryTreeVO(String pipeName) {
        TreeVO root = new TreeVO(pipeName);
        TreeVO pipeline1 = new TreeVO("0.0.1");
        TreeVO pipeline2 = new TreeVO("0.0.2");
        TreeVO pipeline3 = new TreeVO("0.0.3");

        //version 1
        TreeVO type = new TreeVO("type");
        TreeVO dep = new TreeVO("dependency");
        TreeVO instances = new TreeVO("instances");

        TreeVO typeV1 = new TreeVO("SparkPipe");
        TreeVO depV1 = new TreeVO("./dataJoiner/0.0.1/data-joiner-0.0.1.jar");
        TreeVO execution1 = new TreeVO("execution1");
        TreeVO execution2 = new TreeVO("execution2");
        TreeVO execution_3 = new TreeVO("execution_3");

        type.addChild(typeV1);
        dep.addChild(depV1);
        instances.addChild(execution1).addChild(execution2).addChild(execution_3);

        TreeVO in = new TreeVO("in:[data/csvMapper/0.0.1/execution_1]");
        TreeVO out = new TreeVO("in:[data/dataJoiner/0.0.1/execution_1]");
        TreeVO state = new TreeVO("state:Success");

        execution1.addChild(in).addChild(out).addChild(state);
        execution2.addChild(in).addChild(out).addChild(state);
        execution_3.addChild(in).addChild(out).addChild(state);

        //version 2
        TreeVO type2 = new TreeVO("type");
        TreeVO dep2 = new TreeVO("dependency");
        TreeVO instances2 = new TreeVO("instances");

        TreeVO typeV2 = new TreeVO("MRPipe");
        TreeVO depV2 = new TreeVO("./dataJoiner/0.0.2/data-joiner-0.0.2.jar");
        TreeVO execution_4 = new TreeVO("execution_4");
        TreeVO execution_5 = new TreeVO("execution_5");
        TreeVO execution_6 = new TreeVO("execution_6");

        type2.addChild(typeV2);
        dep2.addChild(depV2);
        instances2.addChild(execution_4).addChild(execution_5).addChild(execution_6);

        execution_4.addChild(in).addChild(out).addChild(state);
        execution_5.addChild(in).addChild(out).addChild(state);
        execution_6.addChild(in).addChild(out).addChild(state);

        //version 3
        TreeVO type3 = new TreeVO("type");
        TreeVO dep3 = new TreeVO("dependency");
        TreeVO instances3 = new TreeVO("instances");

        TreeVO typeV3 = new TreeVO("SparkPipe");
        TreeVO depV3 = new TreeVO("./dataJoiner/0.0.3/data-joiner-0.0.3.jar");
        TreeVO execution_7 = new TreeVO("execution_4");
        TreeVO execution_8 = new TreeVO("execution_5");
        TreeVO execution_9 = new TreeVO("execution_6");

        type3.addChild(typeV3);
        dep3.addChild(depV3);
        instances3.addChild(execution_7).addChild(execution_8).addChild(execution_9);

        execution_7.addChild(in).addChild(out).addChild(state);
        execution_8.addChild(in).addChild(out).addChild(state);
        execution_9.addChild(in).addChild(out).addChild(state);


        pipeline1.addChild(type).addChild(dep).addChild(instances);
        pipeline2.addChild(type2).addChild(dep2).addChild(instances2);
        pipeline3.addChild(type3).addChild(dep3).addChild(instances3);
        root.addChild(pipeline1).addChild(pipeline2).addChild(pipeline3);
        return root;
    }


    public static TreeVO getMockPipelineExecutionTreeVO(String pipelineName) {
        TreeVO root = new TreeVO(pipelineName);
        TreeVO execution_1 = new TreeVO("execution_1");
        TreeVO execution_2 = new TreeVO("execution_2");
        TreeVO execution_3 = new TreeVO("execution_3");
        TreeVO csvMapper = new TreeVO("csvMapper#0.0.1");
        TreeVO jsonMapper = new TreeVO("jsonMapper#0.0.1");
        TreeVO textMapper = new TreeVO("textMapper#0.0.1");
        TreeVO xmlMapper = new TreeVO("xmlMapper#0.0.1");
        TreeVO dataJoiner = new TreeVO("dataJoiner#0.0.1");
        TreeVO featureExtractor1 = new TreeVO("featureExtractor1#0.0.1");
        TreeVO featureExtractor2 = new TreeVO("featureExtractor2#0.0.1");
        TreeVO analysis1 = new TreeVO("analysis1#0.0.1");
        TreeVO analysis2 = new TreeVO("analysis2#0.0.1");

        TreeVO featureExtractor2_2 = new TreeVO("featureExtractor2#0.0.2");
        execution_1.addChild(csvMapper)
                .addChild(jsonMapper)
                .addChild(textMapper)
                .addChild(dataJoiner)
                .addChild(featureExtractor1)
                .addChild(featureExtractor2)
                .addChild(analysis1)
                .addChild(analysis2);

        execution_2.addChild(csvMapper)
                .addChild(jsonMapper)
                .addChild(textMapper)
                .addChild(xmlMapper)
                .addChild(dataJoiner)
                .addChild(featureExtractor1)
                .addChild(featureExtractor2)
                .addChild(analysis1)
                .addChild(analysis2);

        execution_3.addChild(csvMapper)
                .addChild(jsonMapper)
                .addChild(textMapper)
                .addChild(xmlMapper)
                .addChild(dataJoiner)
                .addChild(featureExtractor1)
                .addChild(featureExtractor2_2)
                .addChild(analysis1)
                .addChild(analysis2);

        root.addChild(execution_1).addChild(execution_2).addChild(execution_3);
        return root;
    }


}
