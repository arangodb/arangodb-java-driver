/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.util.MapBuilder;

/**
 * @author Mark Vollmary
 */
@SuppressWarnings("ALL")
@RunWith(Parameterized.class)
@Ignore
public class UserAuthTest {

    private static final String DB_NAME = "AuthUnitTestDB";
    private static final String DB_NAME_NEW = DB_NAME + "new";
    private static final String COLLECTION_NAME = "AuthUnitTestCollection";
    private static final String COLLECTION_NAME_NEW = COLLECTION_NAME + "new";
    private static final String USER_NAME = "AuthUnitTestUser";
    private static final String USER_NAME_NEW = USER_NAME + "new";

    static class UserAuthParam {
        final Protocol protocol;
        final Permissions systemPermission;
        final Permissions dbPermission;
        final Permissions colPermission;

        UserAuthParam(final Protocol protocol, final Permissions systemPermission,
                      final Permissions dbPermission, final Permissions colPermission) {
            super();
            this.protocol = protocol;
            this.systemPermission = systemPermission;
            this.dbPermission = dbPermission;
            this.colPermission = colPermission;
        }

    }

    @Parameters
    public static Collection<UserAuthParam> params() {
        final Collection<UserAuthParam> params = new ArrayList<>();
        final Permissions[] permissions = new Permissions[]{Permissions.RW, Permissions.RO, Permissions.NONE};
        for (final Protocol protocol : new Protocol[]{Protocol.VST, Protocol.HTTP_JSON, Protocol.HTTP_VPACK}) {
            for (final Permissions systemPermission : permissions) {
                for (final Permissions dbPermission : permissions) {
                    for (final Permissions colPermission : permissions) {
                        params.add(new UserAuthParam(protocol, systemPermission, dbPermission, colPermission));
                    }
                }
            }
        }
        return params;
    }

    private static ArangoDB arangoDB;
    private static ArangoDB arangoDBRoot;
    private final UserAuthParam param;
    private final String details;

    public UserAuthTest(final UserAuthParam param) {
        super();
        this.param = param;
        if (arangoDB != null || arangoDBRoot != null) {
            shutdown();
        }
        arangoDBRoot = new ArangoDB.Builder().useProtocol(param.protocol).build();
        arangoDBRoot.createUser(USER_NAME, "");
        arangoDB = new ArangoDB.Builder().useProtocol(param.protocol).user(USER_NAME).build();
        arangoDBRoot.createDatabase(DB_NAME);
        arangoDBRoot.db(DB_NAME).createCollection(COLLECTION_NAME);
        arangoDBRoot.db().grantAccess(USER_NAME, param.systemPermission);
        arangoDBRoot.db(DB_NAME).grantAccess(USER_NAME, param.dbPermission);
        arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).grantAccess(USER_NAME, param.colPermission);
        details = param.protocol + "_" + param.systemPermission + "_" +
                param.dbPermission + "_" + param.colPermission;
    }

    @AfterClass
    public static void shutdown() {
        arangoDBRoot.deleteUser(USER_NAME);
        if (arangoDBRoot.db(DB_NAME).exists())
            arangoDBRoot.db(DB_NAME).drop();
        if (arangoDB != null) {
            arangoDB.shutdown();
        }
        arangoDBRoot.shutdown();
        arangoDB = null;
        arangoDBRoot = null;
    }

    @Test
    public void createDatabase() {
        try {
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    assertThat(details, arangoDB.createDatabase(DB_NAME_NEW), is(true));
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.getDatabases(), hasItem(DB_NAME_NEW));
            } else {
                try {
                    arangoDB.createDatabase(DB_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.getDatabases(), not(hasItem(DB_NAME_NEW)));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME_NEW).drop();
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void dropDatabase() {
        try {
            arangoDBRoot.createDatabase(DB_NAME_NEW);
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    assertThat(details, arangoDB.db(DB_NAME).drop(), is(true));
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.getDatabases(), not(hasItem(DB_NAME)));
            } else {
                try {
                    arangoDB.db(DB_NAME).drop();
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.getDatabases(), hasItem(DB_NAME));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME_NEW).drop();
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void createUser() {
        try {
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.createUser(USER_NAME_NEW, "");
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.getUsers(), is(notNullValue()));
            } else {
                try {
                    arangoDB.createUser(USER_NAME_NEW, "");
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                try {
                    arangoDBRoot.getUser(USER_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void deleteUser() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.deleteUser(USER_NAME_NEW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                try {
                    arangoDBRoot.getUser(USER_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            } else {
                try {
                    arangoDB.deleteUser(USER_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.getUsers(), is(notNullValue()));
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void updateUser() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.updateUser(USER_NAME_NEW, new UserUpdateOptions().active(false));
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.getUser(USER_NAME_NEW).getActive(), is(false));
            } else {
                try {
                    arangoDB.updateUser(USER_NAME_NEW, new UserUpdateOptions().active(false));
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.getUser(USER_NAME_NEW).getActive(), is(true));
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void grantUserDBAccess() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.db().grantAccess(USER_NAME_NEW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
            } else {
                try {
                    arangoDB.db().grantAccess(USER_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void resetUserDBAccess() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            arangoDBRoot.db().grantAccess(USER_NAME_NEW);
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.db(DB_NAME).resetAccess(USER_NAME_NEW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
            } else {
                try {
                    arangoDB.db(DB_NAME).resetAccess(USER_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void grantUserCollcetionAccess() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).grantAccess(USER_NAME_NEW, Permissions.RW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).grantAccess(USER_NAME_NEW, Permissions.RW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void resetUserCollectionAccess() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            arangoDBRoot.db().grantAccess(USER_NAME_NEW);
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).resetAccess(USER_NAME_NEW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).resetAccess(USER_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void updateUserDefaultDatabaseAccess() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            arangoDBRoot.db().grantAccess(USER_NAME_NEW);
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.grantDefaultDatabaseAccess(USER_NAME_NEW, Permissions.RW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
            } else {
                try {
                    arangoDB.grantDefaultDatabaseAccess(USER_NAME_NEW, Permissions.RW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void updateUserDefaultCollectionAccess() {
        try {
            arangoDBRoot.createUser(USER_NAME_NEW, "");
            arangoDBRoot.db().grantAccess(USER_NAME_NEW);
            if (Permissions.RW.equals(param.systemPermission)) {
                try {
                    arangoDB.grantDefaultCollectionAccess(USER_NAME_NEW, Permissions.RW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
            } else {
                try {
                    arangoDB.grantDefaultCollectionAccess(USER_NAME_NEW, Permissions.RW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.deleteUser(USER_NAME_NEW);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void createCollection() {
        try {
            if (Permissions.RW.equals(param.dbPermission)) {
                try {
                    arangoDB.db(DB_NAME).createCollection(COLLECTION_NAME_NEW);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).getInfo(),
                        is(notNullValue()));
            } else {
                try {
                    arangoDB.db(DB_NAME).createCollection(COLLECTION_NAME_NEW);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                try {
                    arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).getInfo();
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).drop();
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void dropCollection() {
        try {
            arangoDBRoot.db(DB_NAME).createCollection(COLLECTION_NAME_NEW);
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).grantAccess(USER_NAME, param.colPermission);
            if (Permissions.RW.equals(param.dbPermission) && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME_NEW).drop();
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                try {
                    arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).getInfo();
                    fail(details);
                } catch (final ArangoDBException e) {
                }
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME_NEW).drop();
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).getInfo(),
                        is(notNullValue()));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME_NEW).drop();
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void seeCollection() {
        if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                && (Permissions.RW.equals(param.colPermission) || Permissions.RO.equals(param.colPermission))) {
            try {
                final Collection<CollectionEntity> collections = arangoDB.db(DB_NAME).getCollections();
                boolean found = false;
                for (final CollectionEntity collection : collections) {
                    if (collection.getName().equals(COLLECTION_NAME)) {
                        found = true;
                        break;
                    }
                }
                assertThat(details, found, is(true));
            } catch (final ArangoDBException e) {
                fail(details);
            }
        } else if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))) {
            final Collection<CollectionEntity> collections = arangoDB.db(DB_NAME).getCollections();
            boolean found = false;
            for (final CollectionEntity collection : collections) {
                if (collection.getName().equals(COLLECTION_NAME)) {
                    found = true;
                    break;
                }
            }
            assertThat(details, found, is(false));
        }
    }

    @Test
    public void readCollectionInfo() {
        if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                && (Permissions.RW.equals(param.colPermission) || Permissions.RO.equals(param.colPermission))) {
            try {
                assertThat(details, arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getInfo(), is(notNullValue()));
            } catch (final ArangoDBException e) {
                fail(details);
            }
        } else {
            try {
                arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getInfo();
                fail(details);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void readCollectionProperties() {
        if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                && (Permissions.RW.equals(param.colPermission) || Permissions.RO.equals(param.colPermission))) {
            try {
                assertThat(details, arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getProperties(),
                        is(notNullValue()));
            } catch (final ArangoDBException e) {
                fail(details);
            }
        } else {
            try {
                arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getProperties();
                fail(details);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void writeCollectionProperties() {
        if (Permissions.RW.equals(param.dbPermission) && Permissions.RW.equals(param.colPermission)) {
            try {
                assertThat(details, arangoDB.db(DB_NAME).collection(COLLECTION_NAME)
                                .changeProperties(new CollectionPropertiesOptions().waitForSync(true)),
                        is(notNullValue()));
            } catch (final ArangoDBException e) {
                fail(details);
            }
            assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).getProperties().getWaitForSync(),
                    is(true));
        } else {
            try {
                arangoDB.db(DB_NAME).collection(COLLECTION_NAME)
                        .changeProperties(new CollectionPropertiesOptions().waitForSync(true));
                fail(details);
            } catch (final ArangoDBException e) {
            }
            assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).getProperties().getWaitForSync(),
                    is(false));
        }
    }

    @Test
    public void readCollectionIndexes() {
        if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                && (Permissions.RW.equals(param.colPermission) || Permissions.RO.equals(param.colPermission))) {
            try {
                assertThat(details, arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getIndexes(), is(notNullValue()));
            } catch (final ArangoDBException e) {
                fail(details);
            }
        } else {
            try {
                arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getIndexes();
                fail(details);
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void createCollectionIndex() {
        String id = null;
        try {
            if (Permissions.RW.equals(param.dbPermission) && Permissions.RW.equals(param.colPermission)) {
                try {
                    final IndexEntity createHashIndex = arangoDB.db(DB_NAME).collection(COLLECTION_NAME)
                            .ensureHashIndex(Collections.singletonList("a"), new HashIndexOptions());
                    assertThat(details, createHashIndex, is(notNullValue()));
                    id = createHashIndex.getId();
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).getIndexes().size(), is(2));
            } else {
                try {
                    final IndexEntity createHashIndex = arangoDB.db(DB_NAME).collection(COLLECTION_NAME)
                            .ensureHashIndex(Collections.singletonList("a"), new HashIndexOptions());
                    id = createHashIndex.getId();
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).getIndexes().size(), is(1));
            }
        } finally {
            if (id != null) {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteIndex(id);
            }
        }
    }

    @Test
    public void dropCollectionIndex() {
        final String id = arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME)
                .ensureHashIndex(Collections.singletonList("a"), new HashIndexOptions()).getId();
        try {
            if (Permissions.RW.equals(param.dbPermission) && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).deleteIndex(id);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).getIndexes().size(), is(1));
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).deleteIndex(id);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).getIndexes().size(), is(2));
            }
        } finally {
            if (id != null) {
                try {
                    arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteIndex(id);
                } catch (final ArangoDBException e) {
                }
            }
        }
    }

    @Test
    public void truncateCollection() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).truncate();
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(false));
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).truncate();
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(true));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).truncate();
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void readDocumentByKey() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && (Permissions.RW.equals(param.colPermission) || Permissions.RO.equals(param.colPermission))) {
                assertThat(details,
                        arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getDocument("123", BaseDocument.class),
                        is(notNullValue()));
            } else {
                assertThat(details,
                        arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getDocument("123", BaseDocument.class),
                        is(nullValue()));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void readDocumentByAql() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && (Permissions.RW.equals(param.colPermission) || Permissions.RO.equals(param.colPermission))) {
                assertThat(details,
                        arangoDB.db(DB_NAME).query("FOR i IN @@col RETURN i",
                                new MapBuilder().put("@col", COLLECTION_NAME).get(), new AqlQueryOptions(), BaseDocument.class)
                                .asListRemaining().size(),
                        is(1));
            } else {
                assertThat(details,
                        arangoDB.db(DB_NAME).collection(COLLECTION_NAME).getDocument("123", BaseDocument.class),
                        is(nullValue()));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void insertDocument() {
        try {
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(true));
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(false));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void updateDocumentByKey() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).updateDocument("123",
                            new BaseDocument(new MapBuilder().put("test", "test").get()));
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME)
                                .getDocument("123", BaseDocument.class).getAttribute("test").toString(),
                        is("test"));
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).updateDocument("123",
                            new BaseDocument(new MapBuilder().put("test", "test").get()));
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME)
                                .getDocument("123", BaseDocument.class).getAttribute("test"),
                        is(nullValue()));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void updateDocumentByAql() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).query("FOR i IN @@col UPDATE i WITH @newDoc IN @@col",
                            new MapBuilder().put("@col", COLLECTION_NAME)
                                    .put("newDoc", new BaseDocument(new MapBuilder().put("test", "test").get())).get(),
                            new AqlQueryOptions(), Void.class);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME)
                                .getDocument("123", BaseDocument.class).getAttribute("test").toString(),
                        is("test"));
            } else {
                try {
                    arangoDB.db(DB_NAME).query("FOR i IN @@col UPDATE i WITH @newDoc IN @@col",
                            new MapBuilder().put("@col", COLLECTION_NAME)
                                    .put("newDoc", new BaseDocument(new MapBuilder().put("test", "test").get())).get(),
                            new AqlQueryOptions(), Void.class);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME)
                                .getDocument("123", BaseDocument.class).getAttribute("test"),
                        is(nullValue()));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void deleteDocumentByKey() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(false));
            } else {
                try {
                    arangoDB.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(true));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

    @Test
    public void deleteDocumentByAql() {
        try {
            arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).insertDocument(new BaseDocument("123"));
            if ((Permissions.RW.equals(param.dbPermission) || Permissions.RO.equals(param.dbPermission))
                    && Permissions.RW.equals(param.colPermission)) {
                try {
                    arangoDB.db(DB_NAME).query("REMOVE @key IN @@col",
                            new MapBuilder().put("key", "123").put("@col", COLLECTION_NAME).get(), new AqlQueryOptions(),
                            Void.class);
                } catch (final ArangoDBException e) {
                    fail(details);
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(false));
            } else {
                try {
                    arangoDB.db(DB_NAME).query("REMOVE @key IN @@col",
                            new MapBuilder().put("key", "123").put("@col", COLLECTION_NAME).get(), new AqlQueryOptions(),
                            Void.class);
                    fail(details);
                } catch (final ArangoDBException e) {
                }
                assertThat(details, arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).documentExists("123"),
                        is(true));
            }
        } finally {
            try {
                arangoDBRoot.db(DB_NAME).collection(COLLECTION_NAME).deleteDocument("123");
            } catch (final ArangoDBException e) {
            }
        }
    }

}
