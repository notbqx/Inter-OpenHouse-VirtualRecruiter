package com.sergio.openhouse;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

import java.util.stream.Collectors;

public class HelloController implements Initializable {

     @FXML
     public TextArea textArea;

    @FXML
    private VBox chatContainer; // Use a VBox instead of TextArea
    @FXML
    private ScrollPane scrollPane; // Wrap the VBox in this to allow scrolling

    /**

     * AsistenteVocacionalIA

     * - Chat FAQ con búsqueda por similitud (TF-IDF + coseno)

     * - Recomendación de rutas en CS según intereses

     *

     * Ejecuta y escribe preguntas como:

     *   "¿Qué es ciberseguridad?"

     *   "Me gustan los videojuegos y la matemática, ¿qué estudio?"

     *   "¿Qué es ciencia de datos?"

     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Initialize logic
        AsistenteVocacionalIA.RetrievalQA faq = new AsistenteVocacionalIA.RetrievalQA(AsistenteVocacionalIA.CORPUS);
        AsistenteVocacionalIA.RecomendadorRutas rec = new AsistenteVocacionalIA.RecomendadorRutas();

        // Welcome message using bubbles instead of chatLog.appendText
        addBubble("=== Asistente Vocacional IA (Java) ===", false);
        addBubble("Escribe una pregunta o tus intereses (escribe 'salir' para terminar).", false);

        // 2. Keyboard Listener
        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String query = textArea.getText().trim();
                if (!query.isEmpty()) {
                    // User message (Right)
                    addBubble(query, true);

                    // AI Processing
                    AsistenteVocacionalIA.Doc match = faq.bestMatch(query);
                    String sugerencia = rec.recomendar(query);

                    // AI Response (Left)
                    addBubble(match.text, false);
                    addBubble("💡 Sugerencia: " + sugerencia, false);

                    textArea.clear();
                }
                event.consume(); // Prevents newline in TextArea
            }
        });
    }

    private void addBubble(String message, boolean isUser) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(700); // Slightly wider for better reading

        // Refined CSS styling
        String style = "-fx-padding: 10 15 10 15; -fx-background-radius: 20; -fx-font-size: 14px; ";
        if (isUser) {
            style += "-fx-background-color: #0084FF; -fx-text-fill: white;";
        } else {
            style += "-fx-background-color: #E9E9EB; -fx-text-fill: black;";
        }
        label.setStyle(style);

        HBox container = new HBox(label);
        container.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        container.setAlignment(isUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

        chatContainer.getChildren().add(container);

        // Auto-scroll hack: wait for UI to render then scroll to bottom
        javafx.application.Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    static class AsistenteVocacionalIA {



        // --- Pequeño corpus de FAQs (puedes agregar más) ---

        static class Doc {

            String title;

            String text;

            Doc(String t, String x) { title = t; text = x; }

        }



        static List<Doc> CORPUS = List.of(

                new Doc("¿Qué es Programación?",

                        "Programar es dar instrucciones a una computadora para resolver problemas. " +

                                "En ciencias de cómputos aprendes a diseñar algoritmos, estructuras de datos y a construir software."),

                new Doc("¿Qué es Ciencia de Datos?",

                        "Ciencia de datos combina estadística, programación y análisis para encontrar patrones y hacer predicciones con datos."),

                new Doc("¿Qué es IA / Machine Learning?",

                        "La IA permite a las computadoras aprender de datos. Machine Learning entrena modelos para clasificar, predecir o recomendar."),

                new Doc("¿Qué es Ciberseguridad?",

                        "Ciberseguridad protege sistemas, redes y datos contra ataques. Incluye criptografía, auditorías y respuesta a incidentes."),

                new Doc("¿Qué es Desarrollo de Videojuegos?",

                        "Desarrollo de videojuegos integra programación, gráficos, física y diseño para crear experiencias interactivas.")

        );



        // --- Vocabulario y utilidades TF-IDF ---

        static class TfIdf {

            Map<String, Integer> df = new HashMap<>();

            Set<String> vocab = new HashSet<>();

            int N; // número de documentos



            List<Map<String, Integer>> docTermFreqs = new ArrayList<>();



            static List<String> tokenize(String s) {

                return Arrays.stream(s.toLowerCase()

                                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")

                                .split("\\s+"))

                        .filter(t -> t.length() > 1)

                        .collect(Collectors.toList());

            }



            void fit(List<String> documents) {

                N = documents.size();

                for (String doc : documents) {

                    List<String> tokens = tokenize(doc);

                    Map<String, Integer> tf = new HashMap<>();

                    for (String tok : tokens) {

                        tf.put(tok, tf.getOrDefault(tok, 0) + 1);

                    }

                    docTermFreqs.add(tf);

                    // DF

                    for (String term : tf.keySet()) {

                        df.put(term, df.getOrDefault(term, 0) + 1);

                        vocab.add(term);

                    }

                }

            }



            Map<String, Double> tfidfVector(String text) {

                List<String> tokens = tokenize(text);

                Map<String, Integer> tf = new HashMap<>();

                for (String tok : tokens) tf.put(tok, tf.getOrDefault(tok, 0) + 1);



                Map<String, Double> vec = new HashMap<>();

                for (String term : tf.keySet()) {

                    int termDf = df.getOrDefault(term, 0);

                    if (termDf == 0) continue;

                    double idf = Math.log((N + 1.0) / (termDf + 1.0)) + 1.0; // idf suavizado

                    double val = tf.get(term) * idf;

                    vec.put(term, val);

                }

                return vec;

            }



            static double cosine(Map<String, Double> a, Map<String, Double> b) {

                double dot = 0.0, na = 0.0, nb = 0.0;

                Set<String> keys = new HashSet<>();

                keys.addAll(a.keySet());

                keys.addAll(b.keySet());

                for (String k : keys) {

                    double va = a.getOrDefault(k, 0.0);

                    double vb = b.getOrDefault(k, 0.0);

                    dot += va * vb;

                    na += va * va;

                    nb += vb * vb;

                }

                if (na == 0 || nb == 0) return 0.0;

                return dot / (Math.sqrt(na) * Math.sqrt(nb));

            }

        }



        // --- Motor de FAQ con TF-IDF ---

        static class RetrievalQA {

            TfIdf tfidf = new TfIdf();

            List<Doc> docs;



            RetrievalQA(List<Doc> corpus) {

                this.docs = corpus;

                tfidf.fit(corpus.stream().map(d -> d.text).collect(Collectors.toList()));

            }



            Doc bestMatch(String query) {

                Map<String, Double> qVec = tfidf.tfidfVector(query);

                double bestScore = -1;

                Doc best = null;

                for (int i = 0; i < docs.size(); i++) {

                    Map<String, Double> dVec = tfidf.tfidfVector(docs.get(i).text);

                    double s = TfIdf.cosine(qVec, dVec);

                    if (s > bestScore) {

                        bestScore = s;

                        best = docs.get(i);

                    }

                }

                return best;

            }

        }



        // --- Recomendación de rutas en CS según intereses ---

        static class RecomendadorRutas {

            // Mapa simple de intereses -> áreas (puedes enriquecerlo)

            Map<String, Map<String, Integer>> pesos = new HashMap<>();



            RecomendadorRutas() {

                // Áreas: {IA/ML, CienciaDatos, Ciberseguridad, Backend, Videojuegos}

                add("matematica", Map.of("IA/ML", 3, "CienciaDatos", 3, "Backend", 1));

                add("estadistica", Map.of("CienciaDatos", 3, "IA/ML", 2));

                add("videojuegos", Map.of("Videojuegos", 4, "IA/ML", 1, "Backend", 1));

                add("seguridad", Map.of("Ciberseguridad", 4, "Backend", 1));

                add("redes", Map.of("Ciberseguridad", 3, "Backend", 2));

                add("web", Map.of("Backend", 3));

                add("datos", Map.of("CienciaDatos", 3, "IA/ML", 2));

                add("robotica", Map.of("IA/ML", 3));

                add("grafica", Map.of("Videojuegos", 3));

            }



            void add(String interes, Map<String, Integer> score) {

                pesos.put(interes, score);

            }



            String recomendar(String textoLibre) {

                List<String> toks = TfIdf.tokenize(textoLibre);

                Map<String, Integer> acumulado = new HashMap<>();

                for (String t : toks) {

                    Map<String, Integer> contrib = pesos.get(t);

                    if (contrib == null) continue;

                    for (var e : contrib.entrySet()) {

                        acumulado.put(e.getKey(), acumulado.getOrDefault(e.getKey(), 0) + e.getValue());

                    }

                }

                if (acumulado.isEmpty()) {

                    return "No detecté intereses claros. Prueba mencionar palabras como: 'matemática', 'datos', 'seguridad', 'videojuegos', 'web', 'robótica'.";

                }

                // Top 2 recomendaciones

                return acumulado.entrySet().stream()

                        .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))

                        .limit(2)

                        .map(e -> e.getKey() + " (puntuación: " + e.getValue() + ")")

                        .collect(Collectors.joining(" | "));

            }

        }

    }
}

