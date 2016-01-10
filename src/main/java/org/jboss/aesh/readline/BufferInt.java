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
package org.jboss.aesh.readline;

import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.LoggerUtil;
import org.jboss.aesh.util.WcWidth;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class BufferInt {

    private static final Logger LOGGER = LoggerUtil.getLogger(BufferInt.class.getName());

    private int[] line;
    private int cursor;
    private int size;
    private Prompt prompt;
    private int delta; //need to keep track of a delta for ansi terminal
    private boolean disablePrompt = false;
    private boolean multiLine = false;
    private int[] multiLineBuffer = new int[0];

    public BufferInt() {
        line = new int[1024];
        prompt = new Prompt("");
    }

    public BufferInt(Prompt prompt) {
        line = new int[1024];
        if(prompt != null)
        this.prompt = prompt;
    }

    public BufferInt(BufferInt buf) {
        line = buf.line.clone();
        cursor = buf.cursor;
        size = buf.size;
        prompt = buf.prompt.copy();
    }

    public int get(int pos) {
        if(pos > -1 && pos <= size)
            return line[pos];
        else
            throw new IndexOutOfBoundsException();
    }

    public int size() {
        return size;
    }

    public int getCursor() {
        return cursor;
    }

    protected int getCursorWithPrompt() {
        if(disablePrompt)
            return getCursor()+1;
        else
            return getCursor() + prompt.getLength()+1;
    }

    public void reset() {
        cursor = 0;
        line = new int[1024];
        size = 0;
    }

    /**
     * Need to disable prompt in calculations involving search.
     *
     * @param disable prompt or not
     */
    public void disablePrompt(boolean disable) {
        disablePrompt = disable;
    }

    protected boolean isPromptDisabled() {
        return disablePrompt;
    }

    public int length() {
        if(prompt.isMasking() && prompt.getMask() == 0)
            return 1;
        else
            return size;
    }

    public int totalLength() {
        if(prompt.isMasking()) {
            if(prompt.getMask() == 0)
                return disablePrompt ? 1 : prompt.getLength()+1;
        }
        return disablePrompt ? size+1 : size + prompt.getLength()+1;
    }

    public void setMultiLine(boolean multi) {
        multiLine = multi;
    }

    public void updateMultiLineBuffer() {
        int originalSize = multiLineBuffer.length;
        if(lineEndsWithBackslash()) {
            multiLineBuffer = Arrays.copyOf(multiLineBuffer, size);
            System.arraycopy(line, 0, multiLineBuffer, originalSize, size);
        }
        else {
            multiLineBuffer = Arrays.copyOf(multiLineBuffer, size);
            System.arraycopy(line, 0, multiLineBuffer, size, originalSize);
        }
        clearLine();
        cursor = 0;
        size = 0;
    }

    private boolean lineEndsWithBackslash() {
        return (size > 1 && line[size] == '\\' && line[size-1] == ' ');
    }

    public void insert(int[] data) {
        for (int aData : data) insert(aData);

        delta = data.length;
    }

    public void insert(int data) {
        int width = WcWidth.width(data);
        if(width == -1) {
            //todo: handle control chars...
        }
        else if(width == 1) {
            if(cursor < size)
                System.arraycopy(line, cursor, line, cursor + 1, size - cursor);
            line[cursor++] = data;
            size++;
        }
    }
    /**
     * Move the cursor left if the param is negative,
     * and right if its positive.
     * Return ansi code to represent the move
     *
     * @param move where to move
     * @param termWidth terminal width
     * @return ansi string that represent the move
     */
    protected int[] move(int move, int termWidth) {
        return move(move, termWidth, false);
    }

    protected int[] move(int move, int termWidth, boolean viMode) {
        LOGGER.info("moving: "+move+", width: "+termWidth+", buffer: "+getLine());
        move = moveCursor(move, viMode);

        int currentRow = (getCursorWithPrompt() / (termWidth));
        if(currentRow > 0 && getCursorWithPrompt() % termWidth == 0)
            currentRow--;

        int newRow = ((move + getCursorWithPrompt()) / (termWidth));
        if(newRow > 0 && ((move + getCursorWithPrompt()) % (termWidth) == 0))
            newRow--;

        int row = newRow - currentRow;

        cursor = cursor + move;

        // 0 Masking separates the UI cursor position from the 'real' cursor position.
        // Cursor movement still has to occur, via moveCursor and setCursor above,
        // to put new characters in the correct location in the invisible line,
        // but this method should always return an empty character so the UI cursor does not move.
        if(prompt.isMasking() && prompt.getMask() == 0){
            return new int[0];
        }

        int cursor = getCursorWithPrompt() % termWidth;
        if(cursor == 0 && getCursorWithPrompt() > 0)
            cursor = termWidth;
        if(row > 0) {
            return moveToRowAndColumn(row, 'B', cursor);

            //StringBuilder sb = new StringBuilder();
            //sb.append(printAnsi(row+"B")).append(printAnsi(cursor+"G"));
            //return sb.toString().toCharArray();
        }
        //going up
        else if (row < 0) {
            //check if we are on the "first" row:
            //StringBuilder sb = new StringBuilder();
            //sb.append(printAnsi(Math.abs(row)+"A")).append(printAnsi(cursor+"G"));
            return moveToRowAndColumn(row, 'A', cursor);
            //return sb.toString().toCharArray();
        }
        //staying at the same row
        else {
            LOGGER.info("staying at same row "+move);
            if(move < 0)
                return moveToColumn(move, 'D');
                //return printAnsi(Math.abs(move)+"D");

            else if(move > 0) {
                LOGGER.info("returning: "+ Arrays.toString( moveToColumn(move,'C')));
                //return printAnsi(move + "C");
                return moveToColumn(move, 'C');
            }
            else
                return new int[0];
        }
    }

    private int[] moveToColumn(int column, char direction) {
        int[] out = new int[4];
        out[0] = 27;
        out[1] = '[';
        out[2] = column;
        out[3] = direction;
        return out;
    }

    private int[] moveToRowAndColumn(int row, char rowCommand, int column) {
        int[] out = new int[8];
        out[0] = 27;
        out[1] = '[';
        out[2] = row;
        out[3] = rowCommand;
        out[4] = 27;
        out[5] = '[';
        out[6] = column;
        out[7] = 'G';
        return out;
    }



    /**
     * Make sure that the cursor do not move ob (out of bounds)
     *
     * @param move left if its negative, right if its positive
     * @param viMode if viMode we need other restrictions compared
     * to emacs movement
     * @return adjusted movement
     */
    private int moveCursor(final int move, boolean viMode) {
        // cant move to a negative value
        if(getCursor() == 0 && move <=0 )
            return 0;
        // cant move longer than the length of the line
        if(viMode) {
            if(getCursor() == length()-1 && (move > 0))
                return 0;
        }
        else {
            if(getCursor() == length() && (move > 0))
                return 0;
        }

        // dont move out of bounds
        if(getCursor() + move <= 0)
            return -getCursor();

        if(viMode) {
            if(getCursor() + move > length()-1)
                return (length()-1-getCursor());
        }
        else {
            if(getCursor() + move > length())
                return (length()-getCursor());
        }

        return move;
    }

    /**
     * Get line from given param
     *
     * @param position in line
     * @return line from position
     */
    private int[] getLineFrom(int position) {
        return Arrays.copyOfRange(line, position, size);
    }

    private int[] getLine() {
        if(!prompt.isMasking())
            return Arrays.copyOf(line, size);
        else {
            if(size > 0 && prompt.getMask() != '\u0000') {
                int[] tmpLine = new int[size];
                Arrays.fill(tmpLine, prompt.getMask());
                return tmpLine;
            }
            else
                return new int[0];
        }
    }

    private int[] getLineNoMask() {
        return Arrays.copyOf(line, size);
    }

    private void clearLine() {
        Arrays.fill(this.line, 0, size, 0);
        cursor = 0;
        size = 0;
    }

    public void print(Consumer<int[]> out, int width) {
        replaceLineWhenCursorIsOnLine(out, width);
        delta = 0;
    }

    public void replace(Consumer<int[]> out, String line, int width) {
        int tmpDelta = line.length() - size;
        int oldCursor = cursor + prompt.getLength();
        clearLine();
        insert(Parser.toCodePoints(line));
        delta = tmpDelta;

        if(oldCursor > width) {
            int originalRow = oldCursor / width;
            if(originalRow > 0 && totalLength() % width == 0)
                originalRow--;
            for(int i=0; i < originalRow; i++)
                out.accept(ANSI.MOVE_LINE_UP);
        }

        replaceLineWhenCursorIsOnLine(out, width);
        delta = 0;
    }

    private void replaceLineWhenCursorIsOnLine(Consumer<int[]> out, int width) {
        if(delta >= 0) {
            moveCursorToStartAndPrint(out, false);
        }
        else { // delta < 0
            if((totalLength()+delta) <= width) {
                moveCursorToStartAndPrint(out, true);
            }
            else {
                int numRows = totalLength() / width;
                if(numRows > 0 && totalLength() % width == 0)
                    numRows--;
                clearRowsAndMoveBack(out, numRows);
                moveCursorToStartAndPrint(out, false);
            }
        }
    }

    private void clearRowsAndMoveBack(Consumer<int[]> out, int rows) {
        for(int i=0; i < rows; i++) {
            out.accept(ANSI.MOVE_LINE_DOWN);
            out.accept(ANSI.ERASE_WHOLE_LINE);
        }
        //move back again
        out.accept(new int[]{27,'[',(char)rows,'A'});

    }

    private void moveCursorToStartAndPrint(Consumer<int[]> out, boolean clearLine) {
        if((prompt.getLength() > 0 && cursor != 0) || delta < 0) {
            out.accept(ANSI.CURSOR_START);
            if (clearLine)
                out.accept(ANSI.ERASE_LINE_FROM_CURSOR);
        }
        if(prompt.getLength() > 0)
            out.accept(prompt.getANSI());
        out.accept(getMultiLine());
    }

    private int[] getMultiLine() {
        if (multiLine) {
            int[] tmpLine = Arrays.copyOf(multiLineBuffer, size);
            System.arraycopy(line, 0, tmpLine, multiLineBuffer.length, size);
            return  tmpLine;
        }
        else {
            return getLine();
        }
    }

    public int getMultiCursor() {
        if (multiLine) {
            return multiLineBuffer.length + getCursor();
        }
        else {
            return getCursor();
        }
    }

    public void delete(int delta) {
        if (delta > 0) {
            delta = Math.min(delta, size - cursor);
            System.arraycopy(line, cursor + delta, line, cursor, size - cursor + delta);
            size -= delta;
        }
        else if (delta < 0) {
            delta = - Math.min(- delta, cursor);
            System.arraycopy(line, cursor, line, cursor + delta, size - cursor);
            size += delta;
            cursor += delta;
        }
    }

    /**
     * Write a char to the line and update cursor accordingly
     *
     * @param c char
     */
    public void write(char c) {
        insert(c);
        delta = 1;
    }

    /**
     * Write a string to the line and update cursor accordingly
     *
     * @param str string
     */
    public void write(final String str) {
        insert(Parser.toCodePoints(str));
    }

    public int getDelta() {
        return delta;
    }

    /**
     * Switch case if the current character is a letter.
     *
     * @return false if the character is not a letter, else true
     */
    protected void changeCase() {
        if(Character.isLetter(line[cursor])) {
            if(Character.isLowerCase(line[cursor]))
                line[cursor] = Character.toUpperCase(line[cursor]);
            else
                line[cursor] = Character.toLowerCase(line[cursor]);
        }
    }

    public void upCase() {
        if(Character.isLetter(line[cursor]))
            line[cursor] = Character.toUpperCase(line[cursor]);
    }

    public void lowCase() {
        if(Character.isLetter(line[cursor]))
            line[cursor] = Character.toLowerCase(line[cursor]);
    }

    public void replace(char rChar) {
        replace(getCursor(), rChar);
    }

    public void replace(int pos, int rChar) {
        if(pos > -1 && pos <= size)
            line[pos] = rChar;
    }

}