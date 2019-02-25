package org.aesh.utils;

/**
 * @author <a href="mailto:stalep@gmail.com">Ståle Pedersen</a>
 */
public class ANSIBuilder {

    private static final String COLOR_START = "\u001B[";
    private static final String RESET = "\u001B[0m";

    private StringBuilder b;
    private TextType textType = TextType.DEFAULT;
    private Color bg = Color.DEFAULT;
    private Color text = Color.DEFAULT;
    private boolean havePrintedColor = false;

    public ANSIBuilder() {
        b = new StringBuilder();
    }

    public static ANSIBuilder builder() {
        return new ANSIBuilder();
    }

    private void checkColor() {
        if(!havePrintedColor) {
            havePrintedColor = true;
            doAppendColors();
        }
    }

    private void doAppendColors() {
        if(bg == Color.DEFAULT && text == Color.DEFAULT && textType == TextType.DEFAULT)
            return;
        else {
            b.append(COLOR_START)
                    .append(textType.value()).append(';')
                    .append(text.text()).append(';')
                    .append(bg.bg()).append('m');
        }
    }

    public ANSIBuilder resetColors() {
        if(textType == TextType.DEFAULT && bg == Color.DEFAULT && text == Color.DEFAULT)
            return this;
        else {
            doResetColors();
            b.append(RESET);
            return this;
        }
    }

    private void doResetColors() {
        textType = TextType.DEFAULT;
        bg = Color.DEFAULT;
        text = Color.DEFAULT;
    }

    public ANSIBuilder clear() {
        b = new StringBuilder();
        doResetColors();
        havePrintedColor = false;
        return this;
    }

    public ANSIBuilder text(Color color) {
        if(color != null && this.text != color) {
            this.text = color;
            havePrintedColor = false;
        }
        return this;
    }

    public ANSIBuilder textType(TextType type) {
        if(type != null && textType != type) {
            textType = type;
            havePrintedColor = false;
        }
        return this;
    }

    public ANSIBuilder bg(Color color) {
        if(color != null && this.bg != color) {
            this.bg = color;
            havePrintedColor = false;
        }
        return this;
    }

    public ANSIBuilder blackText() {
        return text(Color.BLACK);
    }

    public ANSIBuilder redText() {
        return text(Color.RED);
    }

    public ANSIBuilder greenText() {
        return text(Color.GREEN);
    }

    public ANSIBuilder yellowText() {
        return text(Color.YELLOW);
    }

    public ANSIBuilder blueText() {
        return text(Color.BLUE);
    }

    public ANSIBuilder magentaText() {
        return text(Color.MAGENTA);
    }

    public ANSIBuilder cyanText() {
        return text(Color.CYAN);
    }

    public ANSIBuilder whiteText() {
        return text(Color.WHITE);
    }

    public ANSIBuilder defaultText() {
        return text(Color.DEFAULT);
    }

    public ANSIBuilder blackBg() {
        return bg(Color.BLACK);
    }

    public ANSIBuilder redBg() {
        return bg(Color.RED);
    }

    public ANSIBuilder greenBg() {
        return bg(Color.GREEN);
    }

    public ANSIBuilder yellowBg() {
        return bg(Color.YELLOW);
    }

    public ANSIBuilder blueBg() {
        return bg(Color.BLUE);
    }

    public ANSIBuilder magentaBg() {
        return bg(Color.MAGENTA);
    }

    public ANSIBuilder cyanBg() {
        return bg(Color.CYAN);
    }

    public ANSIBuilder whiteBg() {
        return bg(Color.WHITE);
    }

    public ANSIBuilder defaultBg() {
        return bg(Color.DEFAULT);
    }

    public ANSIBuilder blackText(String text) {
        return text(Color.BLACK).append(text).resetColors();
    }

    public ANSIBuilder redText(String text) {
        return text(Color.RED).append(text).resetColors();
    }

    public ANSIBuilder greenText(String text) {
        return text(Color.GREEN).append(text).resetColors();
    }

    public ANSIBuilder yellowText(String text) {
        return text(Color.YELLOW).append(text).resetColors();
    }

    public ANSIBuilder blueText(String text) {
        return text(Color.BLUE).append(text).resetColors();
    }

    public ANSIBuilder magentaText(String text) {
        return text(Color.MAGENTA).append(text).resetColors();
    }

    public ANSIBuilder cyanText(String text) {
        return text(Color.CYAN).append(text).resetColors();
    }

    public ANSIBuilder whiteText(String text) {
        return text(Color.WHITE).append(text).resetColors();
    }

    public ANSIBuilder defaultText(String text) {
        return text(Color.DEFAULT).append(text).resetColors();
    }

    public ANSIBuilder blackBg(String text) {
        return bg(Color.BLACK).append(text).resetColors();
    }

    public ANSIBuilder redBg(String text) {
        return bg(Color.RED).append(text).resetColors();
    }

    public ANSIBuilder greenBg(String text) {
        return bg(Color.GREEN).append(text).resetColors();
    }

    public ANSIBuilder yellowBg(String text) {
        return bg(Color.YELLOW).append(text).resetColors();
    }

    public ANSIBuilder blueBg(String text) {
        return bg(Color.BLUE).append(text).resetColors();
    }

    public ANSIBuilder magentaBg(String text) {
        return bg(Color.MAGENTA).append(text).resetColors();
    }

    public ANSIBuilder cyanBg(String text) {
        return bg(Color.CYAN).append(text).resetColors();
    }

    public ANSIBuilder whiteBg(String text) {
        return bg(Color.WHITE).append(text).resetColors();
    }

    public ANSIBuilder defaultBg(String text) {
        return bg(Color.DEFAULT).append(text).resetColors();
    }

    public ANSIBuilder append(String data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(int data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(char data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(CharSequence data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(char[] data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(Object data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(StringBuilder data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(float data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(double data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder append(long data) {
        checkColor();
        b.append(data);
        return this;
    }

    public ANSIBuilder bold() {
        return textType(TextType.BOLD);
    }

    public ANSIBuilder faint() {
        return textType(TextType.FAINT);
    }

    public ANSIBuilder italic() {
        return textType(TextType.ITALIC);
    }

    public ANSIBuilder underline() {
        return textType(TextType.UNDERLINE);
    }

    public ANSIBuilder blink() {
        return textType(TextType.BLINK);
    }

    public ANSIBuilder invert() {
        return textType(TextType.INVERT);
    }

    public ANSIBuilder conceal() {
        return textType(TextType.CONCEAL);
    }

    public ANSIBuilder crossedOut() {
        return textType(TextType.CROSSED_OUT);
    }

    public ANSIBuilder newline() {
        b.append(Config.getLineSeparator());
        return this;
    }

    public ANSIBuilder bold(String text) {
        return textType(TextType.BOLD).append(text).resetColors();
    }

    public ANSIBuilder faint(String text) {
        return textType(TextType.FAINT).append(text).resetColors();
    }

    public ANSIBuilder italic(String text) {
        return textType(TextType.ITALIC).append(text).resetColors();
    }

    public ANSIBuilder underline(String text) {
        return textType(TextType.UNDERLINE).append(text).resetColors();
    }

    public ANSIBuilder blink(String text) {
        return textType(TextType.BLINK).append(text).resetColors();
    }

    public ANSIBuilder invert(String text) {
        return textType(TextType.INVERT).append(text).resetColors();
    }

    public ANSIBuilder conceal(String text) {
        return textType(TextType.CONCEAL).append(text).resetColors();
    }

    public ANSIBuilder crossedOut(String text) {
        return textType(TextType.CROSSED_OUT).append(text).resetColors();
    }

    public String toString() {
        resetColors();
        return b.toString();
    }

    public enum Color {
        BLACK(0),
        RED(1),
        GREEN(2),
        YELLOW(3),
        BLUE(4),
        MAGENTA(5),
        CYAN(6),
        WHITE(7),
        DEFAULT(9);

        private final int value;

        private Color(int index) {
            this.value = index;
        }

        public String toString() {
            return this.name();
        }

        public int value() {
            return this.value;
        }

        public int text() {
            return this.value + 30;
        }

        public int bg() {
            return this.value + 40;
        }
    }

    public enum TextType {
        DEFAULT(0),
        BOLD(1),
        FAINT(2),
        ITALIC(3),
        UNDERLINE(4),
        BLINK(5),
        INVERT(7),
        CONCEAL(8),
        CROSSED_OUT(9);

        private final int value;

        TextType(int c) {
            this.value = c;
        }

        public int value() {
            return value;
        }

    }
}
