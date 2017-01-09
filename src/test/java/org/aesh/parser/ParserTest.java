/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 */
package org.aesh.parser;

import org.aesh.terminal.formatting.TerminalString;
import org.aesh.util.ANSI;
import org.aesh.util.Config;
import org.aesh.util.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ParserTest {

    @Test
    public void testFindStartsWith() {
        List<String> completionList = new ArrayList<>(3);
        completionList.add("foobar");
        completionList.add("foobaz");
        completionList.add("foobor");
        completionList.add("foob");

        Assert.assertEquals("foob", Parser.findStartsWith(completionList));

        completionList.clear();
        completionList.add("foo");
        completionList.add("bar");
        assertEquals("", Parser.findStartsWith(completionList));
    }

    @Test
    public void testfindCurrentWordFromCursor() {
        assertEquals("", LineParser.parseLine(" ", 1).selectedWord().word());
        assertEquals("foo", LineParser.parseLine("foo bar", 3).selectedWord().word());
        assertEquals("bar", LineParser.parseLine("foo bar", 6).selectedWord().word());
        assertEquals("foobar", LineParser.parseLine("foobar", 6).selectedWord().word());
        assertEquals("fo", LineParser.parseLine("foobar", 2).selectedWordToCursor().word());
        assertEquals("", LineParser.parseLine("ls  ", 3).selectedWord().word());
        assertEquals("foo", LineParser.parseLine("ls  foo", 6).selectedWord().word());
        assertEquals("foo", LineParser.parseLine("ls  foo bar", 6).selectedWord().word());
        assertEquals("bar", LineParser.parseLine("ls  foo bar", 11).selectedWordToCursor().word());
        assertEquals("ba", LineParser.parseLine("ls  foo bar", 10).selectedWordToCursor().word());
        assertEquals("b", LineParser.parseLine("ls  foo bar", 9).selectedWordToCursor().word());
        assertEquals("foo", LineParser.parseLine("ls foo ", 6).selectedWordToCursor().word());
        assertEquals("o", LineParser.parseLine("ls o org/jboss/aeshell/Shell.class", 4).selectedWord().word());
        assertEquals("", LineParser.parseLine("ls  org/jboss/aeshell/Shell.class", 3).selectedWord().word());
    }

    @Test
    public void testFindCurrentWordWithEscapedSpaceToCursor() {
        assertEquals("foo bar", LineParser.parseLine("foo\\ bar", 8).selectedWordToCursor().word());
        assertEquals("foo ba", LineParser.parseLine("foo\\ bar", 7).selectedWordToCursor().word());
        assertEquals("foo bar", LineParser.parseLine("ls  foo\\ bar", 12).selectedWordToCursor().word());
    }

    @Test
    public void testFindClosestWholeWordToCursor() {
        assertEquals("foo", LineParser.parseLine("ls  foo bar", 6).selectedWord().word());

        assertEquals("", LineParser.parseLine(" ", 1).selectedWord().word());
        assertEquals("foo", LineParser.parseLine("foo bar", 1).selectedWord().word());
        assertEquals("foo", LineParser.parseLine("foo bar", 3).selectedWord().word());
        assertEquals("foobar", LineParser.parseLine("foobar", 6).selectedWord().word());
        assertEquals("foobar", LineParser.parseLine("foobar", 2).selectedWord().word());
        assertEquals("", LineParser.parseLine("ls  ", 3).selectedWord().word());

        assertEquals("o", LineParser.parseLine("ls o org/jboss/aeshell/Shell.class", 4).selectedWord().word());
        assertEquals("", LineParser.parseLine("ls  org/jboss/aeshell/Shell.class", 3).selectedWord().word());

        assertEquals("foo", LineParser.parseLine("foo bar foo", 3).selectedWord().word());
    }

    @Test
    public void testFindClosestWholeWordToCursorEscapedSpace() {
        assertEquals("foo bar", LineParser.parseLine("foo\\ bar", 7).selectedWord().word());
        assertEquals("foo bar", LineParser.parseLine("ls  foo\\ bar", 11).selectedWord().word());
    }

    @Test
    public void testFindEscapedSpaceWordCloseToEnd() {
        assertEquals("ls\\ foo", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo"));
        assertEquals("foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd("ls foo\\ bar"));
        assertEquals("bar", Parser.findEscapedSpaceWordCloseToEnd("ls foo bar"));
        assertEquals("ls\\ foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd("ls\\ foo\\ bar"));
        assertEquals("\\ ls\\ foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd("\\ ls\\ foo\\ bar"));
        assertEquals("ls\\ foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo\\ bar"));
        assertEquals("ls\\ foo\\ bar\\ ", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo\\ bar\\ "));
        assertEquals("", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo\\ bar\\  "));
    }

    @Test
    public void testFindEscapedSpaceWord() {
        assertTrue(Parser.doWordContainOnlyEscapedSpace("foo\\ bar"));
        assertTrue(Parser.doWordContainOnlyEscapedSpace("foo\\ bar\\ "));
        assertTrue(Parser.doWordContainOnlyEscapedSpace("\\ foo\\ bar\\ "));
        assertFalse(Parser.doWordContainOnlyEscapedSpace(" foo\\ bar\\ "));
        assertFalse(Parser.doWordContainOnlyEscapedSpace("foo bar\\ "));
        assertFalse(Parser.doWordContainOnlyEscapedSpace("foo bar"));
    }

    @Test
    public void testChangeWordWithSpaces() {
        assertEquals("foo bar", Parser.switchEscapedSpacesToSpacesInWord("foo\\ bar"));
        assertEquals(" foo bar", Parser.switchEscapedSpacesToSpacesInWord("\\ foo\\ bar"));
        assertEquals(" foo bar ", Parser.switchEscapedSpacesToSpacesInWord("\\ foo\\ bar\\ "));
        assertEquals(" foo bar", Parser.switchEscapedSpacesToSpacesInWord("\\ foo bar"));

        assertEquals("foo\\ bar", Parser.switchSpacesToEscapedSpacesInWord("foo bar"));
        assertEquals("\\ foo\\ bar", Parser.switchSpacesToEscapedSpacesInWord(" foo bar"));
        assertEquals("\\ foo\\ bar\\ ", Parser.switchSpacesToEscapedSpacesInWord(" foo bar "));
    }

    @Test
    public void testSplitBySizeKeepWords() {
        String words = "foo to bar is how it is i guess";
        List<String> out = Parser.splitBySizeKeepWords(words, 10);
        assertEquals("foo to bar", out.get(0));
        assertEquals("is how it", out.get(1));
        assertEquals("is i guess", out.get(2));

        words = "It is an error to use a backslash prior to any alphabetic";
        out = Parser.splitBySizeKeepWords(words, 20);
        assertEquals("It is an error to", out.get(0));
        assertEquals("use a backslash", out.get(1));
        assertEquals("prior to any", out.get(2));
        assertEquals("alphabetic", out.get(3));
    }

    @Test
    public void testTrim() {
        assertEquals("foo", Parser.trim("  foo "));
        assertEquals("bar foo", Parser.trim("bar foo "));
        assertEquals("bar foo", Parser.trim(" bar foo"));
        assertEquals("\\ foo\\ ", Parser.trim("\\ foo\\  "));
    }

    @Test
    public void testFindFirstWord() {
        assertEquals("foo", Parser.findFirstWord(" foo \\ bar"));
        assertEquals("foo", Parser.findFirstWord(" foo bar baz"));
        assertEquals("foo", Parser.findFirstWord("foo bar baz"));
        assertEquals("foobar", Parser.findFirstWord("foobar baz"));
        assertEquals("foobarbaz", Parser.findFirstWord("foobarbaz"));
    }

    @Test
    public void testTrimInFront() {
        assertEquals("foo ", Parser.trimInFront("  foo "));
        assertEquals("foo", Parser.trimInFront("  foo"));
        assertEquals("foo", Parser.trimInFront("foo"));
    }

    // verify divide by zero fix
    @Test
    public void testFormatDisplayList() {
        List<String> list = new ArrayList<>();
        String s1 = "this is a loooooong string thats longer than the terminal width";
        list.add(s1);

        assertEquals(s1 + "  " + Config.getLineSeparator(), Parser.formatDisplayList(list, 20, 20));
    }

    @Test
    public void testPadLeft() {
        assertEquals(" foo", Parser.padLeft(4, "foo"));
        assertEquals("   foo", Parser.padLeft(6, "foo"));
    }

    @Test
    public void testFindNumberOfSpaces() {
        assertEquals(4, Parser.findNumberOfSpacesInWord("this is a word "));
        assertEquals(4, Parser.findNumberOfSpacesInWord("this is a word !"));
        assertEquals(5, Parser.findNumberOfSpacesInWord(" this is a word !"));
        assertEquals(4, Parser.findNumberOfSpacesInWord(" this is a\\ word !"));
    }

    @Test
    public void testContainsDollar() {
        assertTrue(Parser.containsNonEscapedDollar("foo $bar"));
        assertFalse(Parser.containsNonEscapedDollar("foo bar"));
        assertFalse(Parser.containsNonEscapedDollar("foo \\$bar"));
        assertFalse(Parser.containsNonEscapedDollar("foo \\$bar\\$"));
        assertFalse(Parser.containsNonEscapedDollar("\\$foo \\$bar\\$"));
        assertTrue(Parser.containsNonEscapedDollar("$foo \\$bar\\$"));
        assertTrue(Parser.containsNonEscapedDollar("\\$foo \\$bar$"));
    }

    @Test
    public void testDoesStringContainQuote() {
        assertFalse(Parser.doesStringContainOpenQuote("foo bar is bar"));
        assertFalse(Parser.doesStringContainOpenQuote("\"foo bar is bar is foo is bar\""));
        assertFalse(Parser.doesStringContainOpenQuote("\"foo bar \"is bar is \"foo is bar\""));
        assertFalse(Parser.doesStringContainOpenQuote("\'foo bar \"is bar is \"foo is bar\'"));
        assertTrue(Parser.doesStringContainOpenQuote("\"foo bar is bar is \"foo is bar\""));
        assertFalse(Parser.doesStringContainOpenQuote("\"foo bar is bar is \\\"foo is bar\""));
        assertTrue(Parser.doesStringContainOpenQuote("\"foo bar is bar is \\\"foo is bar\'"));
    }

    @Test
    public void testOriginalInput() {
        String input = "echo foo -i bar";
        ParsedLine line = LineParser.parseLine(input);
        assertEquals(input, line.line());
    }

    @Test
    public void testFindAllWords() {
        ParsedLine line = LineParser.parseLine("", 0);
        assertEquals(-1, line.wordCursor());
        assertEquals(0, line.cursor());
        assertEquals("", line.selectedWord().word());

        line = LineParser.parseLine("   foo bar\\ baz 12345 ", 5);
        assertEquals("foo", line.words().get(0).word());
        assertEquals("bar baz", line.words().get(1).word());
        assertEquals("12345", line.words().get(2).word());
        assertEquals("foo", line.selectedWord().word());
        assertEquals(2, line.wordCursor());

        line = LineParser.parseLine("man < foo\\ bar ", 14);
        assertEquals("man", line.words().get(0).word());
        assertEquals("<", line.words().get(1).word());
        assertEquals("foo bar", line.words().get(2).word());
        assertEquals("foo bar", line.selectedWord().word());
        assertEquals(7, line.wordCursor());

        line = LineParser.parseLine("cd A\\ Directory\\ With\\ Spaces", 2);
        assertEquals("cd", line.words().get(0).word());
        assertEquals("A Directory With Spaces", line.words().get(1).word());
        assertEquals("cd", line.selectedWord().word());
        assertEquals(2, line.wordCursor());

        line = LineParser.parseLine("cd A\\ ",5);
        assertEquals("cd", line.words().get(0).word());
        assertEquals("A ", line.words().get(1).word());
        assertEquals("A ", line.selectedWord().word());
        assertEquals(1, line.wordCursor());

        line = LineParser.parseLine("cd A\\", 4);
        assertEquals("cd", line.words().get(0).word());
        assertEquals("A\\", line.words().get(1).word());
        assertEquals("A\\", line.selectedWord().word());
        assertEquals(1, line.wordCursor());

        line = LineParser.parseLine("ls --files /tmp/A\\ ");
        assertEquals("ls", line.words().get(0).word());
        assertEquals("--files", line.words().get(1).word());
        assertEquals("/tmp/A ", line.words().get(2).word());

        line = LineParser.parseLine("..\\..\\..\\..\\..\\..\\..\\temp\\foo.txt");
        assertEquals("..\\..\\..\\..\\..\\..\\..\\temp\\foo.txt", line.words().get(0).word());
    }

    @Test
    public void testFindAllQuotedWords() {
        ParsedLine line = LineParser.parseLine("foo bar \"baz 12345\" ", 19);
        assertEquals("foo", line.words().get(0).word());
        assertEquals(0, line.words().get(0).lineIndex());
        assertEquals("bar", line.words().get(1).word());
        assertEquals(4, line.words().get(1).lineIndex());
        assertEquals("baz 12345", line.words().get(2).word());
        assertEquals(9, line.words().get(2).lineIndex());
        assertEquals("", line.selectedWord().word());
        assertEquals(0, line.wordCursor());

        line = LineParser.parseLine("java -cp \"foo/bar\" \"Example\"");
        assertEquals("foo/bar", line.words().get(2).word());
        assertEquals("Example", line.words().get(3).word());

        line = LineParser.parseLine("'foo/bar/' Example\\ 1");
        assertEquals("foo/bar/", line.words().get(0).word());
        assertEquals("Example 1", line.words().get(1).word());

        line = LineParser.parseLine("man -f='foo bar/' Example\\ 1 foo");
        assertEquals("man", line.words().get(0).word());
        assertEquals("-f=foo bar/", line.words().get(1).word());
        assertEquals("Example 1", line.words().get(2).word());
        assertEquals("foo", line.words().get(3).word());

        line = LineParser.parseLine("man -f='foo/bar/ Example\\ 1");
        assertEquals(ParserStatus.UNCLOSED_QUOTE, line.status());

        line = LineParser.parseLine("man -f='foo/bar/' Example\\ 1\"");
        assertEquals(ParserStatus.UNCLOSED_QUOTE, line.status());

        line = LineParser.parseLine("-s \'redirectUris=[\"http://localhost:8080/blah/*\"]\'");
        assertEquals("-s", line.words().get(0).word());
        assertEquals("redirectUris=[\"http://localhost:8080/blah/*\"]", line.words().get(1).word());
    }

    @Test
    public void testFindAllTernaryQuotedWords() {
        ParsedLine line = LineParser.parseLine("\"\"  \"\"");
        assertEquals("  ", line.words().get(0).word());
        line = LineParser.parseLine("\"\"  foo bar \"\"");
        assertEquals("  foo bar ", line.words().get(0).word());

        line = LineParser.parseLine("\"\"  \"foo bar\" \"\"");
        assertEquals("  \"foo bar\" ", line.words().get(0).word());

        line = LineParser.parseLine("gah bah-bah  \"\"  \"foo bar\" \"\" boo");
        assertEquals("gah", line.words().get(0).word());
        assertEquals("bah-bah", line.words().get(1).word());
        assertEquals("  \"foo bar\" ", line.words().get(2).word());
        assertEquals("boo", line.words().get(3).word());

        line = LineParser.parseLine(" \"\"/s-ramp/wsdl/Operation[xp2:matches(@name, 'submit.*')]\"\"");
        assertEquals("/s-ramp/wsdl/Operation[xp2:matches(@name, 'submit.*')]", line.words().get(0).word());

        line = LineParser.parseLine(" \"\"/s-ramp/ext/${type} \\ \"\"");
        assertEquals("/s-ramp/ext/${type} \\ ", line.words().get(0).word());
    }

    @Test
    public void testFormatDisplayCompactListTerminalString() {
        TerminalString terminalLong = new TerminalString("This string is too long to terminal width");

        TerminalString terminalShort1 = new TerminalString("str1");
        TerminalString terminalShort2 = new TerminalString("str2");
        TerminalString terminalShort3 = new TerminalString("str3");
        TerminalString terminalShort4 = new TerminalString("str4");

        TerminalString terminalLonger1 = new TerminalString("longer1");
        TerminalString terminalLonger2 = new TerminalString("longer2");
        TerminalString terminalLonger3 = new TerminalString("longer3");

        assertEquals(
            terminalLong.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Collections.singletonList(terminalLong), 10));

        assertEquals(
            terminalShort1.toString() + "  " + terminalShort2.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2), 20));

        assertEquals(
            terminalShort1.toString() + Config.getLineSeparator() + terminalShort2.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2), 10));

        assertEquals(
            terminalShort1.toString() + "  " + terminalShort3.toString() + Config.getLineSeparator() + terminalShort2.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2, terminalShort3), 15));

        assertEquals(
            terminalShort1.toString() + "  " + terminalShort3.toString() + Config.getLineSeparator() +
                terminalShort2.toString() + "  " + terminalShort4.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2, terminalShort3, terminalShort4), 15));

        assertEquals(
            terminalLonger1.toString() + "  " + terminalShort1.toString() + Config.getLineSeparator() +
                terminalLonger2.toString() + Config.getLineSeparator() + terminalLonger3.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(
                Arrays.asList(terminalLonger1, terminalLonger2, terminalLonger3, terminalShort1), 15));
    }

    @Test
    public void testStripAwayAnsiPattern() {
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.BLACK_TEXT + "foo"));
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.BOLD + ANSI.CYAN_BG + "foo"));
        assertEquals("foo", Parser.stripAwayAnsiCodes("foo" + ANSI.BOLD + ANSI.CYAN_BG));
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.ALTERNATE_BUFFER + "foo"));
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.CURSOR_ROW + "foo"));
        assertEquals("foo bar", Parser.stripAwayAnsiCodes("foo" + ANSI.RESET + " bar"));
    }

    @Test
    public void testIsTrimmedArrayEmpty() {
        int[] input = {};
        assertTrue(Parser.isTrimmedArrayEmpty(input));
        input = new int[]{32,32};
        assertTrue(Parser.isTrimmedArrayEmpty(input));
        input = new int[]{32,32,68};
        assertFalse(Parser.isTrimmedArrayEmpty(input));
        input = new int[]{68,32,32};
        assertFalse(Parser.isTrimmedArrayEmpty(input));
        input = new int[]{70};
        assertFalse(Parser.isTrimmedArrayEmpty(input));
    }

    @Test
    public void testArrayContains() {
        int[] source = new int[] {1,2,3,4,5,6,7,8};
        int[] target = new int[] {7};
        assertTrue(Parser.arrayContains(source, target));
        target = new int[] {7,8};
        assertTrue(Parser.arrayContains(source, target));
        target = new int[] {7,8,9};
        assertFalse(Parser.arrayContains(source, target));
        target = new int[] {1,2,3,4};
        assertTrue(Parser.arrayContains(source, target));
        target = new int[] {1,2,3,4,6};
        assertFalse(Parser.arrayContains(source, target));
        target = new int[] {0,1,2,3,4};
        assertFalse(Parser.arrayContains(source, target));
    }

    @Test
    public void testArrayIndexOf() {
        int[] source = new int[] {1,2,3,4,5,6,7,8};
        int[] target = new int[] {7};
        assertEquals(6, Parser.arrayIndexOf(source, target));
        target = new int[] {7,8};
        assertEquals(6, Parser.arrayIndexOf(source, target));
        target = new int[] {7,8,9};
        assertEquals(-1, Parser.arrayIndexOf(source, target));
        target = new int[] {1,2,3,4};
        assertEquals(0, Parser.arrayIndexOf(source, target));
        target = new int[] {1,2,3,4,6};
        assertEquals(-1, Parser.arrayIndexOf(source, target));
        target = new int[] {0,1,2,3,4};
        assertEquals(-1, Parser.arrayIndexOf(source, target));
    }
}
