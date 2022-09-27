package com.arangodb;

import com.arangodb.entity.BaseDocument;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseDocumentTest {
    @Test
    public void serializeBaseDocument() throws IOException, ClassNotFoundException {
        BaseDocument bd = new BaseDocument("poaids");
        bd.setId("apdso/02193");
        bd.setRevision("poip");
        bd.addAttribute("aaa", "bbb");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(bd);

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        BaseDocument bd2 = (BaseDocument) objectInputStream.readObject();
        objectInputStream.close();

        assertThat(bd).isEqualTo(bd2);
    }
}
