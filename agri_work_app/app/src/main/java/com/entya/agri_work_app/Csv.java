package com.entya.agri_work_app;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/** UTF-8 CSVの引用符、カンマ、セル内改行を扱う。 */
public final class Csv {
    private Csv() {}

    public static List<String> readRow(PushbackReader reader) throws IOException {
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        boolean closed = false;
        boolean started = false;
        int c;
        while ((c = reader.read()) != -1) {
            started = true;
            if (quoted) {
                if (c == '"') {
                    int next = reader.read();
                    if (next == '"') cell.append('"');
                    else {
                        quoted = false;
                        closed = true;
                        if (next != -1) reader.unread(next);
                    }
                } else cell.append((char) c);
            } else if (c == ',') {
                row.add(cell.toString());
                cell.setLength(0);
                closed = false;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r') {
                    int next = reader.read();
                    if (next != -1 && next != '\n') reader.unread(next);
                }
                row.add(cell.toString());
                return row;
            } else if (closed) {
                throw new IOException("CSVの閉じ引用符の後に不正な文字があります");
            } else if (c == '"') {
                if (cell.length() != 0) throw new IOException("CSVの引用符が不正です");
                quoted = true;
            } else cell.append((char) c);
        }
        if (quoted) throw new IOException("CSVの引用符が閉じていません");
        if (!started) return null;
        row.add(cell.toString());
        return row;
    }

    public static void writeRow(Writer writer, String... cells) throws IOException {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) writer.write(',');
            String value = cells[i] == null ? "" : cells[i];
            writer.write('"');
            writer.write(value.replace("\"", "\"\""));
            writer.write('"');
        }
        writer.write("\r\n");
    }
}
