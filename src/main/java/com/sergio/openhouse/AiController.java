package com.sergio.openhouse;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

import java.util.stream.Collectors;

public class AiController implements Initializable {

    @FXML
    private Pane pane;

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

        // UI Configuration
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);


        chatContainer.needsLayoutProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                scrollPane.setVvalue(1.0);
            }
        });

        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * 2.5; // Adjust this for speed
            double vvalue = scrollPane.getVvalue();
            scrollPane.setVvalue(vvalue - deltaY / chatContainer.getHeight());
        });

        addBubble("Escribe una pregunta o tus intereses (Ej: programación, videojuegos, ciberseguridad, IA/ML)", false);


        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String query = textArea.getText().trim();
                if (!query.isEmpty()) {
                    addBubble(query, true);

                    AsistenteVocacionalIA.Doc match = faq.bestMatch(query);
                    String sugerencia = rec.recomendar(query);

                    addBubble(match.title + "\n" + match.text, false);
                    addBubble("💡 Sugerencia: " + sugerencia, false);

                    textArea.clear();
                }
                event.consume();
            }
        });
    }

    private void addBubble(String message, boolean isUser) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(500);

        // Refined CSS styling
        String style = "-fx-padding: 10 15 10 15; -fx-background-radius: 20; -fx-font-size: 14px; ";
        if (isUser) {
            style += "-fx-background-color: #d6ad06; -fx-text-fill: white;";
        } else {
            style += "-fx-background-color: #069c06; -fx-text-fill: white;";
        }
        label.setStyle(style);
        HBox container = new HBox(label);
        container.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        container.setAlignment(isUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

        chatContainer.getChildren().add(container);

        javafx.application.Platform.runLater(() -> textArea.requestFocus());
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

                        "La programación es el arte y la ciencia de crear secuencias de instrucciones (código) para que una computadora realice tareas específicas. " +
                                "Es el lenguaje que permite la automatización, el desarrollo de software y la resolución de problemas complejos mediante la lógica algorítmica. " +
                                "Al aprender a programar, dominas herramientas como Python, Java o C++ para transformar ideas en aplicaciones funcionales que impactan al mundo digital." +
                                "\n\nEn ciencias de cómputos aprendes a diseñar algoritmos, estructuras de datos y a construir software." +
                                "\n\nCursos típicos: Estructuras de Datos, Sistemas Operativos."
                ),

                new Doc("¿Qué es Ciencia de Datos?",

                        "Ciencias de datos integra la computación con la estadística matemática para extraer conocimiento " +
                                "estratégico de grandes volúmenes de datos (Big Data). El estudiante aprenderá a procesar " +
                                "información, identificar patrones mediante minería de datos y construir modelos predictivos " +
                                "fundamentales para la toma de decisiones en la industria tecnológica y científica." +
                                "\n\nIntereses: Datos, Estadística, Análisis, Visualización, Graficas, Predicciones."
                ),

                new Doc("¿Qué es IA / Machine Learning?",

                        "La Inteligencia Artificial se enfoca en el desarrollo de sistemas capaces de emular procesos " +
                                "cognitivos humanos. El currículo cubre desde redes neuronales y procesamiento de lenguaje " +
                                "natural hasta aprendizaje reforzado, permitiendo a los estudiantes diseñar soluciones " +
                                "autónomas y sistemas inteligentes de vanguardia." +
                        "\n\nIntereses: Matemáticas, Algoritmos, Robótica, IA generativa, Modelos, Redes Neuronales."
                ),

                new Doc("¿Qué es Ciberseguridad?",

                        "Dedicada a la protección de infraestructuras críticas, sistemas y datos. Esta área de estudio " +
                                "aborda la criptografía, la seguridad en redes, la auditoría de sistemas y la respuesta a " +
                                "incidentes. Los estudiantes se forman en la detección de vulnerabilidades y la implementación " +
                                "de protocolos de defensa robustos contra amenazas digitales globales." +
                        "\n\nIntereses: Seguridad, Hackers (éticos), Redes, Misterios, Criptografia, Proteccion."
                ),

                new Doc("¿Qué es Desarrollo de Videojuegos?",

                        "Esta disciplina integra la informática avanzada, la física computacional y la inteligencia " +
                                "artificial para la creación de entornos interactivos y sistemas de simulación complejos. " +
                                "El programa capacita al estudiante en el uso de motores de desarrollo líderes en la industria " +
                                "como Unity y Unreal Engine, profundizando en la optimización de recursos de hardware, " +
                                "arquitectura de sistemas multijugador y el diseño de mecánicas de juego innovadoras." +
                                "\n\nIntereses: Juegos, Creatividad, Storytelling, Física, Motores, Unity, Unreal. "
                ),

                new Doc("¿Qué es Ingeniería de Software?",
                        "Se centra en la aplicación de un enfoque sistemático y disciplinado al desarrollo, operación " +
                                "y mantenimiento de software de gran escala. El curso prepara al estudiante en metodologías " +
                                "ágiles, diseño de sistemas distribuidos y control de calidad, asegurando la creación de " +
                                "productos tecnológicos confiables y competitivos en el mercado global." +
                                "\n\nIntereses: Resolver Problemas, Trabajo En Equipo, Diseño De Sistemas, Software, Aplicaciones, Sistemas, Arquitectura, Backend, Frontend."
                ),

                new Doc("¿Qué es Desarrollo Web?",
                        "Se enfoca en la ingeniería de plataformas digitales modernas. Abarca tanto el desarrollo del " +
                                "lado del cliente (Frontend) como del servidor (Backend), profundizando en protocolos de " +
                                "comunicación, gestión de bases de datos y la arquitectura de aplicaciones en la nube para " +
                                "garantizar experiencias de usuario óptimas y seguras." +
                                "\n\nIntereses: Diseño, Creatividad, Emprendimiento, Web y apps, Paginas, Frontend, Backend, Fullstack."
                ),

                new Doc("¿Qué es Computación Gráfica y Realidad Virtual (VR/AR)",
                        "Esta especialidad combina matemáticas avanzadas y física para la generación de imágenes y " +
                                "entornos tridimensionales mediante computadora. Los estudiantes exploran el renderizado, " +
                                "la visualización científica y el desarrollo de sistemas de realidad virtual y aumentada " +
                                "aplicados a la ingeniería, la medicina y el entrenamiento especializado." +
                                "\n\nIntereses: Graficos, Animacion, Realidad Virtual, 3D, Arte Digital."

                )



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

                tfidf.fit(corpus.stream()
                        .map(d -> d.title + " " + d.text)
                        .collect(Collectors.toList()));

            }



            Doc bestMatch(String query) {

                Map<String, Double> qVec = tfidf.tfidfVector(query);

                double bestScore = -1;

                Doc best = null;

                for (int i = 0; i < docs.size(); i++) {

                    Map<String, Double> dVec = tfidf.tfidfVector(docs.get(i).title + " " + docs.get(i).text);
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

                add("estadistica", Map.of("CienciaDatos", 3, "IA/ML", 2, "graficas", 1));

                add("videojuegos", Map.of("Videojuegos", 4, "IA/ML", 2, "programación", 3));

                add("seguridad", Map.of("Ciberseguridad", 4, "Backend", 1));

                add("redes", Map.of("Ciberseguridad", 3, "Backend", 2));

                add("web", Map.of("Frontend", 3, "Backend", 3, "Fullstack", 3));

                add("datos", Map.of("CienciaDatos", 3, "IA/ML", 2));

                add("robotica", Map.of("IA/ML", 3));

                add("grafica", Map.of("Videojuegos", 3, "Frontend", 3));

                add("automatizacion", Map.of("IA/ML", 3, "Sistemas", 2, "Matemática", 3));

                add("3D", Map.of("VR/AR", 3, "Videojuegos", 3, "Graficas", 3));

                add("animacion", Map.of("Videojuegos", 3, "VR/AR", 3, "Graficas", 3));

                add("python", Map.of("CienciaDatos", 3, "IA/ML", 3, "Backend", 2));

                add("java", Map.of("Software", 3, "Backend", 3, "Programacion", 3));

                add("unity", Map.of("Videojuegos", 3, "VR/AR", 2, "Graficas", 3));

                add("sql", Map.of("Backend", 3, "CienciaDatos", 2, "Fullstack", 2));


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

                // Top 3 recomendaciones

                return acumulado.entrySet().stream()

                        .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))

                        .limit(3)

                        .map(e -> e.getKey() + " (puntuación: " + e.getValue() + ")")

                        .collect(Collectors.joining(" | "));

            }

        }

    }
}

