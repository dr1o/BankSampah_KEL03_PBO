package banksampah.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitas JSON murni (Pure Java tanpa library eksternal) untuk komunikasi REST API
 * antara Backend OOP Java dan Frontend Web (Vite).
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Mem-parsing JSON object tingkat pertama menjadi Map&lt;String, String&gt;.
     * Nilai string, angka, boolean, maupun raw array/object diambil sebagai teks.
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) {
            return result;
        }
        String s = json.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        int i = 0;
        int n = s.length();
        while (i < n) {
            while (i < n && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) {
                i++;
            }
            if (i >= n) {
                break;
            }
            if (s.charAt(i) != '"') {
                break;
            }
            StringBuilder key = new StringBuilder();
            i = readQuotedString(s, i, key);
            while (i < n && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ':')) {
                i++;
            }
            if (i >= n) {
                break;
            }
            char c = s.charAt(i);
            if (c == '"') {
                StringBuilder val = new StringBuilder();
                i = readQuotedString(s, i, val);
                result.put(key.toString(), val.toString());
            } else if (c == '[' || c == '{') {
                char open = c;
                char close = (c == '[') ? ']' : '}';
                int depth = 0;
                int start = i;
                boolean inStr = false;
                while (i < n) {
                    char ch = s.charAt(i);
                    if (inStr) {
                        if (ch == '\\') {
                            i += 2;
                            continue;
                        }
                        if (ch == '"') {
                            inStr = false;
                        }
                    } else {
                        if (ch == '"') {
                            inStr = true;
                        } else if (ch == open) {
                            depth++;
                        } else if (ch == close) {
                            depth--;
                            if (depth == 0) {
                                i++;
                                break;
                            }
                        }
                    }
                    i++;
                }
                result.put(key.toString(), s.substring(start, i).trim());
            } else {
                int start = i;
                while (i < n && s.charAt(i) != ',' && s.charAt(i) != '}') {
                    i++;
                }
                String raw = s.substring(start, i).trim();
                result.put(key.toString(), "null".equals(raw) ? null : raw);
            }
        }
        return result;
    }

    /**
     * Mem-parsing JSON array yang berisi objek menjadi List&lt;Map&lt;String, String&gt;&gt;.
     */
    public static List<Map<String, String>> parseObjectArray(String jsonArray) {
        List<Map<String, String>> list = new ArrayList<>();
        if (jsonArray == null) {
            return list;
        }
        String s = jsonArray.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        int i = 0;
        int n = s.length();
        while (i < n) {
            while (i < n && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) {
                i++;
            }
            if (i >= n) {
                break;
            }
            if (s.charAt(i) == '{') {
                int depth = 0;
                int start = i;
                boolean inStr = false;
                while (i < n) {
                    char ch = s.charAt(i);
                    if (inStr) {
                        if (ch == '\\') {
                            i += 2;
                            continue;
                        }
                        if (ch == '"') {
                            inStr = false;
                        }
                    } else {
                        if (ch == '"') {
                            inStr = true;
                        } else if (ch == '{') {
                            depth++;
                        } else if (ch == '}') {
                            depth--;
                            if (depth == 0) {
                                i++;
                                break;
                            }
                        }
                    }
                    i++;
                }
                String objStr = s.substring(start, i);
                list.add(parseObject(objStr));
            } else {
                i++;
            }
        }
        return list;
    }

    private static int readQuotedString(String s, int startQuoteIdx, StringBuilder out) {
        int i = startQuoteIdx + 1;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < n) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"':
                        out.append('"');
                        i += 2;
                        break;
                    case '\\':
                        out.append('\\');
                        i += 2;
                        break;
                    case '/':
                        out.append('/');
                        i += 2;
                        break;
                    case 'b':
                        out.append('\b');
                        i += 2;
                        break;
                    case 'f':
                        out.append('\f');
                        i += 2;
                        break;
                    case 'n':
                        out.append('\n');
                        i += 2;
                        break;
                    case 'r':
                        out.append('\r');
                        i += 2;
                        break;
                    case 't':
                        out.append('\t');
                        i += 2;
                        break;
                    case 'u':
                        if (i + 5 < n) {
                            String hex = s.substring(i + 2, i + 6);
                            out.append((char) Integer.parseInt(hex, 16));
                            i += 6;
                        } else {
                            i += 2;
                        }
                        break;
                    default:
                        out.append(next);
                        i += 2;
                        break;
                }
            } else if (c == '"') {
                return i + 1;
            } else {
                out.append(c);
                i++;
            }
        }
        return i;
    }
}
