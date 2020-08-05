package uk.ac.bris.cs.databases.web;

import java.util.List;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.ForumSummaryView;

/**
* The simplified forums handler (no topic info at all).
 * path: /forums0
 * 
 * @author lily
 */
public class ForumsHandler extends SimpleHandler {

    @Override
    RenderPair simpleRender(String p) throws RenderException {
        APIProvider api = ApplicationContext.getInstance().getApi();
        Result<List<ForumSummaryView>> r = api.getForums();
        //把r拆解后放到result里面
        return new RenderPair("ForumsView.ftl", ListWrapper.wrap(r));
    }

    @Override boolean needsParameter() { return false; }
}
