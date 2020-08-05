package uk.ac.bris.cs.databases.web;

import uk.ac.bris.cs.databases.api.Result;

/**
 *
 * @author lily
 */
public class NewForumHandler extends SimpleHandler {

    @Override
    RenderPair simpleRender(String p) throws RenderException {
        return new RenderPair("NewForumView.ftl", Result.success());
    }

    @Override boolean needsParameter() { return false; }
    
}
