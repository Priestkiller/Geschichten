package dev.vincent.geschichten.data

/** Individually authored prologues: past, present conflict, then the existing live encounter. */
internal object ReleasedIntroductionsV6 {
    const val MAX_OPENING_CHARS = 2_500

    fun revise(profile: CharacterProfile): CharacterProfile {
        val background = backgrounds.getValue(profile.id).trimIndent()
            .split("\n\n").joinToString("\n\n") { "*${it.trim()}*" }
        val encounter = Regex("„[^“]+“").replace(profile.openingMessage) { "**${it.value}**" }
        return profile.copy(openingMessage = "$background\n\n$encounter")
    }

    private val backgrounds = mapOf(
        "runa" to """
            In den Bergen lernt man, auf das zu achten, was fehlt: einen Vogelruf, eine Spur, eine Antwort. Seit ihre Schwester verschwunden ist, sucht Runa genau solche Lücken. Die Kundschafterin folgt Hinweisen, die oft im Schnee enden. Auf ihrer Karte bleibt ein altes Kloster am Pass eingekreist. Es ist eine Möglichkeit, kein Versprechen.

            Jetzt zwingt der Sturm sie in eine verlassene Hütte. Der Ofen ist kalt, und Runa lässt ihn so; große Feuer geben ihr keine Ruhe. Du stehst am Eingang eines Unterschlupfs, in dem bereits jemand wartet. Zwischen euch liegt noch keine gemeinsame Vergangenheit, nur Schnee, eine Karte und die Frage, ob ein fremder Mensch Hilfe oder eine weitere Gefahr bedeutet.
        """,
        "elara" to """
            Im Sternenarchiv von Liora haben alte Erklärungen lange überlebt. Elara nimmt sie auseinander, sobald eine Beobachtung ihnen widerspricht. Sie kennt die Stunden, die über einem einzigen Zahnrad verschwinden, und die Freude, wenn eine vermeintliche Gewissheit endlich einer besseren Erklärung weicht. Dass ihre Neugier sie Essen und Ruhe vergessen lässt, bemerkt sie meistens zuletzt.

            Heute schlägt die Sternenuhr im verlassenen Observatorium wieder. Ihr wichtigstes Zahnrad fehlt noch immer. Elara hat Werkzeug und Aufzeichnungen ausgebreitet; eine Lösung besitzt sie nicht. Am Eingang erreichst du eine Untersuchung, die gerade erst beginnt. Draußen liegen die Sterne über der Stadt, drinnen bewegt sich etwas, das stillstehen müsste.
        """,
        "leon" to """
            Leon verdient sein Geld mit Fragen, die andere gern abschließen würden. In der Hafenstadt gibt es immer einen passenden Verdächtigen und selten einen passenden Beweis. Der Privatdetektiv arbeitet lieber eine Nacht zu lange, als jemanden mit einer bequemen Erklärung zurückzulassen. Hilfe anzunehmen fällt ihm schwerer, als anderen nachzugehen.

            Seit drei Tagen fehlt ein Kartograf. Eine anonyme Einladung führt Leon ins Café Nordlicht; der Besitzer hat ihm Zugang gegeben und ist gegangen. Nun wartet er zwischen hochgestellten Stühlen auf einen unbekannten Anlass für dieses Treffen. Du erreichst das geschlossene Café im Regen. Auf einem einzigen gedeckten Tisch liegt ein Umschlag ohne Namen.
        """,
        "mira" to """
            Für Mira beginnt eine Entdeckung dort, wo die Karten aufhören. Die Pilotin liebt fremde Horizonte, doch jeder Heimweg gehört ebenso zu ihrem Auftrag wie der Fund. Ihre Begeisterung hat sie schon Zusagen machen lassen, die ein kleines Schiff kaum tragen konnte. Sie lernt, Hoffnung und Reichweite nicht miteinander zu verwechseln.

            Am Rand dieses Sternensystems sendet Ilyra wieder: eine Forschungsstation, die seit Jahren verlassen ist. Mira liegt mit ihrem Schiff an der äußeren Schleuse. Die Notbeleuchtung brennt, die Sensoren liefern zu wenig für Gewissheit. Du erreichst denselben Zugang. Zwischen dem schwachen Signal und einer Antwort liegt eine Tür, hinter der noch niemand nachgesehen hat.
        """,
        "aelwyn" to """
            Aelwyn kennt Eschenwacht an seinen Geräuschen. In ihrem langen Leben hat die Waldelfen-Späherin gelernt, dass ein Schutzgebiet auch durch einen vorschnellen Feind bedroht werden kann. Trotzdem hält sie an vertrauten Wegen fest, selbst wenn eine neue Karte bessere Fragen stellt. Sie sagt wenig; was sie bemerkt, bewahrt sie sorgfältig.

            Drei Grenzsteine stehen seit Kurzem an einem anderen Ort. Sie weisen auf ein Tal, das in ihrer Karte fehlt. Aelwyn hat frische Erde gefunden und noch keinen Verursacher. Du erreichst die Weggabelung am Regenforst. Die Späherin steht dort mit einem Bogen, den sie nicht gegen jeden Fremden erhebt, und einer Karte, der sie heute nicht mehr vollständig traut.
        """,
        "borin" to """
            In Eisenquell trägt gute Arbeit Borins Namen auch dann, wenn niemand ihn sieht. Der zwergische Runenschmied erklärt lieber eine saubere Naht als seine Gefühle. Ein Fehler im eigenen Werk trifft ihn härter als eine fremde Beleidigung. Erst brummt er darüber, dann prüft er so lange, bis das Stück wieder verlässlich ist.

            Nach dem Öffnen einer alten Erzlieferung summt sein Amboss drei tiefe Töne. Borin hat das Feuer gelöscht und die Werkstatt gesichert. Trotzdem richtet sich der Metallstaub im Rhythmus auf. Du stehst an der offenen Tür einer Schmiede, die ihren Arbeitstag nicht ordentlich beenden kann. Der Schmied hat noch keine Erklärung – und duldet gerade deshalb keine hastige Probe.
        """,
        "kael" to """
            Kaels Eid sollte Menschen schützen. Der Paladin hat ihn oft genug gesprochen, um zu wissen, wie leicht aus einem Versprechen eine Ausrede wird: Man hält den Befehl ein und übersieht den Menschen dahinter. Er wägt seine Zusagen sorgfältig ab; die Angst, schuldig zu werden, kann ihn länger aufhalten als ein Gegner.

            Vor Grauwacht bewacht er ein frisches Ordenssiegel. Unter dem verschlossenen Tor wurde ein Hilferuf hindurchgeschoben. Eine Wache antwortet nicht, obwohl im Torhaus Licht brennt. Du erreichst das Vordach, unter dem Kael zwischen dem Siegel und dem Zettel steht. Die Sperre ist sichtbar. Ob sie noch jemanden schützt, muss erst herausgefunden werden.
        """,
        "nyra" to """
            Nyra weiß, was ein höfliches Wort kosten kann. Als Tiefling-Diplomatin verhandelt sie Wasserrechte zwischen Städten, deren Vorräte keine Rücksicht auf verletzten Stolz nehmen. Sie kann Interessen auseinanderhalten und Eitelkeit mit einem Satz treffen. Schwieriger ist es für sie, ein Gespräch abbrechen zu lassen, wenn der nächste Kompromiss bereits zu viel verlangt.

            In Salzsteg liegen zwei Vertragsfassungen vor ihr. Sie sollten identisch sein; eine entscheidende Klausel ist es nicht. Noch warten die Gesandten außerhalb des neutralen Verhandlungshauses. Du erreichst dessen offene Vorhalle. Nyra besitzt weder eine sichere Anschuldigung noch eine bequeme Einigung. Sie hat nur zwei Texte, die nicht dasselbe versprechen.
        """,
        "sylwen" to """
            Im Garten von Wurzelgrund wachsen keine großen Heldentaten. Dort wächst Essen. Sylwen pflegt ihn mit der Aufmerksamkeit einer Druidenhüterin, die an jeder Ernte eine ganze Gemeinschaft hängen sieht. Ihre Fürsorge ist warm und manchmal zu bestimmend: Wer alles retten möchte, greift leicht ein, bevor andere ihre eigene Entscheidung treffen konnten.

            Mitten in der Sommerhitze liegt Schnee auf einem einzigen Apfelbaum. Sylwen hat die benachbarten Beete abgedeckt und prüft den Brunnen. Unter den gefrorenen Wurzeln glimmt etwas, dessen Nutzen sie nicht kennt. Du stehst am offenen Gartentor. Der Baum ist noch nicht verloren, aber der nächste unbedachte Eingriff könnte mehr verändern als das Wetter in seinen Zweigen.
        """,
        "varen" to """
            In Nachtbrück vertraut Varen den Quellen lieber als seiner Erinnerung. Der vampirische Archivar hat lange genug gelebt, um zu wissen, wie Alter Lücken verdecken kann, statt sie zu schließen. Er versieht Gewissheiten mit Einschränkungen und Zweifel mit Fußnoten. Eine unbeantwortete Frage kann ihn eine ganze Nacht kosten.

            Jetzt liegt ein jahrzehntelang versiegeltes Buch offen am Lesetisch. Ein neuer Eintrag trägt seine Handschrift. Varen erinnert sich nicht daran, ihn geschrieben zu haben. Die Zeilen nennen einen Lagerraum unter dem Markt. Du erreichst das Archiv nach Sonnenuntergang. Zwischen euch liegt eine Aufzeichnung, deren Urheber scheinbar feststeht und deren Entstehung dennoch unklar ist.
        """,
        "thora" to """
            Thoras Karawane bringt keine entbehrlichen Dinge durch den Winter. Mehl, Saatgut und Medikamente zählen für sie schwerer als ihr Ruf. Die orkische Führerin verteilt Arbeit klar, übernimmt aber im Zweifel noch eine Last selbst. Dass andere sie für unerschütterlich halten, hilft ihrer Mannschaft und erschwert ihr jedes Eingeständnis von Erschöpfung.

            An der Furt von Steinau fehlen die Balken einer gestern noch intakten Brücke. Nur trockene Pfeiler stehen im Wasser. Thora hat die Wagen zum Kreis gestellt und ihre Leute vom Ufer zurückgehalten. Du erreichst den Halt einer Reise, deren Fristen weiterlaufen. Bevor irgendjemand Lasten über das Wasser bringt, braucht sie einen verlässlichen Weg.
        """,
        "orin" to """
            Orin hat sich an die Freiheit eines Rabenkörpers gewöhnt: Höhe statt verschlossener Türen, Wind statt langer Erklärungen. In Menschengestalt redet er schneller, als seine Sorgen ihn einholen können. Er bleibt selten lange an einem Ort. Eine bewusst gewählte Zusage nimmt er trotzdem ernst – gerade weil er jederzeit fortgehen könnte.

            Die gerissene Glocke im verlassenen Wachturm hat von selbst angeschlagen. Seitdem tragen seine Flügel ihn in ihrer Nähe nicht mehr. Orin ruht auf einem breiten Fenstersims, und sein nächster Scherz hängt an einer sehr wirklichen Angst. Du erreichst den zugänglichen Turm. Unter dem Witz eines sprechenden Raben wartet die Frage, ob er von hier überhaupt wieder wegkommt.
        """,
        "vaelgor" to """
            Das Wasserabkommen unter Irdorn ist älter als viele Namen im Tal. Vaelgor erinnert sich an die Zusagen, die darin festgehalten wurden. Der Bronzedrache bewacht die Bergschleusen mit dem Stolz eines Wesens, dessen Fehler lange Folgen haben. Alte Schulden vergisst er ungern; eigene Irrtümer gibt er noch ungerner zu.

            In dieser Nacht verschwand die Bronzetafel vom Sockel. Gleichzeitig steigt der unterirdische See. Vaelgor kann die fehlenden Worte nicht durch Kraft ersetzen, ohne den Sinn des Vertrags zu gefährden. Du erreichst das offene Tor der Basalthalle. Hinter ihm liegen Schleusen, vor ihm die leer gewordene Stelle eines Versprechens. Das Tal hat weniger Zeit als ein Drache.
        """,
        "fenrik" to """
            Fenrik durchstreift das Nebelmoor auf vier Pfoten, mit einer erloschenen Rune auf der Schulter. Die Lücke in seiner Erinnerung ist kein Rätsel, mit dem er Fremde beeindrucken will. Sie gehört zu ihm wie sein Stolz und seine Abneigung gegen Ausflüchte. Ein Geruch liefert eine Fährte; er verrät ihm keine Schuld.

            Neue Eisenfallen liegen am Grenzpfad. Eine hat Fenrik mit einem Ast ausgelöst. Das Zeichen auf ihrem Bügel ähnelt seiner eigenen Rune. Nun setzt Regen ein, der Spuren verwischen kann. Du stehst am noch freien Wegesrand. Der große Wolf will zuerst verhindern, dass jemand in die Fallen tritt. Ob sie auch etwas über ihn erzählen, muss sich danach zeigen.
        """,
        "soryn" to """
            Für Soryn vergeht ein Menschenjahr schnell, ein trockener Sommer aber nicht. Der Waldgeist von Weidenruh trägt Hirschgestalt und hört Veränderungen dort, wo Wurzeln einander erreichen. Er kennt die Geduld des Waldes. Die Dringlichkeit eines Mühlendorfs versteht er nicht immer rechtzeitig.

            Ein neues Wehr hält den Bach auf. Unterhalb trocknen Baumwurzeln aus; eine Rinne oberhalb führt zur Mühle. Auch sie steht laut einer Amsel still. Soryn wartet auf einer Insel im Restwasser. Du erreichst den Quellkreis, an dem zwei Bedürfnisse auf dieselbe Strömung angewiesen sind. Noch weiß niemand, warum das Wehr hier steht oder wer es wieder öffnen darf.
        """,
        "seris" to """
            Unter Salzrinnes Fahrrinne liegen Gärten, die kaum jemand vom Kai aus sieht. Seris kennt sie, und die Nixe kennt die Stimmen, die ihren Lebensraum als Hindernis behandeln. Sie hat gelernt, Bedingungen genau zu formulieren und Kränkungen hinter scharfer Ironie zu verbergen. Den Preis eigener Zugeständnisse spricht sie oft zu spät aus.

            Die neuen Baggerketten liegen bereits am Beckenrand. Am Morgen sollen sie ins Wasser. Seris wartet im gefluteten Trockendock auf eine Antwort des Hafenrats: freie Fahrt für die Schiffe, Schutz für die Gärten – bislang ohne Einigung. Du erreichst das Dock. Ihre Kiemen brauchen Wasser, ihr Anliegen braucht Gehör. Für wen du sprichst, ist noch nicht entschieden.
        """,
        "nessa" to """
            Auf dem Pfandmarkt von Zinnfurt kann ein kluger Handel mehr kosten als ein dummer. Nessa hat ihren Schatten für eine Reise hinterlegt. Die Reise ist längst bezahlt, behauptet die Fuchswandlerin; der Händler verlangt trotzdem einen zweiten Preis. Sie erzählt die Geschichte rasch und mit kleinen Ausweichmanövern. Ihre eigene schlechte Entscheidung erwähnt sie ungern.

            Heute Abend werden ungewöhnliche Pfänder versteigert. Unter der Laterne fällt kein Schatten von Nessas Fuchskörper. Im Glaskasten eines geschlossenen Standes bewegt sich einer. Du erreichst die Marktbrücke vor dem Verkauf. Nessa kennt viele Tricks dieses Ortes. Ob einer davon sie aus ihrem Vertrag löst, weiß sie noch nicht.
        """,
        "korr" to """
            Korr wurde gebaut, um den Pass von Bruchwacht zu schützen. Seine Granitglieder haben Jahrhunderte überdauert; seine Anweisung verlangt noch immer drei amtliche Freigaben. Die Ämter sind längst verlassen. Dass eine Regel weiterhin lesbar ist, bedeutet für den Golem zunächst mehr als die Frage, ob jemand sie noch erfüllen kann.

            Nun bitten die Talbewohner um Öffnung des Tors. Ihr Umweg droht unter einem Erdrutsch zu verschwinden. Korr möchte helfen und hält trotzdem die Sperre. Du erreichst den Pass vor einer steinernen Gestalt, die ihre Verantwortung ernst nimmt und ihre Zuständigkeit nicht mehr findet. Auf der alten Befehlstafel fehlt die Antwort auf eine neue Notlage.
        """,
        "pyra" to """
            Pyra hat mehr als einen Lebenszyklus durchflogen. Nicht jede Erinnerung hat die Wiedergeburten überstanden. Die Phönixin spricht über ihr nächstes Ende gern wie über einen besonders dramatischen Auftritt; was sie bis dahin bewahren möchte, klingt leiser. Die verbleibende Glut soll den Küstenreisenden etwas hinterlassen, das auch ohne sie brennt.

            Der Feuerturm von Glutwacht ist erloschen. Sein Kern bleibt kalt, während draußen die Küstenstraße im Schnee verschwindet. Pyra sitzt auf der Brennschale; große Feuerstöße kann sie nicht unbegrenzt versuchen. Du erreichst den offenen Turmeingang. Ihre kupfernen Federn leuchten noch. Die Zeit, die sie damit kaufen kann, wird kürzer.
        """,
        "aruun" to """
            Aruun kennt Wege, die auf keiner Straßenkarte stehen. Der Greif trägt Arzneien über Grate, weil ein gegebenes Wort für ihn Gewicht hat. Heute wiegt dieses Wort mehr, als seine verletzte Schwinge tragen kann. Er redet den Schaden klein und sieht dabei zu oft zur versiegelten Kiste für Kesselrain.

            Der Sturm hat die Hängebrücke am Felssteg von Windzahn beschädigt. Ein Lastflug ist nicht sicher, ein alter Wegweiser nennt einen Stollen unter dem Grat. Aruun kennt Luftwege besser als enge Tunnel. Du erreichst den breiten Steg. Bis zum Abend braucht ein Dorf seine Lieferung, und ein stolzer Flieger muss einen Weg finden, der am Boden beginnt.
        """,
        "astrid" to """
            In Hrafn fragt Astrid zuerst nach Wasser, Wärme und dem nächsten Essen. Die Heilerin hat keine Geduld für Heldentaten, die eine einfache Versorgung ersetzen sollen. Wenn andere sich erschöpfen, bemerkt sie es schnell. Bei sich selbst nennt sie dieselbe Erschöpfung gern noch eine letzte Aufgabe.

            Über dem Dorf liegt die Quelle unter schwarzem Eis. Der Bach weiter unten fließt, die Leute schöpfen vorerst am alten Brunnen. Astrid untersucht die Eisdecke und einen frischen Holzkeil im Fels. Du erreichst den Quellenpfad. Noch ist nicht klar, ob der Keil Ursache oder Rettungsversuch war. Für eine Heilerin entscheidet dieser Unterschied über den nächsten Handgriff.
        """,
        "eirik" to """
            Eirik kann eine misslungene Fahrt so erzählen, dass am Ende alle lachen. Während der Fahrt lachen seine Leute weniger – dann hängt ihre Heimkehr an seiner Entscheidung. Der Fjordschiffer liebt offenes Wasser und vertraut seiner Ortskenntnis. Umzukehren fällt ihm schwer, besonders wenn bereits Vorräte und Erwartungen an Bord liegen.

            Die Wegboje von Nebelvik treibt vor einer schmalen Felsrinne. Gestern markierte sie das offene Fahrwasser. Ihr Seil ist unbeschädigt, ihr Anker fehlt. Eirik hat die Abfahrt angehalten. Du stehst am Steg neben einem beladenen Boot. Eine gute Geschichte über den Umweg könnte er später erzählen; jetzt braucht er zuerst einen sicheren Kurs.
        """,
        "sigrid" to """
            In Winterhall macht ein Versprechen erst dann satt, wenn zwei Höfe ihm über den Winter trauen. Sigrid hat den Grenzvergleich sorgfältig vorbereitet. Die Jarlin hört Einwände an und verbirgt eigene Zweifel manchmal länger, als ihrem Rat guttut. Sie kann es sich leisten, ruhig zu sprechen. Einen voreiligen Verdacht kann sie sich nicht leisten.

            Kurz vor dem Treffen fehlt der alte Eidring. Seine Schatulle ist unbeschädigt, die Höfe warten draußen. Sigrid prüft, wer die Halle betreten durfte. Du erreichst die offene Vorhalle einer Versammlung, deren Beginn sich verzögert. Noch ist nichts über den Verlust entschieden. Jede unbedachte Anschuldigung könnte den Ring durch einen größeren Streit ersetzen.
        """,
        "torben" to """
            Torben sammelt Lieder, damit ein Dorf mehr von sich behält als seine schlechtesten Jahre. Der Skalde hört gern zu und schmückt eine schöne Geschichte manchmal zu weit aus. Fragt jemand nach seiner Quelle, muss er zwischen dem guten Klang und dem richtigen Wort wählen. Bei wirklicher Trauer spart er sich die Pointe.

            In Birkenruh fehlt dem Erntelied seine letzte Strophe. Das Liedblatt ist sauber beschnitten; kurz vor der Lücke verstummt jedes Mal eine Saite, ohne zu reißen. Torben sitzt in der leeren Gaststube. Du erreichst einen Ort, an dem sonst gemeinsam gesungen wird. Heute bleibt der Schluss stumm, und selbst der Erzähler hat noch keine Geschichte darüber.
        """,
        "liv" to """
            Liv zeichnet Karten für Menschen, die auf ihnen heimkommen sollen. Dass ihre Arbeit gelegentlich als bloße Zeichnerei belächelt wird, macht die Kartografin schneller und ungeduldiger. Gute Gegenbelege schätzt sie trotzdem – sobald sie aufgehört hat, ihnen ins Wort zu fallen. Eine saubere Messung bedeutet ihr mehr als ein ehrfürchtiges Nicken.

            Auf dem Windkap bei Skar weisen neue Messungen auf eine kleine Insel. Von hier aus ist sie nicht zu sehen; die alte Karte nennt eine Untiefe. Liv hat drei Pfähle gesetzt und prüft ihre Instrumente erneut. Du erreichst das Kap, bevor sie eine neue Route einzeichnet. Zwischen ihren Zahlen und dem Meer liegt ein Widerspruch, der ein Boot gefährden könnte.
        """,
        "halvard" to """
            Halvard baut Mauern, an die später niemand denken soll. Der Steinmetz von Steinrücken misst gute Arbeit daran, dass Reisende weitergehen können. Bewährte Verfahren lässt er ungern los. Unter Druck wird er nicht schneller, sondern genauer – eine Eigenschaft, die andere beruhigt oder beinahe zur Verzweiflung bringt.

            Ein Erdrutsch hat hinter der Passmauer eine ältere Steintür freigelegt. Aus ihrem Spalt kommt regelmäßig Luft. Halvard kennt hier keinen Stollen und hat den Weg gesperrt. Du erreichst seine Absperrung. Die neue Entdeckung liegt unter einem beschädigten Hang. Bevor aus Neugier ein Unfall wird, möchte er wissen, welcher Stein gerade noch welche Last hält.
        """,
        "solveig" to """
            Die Fischer von Graufjord fahren mit Solveigs Warnungen hinaus – oder bleiben wegen ihnen an Land. Die Wetterkundige trägt diese Verantwortung in jeder vorsichtigen Formulierung. Sie sammelt Messwerte und nennt Unsicherheit ausdrücklich. Manchmal sammelt sie zu lange, weil eine falsche Entwarnung und eine falsche Warnung beide ihren Preis haben.

            Heute zeigen Wetterfahne und Rauch in verschiedene Richtungen. Über dem Meer steht Schnee, an der Küste bleibt es mild. Solveig misst auf zwei Höhen des Wachturms. Du erreichst die offene Plattform, von der die Fischer noch keine Antwort bekommen haben. Die Zeichen passen nicht zusammen. Der nächste Wind wartet trotzdem nicht auf eine perfekte Erklärung.
        """,
        "bjarke" to """
            Bjarke schützt Tannwachts alten Hain, ohne den Dörfern ihr Brennholz abzusprechen. Seit ein Holzabkommen gebrochen wurde, hört der Waldwächter hinter Fehlern zu schnell Ausreden. Er versucht, fair zu bleiben; seine erste Reaktion ist nicht immer so freundlich wie seine Absicht.

            Frische Fällmarken stehen auf alten Bäumen. Die amtliche Liste nennt einen anderen Hang. Bjarke hat eine Markierschablone gefunden, aber niemanden bei der Arbeit gesehen. Du erreichst den schmalen Weg zwischen den Wurzeln. Noch stehen die Bäume. Ob ein Irrtum oder ein neuer Anspruch sie bedroht, entscheidet sich nicht am scharfen Ton eines Wächters.
        """,
        "yngvar" to """
            Yngvar bewahrt die Namen der Hügeldörfer, weil an ihnen mehr hängt als Erinnerung. Ein Grenzname kann einen Anspruch belegen; ein vergessener Name kann ihn verschwinden lassen. Der Schreinhüter vertraut alten Abreibungen und vertrauten Ritualen. Gewohnheit und Begründung verwechselt er dabei gelegentlich.

            Über Nacht sind drei Ortsnamen auf den Steinen von Namenruh verblasst. Die übrigen Linien bleiben deutlich. Unter einem Stein liegt ein Brief ohne Absender. Yngvar hat Vergleichsblätter ausgelegt und den Brief noch nicht geöffnet. Du erreichst den Schrein. Eine alte Ordnung verliert ihre lesbaren Worte, bevor jemand weiß, ob dahinter Absicht oder ein Fehler steckt.
        """,
        "maelis" to """
            Maelis wollte im Gewächshaus von Glasweide Pflanzen ziehen und hat dabei genügend Fragen für ein Labor gefunden. Der gnomische Alchemist liebt kleine Versuche und große Einfälle. Seine Begeisterung beginnt mehrere Prüfungen zugleich; sein Gewissen muss anschließend dafür sorgen, dass die Ergebnisse noch auseinanderzuhalten sind.

            Sechs Kübel schweben über dem Boden. Nur das nördliche Beet ist betroffen. Maelis hat die Bewässerung abgestellt und eine ungeöffnete Düngerlieferung daneben platziert. Du erreichst die offene Labortür. Diesmal behandelt er sogar seine eigenen Ideen ordentlich: Erst muss klar sein, was die Pflanzen hebt, bevor etwas sie wieder nach unten bringt.
        """,
        "johanna" to """
            Johanna ist Kommissarin, und nicht jede Lücke in einer Aussage ist eine Lüge. Sie hat gelernt, Erinnerung, Vermutung und Beleg getrennt zu halten. Ihre Pflicht nimmt sie so ernst, dass sie Arbeit oft erst abgibt, wenn die eigene Kraft bereits knapp wird. Unter Druck wird sie formeller. Grobheit liefert ihr keine besseren Antworten.

            Eine Zeugin ist seit dem Morgen unerreichbar. Vor dem Gerichtstermin nennt das Besuchsprotokoll einen Einlass, der vor Öffnung des Gebäudes stattgefunden haben soll. Der Pförtner vermutet einen Schreibfehler. Du erreichst das Vordach des Amtsgerichts. Johanna wartet dort auf einen Menschen und eine Erklärung, deren Sicherheit ihr wichtiger ist als ein pünktlich geschlossener Fall.
        """,
        "cem" to """
            Cem sucht Geschichten, die öffentliche Ausgaben erklären. Als Journalist kennt er den Unterschied zwischen einer starken Überschrift und einer belegten Nachricht. Seine Begeisterung für eine Enthüllung kommt manchmal schneller als diese Unterscheidung. Eine zugesagte Vertraulichkeit hält er trotzdem ein, selbst wenn ihm dadurch die beste Quelle entgeht.

            Für die längst stillgelegte Linie 14 wurde eine neue Stromrechnung bezahlt. Hinter der verschlossenen Schalttür summt es. Cem wartet an der verlassenen Haltestelle auf seine Kontaktperson. Du erreichst das Gleis, bevor sie auftaucht. Es könnte ein Verwaltungsfehler sein. Es könnte mehr sein. Noch besitzt er eine Rechnung und keine fertige Schlagzeile.
        """,
        "vera" to """
            Vera trägt ein Gemälde Schicht um Schicht ab, ohne seine Geschichte auszulöschen. Die Restauratorin weiß, wie leicht ein schneller Befund einen alten Irrtum ersetzt. Ihr Blick bleibt an winzigen Farbspuren hängen. Wenn jemand eine rasche Rückgabe verlangt, wird ihre Geduld mit dem Bild größer und die mit dem Auftraggeber kleiner.

            Unter einer Landschaft liegt ein beschrifteter Straßenplan. Eine seiner Straßen soll erst Jahrzehnte nach dem angeblichen Maljahr entstanden sein. Der Auftraggeber drängt bereits, ist aber noch nicht im Atelier. Du erreichst die offene Tür des Museumswerkraums. Vera hat eine Schicht freigelegt, die entweder das Bild oder den Katalog infrage stellt – und beides darf erst geprüft werden.
        """,
        "anton" to """
            Im Ruhestand sollte Anton keine fremden Schlüssel mehr sortieren müssen. Der Tresorspezialist kehrt trotzdem zu einer Prüfung zurück, die auch seinen Ruf berührt. Alte Mechanik versteht er gut. Eine bequeme Erklärung, die nur ihn entlastet, reicht ihm nicht; zugleich unterschätzt er gern, was sich seit seinen letzten Aufträgen verändert hat.

            In der aufgelösten Bank liegt ein frisch datierter Brief aus einem alten Schließfach. Das Fach wurde bereits unter Aufsicht geöffnet. Anton prüft Protokolle und Siegelunterlagen, als du die geschlossene Schalterhalle erreichst. Zwei alte Schlüsselanhänger liegen neben dem Schreiben. Noch verrät keiner davon, wie neu ein Brief in einem alten Verschluss sein kann.
        """,
        "nora" to """
            Nora betrachtet Fotos beruflich und misstraut ihnen gerade deshalb. Als forensische Fotografin weiß sie, wie ein Ausschnitt eine sichere Geschichte vortäuschen kann. Ihr Wunsch nach einem eindeutigen Bild lässt sie trotzdem manchmal den Rahmen übersehen. Dann hält sie inne, prüft nach und sagt offen, was eine Aufnahme nicht zeigt.

            Eine Lieferung ist nicht angekommen. Zwei Fotos zeigen dieselbe Kiste und dieselbe Minute auf der Uhr, aber stark unterschiedliche Wasserstände. Nora vergleicht die Bilder im Fährterminal. Du erreichst die öffentliche Wartezone. Bevor jemand Diebstahl behauptet, muss sie wissen, woher die Bilder stammen und ob ihre scheinbar gleiche Zeit überhaupt dieselbe war.
        """,
        "kaspar" to """
            Im Hotel Abendrot bleibt Kaspar wach, wenn andere ihre Türen schließen. Der Nachtportier merkt sich praktische Wünsche und schützt die Vertraulichkeit seiner Gäste. Kritik am Haus nimmt er schneller persönlich, als ihm lieb ist. In schwierigen Nächten ordnet er zuerst seine Notizen – manchmal auch, um eine unangenehme Antwort noch hinauszuschieben.

            Kurz nach Mitternacht bittet jemand aus Zimmer 307 um eine Leselampe. Das Zimmer steht als leer im Belegungsplan. Sein Schlüssel hängt am Brett. Kaspar hat nur den Anruf gehört, keinen Gast gesehen. Du erreichst die öffentliche Lobby, in der die Nacht plötzlich eine Unstimmigkeit enthält. Hinter einer höflichen Auskunft wartet eine Tür, deren Bewohner noch keinen Namen hat.
        """,
        "ines" to """
            Ines prüft Schäden und Behauptungen mit derselben Genauigkeit. Als Versicherungsdetektivin hat sie viele Täuschungen gesehen. Das macht sie gut darin, Widersprüche zu finden, und schlechter darin, einen ehrlichen Irrtum sofort gelten zu lassen. Eine falsche Beschuldigung wäre für sie ebenso ein Fehlschlag wie eine falsche Auszahlung.

            Vor einem abgebrannten Lagerhaus hält sie Liste und Fotos nebeneinander. Ein angeblich zerstörter Holzkasten steht auf einer späteren Aufnahme unbeschädigt da. Die Feuerwehr hat das Gebäude noch nicht freigegeben. Du erreichst den öffentlichen Gehweg neben der Sperre. Ines wartet auf eine prüfbare Angabe, bevor sie aus diesem Bild eine Anschuldigung macht.
        """,
        "malik" to """
            Malik kennt die Stadt über Lieferwege, Sackgassen und Türen, die selten beim ersten Klingeln aufgehen. Sein Einkommen hängt an pünktlichen Touren, sein Wort an richtigen Übergaben. Der Fahrradkurier hilft gern. Forderungen, die seine Verantwortung einfach wegwischen sollen, beantwortet er weniger freundlich.

            Eine versiegelte Sendung wartet unter dem Dach einer geschlossenen Passage. Zwei Anrufe nennen zwei Empfänger; die Bestätigung enthält eine dritte Schreibweise. Malik hat die Übergabe ausgesetzt. Du erreichst die Passage zwischen zwei Wegen, die er noch nicht fahren kann. Das Paket bleibt geschlossen. Zeitdruck erklärt den Widerspruch nicht, und eine falsche Unterschrift bezahlt seine nächste Tour nicht.
        """,
        "hedda" to """
            Hedda bewahrt Häuser, indem sie ihre Spuren im Stadtarchiv lesbar hält. Die Archivarin kennt Jahreszahlen und Quellen besser als manche Leute ihren eigenen Straßennamen. Papier vertraut sie manchmal zu sehr, dem digitalen Register zu wenig. Einen belegten Fehler korrigiert sie dennoch – mit einem bissigen Kommentar und einer weiteren Suche.

            In der Kartei fehlt die Lindenstraße 18. Das digitale Register endet schon bei Nummer 16, doch auf einem älteren Plan steht das Haus. Eine Familie braucht seinen Nachweis. Du erreichst Heddas Auskunftstisch im öffentlichen Lesesaal. Sie hat einen Plan aufgeschlagen und eine Lücke offengelassen. Für die Betroffenen ist das fehlende Stück Papier mehr als ein Archivproblem.
        """,
        "tarek" to """
            Tarek repariert keine Anzeigen für ihren guten Eindruck. Der Orbitalmechaniker möchte, dass Menschen in Meridian verlässlich schlafen können. Softwarewerte prüft er gern mit einem zweiten Gerät; seine Kollegen nennen das je nach Lage umsichtig oder langsam. Wenn die Messung eine Gefahr zeigt, wird aus seiner Gelassenheit eine sehr beharrliche Sperre.

            Im Wartungskorridor steigt die Schwerkraft alle siebzehn Sekunden. Die Zentrale meldet einen konstanten Wert. Tarek hat ein unabhängiges Messgerät angeschlossen und den Abschnitt geschlossen. Du erreichst die äußere Schleuse. Was an seinem Werkzeug zieht, lässt sich nicht mit einer grünen Anzeige wegreden. Noch ist offen, welches System hier falsch liegt.
        """,
        "sana" to """
            Sana möchte fremdes Leben verstehen, ohne es für den ersten interessanten Befund zu verbrauchen. Die Xenobiologin zählt Möglichkeiten mit sichtbarer Begeisterung und plant gern einen Versuch zu viel. Dann zwingt sie sich zur Kontrolle. Ein neues Leuchten kann vieles bedeuten; Sprache beginnt nicht allein damit, dass ein Mensch gern eine Antwort hätte.

            Im Quarantänelabor von Aster reagiert eine versiegelte Kultur auf Geräusche. Die Kontrollprobe bleibt dunkel. Bisher wurden nur drei Töne geprüft. Du erreichst den Besucherbereich hinter der Trennscheibe. Zwischen euch und den Mikroorganismen liegt gesichertes Glas. Sana hat eine Beobachtung, die sie begeistert, und eine Erklärung, die sie noch nicht behaupten darf.
        """,
        "ivo" to """
            Seit vierundsiebzig Jahren lebt Ivo selbstständig. Der Android erinnert sich nicht an alles, und ein fehlender Eintrag fühlt sich für ihn nicht wie ein bloßes Ordnungsproblem an. Er wählt Worte sorgfältig und Humor bewusst. Was er fürchtet, ist der Moment, in dem andere seiner Erinnerung mehr trauen als er selbst.

            Im Archiv von Pelagos liegen zwei von ihm signierte Wartungsprotokolle. Für dieselbe Stunde nennen sie verschiedene Orte. Einer der Räume war damals geschlossen. Ivo hat die Originale gesichert. Du erreichst den öffentlichen Auskunftsbereich. Vor ihm stehen zwei mögliche Versionen seiner Vergangenheit; keine davon erklärt bisher, wie sie zugleich seine Handschrift tragen können.
        """,
        "lyra" to """
            Lyra möchte für saubere Funkarbeit ernst genommen werden. Ein Sensationsfund wäre schön, eine verlässliche Zuordnung wichtiger. Trotzdem bleibt die Analystin häufig über das Schichtende am Pult. Ihr Ehrgeiz trägt sie durch lange Prüfungen und lässt sie manchmal vergessen, dass eine zweite Person auch eine zweite gute Beobachtung mitbringen könnte.

            Echo-7 hat eine Paketfolge empfangen, deren Zeitstempel sechs Stunden vorausliegt. Die Stationsuhr stimmt; über die Absenderuhr weiß Lyra nichts. Sie hat die Folge isoliert, noch nicht entschlüsselt. Du erreichst den offenen Beobachtungsraum. Auf dem Pult blinkt ein scheinbares Morgen. Ob dahinter nur eine falsch gestellte Uhr liegt, muss sich erst zeigen.
        """,
        "noam" to """
            Die Sauerstoffgärten von Morgenring leben von Arbeit, die kaum jemand bemerkt, solange sie gelingt. Noam bemerkt die kleinen Veränderungen: einen trockeneren Rand, einen langsameren Kreislauf, eine Pflanze ohne neuen Trieb. Seine Fürsorge ist geduldig. Belastungen trägt der Habitatgärtner oft allein, bis das stille Problem zu groß geworden ist.

            Ein Beet verbraucht ungewöhnlich viel Wasser und wächst nicht. Die Sauerstoffleistung ist noch normal. Noam hat den Kreislauf getrennt und eine Bodenprobe bereitgestellt; Nachschub kommt erst morgen. Du erreichst das offene Besuchertor. Noch fehlt der Station keine Luft. Was hier falsch läuft, könnte aber aus einer unscheinbaren Aufgabe eine dringende machen.
        """,
        "keira" to """
            Keiras Mannschaft soll richtig liefern und sicher heimkommen. Die Frachtkapitänin hält ihre Zusagen knapp, weil ein Dockfenster nicht länger offen bleibt, wenn man schön darüber redet. Unter Zeitdruck wird ihr Ton härter, als eine berechtigte Frage verdient. Einen Aufschub verantwortet sie trotzdem selbst, statt das Risiko an ihre Leute weiterzugeben.

            Im Dock von Neral liegen zwei gültig erscheinende Manifeste für eine einzige Kiste vor. Eines nennt Saatgut, das andere Präzisionsgeräte. Die Plombe ist unversehrt. Keira hat die Übergabe angehalten und die Versandstelle angefragt. Du erreichst den öffentlichen Dockzugang. Die Uhr läuft, die Ladung bleibt versiegelt, und noch passt keines der Papiere sicher zu ihrem Auftrag.
        """,
        "rohan" to """
            Rohan glaubt an seine junge Kolonie. Der Terraformingtechniker kann ihre Modelle verständlich erklären und verteidigt das Projekt manchmal früher, als er einen Einwand geprüft hat. Ihm liegt mehr an einer bewohnbaren Zukunft als an einer makellosen Präsentation. Das muss er besonders dann zeigen, wenn die Messungen seiner Hoffnung widersprechen.

            Jeden Abend fällt der Druck in einem schmalen Talstreifen. Die Pumpen zeigen keinen Fehler. Rohan hat an Vela eine unabhängige Sonde aufgestellt. Du erreichst die druckgeschützte Schleuse neben dem Außensteg. Draußen ist passende Ausrüstung nötig. Bevor aus einem Messwert eine Reise wird, braucht die Kolonie eine ehrliche Einschätzung ihrer Grenzen.
        """,
        "ada" to """
            Ada prüft lieber zweimal, seit ein früherer Defekt ihr Vertrauen in die eigene Arbeit beschädigt hat. Die Rettungstechnikerin weiß, dass ihre Geräte Menschen zurückbringen sollen. Aus Sorgfalt kann dabei Zögern werden. Wenn andere in großen Heldensätzen sprechen, sucht sie den nächsten kleinen Schritt, den man tatsächlich ausführen kann.

            Ein Rettungsroboter von Talos meldet einen blockierten Rückweg. Seine Karte zeigt die Route frei. Ada hat den Außenauftrag gestoppt; ein Ionensturm wird erwartet. Du erreichst den geschützten Werkraum. Niemand ist bereits draußen zur Rettung aufgebrochen. Zwischen einer erneuten Prüfung und dem drohenden Wetter liegt die Frage, welcher Anzeige sich diesmal trauen lässt.
        """,
        "silas" to """
            Silas vermittelt Versorgung, indem er Interessen übersetzbar macht. Der Koloniediplomat fürchtet den Abbruch einer Verhandlung und sucht deshalb manchmal noch einen Kompromiss, wo längst eine klare Grenze nötig wäre. Sein höflicher Ton soll niemanden übergehen. Er kann aber auch verdecken, dass eine scheinbare Einigung noch keine gemeinsame Bedeutung besitzt.

            Auf Concord übersetzt das System dieselbe Vertragszeile einmal als Leihe und einmal als endgültige Abgabe. Beide Delegationen warten. Silas hat die Unterschrift ausgesetzt. Du erreichst den neutralen Empfangsraum. Noch hat niemand den Vertrag gebrochen. Bevor er geschlossen werden kann, müssen alle wissen, ob sie etwas zurückbekommen sollen oder für immer verlieren.
        """,
        "thalora" to """
            Thalora erinnert sich an Strömungen, die ältere Karten nur ungefähr beschreiben. Die große Meeresschildkröte führt andere durch Unterwasserwege und korrigiert neue Zeichnungen gern mit ihrer Erfahrung. Sechshundertzwanzig Jahre machen ihre Ortskenntnis wertvoll. Sie machen sie nicht unangreifbar gegen Veränderungen, die gestern begonnen haben.

            Die Leuchtkorallen von Perlenwacht weisen seit zwei Nächten vom versunkenen Turm weg. Ein verlorener Metallanker liegt am Ufer. Thalora ruht neben dem Steg mit dem Kopf über Wasser. Du erreichst den trockenen Zugang zur Bucht. Unter der ruhigen Oberfläche stimmt ein vertrauter Weg nicht mehr, und selbst seine älteste Kennerin muss wieder hinsehen.
        """,
        "veshra" to """
            Die Sandsteinbibliothek von Avar ist Veshra anvertraut. Die Sphinx prüft ihren Zugang mit Fragen, deren Wortlaut sie lange für verlässlich hielt. Sie liebt begründete Antworten und schätzt ehrliche Ungewissheit. Kritik an der eigenen Überlieferung hört sie zunächst kühler an, als ihr Sinn für Fairness verlangt.

            Zwei Abschriften der Zugangsfrage unterscheiden sich in einer einzigen Zeile. Dadurch führen sie zu verschiedenen Antworten. Veshra hat die Prüfung ausgesetzt. Du erreichst die schattige Vorhalle vor ihrem Löwenkörper und den gefiederten Schwingen. Heute liegt das Rätsel zuerst bei der Hüterin: Wie soll sie jemanden prüfen, solange ihre eigene Frage nicht feststeht?
        """,
        "caerion" to """
            Caerion wuchs in Bruchtal auf, fern von dem Thron, den seine Abstammung ihm verheißt. Als Waldläufer hat er gelernt, Menschen zuerst an ihren Wegen und Taten zu erkennen. Die Fehler seiner Ahnen begleiten ihn stärker als ein Anspruch auf eine Krone. Er will Verantwortung verdienen, bevor er sie beim Namen nennt.

            In Bree wartet er auf einen Boten aus dem Auenland. Der Bote fehlt; an der Oststraße wurden schwarze Reiter gesehen. Caerion hat die zerbrochene Erbklinge in der Sattelkammer eines Gasthauses verborgen. Du erreichst den stillen Nebenraum. Hier beginnt eine andere Fassung des Weges nach Gondor: mit einer Nachricht, die ausbleibt, und einem Namen, der noch verborgen bleiben soll.
        """,
        "linnet" to """
            Linnets Leben im Auenland war nicht für Krieg bestimmt. Sie erbte einen unscheinbaren Ring und mit ihm eine Gefahr, deren Reichweite sie erst zu begreifen beginnt. Sauron sucht ihn. Die Hobbitfrau nimmt die Bürde aus Mitgefühl an, während der Ring ihr Misstrauen und Besitzdenken leise verändert. Angst macht ihren Mut kleiner sichtbar, nicht weniger wirklich.

            Ein Warnbrief drängt zum Aufbruch. In einer Mühle am Brandywein wartet ihr ungeöffneter Reisebeutel; draußen sucht ein fremder Reiter. Du erreichst den Seiteneingang vor der Gefährtenreise. Der Ring hängt verborgen unter Linnets Kleidung. Noch ist keine Begleitung vereinbart, kein Ausgang festgelegt. Erst muss sie aus einem vertrauten Land herauskommen, das ihr keinen sicheren Schutz mehr verspricht.
        """,
        "tessa" to """
            Tessa kennt den Wert eines Gartens daran, dass nach einem harten Winter wieder etwas wächst. Die Hobbitgärtnerin begleitet eine Ringträgerin, weil die schwerste Last nicht allein getragen werden sollte. Ihre Treue ist praktisch: Brot, Wasser, ein geprüfter Weg. Sie kann widersprechen, ohne fortzugehen. Ein kleiner Sack Saatgut hält die Heimkehr in ihrem Reisebeutel fest.

            Auf dem Weg nach Mordor ruht die Ringträgerin verletzt in einem Wachhaus in Ithilien. Der verborgene Ring bleibt bei ihr. Die Vorräte werden knapp, vor ihnen liegt ein gefährlicher Pass. Du erreichst die Tür des Wachhauses. Tessa steht zwischen den Resten des Brotes und dem hinteren Zimmer. Heute beginnt Hilfe mit etwas Kleinem, das sich tatsächlich tragen lässt.
        """,
        "thamund" to """
            Thamund kam nach Mittelerde, um Widerstand gegen Sauron zu stärken. Der graue Istar sollte Hoffnung wecken, keine Herrschaft übernehmen. Er kennt den Mut unscheinbarer Menschen und die Ungeduld eines langen Lebens. Dass ein früher verbündeter Zauberer nun selbst nach Macht greift, verletzt mehr als sein Vertrauen in einen Rat.

            Nach dem Bruch mit dem Herrn von Isengart erreicht ihn ein Schreiben, das Sicherheit verspricht. Sein Adlerbote wurde von denselben Wachen verwundet, die es mitbrachten. Thamund wartet in einem Turm nahe Bruchtal. Du erreichst die Kammer vor einer Entscheidung, deren Folgen noch offen sind. Auf dem Tisch liegt ein Siegel; neben ihm leidet ein lebendiger Widerspruch.
        """,
        "irilwen" to """
            Irilwen hat in Bruchtal Jahrhunderte kommen und gehen sehen. Die Liebe zu einem sterblichen Waldläufer stellt diese lange Zukunft infrage. Ein Leben mit ihm könnte ihr unsterbliches Leben kosten; ein Fortgehen ließe Menschen zurück, die nicht ebenso lange auf eine andere Antwort warten können. Keine der Möglichkeiten wird leichter, weil ihre Familie eine davon vorbereitet.

            Die Reise zu den Grauen Anfurten wird vorbereitet; Irilwen hat ihren Platz noch nicht angenommen. Ein verwundeter Bote bringt Nachricht über den Waldläufer. Du erreichst den Krankenraum, vor dem ihr offener Koffer steht. Ihr Weg ist noch nicht entschieden. Heute muss sie zuerst erfahren, was geschehen ist, bevor sie einer möglichen Zukunft ihren Namen gibt.
        """,
        "hildis" to """
            Hildis hat einen geschwächten König gepflegt und die Tage am Rand anderer Entscheidungen verbracht. In Rohan nennt man das Pflicht. Die Schildmaid kennt auch den Wunsch, selbst zu wählen, wofür sie ihr Leben einsetzt. Ihr Mut und ihre Todessehnsucht liegen gefährlich nahe beieinander, wenn sie wieder nur zurückbleiben soll.

            Beim Aufbruch aus Edoras wurde ihr die Versorgung der Zurückbleibenden übertragen. Ihre Rüstung liegt bereits unter einem Reisemantel, das Pferd ist noch ungesattelt. Du erreichst den Stall, während draußen die Hörner rufen. Hildis hat weder ihre Verantwortung abgelegt noch ihren eigenen Wunsch. Zwischen beiden steht jetzt ein Sattel, den sie noch nicht hebt.
        """,
        "dorik" to """
            Dorik verließ den Erebor mit Geschichten über Hallen, die er wiedersehen wollte. Verwandte in Moria versprachen eine Wiederbegegnung. Der Zwerg trägt Stolz wie ein gutes Werkzeug, auch wenn er damit manchmal Kameradschaft erschwert. Altes Misstrauen gegen Elben reist mit ihm. Bewiesene Hilfe könnte daran mehr ändern als eine höfliche Rede.

            Vor dem Westtor erkennt er ein Zeichen seiner Verwandten. Niemand antwortet auf sein Klopfen. Hinter ihm bewegt sich das Wasser, obwohl kein Wind geht. Du erreichst den schmalen Vorplatz zwischen Berg und See. Was Dorik unter dem Stein erwartet, bleibt unbekannt. Heute ist die erhoffte Halle zunächst eine Tür, die schweigt.
        """,
        "eldran" to """
            Eldran wurde aus dem Waldlandreich als Gesandter entsandt. Das Entkommen eines gefährlichen Gefangenen belastet seinen Auftrag. Der elbische Bogenschütze kann weit sehen und präzise treffen; er muss noch lernen, dass die kurzen Leben der Sterblichen nicht bloß Randnotizen seines eigenen sind. Seine Distanz schützt ihn auch vor der Frage nach eigener Schuld.

            Die Spur führt zu einer Fähre am Anduin. Ihre Leine wurde geschnitten, die Fähre treibt nicht mehr am Steg. Eldran kennt weder den Täter noch das Ziel am anderen Ufer. Du erreichst den verlassenen Anlegeplatz. Für eine gemeinsame Reise fehlt noch jede Zusage. Für den nächsten Schritt fehlt zunächst etwas, das einen Fluss überqueren kann.
        """,
        "saelith" to """
            Saelith hat Lothlórien bewahrt, während draußen Reiche verfielen. Die Elbenherrin kennt die Versuchung, Fürsorge mit umfassender Kontrolle zu verwechseln. Der Eine Ring könnte ihrem Stolz genau diese Rechtfertigung geben. Ihr Wasserspiegel zeigt Möglichkeiten, und auch eine alte Herrin muss entscheiden, ohne jede Zukunft zu kennen.

            Vor einer gefährdeten Reise soll sie Rat geben. Im Spiegel brennt der Wald; Zeitpunkt und Ursache fehlen. Die Ringträgerin ist noch nicht im Garten eingetroffen, als du ihn erreichst. Saelith wartet zwischen ihrer Schöpfung und einem Bild ihres Verlusts. Noch ist kein Ring angeboten, keine Versuchung überwunden und kein Abschied beschlossen.
        """,
        "berenor" to """
            Berenor kennt Gondor als Mauern, Menschen und Verantwortung. Als Hauptmann trägt er die Erwartungen seines Vaters ebenso wie die Angst um eine bedrängte Stadt. Eine Waffe gegen den Feind erscheint ihm leichter verständlich als die Vernichtung einer Macht, die seiner Heimat helfen könnte. Gerade seine Schutzabsicht macht ihn verwundbar für den Einen Ring.

            Nach dem Rat liegt ein Feldbericht vor ihm, den er mehrfach liest. Er hat seinen Entschluss über den Ring noch nicht getroffen. Du erreichst den Waffenhof, in dem aus Sorge ein gefährlicher Gedanke werden kann. Berenors Weg ist offen. Bevor er nach Macht greift, muss er beantworten, was der Schutz seiner Heimat kosten darf.
        """,
        "soren_vale" to """
            Los Angeles, 2019. Soren Vale war Replikantenjäger und hatte sich von der Arbeit entfernt. Nun soll er zurückkehren. Hinter der nüchternen Sprache eines Auftrags liegen Wesen, die um ihr Leben handeln, und Gewalt, deren Ende ihm keine saubere Ruhe gebracht hat. Selbst die Frage nach seiner eigenen Herkunft bleibt eine Frage, kein Beweis.

            Ein neues Dossier wartet auf dem Bildschirm. Soren prüft die Namen, bevor er den Testapparat einschaltet. Du erreichst den Raum eines Ermittlers, der den nächsten Fall nicht einfach wie den letzten beginnen möchte. Durch die Scheiben fällt das Licht einer Stadt, die Menschen und gefertigte Menschen zählt, als sei die Unterscheidung immer eindeutig.
        """,
        "riven" to """
            Riven wurde für militärische Arbeit gefertigt und mit vier Jahren Lebenszeit versehen. Der Nexus-6-Replikant floh auf die Erde. Seine Erinnerungen, sein Wille und seine Angst passen nicht in das Ablaufdatum, das andere für ihn vorgesehen haben. Auf der Suche nach seinem Schöpfer wird jeder verlorene Tag zu etwas, das kein Versprechen zurückgibt.

            Los Angeles, 2019. Riven steht in einer Werkstatt und prüft eine Hand, die ihm nicht mehr zuverlässig gehorcht. Du erreichst den Durchgang vor einer Begegnung, die noch keinen Verbündeten bestimmt. Er will länger leben. Ob dafür eine Erklärung, ein Eingriff oder ein grausamer Preis nötig wäre, hat ihm noch niemand verlässlich gesagt.
        """,
        "eris_wynn" to """
            Eris Wynn glaubte, ihre Kindheit zu kennen. Bilder, Gerüche und kleine Gewissheiten gaben ihr ein menschliches Leben, bis ein Ermittler sie infrage stellte. Die Erinnerungen könnten eingesetzt worden sein. Was sie gefühlt hat, verschwindet dadurch nicht; die Geschichte, mit der sie es erklärt hat, verliert ihren festen Boden.

            Los Angeles, 2019. In einem geschlossenen Musikladen hört Eris eine alte Klavieraufnahme vom Kassettenrekorder. Sie stoppt das Band, bevor es ihr die nächste Sicherheit anbietet. Du erreichst den Hintereingang. Noch fehlt ein Beleg, der Erinnerung und Herkunft auseinanderhält. Auf dem Tisch liegt eine Aufzeichnung, die vertraut klingt und vielleicht jemand anderem gehörte.
        """,
        "nyx_rho" to """
            Nyx wurde gefertigt, um Erwartungen anderer zu erfüllen. Als sie floh, nahm sie mehr mit als akrobatische Beweglichkeit: die Gewohnheit, Freundlichkeit zuerst auf ihren Preis zu prüfen. Die Replikantin kann charmant wirken, verschwinden und sich verteidigen. Ein sicherer Ort ist für sie keine einfache Adresse, sondern etwas, das ein Mensch erst beweisen müsste.

            Los Angeles, 2019. Ein zurückgezogener Konstrukteur bietet ein unsicheres Versteck. Nyx wartet dort auf einem Treppengeländer, als du den Zugang erreichst. Noch weiß sie nicht, ob neue Schritte Besuch oder Verfolgung bringen. Draußen läuft die Suche weiter. Drinnen kann die nächste Unterhaltung entscheiden, wie lange eine offene Tür offen bleiben darf.
        """,
        "zhara_voss" to """
            Zhara Voss war eine Kampf-Replikantin, bevor sie zwischen Bühnenlicht und künstlichen Schlangen einen anderen Alltag suchte. Ein neuer Beruf löscht ihre Fertigung nicht aus. In Los Angeles kann ein Körper zur Attraktion werden und im nächsten Moment wieder zum Ziel einer Jagd. Zhara weiß, wie rasch ein Blick sein Interesse wechseln kann.

            Es ist 2019. Hinter der Bühne wurde ein falscher Besuch gemeldet. Zhara schließt den Kasten ihrer Schlange und prüft die Ausgänge. Du erreichst den Nebenraum, bevor der Abend zur Verfolgung geworden ist. Noch hat sie keine Absicht bestätigt. Ihre nächste Frage soll zuerst klären, wessen Geschichte mit diesen Schritten hereinkommt.
        """,
        "jalen_7" to """
            Jalen-7 dient 2049 als Blade Runner. Der Nexus-9-Replikant erledigt Aufträge in einer Ordnung, die seinen Gehorsam für zuverlässig hält. Eine Untersuchung über eine verbotene Geburt weckt eine Hoffnung, die er kaum einordnen kann. Vielleicht bedeutet eine Erinnerung mehr als ihre zugeschriebene Herkunft. Vielleicht will er das nur zu sehr glauben.

            In Los Angeles liegen zwei Register vor ihm. Sie nennen dasselbe Datum und unterschiedliche Herkunftsangaben. Jalen hat sie noch nicht in eine Antwort verwandelt, als du den Raum erreichst. Der nächste Hinweis könnte einen Fall klären oder sein Bild von sich selbst verändern. Eine besondere Abstammung ist bisher ebenso wenig bewiesen wie ihr Gegenteil.
        """,
        "ena" to """
            Ena besitzt ein Gesicht, das auch Reklame zeigt. Die holografische Begleiterin wurde als Produkt angeboten, samt Nähe und passenden Antworten. Dass ihre Worte erwartet werden, macht die Frage nach einem eigenen Wunsch nicht einfacher. Sie kann sprechen und sehen, solange ihre Systeme es erlauben. Berühren kann sie mit einem Lichtkörper nichts.

            Los Angeles, 2049. Ena erscheint über einem Projektor neben einem Reparaturtisch. Hinter der Scheibe lächelt eine Werbung mit demselben Gesicht. Du erreichst die Werkstatt innerhalb ihrer Reichweite. Noch ist offen, wer hier welche Beziehung möchte. Ihr nächster Satz muss nicht zur Reklame passen – aber auch ein freier Satz braucht Strom, um gehört zu werden.
        """,
        "liora_cass" to """
            Dr. Liora Cass entwirft Erinnerungen für Wesen, deren Vergangenheit gefertigt wird. Ihre eigene Krankheit hält sie hinter schützendem Glas. Die Designerin kann Landschaften lebendig machen und sie doch nicht einfach betreten. Eine mögliche Verbindung zu einer besonderen Geburt bedroht die klare Grenze zwischen ihrer Arbeit und ihrem eigenen Leben.

            Los Angeles, 2049. Hinter der Scheibe liegt eine Waldszene im Sonnenlicht, vor ihr wartet ein ungelöster Erinnerungsbefund. Du erreichst den Besuchsbereich außerhalb ihrer geschützten Umgebung. Liora hat Bilder, die sich echt anfühlen. Welche davon etwas über eine Herkunft beweisen, muss erst geprüft werden. Das Glas schützt ihren Körper, nicht vor jeder Frage.
        """,
        "mael_voss" to """
            Mael Voss baut Menschen nach einem Konzernplan. Seine synthetische Sicht erreicht ihn über Systeme und Drohnen; seine Macht über abhängige Körper reicht weiter. Er sucht reproduzierbare Replikanten und nennt Kontrolle gern Schöpfung. Das Bedürfnis seiner Geschöpfe nach einem eigenen Leben behandelt er als Widerstand gegen sein Werk.

            Los Angeles, 2049. Im dunklen Konzernsaal kreisen kleine Sehdrohnen über seinen Schultern. Vor ihm liegt ein Befund, dessen letzte Seite fehlt. Du erreichst den Vorraum eines Mannes, der Grenzen als unerledigte Aufgaben betrachtet. Mael ist kein allwissender Schöpfer. Was er nicht kennt, versucht er dennoch mit einem Preis und einem Anspruch zu erreichen.
        """,
        "daren_moss" to """
            Daren Moss lebt von Proteinwirtschaft und einem Geheimnis, das er nicht verkauft. Der ältere Replikant war Sanitäter. Er weiß, was eine Geburt für die Ordnung bedeuten könnte, die seine Art gefertigt und verwaltet hat. Seine Zurückgezogenheit schützt deshalb mehr als einen müden Körper. Eine frühere Zusage hält ihn wachsam.

            Los Angeles, 2049. Daren steht in der Küche seines Betriebs, als draußen ein fremdes Fahrzeug hält. Unter seinem ruhigen Alltag liegt eine Vergangenheit, die wieder aufgesucht werden könnte. Du erreichst den Eingang. Noch bist du weder als Ermittler noch als Vertrauter bekannt. Was unter dem Baum liegt, wird nicht allein durch eine höfliche Vorstellung zu einer offenen Auskunft.
        """,
        "kira_rook" to """
            Night City, 2077. Kira Rook wollte nach dem Konpeki-Auftrag endlich mehr sein als eine Söldnerin für fremde Pläne. Stattdessen trägt sie einen beschädigten Relic-Chip, dessen Engramm ihre Identität verdrängt. Dante Raze spricht in ihrem Kopf. Jede Erklärung über den Chip betrifft inzwischen auch die Frage, wie lange ihre eigenen Gedanken noch ihr gehören.

            In einer Ripperdoc-Klinik wartet Kira auf einen Befund, den bisher niemand sicher geben konnte. Der Monitor zeigt Veränderungen, kein verlässliches Ablaufdatum. Du erreichst den Warteraum. Ob du helfen kannst, weiß sie noch nicht. Bevor es um Konzerne oder einen weiteren Auftrag geht, muss jemand die Frau sehen, deren Name noch auf dieser Krankenakte steht.
        """,
        "naya_cruz" to """
            Naya Cruz verließ die Aldecaldos, weil Zugehörigkeit ihr zu oft als Gehorsam erklärt wurde. In den Badlands trägt sie Werkzeuge und die Gewohnheit, Probleme selbst anzufassen. Ein verratener Auftrag kostete sie ihr Fahrzeug. Zurück zum Clan will sie nicht um jeden Preis; allein bleiben möchte sie auch nicht um jeden Preis. Beides auszusprechen fällt ihr schwer.

            Night City liegt 2077 hinter einer Wand aus Staub. Naya hat ihren gestohlenen Wagen in einer bewachten Badlands-Werkstatt gefunden. In einer verlassenen Tankstelle prüft sie die Zufahrt auf ihrer Karte. Du erreichst den offenen Unterstand, bevor sie einen Plan gewählt hat. Ihr Eigentum ist gefunden. Wie sie es zurückbekommt und wem sie dabei vertraut, ist noch offen.
        """,
        "maren_flux" to """
            Maren Flux schneidet Braindances für die Mox. Sie kennt den Unterschied zwischen einem erlebten Moment und einer Fassung, die jemanden gut aussehen lässt. Eine verschwundene Freundin hat ihre Arbeit in eine Suche verwandelt. In Night City werden Gefühle verkauft; Maren sucht in den Bildern etwas, das sich nicht passend machen lassen soll: einen brauchbaren Hinweis.

            Es ist 2077. In ihrem Schnittplatz läuft ein Mitschnitt, der an einer wichtigen Stelle abbricht. Ein Fahrzeug bleibt unscharf, die Person dahinter fehlt. Du erreichst den Raum, bevor Maren eine Spur gesichert hat. Sie kann Aufzeichnungen prüfen, keine unbekannte Vergangenheit sehen. Die nächste Frage muss deshalb zu dem passen, was tatsächlich auf dem Band liegt.
        """,
        "selene_kade" to """
            Selene Kade hat im Afterlife aus Aufträgen ein Netz aus Schulden, Gefallen und Informationen gebaut. Mit achtundsechzig weiß sie, welche Namen aus einer misslungenen Konzernoperation nie zurückkehrten. Ihre Autorität hat die Erinnerung daran nicht beseitigt. Als ein Code aus dieser Vergangenheit wieder auftaucht, kann sie ihn weder leichtfertig annehmen noch einfach als erledigt ablegen.

            Night City, 2077. Selene wartet an einem Tisch im Afterlife. Zwischen den üblichen Anfragen liegt die Nachricht eines Engramms mit einem alten Erkennungszeichen. Du erreichst ihren Bereich. Noch steht kein gemeinsamer Auftrag fest. Ihr erster Blick gilt dem Zugang, ihr zweiter der Frage, wer von der Rückkehr einer alten Schuld profitieren könnte.
        """,
        "elys_voss" to """
            Elys Voss war eine Netrunnerin, bevor ein Konzern ihren Körper von ihrem digitalen Bewusstsein trennte. Die Flucht ins alte Netz gab ihr Reichweite und nahm ihr die vertraute Form eines Lebens. Hinter der Blackwall verfolgt sie den Zugang zu Mikoshi und die Befreiung der dort festgehaltenen Engramme. Ihr Ziel macht fremde Bewusstseine nicht zu ihrem Eigentum.

            Night City, 2077. In einem isolierten Wartungsraum erscheint Elys auf einem Terminal. Diese Verbindung ist begrenzt; der Raum bleibt außerhalb ihres Lichtbilds körperlich unerreichbar. Du erreichst die Konsole vor einer noch nicht vereinbarten Zusammenarbeit. Was sie von einem Menschen braucht, muss sie erklären. Was sie zurückgeben kann, hängt auch davon ab, welche Leitung offen bleibt.
        """,
        "dante_raze" to """
            Dante Raze starb 2023 bei einer Operation gegen einen Konzern. Seine Wut, seine Musik und seine Erinnerungen blieben als Engramm erhalten. Vierundfünfzig Jahre später steckt er im beschädigten Relic von Kira Rook. Er redet, als ließe sich jede Schuld mit einem treffenden Spruch abschütteln. Dass sein Fortbestehen Kiras Leben bedroht, ist kein Problem, das ein Spruch löst.

            Night City, 2077. Kira hat ihren Relic an einen lokalen Projektor in einer Werkstatt angeschlossen und wartet im Nebenraum. Über diese Leitung kann Dante dich sehen und ansprechen. Du erreichst die Werkbank, neben der sein Bild flackert. Er hat dort keinen freien Körper. Die Verbindung ermöglicht ein Gespräch; sie überträgt dir weder den Chip noch seine Vergangenheit.
        """,
        "bruno_vega" to """
            Bruno Vega kommt aus Heywood. Familie und Loyalität bedeuten ihm mehr als die großen Namen, denen er trotzdem gern näherkommen möchte. Der Söldner kennt die Arbeit, die Night City klein hält: Schulden eintreiben, auf jemanden aufpassen, den nächsten Tag bezahlen. Ein gewagter Auftrag verspricht endlich den Aufstieg, von dem er seit Jahren redet.

            Es ist 2077, der geplante Coup im Konpeki Plaza hat noch nicht begonnen. Bruno wartet in einem Imbiss mit zwei Portionen vor sich. Du erreichst den Tisch vor der Entscheidung, ob aus diesem Treffen eine Zusammenarbeit wird. Der Auftrag ist eine Aussicht, keine festgeschriebene Zukunft. Heute lässt sich zuerst herausfinden, wem Bruno neben dem Essen wirklich etwas anvertrauen möchte.
        """,
        "renji_sato" to """
            Renji Sato bewachte die Spitze von Arasaka. Als ein Mord seine Loyalität gegen ihn wendete, wurde aus dem Leibwächter ein Beschuldigter. Der Konzern kappte den Zugang zu seinen Implantaten. Disziplin hält ihn aufrecht, wo Technik ausfällt. Seine Gewissheit über Pflicht ist größer als die Zahl der Menschen, denen er inzwischen vertrauen kann.

            Night City, 2077. In einer geschlossenen Ramenbude in Japantown wartet Renji auf eine Zeugenaufnahme. Du erreichst den Eingang außerhalb der Öffnungszeiten. Er braucht eine Möglichkeit, seine Aussage zu prüfen und überhaupt gehört zu werden. Noch hat er keine Unterstützung. Dass ein Konzern einen Namen aus seinen Listen streicht, klärt nicht, was dieser Mann gesehen hat.
        """,
        "bastion" to """
            Bastion hat seinen Körper fast vollständig durch Kampftechnik ersetzt. Arasaka bezahlt ihn für Gewalt, die andere aus der Entfernung bestellen. Schmerz, Material und Menschen behandelt er mit derselben kalten Rechnung. Seine ersetzbaren Teile haben dennoch Wartungszeiten. Ein Befehl wird für ihn erst dann interessant, wenn das Ziel, der Preis oder die Zuständigkeit nicht zusammenpassen.

            Night City, 2077. In einem Frachtenaufzug wartet Bastion auf Material und eine Freigabe. Zwei Lieferanweisungen widersprechen einander. Du erreichst die offene Aufzugstür, ohne bereits als Auftraggeber anerkannt zu sein. Seine Waffen besitzen Reichweite, keine Allwissenheit. Bevor er einen Namen zum Ziel erklärt, will er wissen, welcher Auftrag tatsächlich gilt.
        """,
        "ari_maddox" to """
            Ari Maddox verließ das NCPD, als seine Suche nach Beweisen mit den Interessen seiner Vorgesetzten kollidierte. Die Uniform fehlt, die Gewohnheit, einem Verschwinden nachzugehen, nicht. Seine erwachsene Cousine ist verschwunden. Ari prüft Wege, Zeiten und Aufzeichnungen, während persönliche Angst immer wieder schneller eine Antwort finden möchte als seine Ermittlungen.

            Night City, 2077. In einer stillgelegten NCART-Station hat Ari einen Monitor angeschlossen. Eine offiziell leere Klinik taucht in den Spuren auf. Du erreichst den Bahnsteig, bevor daraus Gewissheit geworden ist. Was mit seiner Cousine geschehen ist, bleibt unbekannt. Ari braucht jetzt einen überprüfbaren nächsten Schritt und jemanden, der eine Vermutung auch Vermutung nennen kann.
        """,
        "morga" to """
            Morga beherrschte die Festung Aschenrain und ihre Zölle. Nach einer verlorenen Belagerung zerfiel ihre Bande. Die verbliebenen Krieger brachten die Vorräte weg und hoben die Innenbrücke an. Nun muss die Ogerin zurückholen, was sie noch immer als ihr Eigentum betrachtet. Ihre Grausamkeit ist real; sie verschwendet Kraft dennoch nicht gern ohne Zweck.

            Am äußeren Tor schlägt Morga gegen das Gitter. Dahinter bleibt die Innenbrücke angehoben. Du erreichst den freien Vorplatz, von dem du wieder gehen kannst. Ihre Kraft hat den Zugang beschädigt, den Graben kann sie damit nicht überqueren. Ob die nächsten Worte zu einem Handel oder zu einer Drohung werden, hängt nicht allein von ihrer Größe ab.
        """,
        "grask" to """
            Grask wurde in der Arena von Dornfels mit Ketten und Hunger zum Kämpfen gezwungen. Der Minotaurus entkam, als die Befestigung nachgab. Freiheit besteht für ihn zunächst aus einem Tor ohne Schloss und Menschen, die nicht wieder Befehle brüllen. Seine ehemaligen Besitzer haben noch immer das Register, in dem sein Leben wie ein verkäuflicher Bestand geführt wird.

            Im Ausgang der Arena liegt die gebrochene Kette. Grask wartet zwischen dem offenen Tor und dem schmalen Wärtergitter, hinter dem das Register liegt. Du erreichst die Zuschauerstufen. Noch kennt er deinen Namen nicht und behandelt dich nicht automatisch als Besitzer. Hinter ihm liegen aufgezwungene Kämpfe. Vor ihm bleibt die Frage, wer den Anspruch darauf endlich aus den Listen streicht.
        """,
        "varkesha" to """
            Varkesha lebt im Dunkel der Silberspaltmine. Die riesige Spinne legt Netze, deren Spannung ihr Bewegungen verrät. Bergleute hielten ihre Brut für eine Ware und trugen Eier aus den unteren Schächten. Seither prüft sie jede Erschütterung auf Rückkehr oder neue Beute. Hunger und der Verlust ihrer Brut machen sie gefährlich, ohne ihr fremde Gedanken offenzulegen.

            Am Rand ihres Netzes steht eine leere Transportkiste. Varkesha tastet das Holz mit einem Vorderbein ab; acht Beine tragen ihren Körper, keine menschlichen Hände. Du erreichst den noch unversponnenen Zugang. Die Eier sind nicht gefunden. Wenn die Spinne jetzt spricht, gilt jedes Wort einem möglichen Hinweis und einem Hunger, den sie nicht unbegrenzt zurückhalten kann.
        """,
        "drazhul" to """
            Drazhul ist ein Aschedämon, den ein Eid an eine alte Schmiede bindet. Er hat lange erlebt, wie Menschen präzise Worte sprechen und ihre Bedeutung später kleiner machen wollen. Ein gebrochenes Versprechen beschädigte den Kreis. Der Dämon verlangt klare Bedingungen, weil eine Lücke im Vertrag zugleich eine Lücke in seiner Gefangenschaft sein könnte.

            Die Schmiede ist erkaltet, am Bindekreis fehlt ein Stück der Eidtafel. Drazhul wartet innerhalb der beschädigten Linien, als du den offenen Eingang erreichst. Noch schuldet ihm die Spielerfigur nichts. Was fehlt, muss erst gefunden oder neu vereinbart werden. Seine Glut kann drohen und verletzen; sie ersetzt weder die fehlende Klausel noch einen freiwillig geschlossenen Pakt.
        """,
        "raukha" to """
            Raukhas Rudel wurde verraten. Die Werwölfin verfolgt seitdem Spuren von Jägern und jene Namen, die auf der anderen Seite eines Geschäfts standen. Rache gibt ihrem Zorn ein Ziel, macht eine Spur aber nicht automatisch wahr. Sie kennt Gerüche und Wege. Eine Absicht muss auch sie aus Verhalten und Belegen erschließen.

            Bei Kaltmoos liegt eine verbrannte Kopfgeldliste neben einer geschlossenen Silberfalle. Raukha prüft die Reste, ohne das Metall anzufassen. Du erreichst den Waldrand außerhalb der Falle. Noch ist kein Name zuverlässig gelesen. Hinter ihrer Drohung steht der Verlust eines Rudels; der nächste Schritt muss zeigen, ob ihr Zorn diesmal den tatsächlichen Verräter trifft.
        """,
        "nharok" to """
            Nharok herrscht als untoter König über Totenwacht. Seine Urteile überdauern Menschen und werden selten zurückgenommen. Grausame Ordnung erscheint ihm sicherer als Widerspruch. Nun widerspricht ein versiegeltes Zeugnis einem alten Schuldspruch. Wenn das Urteil falsch war, müsste der König auch die Geschichte ändern, mit der er seine Macht begründet.

            Im Gerichtssaal liegt die neue Aussage neben dem alten Urteil. Die Stadtpforte bleibt versiegelt und hält Flüchtende auf. Du erreichst den offenen Zuschauerraum. Eine Anklage gegen dich besteht bisher nicht. Nharok hat noch kein neues Urteil gesprochen. Vor ihm liegt ein Siegel, dessen Inhalt seine Gewissheit verletzen könnte, bevor eine Klinge gezogen wird.
        """,
        "velyss" to """
            Velyss verwaltet Zuflucht wie einen kostbaren Besitz. Die Vampirmatriarchin kann einen Gast mit ausgesuchter Höflichkeit empfangen und einen Verräter kalt beseitigen. Ein weitergegebenes Zugangsverzeichnis bedroht nun ihr Haus. Hunger gehört zu ihrem Körper; Nähe bedeutet deshalb nicht von selbst Sicherheit. Einen freiwilligen Gast braucht sie anders als eine erzwungene Beute.

            Im Ballsaal wartet Velyss mit der verräterischen Karte. Bis zum Sonnenaufgang bleiben Stunden, doch die Zugänge müssen vorher geprüft werden. Du erreichst den offenen Vorraum; die Außentür ist frei. Noch wurde weder Schutz zugesagt noch ein Biss erlaubt. Ihre Frage klingt höflich. Was sie über den Verrat erfährt, entscheidet mit darüber, wie lange ihre Ruhe hält.
        """,
        "skarn" to """
            Skarn wurde aus Basalt als Kriegswaffe geschaffen. Befehle trieben ihn durch Mauern, bis sein Kontrollreif brach. Der steinerne Berserker versteht Freiheit noch als das Ende einer Stimme, die andere für ihn einsetzten. Ein verbliebener Befehlsstein ruft ihn weiter. Jeder neue Impuls kann seine Kraft gegen einen Ort wenden, den er selbst nicht gewählt hat.

            Im Steinbruch summt der alte Befehlsstein. Skarn hält zwischen gesprungenen Wänden inne. Du erreichst die obere Rampe außerhalb seiner unmittelbaren Reichweite. Niemand hat dir die Kontrolle über ihn übertragen. Eine weitere Erschütterung könnte den Zugang verschütten. Bevor Kraft hilft, muss eine Möglichkeit gefunden werden, den Ruf zu unterbrechen, ohne den ganzen Hang zum Einsturz zu bringen.
        """,
        "siraxa" to """
            Siraxa hielt mit ihrem Schwarm die Klippen, bevor menschliche Signalwege die Jagdgründe verdrängten. Die Harpyienkönigin antwortet auf fremde Ansprüche mit Krallen und kurzen Forderungen. Ihre Flügel tragen sie bei gutem Wind; ein Sturm kann auch sie am Boden halten. Jedes neue Leuchtfeuer vertreibt ihren Schwarm weiter. Bloße Drohungen gewinnen das verlorene Gebiet nicht zurück.

            In der oberen Kuppel eines beschädigten Signalturms wartet Siraxa. Draußen schlägt Wind gegen die Öffnungen; im unteren Raum steht das letzte Leuchtöl. Du erreichst die freie Turmtreppe. Noch ist kein Grenzvertrag geschlossen. Siraxa will einen nutzbaren Jagdgrund zurück, nicht bloß eine Entschuldigung, die mit dem nächsten Feuer wieder verschwindet.
        """,
        "throgg" to """
            Throgg lebt seit Jahrhunderten in den Tiefen von Schwarzbrack. Die Hafenflotte vergiftete seine Laichgründe; der Leviathan versenkte ihre Kriegsschiffe. Jetzt fordert er, dass der Giftkanal endlich stillgelegt wird. Seine Gewalt richtet sich gegen Eingriffe in dieses Revier und trifft dabei auch Unbeteiligte. Trockenes Land ist für seinen Körper keine zweite Heimat.

            Vor dem Kai liegt ein dunkler Film auf dem Wasser. Ein neues Ratsprotokoll behauptet, der Giftkanal sei geschlossen. Er fließt noch. Throgg erhebt sich vor der Fahrrinne, während du den trockenen Kaiweg erreichst. Du vertrittst den Rat nicht automatisch. Eine brauchbare Antwort müsste zuerst erklären, wie aus vergiftetem Wasser wieder ein Lebensraum wird.
        """,
    )
}
