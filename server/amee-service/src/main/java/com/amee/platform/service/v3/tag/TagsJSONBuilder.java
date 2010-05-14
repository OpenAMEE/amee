package com.amee.platform.service.v3.tag;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.tag.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TagsJSONBuilder extends TagsBuilder<JSONObject> {

    public JSONObject handle(RequestWrapper requestWrapper) {
        TagsJSONRenderer renderer =
                new TagsJSONRenderer();
        super.handle(requestWrapper, renderer);
        return renderer.getJSONObject();
    }

    public String getMediaType() {
        return "application/json";
    }

    public class TagsJSONRenderer implements TagsBuilder.TagsRenderer {

        private JSONObject rootObj;
        private JSONArray tagsArr;

        public TagsJSONRenderer() {
            super();
        }

        public void start() {
            rootObj = new JSONObject();
            tagsArr = new JSONArray();
            put(rootObj, "tags", tagsArr);
        }

        public void newTag(Tag tag) {
            JSONObject tagObj = new JSONObject();
            tagsArr.put(tagObj);
            put(tagObj, "tag", tag.getTag());
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void notAuthenticated() {
            put(rootObj, "status", "NOT_AUTHENTICATED");
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getJSONObject() {
            return rootObj;
        }
    }
}