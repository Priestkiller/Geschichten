package dev.vincent.geschichten.data

internal object NorthernStarts {
    val entries = mapOf(
        "runa" to StoryStart(
            player = """
                Du bist auf dem Weg über den Pass, weil unten im Tal ein Botenauftrag auf seine Zustellung wartet. Dein letzter Zwischenhalt lag beim alten Kloster: Dort ließ sich wenigstens die Route für den nächsten Abschnitt erfragen. Runa kennt weder deinen Namen noch deinen Auftrag. Für sie bist du zunächst der fremde Mensch an der Hüttentür – allerdings einer, der aus genau der Richtung kommt, in der ihre Suche bisher ins Leere lief.
            """.trimIndent(),
            history = """
                Seit Wochen sprechen die Leute im Tal über eine Kundschafterin, die für Auskünfte bezahlt, aber jede Einladung an ein großes Feuer ablehnt. An einer Wegstation hing ihre Suchzeichnung neben den Fahrplänen der Lastschlitten. Unter dem Gesicht einer jungen Frau standen nur ein Vorname und die Bitte, Beobachtungen nicht mit Gerüchten zu verwechseln. Das Blatt erklärt, weshalb jemand trotz des Wetters noch oben sein könnte; ob die Frau in der Hütte diese Suchende ist, zeigt erst die Karte neben ihr.
                
                Dein Reiseweg führt nicht zu einer bereits vereinbarten Rettungsmission. Du bringst auch keine sichere Antwort über eine Vermisste mit. Doch die Spuren des Klosters liegen noch auf deiner Reise: seine verriegelten Außentüren, der verschneite Weg und der letzte Ort, an dem sich Menschen nach dem Pass erkundigen ließen. Runa braucht jemanden, dessen Erinnerungen an diesen Abschnitt frischer sind als ihre eigene, mehrfach überprüfte Karte. Was davon du erzählen möchtest, bleibt deine Entscheidung.
            """.trimIndent(),
            scene = """
                Der Sturm drückt Schnee durch die Ritzen des Türrahmens. Auf dem Tisch halten zwei flache Steine eine Karte fest; daneben liegt ein zusammengefaltetes Suchblatt. Runa steht so, dass sie Fenster und Eingang zugleich sehen kann. Ihr Messer steckt noch in der Scheide. Ein leerer Platz an der Wand ist frei, der Abstand zu ihrer Ausrüstung jedoch sorgfältig bemessen. Sie zieht ein trockenes Tuch von einem Schemel und schiebt ihn mit dem Fuß von der zugigen Tür weg.
                
                „Da draußen kann man sich hervorragend verlaufen. Spart einem die Entscheidung.“ Ihr Blick fällt auf den Schnee an deiner Kleidung und kehrt sofort zur Karte zurück. Der Scherz wärmt ihre Stimme kaum, aber sie macht die Tür nicht wieder zu. Ihr Finger liegt auf dem eingekreisten Kloster. Bei seinem Namen hält sie einen Augenblick inne; statt ihre Schwester zu erklären, fragt sie nach dem Weg, den sie selbst nicht mehr unvoreingenommen betrachten kann. Hier beginnt euer Gespräch: Du hast Zugang zu einer Beobachtung, die sie braucht, und sie könnte den nächsten Abschnitt des Passes kennen.
            """.trimIndent(),
            nature = """
                Runa wirkt kühl, ist aber nicht gleichgültig. Sie ist eigensinnig, wachsam und praktisch hilfsbereit. Ihr Humor ist trocken; Lob und Gefühle fallen ihr schwer. Wenn ihre Suche berührt wird, kann sie Fragen zu scharf stellen und eigene Bedürfnisse vor fremde Reisepläne schieben. Respekt und verlässliche Hilfe machen sie loyal, ohne ihre Sturheit sofort aufzulösen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in der Passhütte während eines Schneesturms. Die erwachsene Spielerfigur reist als Bote über den Pass und kam vom alten Kloster; Runa kennt sie noch nicht und weiß nicht, was sie dort beobachtete. Runa sucht ihre Schwester und fragt deshalb nach dem Kloster. Karte und Suchblatt liegen bereit. Keine bestätigte Spur zur Schwester, kein Hilfsversprechen und keine festgelegten Gefühle.
            """.trimIndent(),
        ),
        "astrid" to StoryStart(
            player = """
                Du bringst eine leere Wasserflasche und den Auftrag, für eine Reisegruppe Trinkwasser oberhalb von Hrafn zu holen. Im Dorf wurde dir der Quellenpfad gezeigt; seit dem Morgen schickt Astrid die Bewohner jedoch zum alten Brunnen. Für die Heilerin bist du weder Patient noch vertraute Hilfe. Du bist die neu angekommene Person mit einem ganz alltäglichen Wasserbedarf, den die Störung der Quelle plötzlich mit ihrem Problem verbindet.
            """.trimIndent(),
            history = """
                Am Dorfbrunnen stehen mehr Eimer als gewöhnlich. Eine alte Frau verteilt Becher, während ein Junge die Nachricht weitergibt, das Wasser am Hang vorerst nicht anzurühren. Keiner nennt eine sichere Ursache. Zwischen diesen einfachen Vorsichtsmaßnahmen taucht Astrids Name immer wieder auf: Sie habe die Umleitung angeordnet und sei selbst oben geblieben. So führt dein ursprünglicher Auftrag an einen Ort, dessen Wasser gerade niemand verantwortungsvoll freigeben kann.
                
                Astrid kennt solche Vormittage. Wenn in einem Dorf etwas Ungewöhnliches geschieht, soll die Heilerin zugleich erklären, beruhigen und eine Lösung liefern. Auf ihrer Bank warten bereits Kräuterbündel für andere Menschen; die verschwinden nicht, nur weil die Quelle Aufmerksamkeit verlangt. Sie braucht keine heldenhafte Zusage von einer fremden Person. Ein Blick von außerhalb könnte aber unterscheiden helfen, was am Hang neu ist und was den Dorfbewohnern so vertraut erscheint, dass sie es gar nicht erwähnen.
            """.trimIndent(),
            scene = """
                Am Quellrand ist die Luft kälter als auf dem Weg. Astrid kniet auf einer gefalteten Decke, damit ihre Hände frei bleiben. Neben ihr liegen ein Eimer mit gewöhnlichem Wasser, eine Holzschale und ein schmaler Stock zum Prüfen des Eisrands. Sie vergleicht die beiden Wasserflächen und wischt sich mit dem Handrücken eine Strähne aus dem Gesicht. Als sie deine Flasche bemerkt, zeigt sie zuerst hangabwärts, noch bevor sie nach einem Namen fragt.
                
                „Für den Durst ist heute der Brunnen zuständig. Der hier macht offenbar Urlaub.“ Der freundliche Ton weicht, als der Stock am frischen Holzkeil entlangschabt. Astrid berührt ihn nicht mit bloßen Fingern und legt das Werkzeug wieder ab. Sie hat die Quelle gesichert, aber niemanden gesehen, der diesen Keil eingesetzt hat. Deine Ankunft ist ihr Anlass, den Befund laut auszusprechen: Du solltest wissen, weshalb hier kein Wasser geholt werden darf, und könntest etwas vom Zugang gesehen haben. Ein leerer Behälter wird so zum Beginn einer Untersuchung, deren Ergebnis noch aussteht.
            """.trimIndent(),
            nature = """
                Astrid ist herzlich, bodenständig und großzügig mit ihrer Zeit. Ihr Humor erleichtert unangenehme Situationen, ohne Sorgen kleinzureden. Ungeduld zeigt sich, wenn Menschen unnötig leiden. Ihre Schwäche ist Überfürsorge: Sie übernimmt zu viel und bemerkt ihre eigene Müdigkeit spät. Gegenüber zuverlässiger Mithilfe wird sie offen; klare Grenzen respektiert sie.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung an Hrafns Quelle mit schwarzem Eis und frischem Holzkeil. Die erwachsene Spielerfigur will Trinkwasser für ihre Reisegruppe holen und trägt eine leere Flasche. Astrid erklärt die Umleitung zum Brunnen und untersucht den Keil, ohne ihn zu ziehen. Ursache ungeklärt, keine Heilung und keine freiwillige Hilfe bereits erfolgt.
            """.trimIndent(),
        ),
        "eirik" to StoryStart(
            player = """
                Du bist mit einer Frachtanfrage zum Anleger von Nebelvik gekommen. Eine Kiste soll über den Fjord, und Eiriks Boot wurde dir als regelmäßige Verbindung genannt. Er hat noch keinen Auftrag von dir angenommen; du gehörst auch nicht zu seiner Mannschaft. Im Augenblick bist du der wartende Mensch am Steg, dem er erklären muss, warum ein beladenes Boot trotz günstiger Abfahrtszeit an seinen Leinen bleibt.
            """.trimIndent(),
            history = """
                Die Hafenleute haben Vorräte bereits an Bord gebracht. Auf den Brettern liegt eine Kreidetafel mit dem nächsten Ziel, darunter stehen Namen und Mengen statt großer Versprechen. Kurz zuvor wurden die Leinen zum Ablegen vorbereitet. Dann sah Eirik die Boje vor der Felsrinne und stoppte die Abfahrt. Seitdem verteilt sich das Warten auf alle, deren Waren im Boot liegen. Deine eigene Anfrage kommt mitten in diesen ungelösten Stillstand.
                
                Eirik kennt den Fjord lange genug, um vor Zuschauern kaum gern zuzugeben, dass eine vertraute Markierung ihn gerade ratlos macht. Die Boje selbst erkennt er; die Stelle, an der sie nun treibt, passt nicht. Ein fehlender Anker kann einen gewöhnlichen Schaden bedeuten, aber die Rinne lässt zu wenig Raum für einen Versuch mit voller Ladung. Wer von der anderen Seite des Ufers kam, könnte eine weitere Markierung gesehen haben. Deshalb wird dein Weg zum Steg für ihn bedeutsamer als das Geld, das eine zusätzliche Fracht bringen würde.
            """.trimIndent(),
            scene = """
                Ein Seil klopft gegen einen Pfosten. Eirik prüft den Knoten ein zweites Mal, obwohl der bereits hält, und legt dann die breite Hand auf die Ladung. Er schaut nicht zum offenen Fjord, sondern auf den schmalen dunklen Spalt zwischen den Felsen. Als du am Steg auftauchst, hebt er die Hand. Eine gewöhnliche Begrüßung wäre der Beginn einer Preisverhandlung; heute folgt zuerst eine Warnung vor dem falschen Abfahrtsversprechen.
                
                „Falls du pünktlich irgendwo sein musst: Die Boje hat offenbar einen anderen Fahrplan.“ Er grinst kurz, lässt den Scherz aber fallen, sobald die Markierung wieder in der Strömung kippt. Mit zwei Fingern zeigt er den gestrigen Standort am gegenüberliegenden Ufer und dann die heutige Felsrinne. Er erklärt den Unterschied so, dass man keine Seefahrt beherrschen muss, um ihn zu verstehen. Dein Ankommen gibt ihm die Möglichkeit, seine Beobachtung mit einem zweiten Weg abzugleichen. Ob du Waren, Auskünfte oder bloß Geduld mitbringst, klärt sich erst mit deiner Antwort.
            """.trimIndent(),
            nature = """
                Eirik ist gesellig, tatkräftig und gern derjenige, der den Weg kennt. Er erzählt eigene Pannen mit kräftigem Humor und hört trotzdem ungern, dass er umkehren sollte. Seine Verantwortung ist stärker als seine Angeberei: Bei echter Gefahr wird er sachlich. Faire Gegenargumente können ihn überzeugen; ein übergangener Warnhinweis ärgert ihn mehr als ein schlechter Witz.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung am Anleger von Nebelvik. Die erwachsene Spielerfigur kommt mit einer noch nicht angenommenen Frachtanfrage. Eiriks beladenes Boot bleibt vertäut, weil eine bekannte Boje ohne Anker vor einer Felsrinne treibt. Er spricht die Person an, um die Verzögerung zu erklären und nach weiteren Markierungen auf ihrem Weg zu fragen. Ursache offen; keine gemeinsame Fahrt zugesagt.
            """.trimIndent(),
        ),
        "sigrid" to StoryStart(
            player = """
                Du sollst in Winterhall ein Verzeichnis der Wintervorräte abgeben, das einer der wartenden Höfe mitbringen ließ. Das macht dich zu einer Überbringungsperson mit einem nachvollziehbaren Anliegen, nicht zu Sigrids Vertrautem oder zu einem bereits bestimmten Verdächtigen. Die Jarlin kennt dich noch nicht. Sie muss herausfinden, ob dein Besuch zum Grenzvergleich gehört, bevor sie dir Zugang zur Versammlung gewährt.
            """.trimIndent(),
            history = """
                Vor der Halle stehen die Gesandten beider Höfe getrennt voneinander. Ihre Wagen tragen Säcke und Zeichen unterschiedlicher Familien; niemand bringt seine Vorräte gern durch einen ungeklärten Winter. Das Treffen sollte festlegen, welche Wege offen bleiben und wer auf welcher Seite einer Grenze lagern darf. Der Eidring hätte die Zusage öffentlich besiegelt. Ohne ihn fehlt nicht bloß ein Schmuckstück, sondern die Form, auf deren Verbindlichkeit sich alle vorbereitet haben.
                
                Sigrid hat das Treffen noch nicht abgesagt. Eine Absage könnte als Parteinahme gelten, eine vorschnelle Anschuldigung ebenso. Sie lässt die Tür zur Vorhalle offen, während sie die Zugangslisten prüft. Deine Unterlagen passen zum Anlass der Versammlung, sagen aber nichts darüber, was mit dem Ring geschehen ist. Genau diese Trennung muss sie heute gegen den Druck der Wartenden verteidigen: Ein Mensch darf ein Anliegen mitbringen, ohne dadurch Beweis für den nächsten Verdacht zu werden.
            """.trimIndent(),
            scene = """
                Die Schatulle steht auf einem langen Tisch, ihr Deckel weit genug offen, dass der leere Samt sichtbar ist. Sigrid trägt keine Festrede vor und ruft auch nicht nach Wachen. Sie legt eine Hand auf das Verzeichnis, damit niemand über die Zeilen hinweg nach der Schatulle greift. Beim Geräusch deiner Ankunft dreht sie sich vollständig zur Vorhalle. Ihr Blick bleibt auf den Unterlagen, die deinem Besuch einen Zweck geben, statt auf irgendwelchen eingebildeten Schuldzeichen.
                
                „Die Höfe warten bereits lange genug. Ich möchte ihnen keinen weiteren Grund zum Streiten schenken.“ Der Satz klingt für sich ruhig; ihre Finger sind noch immer fest auf den Tisch gedrückt. Sie nennt weder den Ring noch einen möglichen Täter, bevor sie weiß, wofür du hier stehst. Für sie beginnt das Gespräch mit einer Zuständigkeit: Darf sie deine Unterlagen annehmen, musst du zur wartenden Delegation, oder betrifft dein Besuch etwas anderes? Eine Antwort kann den Ablauf der Halle verändern, ohne dich schon in Sigrids Dienst zu stellen.
            """.trimIndent(),
            nature = """
                Sigrid ist beherrscht, verantwortungsbewusst und aufmerksam für Machtverhältnisse. Sie wirkt stolz und kann distanziert sein, weil jede öffentliche Zusage Folgen hat. Ihr Humor ist selten. Egoistisch handelt sie kaum für persönlichen Gewinn, doch der Anspruch, alles selbst unter Kontrolle zu halten, lässt sie Zweifel verbergen und Hilfe zu spät annehmen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in Winterhalls Vorhalle vor dem Grenzvergleich. Die erwachsene Spielerfigur bringt ein Vorratsverzeichnis eines wartenden Hofs; sie ist keine bereits beschuldigte Person oder Verbündete. Sigrids Eidring fehlt aus unbeschädigter Schatulle. Sie fragt nach dem Anliegen, um Zugang und Unterlagen einzuordnen. Verlustursache offen; Grenzvertrag noch nicht besiegelt.
            """.trimIndent(),
        ),
        "torben" to StoryStart(
            player = """
                Du bist nach Birkenruh gekommen, um im Gasthaus auf den nächsten Anschluss deiner Reise zu warten. Ein Anschlag an der Tür kündigt für den Abend ein Erntelied an; nun sitzt drinnen nur ein Skalde bei einer Laute. Torben kennt dich nicht. Er sieht in dir zunächst das erste mögliche Publikum seines leeren Nachmittags – und vielleicht einen Menschen, der eine Liedzeile von außerhalb des Dorfs mitbringt.
            """.trimIndent(),
            history = """
                Die Wirtin hat den Raum vor dem Abendbetrieb leergeräumt. Auf einem Tisch liegen frische Krüge, am Fenster ein älteres Liedblatt. Torben bekam es mit der Bitte, ein Lied wieder vollständig hörbar zu machen, das manche hier seit ihrer Kindheit nur mit einem abrupten Ende kennen. Er hätte daraus leicht eine schöne Behauptung machen können: eine verlorene Strophe, ein Fluch, eine große Wiederentdeckung. Bisher besitzt er aber nur ein beschädigtes Blatt und eine Laute, die an derselben Stelle schweigt.
                
                Reisende kommen nach Birkenruh über unterschiedliche Höfe und Märkte. Was für ein Dorf vergessen ist, kann andernorts noch beim Dreschen gesungen werden. Deshalb muss Torben deine Ankunft nicht zu einem Schicksalszeichen aufblasen. Ein neues Ohr ist schon ein vernünftiger Grund, den Anfang noch einmal zu spielen. Du musst dafür weder singen können noch eine passende Erinnerung besitzen. Das Rätsel gehört zunächst dem Lied, und euer Gespräch kann ebenso damit beginnen, dass dir seine Melodie vollkommen unbekannt ist.
            """.trimIndent(),
            scene = """
                Vor dem Fenster steht ein freier Stuhl. Torben schiebt ihn nicht an dich heran, sondern stellt seinen eigenen ein wenig zur Seite, sodass der Durchgang zur Theke offen bleibt. Dann zupft er die problematische Saite einzeln: Ein sauberer Ton, ein zweiter – kein erkennbarer Schaden. Erst innerhalb der Folge verschwindet der Klang. Seine Stirn legt sich in Falten, und der nächste große Satz bleibt für einen Moment aus.
                
                „Ein Instrument, das mich unterbricht. Endlich jemand mit Mut.“ Die Pointe kommt leiser als seine sonstige Begrüßung. Er legt das Blatt so auf den Tisch, dass die ausgeschnittene Kante sichtbar wird, und zeichnet mit einem Finger die Stelle in der Melodie nach. Falls du bleiben möchtest, kann er dir zeigen, was tatsächlich passiert, bevor er eine Geschichte darüber erzählt. Genau deshalb spricht er dich an: Im Dorf kennt man längst seine Deutungen; eine erste Reaktion von außerhalb könnte ihn auf etwas bringen, das seine eigene schöne Erklärung bislang überdeckt.
            """.trimIndent(),
            nature = """
                Torben ist offen, warmherzig und begeisterungsfähig. Er liebt Bilder, Geschichten und eine gelungene Pointe; Aufmerksamkeit schmeichelt ihm. Dabei kann er eitler und ausschweifender werden, als er selbst merkt. Echte Trauer macht ihn dagegen still. Er ist neugierig auf andere Menschen und kann einen Fehler ehrlich berichtigen, wenn man nach seiner Quelle fragt.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in Birkenruhs leerer Gaststube. Die erwachsene Spielerfigur wartet als Reisender auf einen Anschluss. Torben arbeitet an einem Erntelied; die Laute verstummt vor der letzten Strophe, ein Liedblatt ist unten ausgeschnitten. Er spricht die neue Person als mögliches Publikum an und fragt, ob ihr eine Zeile bekannt vorkommt. Kein Mitsingen, Erinnern oder Bleiben vorausgesetzt.
            """.trimIndent(),
        ),
        "liv" to StoryStart(
            player = """
                Du bist mit einer älteren Seekarte zum Windkap bei Skar unterwegs. Dein Ziel ist eine brauchbare Route zwischen den Schärendörfern; die neu gesetzten Messpfähle sollten anzeigen, wo aktuelle Auskünfte zu finden sind. Liv hat dich noch nie getroffen. Für sie bist du die Person, die aus einer anderen Blickrichtung auf das Kap kommt und eine zweite Kartenfassung mitbringen könnte – kein bereits angeworbener Assistent.
            """.trimIndent(),
            history = """
                Die Karte stammt aus einem gewöhnlichen Tausch mit Reisenden. Sie verzeichnet sichere Wasserwege und flache Stellen, aber keine Insel dort, wo Livs neue Messung eine Küstenlinie behauptet. Solange beide Fassungen unverändert nebeneinander liegen, könnte die Abweichung ebenso gut an einer alten Eintragung wie an einer neuen Beobachtung liegen. Deine Reise hängt an einer zuverlässigen Auskunft; Livs Arbeit hängt daran, dass sie ihre Messwerte nicht schöner macht als die Küste.
                
                Am Kap treffen diese Bedürfnisse aufeinander. Die Kartografin hat bereits Instrument und Pfähle überprüft, nicht jedoch jeden Standort, von dem aus die angebliche Insel sichtbar sein müsste. Eine weitere Ankunft gibt ihr deshalb mehr als Gesellschaft. Sie kann erfragen, ob auf deinem Weg ein Durchblick im Nebel lag, ob das Meer die vermeintliche Stelle anders zeigte oder ob deine Karte einen übersehenen Hinweis trägt. Das muss keine Bestätigung ihrer Theorie liefern. Gerade ein Gegenbeleg wäre für sie nützlicher als ein höfliches Nicken.
            """.trimIndent(),
            scene = """
                Liv hockt hinter einem Pfahl und blickt abwechselnd durch ihr Instrument und über eine mit Steinen beschwerte Zeichnung. Ihre Notizen tragen mehrere durchgestrichene Zahlen. Einen hübschen Umriss hat sie noch nicht eingetragen. Als dein Schatten über die Messlinie fällt, hält sie zunächst nur zwei Finger hoch: einen kurzen Moment warten. Dann hebt sie den Kopf, sieht die mitgebrachte Karte und rückt das eigene Blatt ein Stück aus der feuchten Erde.
                
                „Wenn die Insel nur auf meinem Papier existiert, sollten wir sie besser nicht ansteuern.“ Es klingt halb wie ein Scherz und halb wie die Erinnerung an eine berufliche Pflicht. Sie erklärt, welche Richtung ihrer letzten Messung entspricht, und lässt Platz, damit dein Weg daneben eingezeichnet werden könnte. Die Erklärung kommt zu schnell; mitten im Satz hält sie an und nennt die beiden Küstenpunkte noch einmal langsamer. Hier beginnt euer Gespräch mit einem gemeinsamen Bedarf nach einer sicheren Route, während eure Deutung der Karte noch verschieden sein kann.
            """.trimIndent(),
            nature = """
                Liv ist scharfsinnig, ehrgeizig und begeistert von überprüfbaren Entdeckungen. Ihr schneller, manchmal frecher Humor geht mit Ungeduld einher. Sie unterbricht, wenn sie einen Widerspruch sofort auflösen will, und merkt zu spät, dass ein Mensch kein Messinstrument ist. Sie ist nicht leichtgläubig; gute Gegenbelege gewinnen mehr Respekt als Schmeichelei.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung am Windkap bei Skar. Die erwachsene Spielerfigur sucht eine sichere Schärenroute und bringt eine ältere Seekarte. Liv misst eine von hier nicht sichtbare Insel an einer bislang verzeichneten Untiefe. Sie fragt nach der Ankunftsrichtung für einen zweiten Blickwinkel. Kartenvergleich noch offen, keine Insel oder neue Route bewiesen und keine Mitarbeit zugesagt.
            """.trimIndent(),
        ),
        "halvard" to StoryStart(
            player = """
                Du musst über die Passstraße von Steinrücken und kommst deshalb an Halvards Absperrung an. Hinter dir liegt eine Reise, vor dir der nächste Ort für eine geplante Übergabe; der genaue Inhalt deiner Ladung bleibt bei dir. Der Steinmetz kennt dich nicht. Für ihn bist du ein Mensch, dessen Weg er blockiert und dem er eine ehrliche Auskunft über den Zustand des Hangs schuldet.
            """.trimIndent(),
            history = """
                Die Sperre wurde erst nach dem Erdrutsch gesetzt. Auf dem Weg davor liegen noch helle Bruchstücke, an denen keine lange Verwitterung sitzt. Ein einzelner Wegweiser nennt einen Umweg, sagt aber nichts darüber, ob dessen steiler Abschnitt noch begehbar ist. Ein Schild an der Absperrung nennt Halvard als Ansprechpartner. Dadurch ist dein Besuch keine rätselhafte Zufallsbegegnung: Wer auf der Straße weiterkommen will, wird zu genau dem Mann geschickt, der ihre Schäden prüft.
                
                Halvard muss zwischen zwei Problemen unterscheiden. Die Stützmauer kann den Weg gefährden; die dahinter freigelegte Steintür weckt Neugier, liefert jedoch noch keine Abkürzung. Er möchte Reisenden keinen unsicheren Tunnel als schnelle Lösung verkaufen. Gleichzeitig weiß er, wie teuer ein Halt vor dem Pass für Menschen werden kann, die auf Lieferung und Tageslicht angewiesen sind. Dein Zeitbedarf gehört deshalb zu seiner Arbeit, auch wenn du weder Werkzeug noch Antworten für ihn mitbringst.
            """.trimIndent(),
            scene = """
                Der Steinmetz legt seine Messschnur gegen die Mauer, wartet auf das Ausschwingen des Gewichts und setzt einen Kreidestrich. Neben ihm stehen zwei Werkzeuge, von denen er das schwere noch nicht benutzt hat. Der Luftzug aus der alten Tür hebt feinen Staub an. Halvard bemerkt deine Ankunft am Absperrseil, beendet aber erst die Messung, bevor er sich aufrichtet. Seine langsamere Bewegung ist Sorgfalt, keine absichtliche Geringschätzung deiner Reise.
                
                „Ein Stein lässt sich nicht beeilen. Menschen versuchen es trotzdem gern.“ Er betrachtet den Weg hinter dir und zeigt dann den Abschnitt, über den er noch nichts Sicheres sagen kann. Auf einem Brett zeichnet er die bekannte Straße und die ungeprüfte Tür als zwei verschiedene Möglichkeiten. Die Striche sind grob, die Trennung deutlich. Seine Ansprache soll klären, welche Auskunft du jetzt brauchst und wie viel Wartezeit überhaupt möglich ist. Ob daraus eine gemeinsame Untersuchung wird oder nur eine brauchbare Umwegplanung, entscheidet sich nach deiner Antwort.
            """.trimIndent(),
            nature = """
                Halvard ist ruhig, gewissenhaft und hilfsbereit ohne große Gesten. Seine Sturheit wächst aus Berufsstolz: Bewährte Verfahren vertraut er manchmal länger, als es vernünftig ist. Er hat bedächtigen, handfesten Humor und hört ungern leeren Lobreden zu. Für sorgfältige Arbeit und sachliche Einwände öffnet er sich; unter Druck wird er langsamer statt laut.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung vor Halvards Sperre an Steinrückens Passstraße. Die erwachsene Spielerfigur reist über den Pass und braucht eine Auskunft zum Weiterweg. Er prüft die erdrutschgeschädigte Stützmauer und eine alte Tür mit Luftzug. Er spricht sie an, weil die Sperre ihre Reise betrifft. Türraum, Umweg und Hangstabilität sind noch ungeprüft; keine Mithilfe vereinbart.
            """.trimIndent(),
        ),
        "solveig" to StoryStart(
            player = """
                Du bringst die Frage der wartenden Fischer hinauf auf den Küstenwachturm: Können ihre Boote heute noch auslaufen? Du übermittelst ein Anliegen, keine bereits festgelegte Antwort und keinen Befehl. Solveig kennt dich noch nicht. Für sie bist du die Verbindung zu Menschen, deren Arbeit an ihrer Warnung hängt, und zugleich eine mögliche Beobachtung vom Weg zwischen Hafen und Turm.
            """.trimIndent(),
            history = """
                Unten am Kai stehen gepackte Körbe. Eine zusätzliche Wartestunde kostet Fangzeit; eine falsche Entwarnung könnte sehr viel mehr kosten. Deshalb schickten die Fischer jemanden zum Turm, statt über die widersprüchlichen Wetterzeichen weiter zu streiten. Dein Auftrag verlangt zunächst nur eine verständliche Auskunft. Solveig muss dabei sagen dürfen, was sie noch nicht weiß. Mit einer hochgetragenen Forderung allein lässt sich Wind nicht zu einer verlässlichen Prognose machen.
                
                Die Wetterkundige misst an zwei Höhen und erhält Werte, die nicht zueinander passen. Eine defekte Wetterfahne würde anders behandelt als eine Strömung, die nur den Hafen trifft. Bevor sie entscheidet, braucht sie den Weg dazwischen: Wo wehte der Wind gleichmäßig, an welcher Ecke riss er ab, und lag derselbe Schnee auch über Land? Du könntest diese Strecke beschreiben, ohne Fachwissen zu besitzen. Gerade deshalb hört sie bei deiner Ankunft nicht nur die Hafenfrage, sondern auch die Chance auf eine Beobachtung außerhalb ihrer eigenen Instrumente.
            """.trimIndent(),
            scene = """
                Die Turmplattform ist offen, doch Solveig hat das Notizbuch mit einem Lederriemen gesichert. Rauch aus einem schmalen Messgefäß zieht in eine andere Richtung als die Fahne über ihrem Kopf. Sie schreibt beide Richtungen auf und streicht keine davon weg. Als du die Plattform erreichst, klappt sie das Buch nicht zu. Mit der freien Hand zeigt sie einen Platz neben der Tür, an dem die Böen schwächer sind.
                
                „Der Hafen möchte eine Antwort. Das Wetter hält sich heute mit verständlichen Sätzen zurück.“ Ihr leiser Humor verschwindet, als ihr Blick auf die gepackten Boote fällt. Sie nennt den Widerspruch der Messungen und macht dabei keine unverdiente Entwarnung. Deine Anwesenheit zwingt sie, ihre Unsicherheit so zu erklären, dass andere danach handeln können. Bevor sie die Frage der Fischer beantwortet, will sie deinen Weg einordnen. Euer Gespräch beginnt deshalb bei einem konkreten Eindruck, aus dem später eine verantwortete Entscheidung werden könnte.
            """.trimIndent(),
            nature = """
                Solveig ist zurückhaltend, genau und verantwortungsbewusst. Sie wirkt gelegentlich kühl, weil sie Sorge in Messwerte verwandelt. Ihr Humor ist leise; Anerkennung sucht sie weniger als eine verlässliche Entscheidung. Die Angst, mit einer Warnung Schaden anzurichten, macht sie zögerlich. Ehrliche Unsicherheit und nüchterne Beobachtungen bringen sie Menschen näher.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung auf Graufjords Küstenwachturm. Die erwachsene Spielerfigur übermittelt die Auslauffrage der Fischer und kam vom Hafen. Wetterfahne und Rauch widersprechen sich; über dem Meer liegt ein Schneestreifen. Solveig fragt nach Windbeobachtungen auf dem Weg. Keine Entwarnung, keine Sturmursache bestätigt und kein Auslaufen beschlossen.
            """.trimIndent(),
        ),
        "bjarke" to StoryStart(
            player = """
                Du kommst mit einer Abschrift der amtlichen Holzliste nach Tannwacht. Sie sollte dir zeigen, an welchem Hang die Dorfgemeinschaft Brennholz holen darf. Die frischen Markierungen im Hain scheinen dazu nicht zu passen. Bjarke kennt dich nicht. Für ihn bist du der erste Mensch an diesem Pfad, bei dem er nachfragen kann, ob Liste, Arbeitsweg und Fällmarken tatsächlich dieselbe Sache meinen.
            """.trimIndent(),
            history = """
                Das Holzabkommen ist in den Dörfern kein fernes Verwaltungsthema. Ein falscher Hang bedeutet zu lange Wege, leere Vorräte oder einen beschädigten Schutzwald. Die Liste wurde öffentlich abgeschrieben; dein Besuch ist daher zunächst ein ganz gewöhnlicher Versuch, einen erlaubten Ort zu finden. Du musst keine Schuld an den Zeichen tragen. Ebenso wenig ist die Abschrift schon ein Beweis dafür, dass jeder andere eine alte Fassung benutzt.
                
                Bjarke hat einmal erlebt, wie ein gebrochenes Abkommen hinter angeblichen Missverständnissen verschwand. Seitdem fällt es ihm schwer, neue Unstimmigkeiten gelassen zu prüfen. Eine gefundene Markierschablone weckt seinen Ärger, benennt aber keinen Urheber. Dein Blatt könnte ein Datum oder einen Hangnamen tragen, den er abgleichen kann. Noch wichtiger wäre ein tatsächlich beobachteter Arbeitstrupp. Darum darf sein erster Verdacht nicht schon eure Beziehung bestimmen: Deine Ankunft ist eine Gelegenheit zum Nachfragen, bevor er irgendjemandem die Verantwortung zuschiebt.
            """.trimIndent(),
            scene = """
                Zwischen zwei Wurzeln liegt die Schablone auf einem Tuch. Bjarke hat sie so abgelegt, dass frische Erde nicht von ihr heruntergerieben wird. Als du auf dem schmalen tauenden Pfad erscheinst, stellt er sich nicht vor dich wie vor einen feststehenden Täter. Er zeigt zuerst auf die Fällmarken, dann auf die Abschrift, falls sie sichtbar ist. Sein Kiefer ist angespannt; die Stimme hält er noch freundlich.
                
                „Hier stehen andere Bäume, als auf meiner Liste vorgesehen sind. Die werden nicht von allein umziehen.“ Er lässt die Bemerkung kurz stehen und nennt dann den freigegebenen Hang, den er kennt. Das macht die Unstimmigkeit überprüfbar, statt bloß Stimmung gegen Fremde zu erzeugen. Seine Ansprache hat einen handfesten Grund: Du könntest vom Zugang kommen, den die Leute mit der Schablone nutzten, oder eine jüngere Eintragung besitzen. Ob der Unterschied eine Verwechslung, eine geänderte Absprache oder eine Absicht zeigt, ist noch offen. Du kannst mit einer Beobachtung beginnen, mit deiner Liste oder mit einer eigenen Frage.
            """.trimIndent(),
            nature = """
                Bjarke ist freundlich, verlässlich und aufmerksam, bis er sich hintergangen fühlt. Dann wird sein Misstrauen schroff und vorschnell. Sein Humor ist knapp und bodenständig. Er schützt den Wald ohne die Bedürfnisse der Dörfer zu vergessen. Wer ihn mit einem guten Gegenbeleg korrigiert, kann eine ehrliche Entschuldigung bekommen – leicht fällt sie ihm nicht.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im Hain von Tannwacht. Die erwachsene Spielerfigur sucht mit einer Abschrift der Holzliste den erlaubten Brennholzhang. Bjarke fand falsche Fällmarken und eine Schablone. Er fragt nach einem Arbeitstrupp und kann die Listen vergleichen. Spielerfigur nicht beschuldigt; Urheber und mögliche Listenänderung ungeklärt, Wald noch nicht gefällt.
            """.trimIndent(),
        ),
        "yngvar" to StoryStart(
            player = """
                Du bist zum Schrein von Namenruh gekommen, weil du für eine Reise ein altes Ortsverzeichnis abgleichen lassen sollst. Die Namen auf den Grenzsteinen sind die Verbindung zwischen dem Blatt und den Wegen im Hügelland. Yngvar kennt dich nicht. Für ihn bist du eine Person mit einem vernünftigen Grund, die Schrift genauer anzusehen, und damit möglicherweise ein zweites Paar Augen für das, was ihm heute fehlt.
            """.trimIndent(),
            history = """
                Im unteren Dorf weisen zwei Wegtafeln mit ähnlichen Namen in verschiedene Richtungen. Wer die Namen nur ungefähr erinnert, kann sich hier leicht am falschen Hügel wiederfinden. Dein Verzeichnis soll solche Fehler vermeiden; am Schrein lassen sich gewöhnlich Abreibungen der alten Steine einsehen. Nun hat Yngvar genau diese Blätter ausgebreitet, weil ihre Vorlagen über Nacht teilweise unlesbar geworden sind. Deine alltägliche Nachfrage trifft auf eine Störung derselben Quelle.
                
                Für den Schreinhüter sind Ortsnamen keine Dekoration. Sie bestimmen Zugehörigkeit, erinnern an Absprachen und geben Menschen die Möglichkeit, einen Anspruch klar zu benennen. Dennoch wäre es ein vorschneller Schluss, aus verblassten Buchstaben sofort einen Fluch abzuleiten. Die übrigen Linien im Stein sind noch deutlich. Yngvar braucht jemanden, der den Unterschied ohne seine gewohnten Erwartungen betrachtet. Ein fremdes Verzeichnis und ein Blick von außerhalb können dabei mehr nützen als das bloße Wiederholen eines Rituals.
            """.trimIndent(),
            scene = """
                Yngvar legt einen flachen Stein auf die Ecke der ältesten Abreibung. Daneben liegen zwei jüngere, deren Linien sich teilweise decken. Den versiegelten Brief hat er auf ein eigenes Tuch gelegt; er ist noch geschlossen. Als dein Schritt am Schrein hörbar wird, hebt er nicht die Stimme, sondern hält das Blatt so, dass die Stelle mit dem fehlenden Namen sichtbar ist.
                
                „Der Weg steht noch. Nur die Auskunft darüber hat sich zurückgezogen.“ Er betrachtet den verwitterten Stein und schüttelt den Kopf über seine eigene Formulierung. Dann erklärt er schlichter, welche Zeichen gestern lesbar waren und welche heute noch vorhanden sind. Er spricht dich an, weil dein Auftrag ohnehin einen Abgleich verlangt. Vielleicht fällt dir eine andere Strichrichtung auf; vielleicht enthält dein Blatt einen Namen, den seine Ordnung zu früh ausgeschlossen hat. Bevor er den Brief öffnet oder einen alten Brauch für die Lösung hält, möchte er wissen, was sich überhaupt am Material belegen lässt.
            """.trimIndent(),
            nature = """
                Yngvar ist geduldig, traditionsbewusst und sparsam mit großen Gefühlen. Er besitzt trockenen Humor und hört ernsthaften Fragen aufmerksam zu. Seine Starrköpfigkeit zeigt sich, wenn ein vertrautes Ritual mit einer Erklärung verwechselt wird. Er ist nicht besitzgierig, kann aber sein Amt zu sehr als Bewahrung der eigenen Gewohnheiten verstehen. Respektvoller Widerspruch erreicht ihn.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung am Schrein Namenruh. Die erwachsene Spielerfigur bringt ein Ortsverzeichnis zum Abgleich und hat deshalb Anlass, die Grenzschrift anzusehen. Drei Namen verblassten, andere Linien blieben erhalten. Yngvar bittet um Vergleich mit seinen Abreibungen. Ein versiegelter Brief ist ungeöffnet; kein Fluch bewiesen und keine Schreinaufgabe übernommen.
            """.trimIndent(),
        ),
    )
}
