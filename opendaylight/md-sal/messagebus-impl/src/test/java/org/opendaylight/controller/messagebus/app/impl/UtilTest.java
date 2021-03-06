/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.messagebus.app.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class UtilTest {

    @Test
    public void testMD5Hash() throws Exception {
        // empty string
        createAndAssertHash("", "d41d8cd98f00b204e9800998ecf8427e");

        // non-empty string
        createAndAssertHash("The Guardian", "69b929ae473ed732d5fb8e0a55a8dc8d");

        // the same hash for the same string
        createAndAssertHash("The Independent", "db793706d70c37dcc16454fa8eb21b1c");
        createAndAssertHash("The Independent", "db793706d70c37dcc16454fa8eb21b1c"); // one more time

        // different strings must have different hashes
        createAndAssertHash("orange", "fe01d67a002dfa0f3ac084298142eccd");
        createAndAssertHash("yellow", "d487dd0b55dfcacdd920ccbdaeafa351");
    }

    //TODO: IllegalArgumentException would be better
    @Test(expected = RuntimeException.class)
    public void testMD5HashInvalidInput() throws Exception {
        Util.md5String(null);
    }

    @Test
    public void testWildcardToRegex() throws Exception {
        // empty wildcard string
        createAndAssertRegex("", "^$");

        // wildcard string is a char to be replaced
        createAndAssertRegex("*", "^.*$");
        createAndAssertRegex("?", "^.$");
        final String relevantChars = "()[]$^.{}|\\";
        for (final char c : relevantChars.toCharArray()) {
            final char oneChar[] = {c};
            final String wildcardStr = new String(oneChar);
            final String expectedRegex = "^\\" + c + "$";
            createAndAssertRegex(wildcardStr, expectedRegex);
        }

        // wildcard string consists of more chars
        createAndAssertRegex("a", "^a$");
        createAndAssertRegex("aBc", "^aBc$");
        createAndAssertRegex("a1b2C34", "^a1b2C34$");
        createAndAssertRegex("*?()[]$^.{}|\\X", "^.*.\\(\\)\\[\\]\\$\\^\\.\\{\\}\\|\\\\X$");
        createAndAssertRegex("a*BB?37|42$", "^a.*BB.37\\|42\\$$");
    }

    @Test
    public void testResultFor() throws Exception {
        {
            final String expectedResult = "dummy string";
            RpcResult<String> rpcResult = Util.resultFor(expectedResult).get();
            assertEquals(expectedResult, rpcResult.getResult());
            assertTrue(rpcResult.isSuccessful());
            assertTrue(rpcResult.getErrors().isEmpty());
        }
        {
            final Integer expectedResult = 42;
            RpcResult<Integer> rpcResult = Util.resultFor(expectedResult).get();
            assertEquals(expectedResult, rpcResult.getResult());
            assertTrue(rpcResult.isSuccessful());
            assertTrue(rpcResult.getErrors().isEmpty());
        }
    }

    @Test
    public void testExpandQname() throws Exception {
        // match no path because the list of the allowed paths is empty
        {
            final List<SchemaPath> paths = new ArrayList<>();
            final Pattern regexPattern = Pattern.compile(".*"); // match everything
            final List<SchemaPath> matchingPaths = Util.expandQname(paths, regexPattern);
            assertTrue(matchingPaths.isEmpty());
        }

        // match no path because of regex pattern
        {
            final List<SchemaPath> paths = createSchemaPathList();
            final Pattern regexPattern = Pattern.compile("^@.*");
            final List<SchemaPath> matchingPaths = Util.expandQname(paths, regexPattern);
            assertTrue(matchingPaths.isEmpty());
        }

        // match all paths
        {
            final List<SchemaPath> paths = createSchemaPathList();
            final Pattern regexPattern = Pattern.compile(".*");
            final List<SchemaPath> matchingPaths = Util.expandQname(paths, regexPattern);
            assertTrue(matchingPaths.contains(paths.get(0)));
            assertTrue(matchingPaths.contains(paths.get(1)));
            assertEquals(paths.size(), matchingPaths.size());
        }

        // match one path only
        {
            final List<SchemaPath> paths = createSchemaPathList();
            final Pattern regexPattern = Pattern.compile(".*yyy$");
            final List<SchemaPath> matchingPaths = Util.expandQname(paths, regexPattern);
            assertTrue(matchingPaths.contains(paths.get(1)));
            assertEquals(1, matchingPaths.size());
        }
    }

    private static void createAndAssertHash(final String inString, final String expectedHash) {
        assertEquals("Incorrect hash.", expectedHash, Util.md5String(inString));
    }

    private static void createAndAssertRegex(final String wildcardStr, final String expectedRegex) {
        assertEquals("Incorrect regex string.", expectedRegex, Util.wildcardToRegex(wildcardStr));
    }

    private static List<SchemaPath> createSchemaPathList() {
        final QName qname1 = QName.create("urn:odl:xxx", "2015-01-01", "localName");
        final QName qname2 = QName.create("urn:odl:yyy", "2015-01-01", "localName");
        final SchemaPath path1 = SchemaPath.create(true, qname1);
        final SchemaPath path2 = SchemaPath.create(true, qname2);
        return Arrays.asList(path1, path2);
    }
}
