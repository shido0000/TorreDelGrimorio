package com.grimorio.torre;

/** Textos de la historia: capítulos con diálogos (100 % en español). */
public final class Story {

    public static class Line {
        public int who;       // retrato: 0 tú, 1 Mocha, 2 Aldric, 3 Orin, 4 Elara
        public String name;
        public String text;

        public Line(int who, String name, String text) {
            this.who = who; this.name = name; this.text = text;
        }
    }

    public static class Chapter {
        public String title;
        public int triggerWave;   // se muestra al llegar a esta oleada
        public Line[] lines;

        public Chapter(String title, int triggerWave, Line[] lines) {
            this.title = title; this.triggerWave = triggerWave; this.lines = lines;
        }
    }

    public static final Chapter[] CHAPTERS = {
            new Chapter("Capítulo 1: El Grimorio Codicioso", 1, new Line[] {
                    new Line(3, "Maestro Orin", "Aprendiz... el Reino descubre que el Grimorio Codicioso convierte la magia en oro. Vendrán a robarlo."),
                    new Line(0, "Tú (Aprendiz)", "¡Maestro, no puedo con todo el reino yo solo!"),
                    new Line(3, "Maestro Orin", "Por eso te lego lo único que importa: tu ingenio. Invoca monstruos, colócalos junto al camino y que cada caballero que toque la Torre pague en oro."),
                    new Line(1, "Mocha", "Miau. (Yo vigilo el cofre. Tú vigila las oleadas.)"),
                    new Line(0, "Tú (Aprendiz)", "Que vengan. La Torre del Grimorio no caerá... ¡y cada moneda robada será nuestra!"),
            }),
            new Chapter("Capítulo 2: Los estandartes del traidor", 10, new Line[] {
                    new Line(2, "Sir Aldric", "¿Recuerdas al discípulo favorito de Orin? Yo lo traicioné. Ahora soy el Rey Héroe... y ese Grimorio me pertenece."),
                    new Line(4, "Elara", "Capitana de sus ejércitos. Sus monstruos nos costaron caro, mi rey."),
                    new Line(2, "Sir Aldric", "Entonces enviemos caballeros de verdad. ¡Al asalto!"),
                    new Line(0, "Tú (Aprendiz)", "Mocha... sube de nivel a los monstruos. Esto se pone serio."),
            }),
            new Chapter("Capítulo 3: La capitana Elara", 20, new Line[] {
                    new Line(4, "Elara", "Impresionante muralla de colmillos y limos. Pero yo no fallo dos veces."),
                    new Line(1, "Mocha", "Miau-miau. (Sus flechas alcanzan desde lejos. Necesitamos ojos que vean lejos.)"),
                    new Line(0, "Tú (Aprendiz)", "Entonces invocaré al Ojo Místico. Que pruebe a esquivar lo que todo lo ve."),
            }),
            new Chapter("Capítulo 4: El oro es poder", 30, new Line[] {
                    new Line(2, "Sir Aldric", "¿Sabes por qué codicio el Grimorio? No por riqueza. Con él, quien posee el oro... posee a los héroes."),
                    new Line(0, "Tú (Aprendiz)", "Y por eso perderás: tú gastas oro en ejércitos. Yo lo invierto en mis monstruos."),
                    new Line(1, "Mocha", "Miau. (Y en pienso para gatos. Sobre todo en pienso.)"),
            }),
            new Chapter("Capítulo 5: El asalto final... ¿o no?", 40, new Line[] {
                    new Line(2, "Sir Aldric", "Basta de juegos. Yo en persona, y mi Guardia de Hierro detrás."),
                    new Line(3, "Maestro Orin", "Escucha mi último consejo, aprendiz: la Torre es fuerte cuando el cofre rebosa. Mantén el oro fluyendo, siempre."),
                    new Line(0, "Tú (Aprendiz)", "Gracias, maestro. ¡Vamos, monstruos míos! ¡Por Orin! ¡Por el Grimorio!"),
            }),
            new Chapter("Epílogo: La Torre eterna", 50, new Line[] {
                    new Line(2, "Sir Aldric", "Imposible... derrotado por limos y gatas..."),
                    new Line(0, "Tú (Aprendiz)", "No eres el primero ni el último, Aldric. La Torre seguirá aquí cuando tu nombre sea polvo."),
                    new Line(1, "Mocha", "Miau. (Fin del modo historia. Las oleadas infinitas... empiezan ahora.)"),
            }),
    };

    /** Capítulo que debe mostrarse al alcanzar la oleada n, o -1 si ninguno. */
    public static int chapterForWave(int wave, int storySeen) {
        for (int i = 0; i < CHAPTERS.length; i++) {
            if (storySeen <= i && CHAPTERS[i].triggerWave <= wave) return i;
        }
        return -1;
    }
}
