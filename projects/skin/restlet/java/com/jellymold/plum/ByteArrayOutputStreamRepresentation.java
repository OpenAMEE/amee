package com.jellymold.plum;

import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// TODO: move this

public class ByteArrayOutputStreamRepresentation extends OutputRepresentation {

    private ByteArrayOutputStream baos;

    public ByteArrayOutputStreamRepresentation(ByteArrayOutputStream baos, MediaType mediaType) {
        super(mediaType);
        this.baos = baos;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        baos.writeTo(outputStream);
    }
}
