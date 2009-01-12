package com.jellymold.utils;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;

import java.util.List;

public class MediaTypeUtils {

    public static boolean isStandardWebBrowser(Request request) {
        // TODO: does this cover all web browsers?
        return MediaTypeUtils.doesClientAccept(MediaType.TEXT_HTML, request) ||
                MediaTypeUtils.doesClientAccept(MediaType.ALL, request);
    }

    // this ignores Preference 'quality'
    public static boolean doesClientAccept(MediaType mediaType, Request request) {
        List<Preference<MediaType>> mediaTypePrefs =
                request.getClientInfo().getAcceptedMediaTypes();
        for (Preference<MediaType> mediaTypePref : mediaTypePrefs) {
            if (mediaTypePref.getMetadata().equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    public static void forceMediaType(MediaType mediaType, Request request) {
        request.getClientInfo().getAcceptedMediaTypes().clear();
        request.getClientInfo().getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
    }
}
