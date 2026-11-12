/// <summary>Textos de la historia: capítulos con diálogos (100 % en español).</summary>
public static class StoryData {
    public class Line {
        public int who; // 0 tú, 1 Mocha, 2 Aldric, 3 Orin, 4 Elara
        public string name, text;
        public Line(int who, string name, string text) { this.who = who; this.name = name; this.text = text; }
    }

    public class Chapter {
        public string title;
        public int triggerWave;
        public Line[] lines;
        public Chapter(string title, int triggerWave, Line[] lines) {
            this.title = title; this.triggerWave = triggerWave; this.lines = lines;
        }
    }

    public static readonly Chapter[] Chapters = {
        new Chapter("Capítulo 1: El Grimorio Codicioso", 1, new[] {
            new Line(3, "Maestro Orin", "Aprendiz... el Reino descubre que el Grimorio Codicioso convierte la magia en oro. Vendrán a robarlo."),
            new Line(0, "Tú (Aprendiz)", "¡Maestro, no puedo con todo el reino yo solo!"),
            new Line(3, "Maestro Orin", "Por eso te lego lo único que importa: tu ingenio. Invoca monstruos, colócalos junto al camino y que cada caballero que toque la Torre pague en oro."),
            new Line(1, "Mocha", "Miau. (Yo vigilo el cofre. Tú vigila las oleadas.)"),
            new Line(0, "Tú (Aprendiz)", "Que vengan. La Torre del Grimorio no caerá... ¡y cada moneda robada será nuestra!"),
        }),
        new Chapter("Capítulo 2: Los estandartes del traidor", 10, new[] {
            new Line(2, "Sir Aldric", "¿Recuerdas al discípulo favorito de Orin? Yo lo traicioné. Ahora soy el Rey Héroe... y ese Grimorio me pertenece."),
            new Line(4, "Elara", "Capitana de sus ejércitos. Sus monstruos nos costaron caro, mi rey."),
            new Line(2, "Sir Aldric", "Entonces enviemos caballeros de verdad. ¡Al asalto!"),
            new Line(0, "Tú (Aprendiz)", "Mocha... sube de nivel a los monstruos. Esto se pone serio."),
        }),
        new Chapter("Capítulo 3: La capitana Elara", 20, new[] {
            new Line(4, "Elara", "Impresionante muralla de colmillos y limos. Pero yo no fallo dos veces."),
            new Line(1, "Mocha", "Miau-miau. (Sus flechas alcanzan desde lejos. Necesitamos ojos que vean lejos.)"),
            new Line(0, "Tú (Aprendiz)", "Entonces invocaré al Ojo Místico. Que pruebe a esquivar lo que todo lo ve."),
        }),
        new Chapter("Capítulo 4: El oro es poder", 30, new[] {
            new Line(2, "Sir Aldric", "¿Sabes por qué codicio el Grimorio? No por riqueza. Con él, quien posee el oro... posee a los héroes."),
            new Line(0, "Tú (Aprendiz)", "Y por eso perderás: tú gastas oro en ejércitos. Yo lo invierto en mis monstruos."),
            new Line(1, "Mocha", "Miau. (Y en pienso para gatos. Sobre todo en pienso.)"),
        }),
        new Chapter("Capítulo 5: El asalto final... ¿o no?", 40, new[] {
            new Line(2, "Sir Aldric", "Basta de juegos. Yo en persona, y mi Guardia de Hierro detrás."),
            new Line(3, "Maestro Orin", "Escucha mi último consejo, aprendiz: la Torre es fuerte cuando el cofre rebosa. Mantén el oro fluyendo, siempre."),
            new Line(0, "Tú (Aprendiz)", "Gracias, maestro. ¡Vamos, monstruos míos! ¡Por Orin! ¡Por el Grimorio!"),
        }),
        new Chapter("Epílogo: La Torre eterna", 50, new[] {
            new Line(2, "Sir Aldric", "Imposible... derrotado por limos y gatas..."),
            new Line(0, "Tú (Aprendiz)", "No eres el primero ni el último, Aldric. La Torre seguirá aquí cuando tu nombre sea polvo."),
            new Line(1, "Mocha", "Miau. (Fin del modo historia. Las oleadas infinitas... empiezan ahora.)"),
        }),
        new Chapter("Capítulo 6: La flecha rota", 60, new[] {
            new Line(4, "Elara", "Alto... ¿qué estamos haciendo? He gastado mil flechas y tu torre sigue en pie."),
            new Line(0, "Tú (Guardián)", "¿Capitana Elara? ¿Vienes a rendirte?"),
            new Line(4, "Elara", "Vengo a advertirte: Aldric ha contratado a los Asedios Dorados. Mercenarios que no conocen el miedo."),
            new Line(1, "Mocha", "Miau-miau. (El portal escupe criaturas nuevas: un pingüino con casco. Se hace llamar Pengüi, caballero del hielo eterno.)"),
            new Line(0, "Tú (Guardián)", "Pengüi, acepto tu espada... o tu pico. Los Asedios Dorados descubrirán que el hielo también corta."),
        }),
        new Chapter("Capítulo 7: El asedio de las cien banderas", 70, new[] {
            new Line(2, "Sir Aldric", "¡Cien banderas! Cada casa noble del reino marcha contra tu torre, aprendiz."),
            new Line(0, "Tú (Guardián)", "Cien banderas, cien tesoros para mis monstruos."),
            new Line(2, "Sir Aldric", "¿No lo entiendes? ¡Es el fin!"),
            new Line(0, "Tú (Guardián)", "No. Es la oleada 70. Las otras 69 también decían que era el fin."),
        }),
        new Chapter("Capítulo 8: El Grimorio susurra", 80, new[] {
            new Line(3, "Maestro Orin", "Aprendiz... ¿lo oyes? El Grimorio despierta. Cada moneda que defiendes lo hace más fuerte... y más hambriento."),
            new Line(0, "Tú (Guardián)", "¿Hambriento?"),
            new Line(3, "Maestro Orin", "El oro llama al oro. Pronto querrá más. Prométeme que nunca lo alimentarás con lo que amas."),
            new Line(3, "Maestro Orin", "Y escucha... una de las almas atrapadas en el Grimorio ha despertado: Espectro. Susurra maldiciones a quien toque tu cofre."),
            new Line(0, "Tú (Guardián)", "Bienvenido, Espectro. El Grimorio da, el Grimorio quita... y hoy, da un aliado."),
            new Line(1, "Mocha", "Miau... (Yo no como oro. Bueno... casi nunca.)"),
        }),
        new Chapter("Capítulo 9: El precio del oro", 90, new[] {
            new Line(2, "Sir Aldric", "Última oferta: dime el secreto del Grimorio y te doy la mitad del reino."),
            new Line(0, "Tú (Guardián)", "El secreto no es el oro, Aldric. Es lo que proteges con él. Tú proteges tu codicia; yo protejo a mi familia."),
            new Line(1, "Mocha", "Miau. (Familia soy yo. Por supuesto.)"),
            new Line(0, "Tú (Guardián)", "Vuelve con tus héroes si quieres. La torre estará aquí."),
        }),
        new Chapter("Capítulo 10: El Invocador Coronado", 100, new[] {
            new Line(2, "Sir Aldric", "Basta... me rindo. ¿De qué sirve una corona de héroe si no quedan héroes que la quieran llevar?"),
            new Line(4, "Elara", "El rey ha caído... de rodillas. La torre gana."),
            new Line(0, "Tú (Guardián)", "Aquí no hay rey ni aprendiz, Aldric. Solo un invocador y sus monstruos: Pengüi, Espectro y hasta la Quimera del portal final. Quédate como aliado... o vete en paz."),
            new Line(1, "Mocha", "Miau miau. (Modo historia completado. Oleadas infinitas: desbloqueadas. Ahora, ¿me das atún?)"),
            new Line(3, "Maestro Orin", "Bien hecho, aprendiz. Ya no eres aprendiz... eres el Guardián del Grimorio."),
        }),
    };

    public static int ChapterForWave(int wave, int storySeen) {
        for (int i = 0; i < Chapters.Length; i++)
            if (storySeen <= i && Chapters[i].triggerWave <= wave) return i;
        return -1;
    }
}
