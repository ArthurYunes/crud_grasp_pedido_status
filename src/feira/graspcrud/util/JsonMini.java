package feira.graspcrud.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Utilitário de leitura e escrita de JSON sem nenhuma biblioteca externa.
 *
 * <p>Padrão GRASP: Pure Fabrication — classe criada exclusivamente para manter
 * o domínio livre de detalhes de persistência. Não existe no mundo real da
 * feira livre; é uma abstração técnica introduzida para atender ao requisito
 * de persistência em JSON.
 *
 * <p>Suporta objetos simples (chave-valor com tipos primitivos) e listas
 * de objetos aninhados (para o caso de {@code statusPedido} dentro de Pedido).
 */
public class JsonMini {

    private JsonMini() {}

    // ── Escrita ────────────────────────────────────────────────────────────

    /**
     * Serializa uma lista de mapas para um arquivo JSON com indentação de 2 espaços.
     *
     * @param caminho caminho do arquivo de destino
     * @param lista   lista de mapas (campo → valor) representando os objetos
     * @throws UncheckedIOException em caso de erro de I/O
     */
    public static void escrever(Path caminho, List<Map<String, Object>> lista) {
        try {
            Path parent = caminho.getParent();
            if (parent != null) Files.createDirectories(parent);
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < lista.size(); i++) {
                sb.append(serializarObjeto(lista.get(i), "  "));
                if (i < lista.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(caminho, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao escrever JSON em " + caminho, e);
        }
    }

    /**
     * Serializa um mapa (objeto) para JSON com a indentação fornecida.
     *
     * @param mapa   mapa de campos
     * @param indent prefixo de indentação
     * @return string JSON do objeto
     */
    private static String serializarObjeto(Map<String, Object> mapa, String indent) {
        StringBuilder sb = new StringBuilder(indent + "{\n");
        List<String> chaves = new ArrayList<>(mapa.keySet());
        for (int i = 0; i < chaves.size(); i++) {
            String chave = chaves.get(i);
            Object valor = mapa.get(chave);
            sb.append(indent).append("  ").append("\"").append(chave).append("\": ");
            sb.append(serializarValor(valor, indent + "  "));
            if (i < chaves.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("}");
        return sb.toString();
    }

    /**
     * Serializa um valor Java para representação JSON.
     *
     * @param valor  valor a serializar (String, Number, Boolean, Map, null)
     * @param indent prefixo de indentação (para objetos aninhados)
     * @return representação JSON do valor
     */
    @SuppressWarnings("unchecked")
    private static String serializarValor(Object valor, String indent) {
        if (valor == null)             return "null";
        if (valor instanceof String)   return "\"" + escapar((String) valor) + "\"";
        if (valor instanceof Boolean)  return valor.toString();
        if (valor instanceof Number)   return valor.toString();
        if (valor instanceof Map)      return "\n" + serializarObjeto((Map<String, Object>) valor, indent);
        return "\"" + escapar(valor.toString()) + "\"";
    }

    /**
     * Escapa caracteres especiais de uma String para JSON.
     *
     * @param s string a escapar
     * @return string com caracteres escapados
     */
    private static String escapar(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ── Leitura ────────────────────────────────────────────────────────────

    /**
     * Lê um arquivo JSON e retorna uma lista de mapas (campo → valor).
     * Retorna lista vazia se o arquivo não existir.
     *
     * @param caminho caminho do arquivo JSON
     * @return lista de mapas representando os objetos do arquivo
     * @throws UncheckedIOException em caso de erro de I/O diferente de arquivo inexistente
     */
    public static List<Map<String, Object>> ler(Path caminho) {
        if (!Files.exists(caminho)) return new ArrayList<>();
        try {
            String conteudo = Files.readString(caminho, StandardCharsets.UTF_8).trim();
            if (conteudo.isEmpty() || conteudo.equals("[]")) return new ArrayList<>();
            return parsearLista(conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao ler JSON de " + caminho, e);
        }
    }

    /**
     * Faz o parse de uma string JSON que representa uma lista de objetos.
     *
     * @param json string JSON
     * @return lista de mapas
     */
    private static List<Map<String, Object>> parsearLista(String json) {
        List<Map<String, Object>> lista = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("[")) return lista;

        // Remove colchetes externos
        json = json.substring(1, json.length() - 1).trim();

        List<String> objetos = dividirObjetos(json);
        for (String obj : objetos) {
            obj = obj.trim();
            if (!obj.isEmpty()) {
                lista.add(parsearObjeto(obj));
            }
        }
        return lista;
    }

    /**
     * Divide a string de objetos JSON em partes individuais,
     * respeitando objetos aninhados ({}).
     *
     * @param json conteúdo interno do array JSON
     * @return lista de strings, cada uma representando um objeto JSON
     */
    private static List<String> dividirObjetos(String json) {
        List<String> objetos = new ArrayList<>();
        int profundidade = 0;
        int inicio = -1;
        boolean emString = false;
        boolean escapado = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escapado) {
                escapado = false;
                continue;
            }
            if (c == '\\' && emString) {
                escapado = true;
                continue;
            }
            if (c == '"') {
                emString = !emString;
                continue;
            }
            if (!emString) {
                if (c == '{') {
                    if (profundidade == 0) inicio = i;
                    profundidade++;
                } else if (c == '}') {
                    profundidade--;
                    if (profundidade == 0 && inicio >= 0) {
                        objetos.add(json.substring(inicio, i + 1));
                        inicio = -1;
                    }
                }
            }
        }
        return objetos;
    }

    /**
     * Faz o parse de uma string JSON que representa um único objeto.
     *
     * @param json string JSON do objeto (com chaves externas {})
     * @return mapa de campos e valores
     */
    private static Map<String, Object> parsearObjeto(String json) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1, json.length() - 1).trim();

        int i = 0;
        while (i < json.length()) {
            // Pula espaços e vírgulas
            while (i < json.length() && (json.charAt(i) == ',' || Character.isWhitespace(json.charAt(i)))) i++;
            if (i >= json.length()) break;

            // Lê a chave
            if (json.charAt(i) != '"') { i++; continue; }
            int fimChave = json.indexOf('"', i + 1);
            if (fimChave == -1) break; // JSON malformado: aborta
            String chave = json.substring(i + 1, fimChave);
            i = fimChave + 1;

            // Pula ':'
            while (i < json.length() && json.charAt(i) != ':') i++;
            i++;
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

            // Lê o valor
            Object valor;
            if (i >= json.length()) break;
            char c = json.charAt(i);

            if (c == '"') {
                // String — rastreia escape com flag dedicada para evitar falso positivo
                // quando fimStr == i+1 (string vazia: charAt(fimStr-1) seria a abertura '"')
                int fimStr = i + 1;
                boolean escapado = false;
                while (fimStr < json.length()) {
                    char ch = json.charAt(fimStr);
                    if (escapado) {
                        escapado = false;
                    } else if (ch == '\\') {
                        escapado = true;
                    } else if (ch == '"') {
                        break;
                    }
                    fimStr++;
                }
                valor = desescapar(json.substring(i + 1, fimStr));
                i = fimStr + 1;
            } else if (c == '{') {
                // Objeto aninhado
                int fim = encontrarFimObjeto(json, i);
                valor = parsearObjeto(json.substring(i, fim + 1));
                i = fim + 1;
            } else if (c == 'n' && json.startsWith("null", i)) {
                valor = null;
                i += 4;
            } else if (c == 't' && json.startsWith("true", i)) {
                valor = Boolean.TRUE;
                i += 4;
            } else if (c == 'f' && json.startsWith("false", i)) {
                valor = Boolean.FALSE;
                i += 5;
            } else {
                // Número
                int fimNum = i;
                while (fimNum < json.length() && ",} \n\r\t".indexOf(json.charAt(fimNum)) == -1) fimNum++;
                String numStr = json.substring(i, fimNum).trim();
                try {
                    if (numStr.contains(".")) valor = Double.parseDouble(numStr);
                    else valor = Long.parseLong(numStr);
                } catch (NumberFormatException e) {
                    valor = numStr;
                }
                i = fimNum;
            }
            mapa.put(chave, valor);
        }
        return mapa;
    }

    /**
     * Encontra o índice do '}' que fecha o objeto que começa em {@code inicio}.
     *
     * @param json   string JSON
     * @param inicio índice do '{' de abertura
     * @return índice do '}' de fechamento correspondente
     */
    private static int encontrarFimObjeto(String json, int inicio) {
        int prof = 0;
        boolean emStr = false;
        boolean escapado = false;
        for (int i = inicio; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escapado) { escapado = false; continue; }
            if (c == '\\' && emStr) { escapado = true; continue; }
            if (c == '"') { emStr = !emStr; continue; }
            if (!emStr) {
                if (c == '{') prof++;
                else if (c == '}') { prof--; if (prof == 0) return i; }
            }
        }
        return json.length() - 1;
    }

    /**
     * Remove as sequências de escape de uma string JSON.
     *
     * @param s string com escapes JSON
     * @return string sem escapes
     */
    private static String desescapar(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
