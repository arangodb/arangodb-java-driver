/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class DocumentTest extends BaseTest {

    private static final String COLLECTION_NAME = "DocumentTest_collection";
    private final ArangoCollection collection;

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(COLLECTION_NAME);
    }

    public DocumentTest(final ArangoDB arangoDB) {
        super(arangoDB);
        collection = db.collection(COLLECTION_NAME);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void insertAsJson() {
        //@formatter:off
        final String json =
                "{"
                        + "\"article\": {"
                        + "\"artist\": \"PREGARDIEN/RHEINISCHE KANTOREI/DAS\","
                        + "\"releaseDate\": \"1970-01-01\","
                        + "\"composer\": \"BACH\","
                        + "\"format\": \"CD\","
                        + "\"vat\": \"H\","
                        + "\"carriers\": 1,"
                        + "\"label\": \"CAPRICCIO\","
                        + "\"title\": \"BACH ST MATTHEW PASSION BWV244\","
                        + "\"barcode\": ["
                        + "\"4006408600466\""
                        + "],"
                        + "\"conductor\": \"MAX, H.\""
                        + "},"
                        + "\"stock\": {"
                        + "\"status\": \"RMV\","
                        + "\"lastUpdate\": \"2016-11-01 00:00\""
                        + "}"
                        + "}";
        //@formatter:on
        final DocumentCreateEntity<String> createResult = collection.insertDocument(json);
        final BaseDocument doc = collection.getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(doc, is(notNullValue()));
        final Object article = doc.getAttribute("article");
        assertThat(article, is(notNullValue()));
        final Object artist = ((Map<String, Object>) article).get("artist");
        assertThat(artist, is(notNullValue()));
        assertThat(artist.toString(), is("PREGARDIEN/RHEINISCHE KANTOREI/DAS"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void insertAsBaseDocument() {
        final BaseDocument document = new BaseDocument();
        {
            final BaseDocument article = new BaseDocument();
            document.addAttribute("article", article);
            article.addAttribute("artist", "PREGARDIEN/RHEINISCHE KANTOREI/DAS");
            article.addAttribute("releaseDate", "1970-01-01");
            article.addAttribute("composer", "BACH");
            article.addAttribute("format", "CD");
            article.addAttribute("vat", "H");
            article.addAttribute("carriers", 1);
            article.addAttribute("label", "CAPRICCIO");
            article.addAttribute("title", "BACH ST MATTHEW PASSION BWV244");
            article.addAttribute("barcode", new String[]{"4006408600466"});
            article.addAttribute("conductor", "MAX, H.");
            final BaseDocument stock = new BaseDocument();
            document.addAttribute("stock", stock);
            stock.addAttribute("status", "RMV");
            stock.addAttribute("lastUpdate", "2016-11-01 00:00");
        }
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(document);
        final BaseDocument doc = collection.getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(doc, is(notNullValue()));
        final Object article = doc.getAttribute("article");
        assertThat(article, is(notNullValue()));
        final Object artist = ((Map<String, Object>) article).get("artist");
        assertThat(artist, is(notNullValue()));
        assertThat(artist.toString(), is("PREGARDIEN/RHEINISCHE KANTOREI/DAS"));
    }

    @Test
    public void documentKeyWithSpecialChars() {
        final String key = "_-:.@()+,=;$!*'%" + UUID.randomUUID().toString();
        final BaseDocument document = new BaseDocument(key);
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(document);
        final BaseDocument doc = collection.getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getKey(), is(key));
    }

}
