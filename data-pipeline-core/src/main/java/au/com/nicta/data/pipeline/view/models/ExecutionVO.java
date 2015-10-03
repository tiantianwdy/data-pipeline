package au.com.nicta.data.pipeline.view.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tiantian on 2/10/15.
 */
public class ExecutionVO {

    private final static Map<String, Integer> groupMapping = new HashMap<String, Integer>();

    static {
        groupMapping.put("success", 1);
        groupMapping.put("running", 3);
        groupMapping.put("waiting", 4);
        groupMapping.put("failed", 5);

    }

    public  static  Integer getGroup(String status) {
        String key = status.toLowerCase();
        if(groupMapping.containsKey(key))
        return groupMapping.get(key);
        else return 0;
    }
}
