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


package utils;


/**
 * @author Michele Rastelli
 */
public final class TestUtils {

    private TestUtils() {
    }

    /**
     * Parses {@param version} and checks whether it is greater or equal to <{@param otherMajor}, {@param otherMinor},
     * {@param otherPatch}> comparing the corresponding version components in lexicographical order.
     */
    public static boolean isAtLeastVersion(final String version, final int otherMajor, final int otherMinor,
                                           final int otherPatch) {
        return compareVersion(version, otherMajor, otherMinor, otherPatch) >= 0;
    }

    /**
     * Parses {@param version} and checks whether it is less than <{@param otherMajor}, {@param otherMinor},
     * {@param otherPatch}> comparing the corresponding version components in lexicographical order.
     */
    public static boolean isLessThanVersion(final String version, final int otherMajor, final int otherMinor,
                                            final int otherPatch) {
        return compareVersion(version, otherMajor, otherMinor, otherPatch) < 0;
    }

    private static int compareVersion(final String version, final int otherMajor, final int otherMinor,
                                      final int otherPatch) {
        String[] parts = version.split("-")[0].split("\\.");

        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);

        int majorComparison = Integer.compare(major, otherMajor);
        if (majorComparison != 0) {
            return majorComparison;
        }

        int minorComparison = Integer.compare(minor, otherMinor);
        if (minorComparison != 0) {
            return minorComparison;
        }

        return Integer.compare(patch, otherPatch);
    }

}
