package com.amee.platform.service.v3.tag;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.tag.Tag;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TagsDOMBuilder extends TagsBuilder<Document> {

    public Document handle(RequestWrapper requestWrapper) {
        TagsDOMRenderer renderer =
                new TagsDOMRenderer();
        super.handle(requestWrapper, renderer);
        return renderer.getDocument();
    }

    public String getMediaType() {
        return "application/json";
    }

    public class TagsDOMRenderer implements TagsRenderer {

        private Element rootElem;
        private Element tagsElem;

        public TagsDOMRenderer() {
            super();
        }

        public void start() {
            rootElem = new Element("Representation");
            tagsElem = new Element("Tags");
            rootElem.addContent(tagsElem);
        }

        public void newTag(Tag tag) {
            Element tagElem = new Element("Tag");
            tagElem.addContent(new Element("Tag").setText(tag.getTag()));
            tagElem.addContent(new Element("Count").setText("" + tag.getCount()));
            tagsElem.addContent(tagElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void notAuthenticated() {
            rootElem.addContent(new Element("Status").setText("NOT_AUTHENTICATED"));
        }

        public Document getDocument() {
            return new Document(rootElem);
        }
    }
}