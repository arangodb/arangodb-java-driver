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

package com.arangodb.example.graph;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.model.CollectionCreateOptions;

/**
 * @author Mark Vollmary
 * @see <a href="https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html">AQL Example Queries on an
 * Actors and Movies Database</a>
 */
public class AQLActorsAndMoviesExample {

    private static final String TEST_DB = "actors_movies_test_db";
    private static ArangoDB arangoDB;
    private static ArangoDatabase db;

    @BeforeClass
    public static void setUp() {
        arangoDB = new ArangoDB.Builder().build();
        if (arangoDB.db(TEST_DB).exists())
            arangoDB.db(TEST_DB).drop();
        arangoDB.createDatabase(TEST_DB);
        db = arangoDB.db(TEST_DB);
        createData();
    }

    @AfterClass
    public static void tearDown() {
        db.drop();
        arangoDB.shutdown();
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#all-actors-who-acted-in-movie1-or-movie2">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void allActorsActsInMovie1or2() {
        final ArangoCursor<String> cursor = db.query(
                "WITH actors FOR x IN ANY 'movies/TheMatrix' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN x._id",
                null, null, String.class);
        assertThat(cursor.asListRemaining(),
                hasItems("actors/Keanu", "actors/Hugo", "actors/Emil", "actors/Carrie", "actors/Laurence"));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#all-actors-who-acted-in-movie1-or-movie2">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void allActorsActsInMovie1or2UnionDistinct() {
        final ArangoCursor<String> cursor = db.query(
                "WITH actors FOR x IN UNION_DISTINCT ((FOR y IN ANY 'movies/TheMatrix' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN y._id), (FOR y IN ANY 'movies/TheDevilsAdvocate' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN y._id)) RETURN x",
                null, null, String.class);
        assertThat(cursor.asListRemaining(), hasItems("actors/Emil", "actors/Hugo", "actors/Carrie", "actors/Laurence",
                "actors/Keanu", "actors/Al", "actors/Charlize"));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#all-actors-who-acted-in-both-movie1-and-movie2-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void allActorsActsInMovie1and2() {
        final ArangoCursor<String> cursor = db.query(
                "WITH actors FOR x IN INTERSECTION ((FOR y IN ANY 'movies/TheMatrix' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN y._id), (FOR y IN ANY 'movies/TheDevilsAdvocate' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN y._id)) RETURN x",
                null, null, String.class);
        assertThat(cursor.asListRemaining(), hasItems("actors/Keanu"));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#all-common-movies-between-actor1-and-actor2-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void allMoviesBetweenActor1andActor2() {
        final ArangoCursor<String> cursor = db.query(
                "WITH movies FOR x IN INTERSECTION ((FOR y IN ANY 'actors/Hugo' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN y._id), (FOR y IN ANY 'actors/Keanu' actsIn OPTIONS {bfs: true, uniqueVertices: 'global'} RETURN y._id)) RETURN x",
                null, null, String.class);
        assertThat(cursor.asListRemaining(),
                hasItems("movies/TheMatrixRevolutions", "movies/TheMatrixReloaded", "movies/TheMatrix"));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#all-actors-who-acted-in-3-or-more-movies-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void allActorsWhoActedIn3orMoreMovies() {
        final ArangoCursor<Actor> cursor = db.query(
                "FOR x IN actsIn COLLECT actor = x._from WITH COUNT INTO counter FILTER counter >= 3 RETURN {actor: actor, movies: counter}",
                null, null, Actor.class);
        assertThat(cursor.asListRemaining(),
                hasItems(new Actor("actors/Carrie", 3), new Actor("actors/CubaG", 4), new Actor("actors/Hugo", 3),
                        new Actor("actors/Keanu", 4), new Actor("actors/Laurence", 3), new Actor("actors/MegR", 5),
                        new Actor("actors/TomC", 3), new Actor("actors/TomH", 3)));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#all-movies-where-exactly-6-actors-acted-in-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void allMoviesWhereExactly6ActorsActedIn() {
        final ArangoCursor<String> cursor = db.query(
                "FOR x IN actsIn COLLECT movie = x._to WITH COUNT INTO counter FILTER counter == 6 RETURN movie", null,
                null, String.class);
        assertThat(cursor.asListRemaining(),
                hasItems("movies/SleeplessInSeattle", "movies/TopGun", "movies/YouveGotMail"));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#the-number-of-actors-by-movie-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void theNumberOfActorsByMovie() {
        final ArangoCursor<Movie> cursor = db.query(
                "FOR x IN actsIn COLLECT movie = x._to WITH COUNT INTO counter RETURN {movie: movie, actors: counter}",
                null, null, Movie.class);
        assertThat(cursor.asListRemaining(),
                hasItems(new Movie("movies/AFewGoodMen", 11), new Movie("movies/AsGoodAsItGets", 4),
                        new Movie("movies/JerryMaguire", 9), new Movie("movies/JoeVersustheVolcano", 3),
                        new Movie("movies/SleeplessInSeattle", 6), new Movie("movies/SnowFallingonCedars", 4),
                        new Movie("movies/StandByMe", 7), new Movie("movies/TheDevilsAdvocate", 3),
                        new Movie("movies/TheMatrix", 5), new Movie("movies/TheMatrixReloaded", 4),
                        new Movie("movies/TheMatrixRevolutions", 4), new Movie("movies/TopGun", 6),
                        new Movie("movies/WhatDreamsMayCome", 5), new Movie("movies/WhenHarryMetSally", 4),
                        new Movie("movies/YouveGotMail", 6)));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#the-number-of-movies-by-actor-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void theNumberOfMoviesByActor() {
        final ArangoCursor<Actor> cursor = db.query(
                "FOR x IN actsIn COLLECT actor = x._from WITH COUNT INTO counter RETURN {actor: actor, movies: counter}",
                null, null, Actor.class);
        assertThat(cursor.asListRemaining(),
                hasItems(new Actor("actors/Al", 1), new Actor("actors/AnnabellaS", 1), new Actor("actors/AnthonyE", 1),
                        new Actor("actors/BillPull", 1), new Actor("actors/BillyC", 1), new Actor("actors/BonnieH", 1),
                        new Actor("actors/BrunoK", 1), new Actor("actors/Carrie", 3), new Actor("actors/CarrieF", 1),
                        new Actor("actors/Charlize", 1), new Actor("actors/ChristopherG", 1), new Actor("actors/CoreyF", 1),
                        new Actor("actors/CubaG", 4), new Actor("actors/DaveC", 1), new Actor("actors/DemiM", 1),
                        new Actor("actors/Emil", 1), new Actor("actors/EthanH", 1), new Actor("actors/GregK", 2),
                        new Actor("actors/HelenH", 1), new Actor("actors/Hugo", 3), new Actor("actors/JackN", 2),
                        new Actor("actors/JamesC", 1), new Actor("actors/JamesM", 1), new Actor("actors/JayM", 1),
                        new Actor("actors/JerryO", 2), new Actor("actors/JohnC", 1), new Actor("actors/JonathanL", 1),
                        new Actor("actors/JTW", 1), new Actor("actors/Keanu", 4), new Actor("actors/KellyM", 1),
                        new Actor("actors/KellyP", 1), new Actor("actors/KevinB", 1), new Actor("actors/KevinP", 1),
                        new Actor("actors/KieferS", 2), new Actor("actors/Laurence", 3), new Actor("actors/MarshallB", 1),
                        new Actor("actors/MaxS", 2), new Actor("actors/MegR", 5), new Actor("actors/Nathan", 1),
                        new Actor("actors/NoahW", 1), new Actor("actors/ParkerP", 1), new Actor("actors/ReginaK", 1),
                        new Actor("actors/ReneeZ", 1), new Actor("actors/RickY", 1), new Actor("actors/RitaW", 1),
                        new Actor("actors/RiverP", 1), new Actor("actors/Robin", 1), new Actor("actors/RosieO", 1),
                        new Actor("actors/SteveZ", 1), new Actor("actors/TomC", 3), new Actor("actors/TomH", 3),
                        new Actor("actors/TomS", 1), new Actor("actors/ValK", 1), new Actor("actors/VictorG", 1),
                        new Actor("actors/WernerH", 1), new Actor("actors/WilW", 1)));
    }

    /**
     * @see <a href=
     * "https://docs.arangodb.com/current/cookbook/Graph/ExampleActorsAndMovies.html#the-number-of-movies-acted-in-between-2005-and-2010-by-actor-">AQL
     * Example Queries on an Actors and Movies Database</a>
     */
    @Test
    public void theNumberOfMoviesActedInBetween2005and2010byActor() {
        final ArangoCursor<Actor> cursor = db.query(
                "FOR x IN actsIn FILTER x.year >= 1990 && x.year <= 1995 COLLECT actor = x._from WITH COUNT INTO counter RETURN {actor: actor, movies: counter}",
                null, null, Actor.class);
        assertThat(cursor.asListRemaining(),
                hasItems(new Actor("actors/BillPull", 1), new Actor("actors/ChristopherG", 1), new Actor("actors/CubaG", 1),
                        new Actor("actors/DemiM", 1), new Actor("actors/JackN", 1), new Actor("actors/JamesM", 1),
                        new Actor("actors/JTW", 1), new Actor("actors/KevinB", 1), new Actor("actors/KieferS", 1),
                        new Actor("actors/MegR", 2), new Actor("actors/Nathan", 1), new Actor("actors/NoahW", 1),
                        new Actor("actors/RitaW", 1), new Actor("actors/RosieO", 1), new Actor("actors/TomC", 1),
                        new Actor("actors/TomH", 2), new Actor("actors/VictorG", 1)));
    }

    @SuppressWarnings("WeakerAccess")
    public static class Actor {
        private String actor;
        private Integer movies;

        public Actor() {
            super();
        }

        public Actor(final String actor, final Integer movies) {
            super();
            this.actor = actor;
            this.movies = movies;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((actor == null) ? 0 : actor.hashCode());
            result = prime * result + ((movies == null) ? 0 : movies.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Actor other = (Actor) obj;
            if (actor == null) {
                if (other.actor != null) {
                    return false;
                }
            } else if (!actor.equals(other.actor)) {
                return false;
            }
            if (movies == null) {
                return other.movies == null;
            } else return movies.equals(other.movies);
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class Movie {
        private String movie;
        private Integer actors;

        public Movie() {
            super();
        }

        public Movie(final String movie, final Integer actors) {
            super();
            this.movie = movie;
            this.actors = actors;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((actors == null) ? 0 : actors.hashCode());
            result = prime * result + ((movie == null) ? 0 : movie.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Movie other = (Movie) obj;
            if (actors == null) {
                if (other.actors != null) {
                    return false;
                }
            } else if (!actors.equals(other.actors)) {
                return false;
            }
            if (movie == null) {
                return other.movie == null;
            } else return movie.equals(other.movie);
        }

    }

    private static DocumentCreateEntity<BaseDocument> saveMovie(
            final ArangoCollection movies,
            final String key,
            final String title,
            final int released,
            final String tagline) {
        final BaseDocument value = new BaseDocument();
        value.setKey(key);
        value.addAttribute("title", title);
        value.addAttribute("released", released);
        value.addAttribute("tagline", tagline);
        return movies.insertDocument(value);
    }

    private static DocumentCreateEntity<BaseDocument> saveActor(
            final ArangoCollection actors,
            final String key,
            final String name,
            final int born) {
        final BaseDocument value = new BaseDocument();
        value.setKey(key);
        value.addAttribute("name", name);
        value.addAttribute("born", born);
        return actors.insertDocument(value);
    }

    private static void saveActsIn(
            final ArangoCollection actsIn,
            final String actor,
            final String movie,
            final String[] roles,
            final int year) {
        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(actor);
        value.setTo(movie);
        value.addAttribute("roles", roles);
        value.addAttribute("year", year);
        actsIn.insertDocument(value);
    }

    private static void createData() {
        db.createCollection("actors");
        final ArangoCollection actors = db.collection("actors");
        db.createCollection("movies");
        final ArangoCollection movies = db.collection("movies");
        db.createCollection("actsIn", new CollectionCreateOptions().type(CollectionType.EDGES));
        final ArangoCollection actsIn = db.collection("actsIn");

        final String theMatrix = saveMovie(movies, "TheMatrix", "The Matrix", 1999, "Welcome to the Real World")
                .getId();
        final String keanu = saveActor(actors, "Keanu", "Keanu Reeves", 1964).getId();
        final String carrie = saveActor(actors, "Carrie", "Carrie-Anne Moss", 1967).getId();
        final String laurence = saveActor(actors, "Laurence", "Laurence Fishburne", 1961).getId();
        final String hugo = saveActor(actors, "Hugo", "Hugo Weaving", 1960).getId();
        final String emil = saveActor(actors, "Emil", "Emil Eifrem", 1978).getId();

        saveActsIn(actsIn, keanu, theMatrix, new String[]{"Neo"}, 1999);
        saveActsIn(actsIn, carrie, theMatrix, new String[]{"Trinity"}, 1999);
        saveActsIn(actsIn, laurence, theMatrix, new String[]{"Morpheus"}, 1999);
        saveActsIn(actsIn, hugo, theMatrix, new String[]{"Agent Smith"}, 1999);
        saveActsIn(actsIn, emil, theMatrix, new String[]{"Emil"}, 1999);

        final String theMatrixReloaded = saveMovie(movies, "TheMatrixReloaded", "The Matrix Reloaded", 2003,
                "Free your mind").getId();
        saveActsIn(actsIn, keanu, theMatrixReloaded, new String[]{"Neo"}, 2003);
        saveActsIn(actsIn, carrie, theMatrixReloaded, new String[]{"Trinity"}, 2003);
        saveActsIn(actsIn, laurence, theMatrixReloaded, new String[]{"Morpheus"}, 2003);
        saveActsIn(actsIn, hugo, theMatrixReloaded, new String[]{"Agent Smith"}, 2003);

        final String theMatrixRevolutions = saveMovie(movies, "TheMatrixRevolutions", "The Matrix Revolutions", 2003,
                "Everything that has a beginning has an end").getId();
        saveActsIn(actsIn, keanu, theMatrixRevolutions, new String[]{"Neo"}, 2003);
        saveActsIn(actsIn, carrie, theMatrixRevolutions, new String[]{"Trinity"}, 2003);
        saveActsIn(actsIn, laurence, theMatrixRevolutions, new String[]{"Morpheus"}, 2003);
        saveActsIn(actsIn, hugo, theMatrixRevolutions, new String[]{"Agent Smith"}, 2003);

        final String theDevilsAdvocate = saveMovie(movies, "TheDevilsAdvocate", "The Devil's Advocate", 1997,
                "Evil has its winning ways").getId();
        final String charlize = saveActor(actors, "Charlize", "Charlize Theron", 1975).getId();
        final String al = saveActor(actors, "Al", "Al Pacino", 1940).getId();
        saveActsIn(actsIn, keanu, theDevilsAdvocate, new String[]{"Kevin Lomax"}, 1997);
        saveActsIn(actsIn, charlize, theDevilsAdvocate, new String[]{"Mary Ann Lomax"}, 1997);
        saveActsIn(actsIn, al, theDevilsAdvocate, new String[]{"John Milton"}, 1997);

        final String AFewGoodMen = saveMovie(movies, "AFewGoodMen", "A Few Good Men", 1992,
                "In the heart of the nation's capital, in a courthouse of the U.S. government, one man will stop at nothing to keep his honor, and one will stop at nothing to find the truth.")
                .getId();
        final String tomC = saveActor(actors, "TomC", "Tom Cruise", 1962).getId();
        final String jackN = saveActor(actors, "JackN", "Jack Nicholson", 1937).getId();
        final String demiM = saveActor(actors, "DemiM", "Demi Moore", 1962).getId();
        final String kevinB = saveActor(actors, "KevinB", "Kevin Bacon", 1958).getId();
        final String kieferS = saveActor(actors, "KieferS", "Kiefer Sutherland", 1966).getId();
        final String noahW = saveActor(actors, "NoahW", "Noah Wyle", 1971).getId();
        final String cubaG = saveActor(actors, "CubaG", "Cuba Gooding Jr.", 1968).getId();
        final String kevinP = saveActor(actors, "KevinP", "Kevin Pollak", 1957).getId();
        final String jTW = saveActor(actors, "JTW", "J.T. Walsh", 1943).getId();
        final String jamesM = saveActor(actors, "JamesM", "James Marshall", 1967).getId();
        final String christopherG = saveActor(actors, "ChristopherG", "Christopher Guest", 1948).getId();
        saveActsIn(actsIn, tomC, AFewGoodMen, new String[]{"Lt. Daniel Kaffee"}, 1992);
        saveActsIn(actsIn, jackN, AFewGoodMen, new String[]{"Col. Nathan R. Jessup"}, 1992);
        saveActsIn(actsIn, demiM, AFewGoodMen, new String[]{"Lt. Cdr. JoAnne Galloway"}, 1992);
        saveActsIn(actsIn, kevinB, AFewGoodMen, new String[]{"Capt. Jack Ross"}, 1992);
        saveActsIn(actsIn, kieferS, AFewGoodMen, new String[]{"Lt. Jonathan Kendrick"}, 1992);
        saveActsIn(actsIn, noahW, AFewGoodMen, new String[]{"Cpl. Jeffrey Barnes"}, 1992);
        saveActsIn(actsIn, cubaG, AFewGoodMen, new String[]{"Cpl. Carl Hammaker"}, 1992);
        saveActsIn(actsIn, kevinP, AFewGoodMen, new String[]{"Lt. Sam Weinberg"}, 1992);
        saveActsIn(actsIn, jTW, AFewGoodMen, new String[]{"Lt. Col. Matthew Andrew Markinson"}, 1992);
        saveActsIn(actsIn, jamesM, AFewGoodMen, new String[]{"Pfc. Louden Downey"}, 1992);
        saveActsIn(actsIn, christopherG, AFewGoodMen, new String[]{"Dr. Stone"}, 1992);

        final String topGun = saveMovie(movies, "TopGun", "Top Gun", 1986, "I feel the need, the need for speed.")
                .getId();
        final String kellyM = saveActor(actors, "KellyM", "Kelly McGillis", 1957).getId();
        final String valK = saveActor(actors, "ValK", "Val Kilmer", 1959).getId();
        final String anthonyE = saveActor(actors, "AnthonyE", "Anthony Edwards", 1962).getId();
        final String tomS = saveActor(actors, "TomS", "Tom Skerritt", 1933).getId();
        final String megR = saveActor(actors, "MegR", "Meg Ryan", 1961).getId();
        saveActsIn(actsIn, tomC, topGun, new String[]{"Maverick"}, 1986);
        saveActsIn(actsIn, kellyM, topGun, new String[]{"Charlie"}, 1986);
        saveActsIn(actsIn, valK, topGun, new String[]{"Iceman"}, 1986);
        saveActsIn(actsIn, anthonyE, topGun, new String[]{"Goose"}, 1986);
        saveActsIn(actsIn, tomS, topGun, new String[]{"Viper"}, 1986);
        saveActsIn(actsIn, megR, topGun, new String[]{"Carole"}, 1986);

        final String jerryMaguire = saveMovie(movies, "JerryMaguire", "Jerry Maguire", 2000,
                "The rest of his life begins now.").getId();
        final String reneeZ = saveActor(actors, "ReneeZ", "Renee Zellweger", 1969).getId();
        final String kellyP = saveActor(actors, "KellyP", "Kelly Preston", 1962).getId();
        final String jerryO = saveActor(actors, "JerryO", "Jerry O'Connell", 1974).getId();
        final String jayM = saveActor(actors, "JayM", "Jay Mohr", 1970).getId();
        final String bonnieH = saveActor(actors, "BonnieH", "Bonnie Hunt", 1961).getId();
        final String reginaK = saveActor(actors, "ReginaK", "Regina King", 1971).getId();
        final String jonathanL = saveActor(actors, "JonathanL", "Jonathan Lipnicki", 1996).getId();
        saveActsIn(actsIn, tomC, jerryMaguire, new String[]{"Jerry Maguire"}, 2000);
        saveActsIn(actsIn, cubaG, jerryMaguire, new String[]{"Rod Tidwell"}, 2000);
        saveActsIn(actsIn, reneeZ, jerryMaguire, new String[]{"Dorothy Boyd"}, 2000);
        saveActsIn(actsIn, kellyP, jerryMaguire, new String[]{"Avery Bishop"}, 2000);
        saveActsIn(actsIn, jerryO, jerryMaguire, new String[]{"Frank Cushman"}, 2000);
        saveActsIn(actsIn, jayM, jerryMaguire, new String[]{"Bob Sugar"}, 2000);
        saveActsIn(actsIn, bonnieH, jerryMaguire, new String[]{"Laurel Boyd"}, 2000);
        saveActsIn(actsIn, reginaK, jerryMaguire, new String[]{"Marcee Tidwell"}, 2000);
        saveActsIn(actsIn, jonathanL, jerryMaguire, new String[]{"Ray Boyd"}, 2000);

        final String standByMe = saveMovie(movies, "StandByMe", "Stand By Me", 1986,
                "For some, it's the last real taste of innocence, and the first real taste of life. But for everyone, it's the time that memories are made of.")
                .getId();
        final String riverP = saveActor(actors, "RiverP", "River Phoenix", 1970).getId();
        final String coreyF = saveActor(actors, "CoreyF", "Corey Feldman", 1971).getId();
        final String wilW = saveActor(actors, "WilW", "Wil Wheaton", 1972).getId();
        final String johnC = saveActor(actors, "JohnC", "John Cusack", 1966).getId();
        final String marshallB = saveActor(actors, "MarshallB", "Marshall Bell", 1942).getId();
        saveActsIn(actsIn, wilW, standByMe, new String[]{"Gordie Lachance"}, 1986);
        saveActsIn(actsIn, riverP, standByMe, new String[]{"Chris Chambers"}, 1986);
        saveActsIn(actsIn, jerryO, standByMe, new String[]{"Vern Tessio"}, 1986);
        saveActsIn(actsIn, coreyF, standByMe, new String[]{"Teddy Duchamp"}, 1986);
        saveActsIn(actsIn, johnC, standByMe, new String[]{"Denny Lachance"}, 1986);
        saveActsIn(actsIn, kieferS, standByMe, new String[]{"Ace Merrill"}, 1986);
        saveActsIn(actsIn, marshallB, standByMe, new String[]{"Mr. Lachance"}, 1986);

        final String asGoodAsItGets = saveMovie(movies, "AsGoodAsItGets", "As Good as It Gets", 1997,
                "A comedy from the heart that goes for the throat.").getId();
        final String helenH = saveActor(actors, "HelenH", "Helen Hunt", 1963).getId();
        final String gregK = saveActor(actors, "GregK", "Greg Kinnear", 1963).getId();
        saveActsIn(actsIn, jackN, asGoodAsItGets, new String[]{"Melvin Udall"}, 1997);
        saveActsIn(actsIn, helenH, asGoodAsItGets, new String[]{"Carol Connelly"}, 1997);
        saveActsIn(actsIn, gregK, asGoodAsItGets, new String[]{"Simon Bishop"}, 1997);
        saveActsIn(actsIn, cubaG, asGoodAsItGets, new String[]{"Frank Sachs"}, 1997);

        final String whatDreamsMayCome = saveMovie(movies, "WhatDreamsMayCome", "What Dreams May Come", 1998,
                "After life there is more. The end is just the beginning.").getId();
        final String annabellaS = saveActor(actors, "AnnabellaS", "Annabella Sciorra", 1960).getId();
        final String maxS = saveActor(actors, "MaxS", "Max von Sydow", 1929).getId();
        final String wernerH = saveActor(actors, "WernerH", "Werner Herzog", 1942).getId();
        final String robin = saveActor(actors, "Robin", "Robin Williams", 1951).getId();
        saveActsIn(actsIn, robin, whatDreamsMayCome, new String[]{"Chris Nielsen"}, 1998);
        saveActsIn(actsIn, cubaG, whatDreamsMayCome, new String[]{"Albert Lewis"}, 1998);
        saveActsIn(actsIn, annabellaS, whatDreamsMayCome, new String[]{"Annie Collins-Nielsen"}, 1998);
        saveActsIn(actsIn, maxS, whatDreamsMayCome, new String[]{"The Tracker"}, 1998);
        saveActsIn(actsIn, wernerH, whatDreamsMayCome, new String[]{"The Face"}, 1998);

        final String snowFallingonCedars = saveMovie(movies, "SnowFallingonCedars", "Snow Falling on Cedars", 1999,
                "First loves last. Forever.").getId();
        final String ethanH = saveActor(actors, "EthanH", "Ethan Hawke", 1970).getId();
        final String rickY = saveActor(actors, "RickY", "Rick Yune", 1971).getId();
        final String jamesC = saveActor(actors, "JamesC", "James Cromwell", 1940).getId();
        saveActsIn(actsIn, ethanH, snowFallingonCedars, new String[]{"Ishmael Chambers"}, 1999);
        saveActsIn(actsIn, rickY, snowFallingonCedars, new String[]{"Kazuo Miyamoto"}, 1999);
        saveActsIn(actsIn, maxS, snowFallingonCedars, new String[]{"Nels Gudmundsson"}, 1999);
        saveActsIn(actsIn, jamesC, snowFallingonCedars, new String[]{"Judge Fielding"}, 1999);

        final String youveGotMail = saveMovie(movies, "YouveGotMail", "You've Got Mail", 1998,
                "At odds in life... in love on-line.").getId();
        final String parkerP = saveActor(actors, "ParkerP", "Parker Posey", 1968).getId();
        final String daveC = saveActor(actors, "DaveC", "Dave Chappelle", 1973).getId();
        final String steveZ = saveActor(actors, "SteveZ", "Steve Zahn", 1967).getId();
        final String tomH = saveActor(actors, "TomH", "Tom Hanks", 1956).getId();
        saveActsIn(actsIn, tomH, youveGotMail, new String[]{"Joe Fox"}, 1998);
        saveActsIn(actsIn, megR, youveGotMail, new String[]{"Kathleen Kelly"}, 1998);
        saveActsIn(actsIn, gregK, youveGotMail, new String[]{"Frank Navasky"}, 1998);
        saveActsIn(actsIn, parkerP, youveGotMail, new String[]{"Patricia Eden"}, 1998);
        saveActsIn(actsIn, daveC, youveGotMail, new String[]{"Kevin Jackson"}, 1998);
        saveActsIn(actsIn, steveZ, youveGotMail, new String[]{"George Pappas"}, 1998);

        final String sleeplessInSeattle = saveMovie(movies, "SleeplessInSeattle", "Sleepless in Seattle", 1993,
                "What if someone you never met, someone you never saw, someone you never knew was the only someone for you?")
                .getId();
        final String ritaW = saveActor(actors, "RitaW", "Rita Wilson", 1956).getId();
        final String billPull = saveActor(actors, "BillPull", "Bill Pullman", 1953).getId();
        final String victorG = saveActor(actors, "VictorG", "Victor Garber", 1949).getId();
        final String rosieO = saveActor(actors, "RosieO", "Rosie O'Donnell", 1962).getId();
        saveActsIn(actsIn, tomH, sleeplessInSeattle, new String[]{"Sam Baldwin"}, 1993);
        saveActsIn(actsIn, megR, sleeplessInSeattle, new String[]{"Annie Reed"}, 1993);
        saveActsIn(actsIn, ritaW, sleeplessInSeattle, new String[]{"Suzy"}, 1993);
        saveActsIn(actsIn, billPull, sleeplessInSeattle, new String[]{"Walter"}, 1993);
        saveActsIn(actsIn, victorG, sleeplessInSeattle, new String[]{"Greg"}, 1993);
        saveActsIn(actsIn, rosieO, sleeplessInSeattle, new String[]{"Becky"}, 1993);

        final String joeVersustheVolcano = saveMovie(movies, "JoeVersustheVolcano", "Joe Versus the Volcano", 1990,
                "A story of love, lava and burning desire.").getId();
        final String nathan = saveActor(actors, "Nathan", "Nathan Lane", 1956).getId();
        saveActsIn(actsIn, tomH, joeVersustheVolcano, new String[]{"Joe Banks"}, 1990);
        saveActsIn(actsIn, megR, joeVersustheVolcano,
                new String[]{"DeDe', 'Angelica Graynamore', 'Patricia Graynamore"}, 1990);
        saveActsIn(actsIn, nathan, joeVersustheVolcano, new String[]{"Baw"}, 1990);

        final String whenHarryMetSally = saveMovie(movies, "WhenHarryMetSally", "When Harry Met Sally", 1998,
                "At odds in life... in love on-line.").getId();
        final String billyC = saveActor(actors, "BillyC", "Billy Crystal", 1948).getId();
        final String carrieF = saveActor(actors, "CarrieF", "Carrie Fisher", 1956).getId();
        final String brunoK = saveActor(actors, "BrunoK", "Bruno Kirby", 1949).getId();
        saveActsIn(actsIn, billyC, whenHarryMetSally, new String[]{"Harry Burns"}, 1998);
        saveActsIn(actsIn, megR, whenHarryMetSally, new String[]{"Sally Albright"}, 1998);
        saveActsIn(actsIn, carrieF, whenHarryMetSally, new String[]{"Marie"}, 1998);
        saveActsIn(actsIn, brunoK, whenHarryMetSally, new String[]{"Jess"}, 1998);
    }

}
