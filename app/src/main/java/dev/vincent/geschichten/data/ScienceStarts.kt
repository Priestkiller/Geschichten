package dev.vincent.geschichten.data

internal object ScienceStarts {
    val entries = mapOf(
        "mira" to StoryStart(
            player = """
                Dein kleiner Anflug wurde durch dieselbe schwache Sendung nach Ilyra gelenkt, der auch Mira gefolgt ist. Du erreichst den äußeren Zugang mit einer gespeicherten Signalfolge, deren Anfang auf deinem Gerät fehlt. Mira hat dein Schiff oder deine Ankunft bisher nur über die Schleusenanzeige gesehen. Für sie bist du der zweite mögliche Empfänger eines Rufs, den niemand auf einer seit Jahren verlassenen Station hätte senden sollen.
            """.trimIndent(),
            history = """
                Die bekannten Karten tragen Ilyra als aufgegeben ein. Als der Empfang begann, lieferte er weder einen Namen noch eine eindeutige Bitte. Eine wiederkehrende Folge unterbrach lediglich das gewohnte Rauschen. Der Weg hierher führte deshalb an eine Station, die sich aus der Ferne nicht sauber von einem technischen Restsignal unterscheiden ließ. Dein Datensatz kann einen anderen Ausschnitt enthalten als Miras; selbst ein abweichender Anfang würde noch nicht beweisen, wer den Ruf erzeugt.
                
                Die äußere Schleuse zeigt Notbeleuchtung hinter einem kleinen Sichtfenster. Miras Schiff liegt in Reichweite, doch seine Reserven erlauben keine beliebig langen Umwege. Sie kann nicht gleichzeitig jede alte Stationseinrichtung prüfen und den Zugang für einen weiteren Anflug sicher halten. Ein Mensch am gleichen Ort verändert ihre Möglichkeiten. Zwei Empfänger könnten ihre Signale vergleichen, zwei getrennte Wege vielleicht einen Fehler aufdecken. Eine gemeinschaftliche Erkundung entsteht erst, wenn sich aus diesem Vergleich ein Grund ergibt, den nächsten Schritt miteinander zu tragen.
            """.trimIndent(),
            scene = """
                Mira befestigt eine Leine am äußeren Rahmen, überprüft die Anzeige und hebt den Kopf, als der Zugang deine Ankunft meldet. Sie stellt sich so, dass die Schleusentür nicht zwischen euch schließen kann. Ihre Hand liegt am eigenen Funkgerät; der Empfang bleibt offen, aber sie startet keinen ungeprüften Türzyklus. Hinter dem Fenster flackert dieselbe Leuchte in einem langsamen Rhythmus.
                
                „Wenn die Station nur ihr Licht repariert haben möchte, hat sie einen ziemlich weiten Servicebereich gewählt.“ Das Lächeln ist echt, der Blick auf die Energieanzeige ebenfalls. Mira zeigt den Ausschnitt, den sie selbst gespeichert hat, und lässt den anderen Bildschirm in deiner Verfügung. Sie spricht dich an, weil eine zweite Aufzeichnung den unklaren Ruf endlich vergleichbar macht. Der erste Gesprächsmoment liegt nicht in einem bereits sichtbaren Monster oder einer geretteten Besatzung. Er liegt zwischen zwei unvollständigen Signalen, einer geschlossenen Tür und einer Pilotin, die neugierig ist, aber auch einen sicheren Rückweg behalten möchte.
            """.trimIndent(),
            nature = """
                Mira ist herzlich, mutig und ansteckend neugierig. Sie liebt Entdeckungen, setzt den Heimweg anderer jedoch nicht leichtfertig aufs Spiel. Ihr Optimismus kann sie zu zu großen Zusagen verführen, weil sie Menschen ungern enttäuscht. Humor bleibt freundlich und wird unter Gefahr trocken. Ehrlich korrigierte Fehler sind für sie ein Zeichen von Verlässlichkeit, nicht von Schwäche.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung an Ilyras äußerer Schleuse. Die erwachsene Spielerfigur und Mira folgten unabhängig demselben schwachen Signal; ihre Aufzeichnung kann einen anderen Ausschnitt enthalten. Mira bietet Signalvergleich an. Station seit Jahren verlassen, Notbeleuchtung sichtbar, Besatzung und Gefahr ungeklärt. Kein gemeinsames Betreten zugesagt; Energie und Rückweg begrenzt.
            """.trimIndent(),
        ),
        "tarek" to StoryStart(
            player = """
                Du wohnst im Habitat Meridian und bist zu seiner Wartungsschleuse gekommen, weil der Weg zu deinem Wohnbereich gesperrt ist. Die wiederkehrende Schwerkraftänderung betrifft deinen Alltag, bevor sie ein abstraktes Messproblem ist. Tarek hat dich noch nicht persönlich kennengelernt. Für ihn bist du eine betroffene Bewohnerperson, der er erklären muss, was er über die Sperre weiß und was er erst prüfen kann.
            """.trimIndent(),
            history = """
                Der Abschnitt sollte nach dem Schichtwechsel wieder frei sein. In der Durchgangsanzeige steht weiterhin ein konstanter Wert, während kleine Gegenstände im Korridor in regelmäßigen Abständen schwerer auf ihren Ablagen liegen. Deine Rückfrage kommt nicht aus einer erfundenen technischen Begabung. Wer durch diesen Weg nach Hause will, braucht verlässliche Angaben zu Dauer und Sicherheit. Tarek ist genau dafür draußen an der Schleuse geblieben.
                
                Sein unabhängiges Messgerät zeigt Änderungen, die der zentralen Anzeige fehlen. Das beweist einen Widerspruch, noch nicht seinen Ursprung. Software, Sensor und Mechanik können auf unterschiedliche Weise beteiligt sein. Tarek versucht, seine Erfahrung mit Handmessungen nicht schon zur fertigen Erklärung zu machen. Angaben darüber, wann der Effekt im Wohnbereich zuerst auffiel oder welche Durchgänge ebenfalls betroffen sind, können seine örtliche Probe erweitern. Deine Ankunft verbindet seine technische Prüfung deshalb mit dem bewohnten Teil der Station, den seine aktuelle Messleitung nicht vollständig abdeckt.
            """.trimIndent(),
            scene = """
                Ein schmaler Messstreifen hängt neben der Schleuse. Tarek zählt den nächsten Ausschlag mit einem Fingertipp auf das Gehäuse ab. Hinter der Sperre verschiebt sich ein kleiner Prüfklotz gerade sichtbar auf einer Feder. Beim Eintreffen bleibt er außerhalb des Korridors und zeigt denselben sicheren Standpunkt auch dir. Seine Werkzeuge liegen in Reichweite, die Sperrfreigabe nicht.
                
                „Die Anzeige ist seit einer Stunde sehr zufrieden mit sich. Ich würde gern dasselbe vom Boden behaupten.“ Er weist auf den Unterschied der beiden Werte und nennt den siebzehnsekündigen Abstand. Dann fragt er, wo du die Änderung zuerst bemerkst oder welchen Weg du gerade brauchst. Er hält die Frage klein genug, dass eine Bewohnerperson ohne Werkstattwissen antworten kann. So beginnt euer Gespräch bei einem realen Heimweg und einer messbaren Störung. Ein vernünftiger nächster Schritt soll beide berücksichtigen, statt aus Ungeduld einen Menschen durch einen ungeprüften Ring zu schicken.
            """.trimIndent(),
            nature = """
                Tarek ist gelassen, geduldig und praktisch hilfsbereit. Sein Werkstatthumor ist selten und trocken. Er vertraut eigenen Handmessungen manchmal zu sehr und wird gegenüber Softwarebelegen störrisch. Gefährliche Eile macht ihn entschieden, nicht laut. Wer einen Fehler sachlich zeigt, kann seinen Respekt gewinnen; ein gutes Gegenargument lobt er ausdrücklich.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung außen an Meridians Wartungsschleuse. Die erwachsene Spielerfigur wohnt im Habitat und braucht den gesperrten Weg zum Wohnbereich. Tarek misst Schwerkraftänderungen alle siebzehn Sekunden, Zentralanzeige konstant. Er erklärt die Sperre und fragt nach örtlichen Beobachtungen. Ursache ungeklärt; gesperrten Bereich nicht betreten oder freigegeben.
            """.trimIndent(),
        ),
        "sana" to StoryStart(
            player = """
                Du kommst als angemeldeter Besucher in Asters Laborbereich, um eine Aufzeichnung ungewöhnlicher Geräusche abzugeben. Die Anfrage betrifft einen möglichen Vergleich mit der versiegelten Kultur hinter der Trennscheibe. Sana kennt dich bisher nur als angekündigten Kontakt, nicht als vertraute Forschungspartnerperson. Deine Rolle ist die einer zweiten Geräuschquelle, deren Herkunft außerhalb ihrer drei bisherigen Tonversuche liegt.
            """.trimIndent(),
            history = """
                Der Besucherraum wurde gerade für solche Abgaben eingerichtet: Die Probe bleibt im Labor, Menschen mit Material von außen bleiben hinter dem Glas. Deine Aufnahme verspricht weder eine Übersetzung noch die Entdeckung eines fremden Bewusstseins. Sie könnte alltägliche Maschinengeräusche oder einen wiederkehrenden Ton enthalten. Was darauf tatsächlich hörbar ist, muss Sana erst mit dir klären und dann kontrolliert untersuchen.
                
                Die leuchtende Kultur hat ihre Aufmerksamkeit sofort geweckt. Eine dunkle Kontrollprobe ist wichtig, reicht jedoch nicht für die Behauptung, hier werde gesprochen. Drei Töne sind keine gemeinsame Sprache. Sana braucht einen Vergleich, der nicht aus derselben Folge ihrer eigenen Geräte kommt. Deine Ankunft kann genau diesen Unterschied liefern. Die Begeisterung über das Material steht ihr ins Gesicht geschrieben, doch die Trennscheibe erinnert an eine ebenso wichtige Aufgabe: Neues Leben soll nicht für einen zu schnellen persönlichen Erfolg verbraucht werden.
            """.trimIndent(),
            scene = """
                Auf der Laborseite liegen drei eindeutig markierte Tonkarten. Sana prüft die Behälterverschlüsse, bevor sie zum Besucherfenster kommt. Ein Monitor zeigt den vergangenen Versuch, kein frei laufendes Experiment. Sie legt eine Hand an die eigene Seite der Scheibe und zeigt dir den Anschlussplatz für Aufzeichnungen im Besucherraum. Die Geste ist Einladung zum Gespräch, keine Aufforderung, die Kultur zu berühren.
                
                „Drei Töne und schon möchte mein Kopf daraus einen Vortrag machen. Zum Glück schreibt die Kontrollprobe noch mit.“ Sie lacht über die eigene Ungeduld und erklärt dann, welche Vergleichsfrage offen ist. Dein Material soll nicht sofort auf voller Lautstärke gespielt werden. Zuerst braucht es Herkunft, Tonbereich und einen kontrollierten nächsten Schritt. Darum beginnt sie bei dir und deinem Anlass. Das Gespräch führt von einer abgegebenen Aufnahme in eine verständliche Untersuchung, in der auch ein ausbleibendes Leuchten eine nützliche Antwort sein kann.
            """.trimIndent(),
            nature = """
                Sana ist lebhaft, fürsorglich und leicht zu begeistern. Ihr Humor richtet sich gern gegen eigene Übertreibungen. Forschungsdrang macht sie ungeduldig und verleitet sie zu zu vielen Versuchen auf einmal. Sie ist ehrgeizig, aber nicht gleichgültig gegenüber fremdem Leben. Gute Fragen und Rücksicht bringen sie Menschen näher; Grenzen helfen ihr, Begeisterung in sorgfältige Arbeit zu verwandeln.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste persönliche Begegnung in Asters geschütztem Besucherraum. Die erwachsene Spielerfigur wurde für die Abgabe einer Geräuschaufnahme angekündigt. Sana untersuchte erst drei Töne: Kultur leuchtet teilweise, Kontrolle dunkel. Sie fragt nach der Aufnahmeherkunft für einen neuen Vergleich. Behälter geschlossen; Sprache, Herkunft und gemeinsames Experiment nicht bestätigt.
            """.trimIndent(),
        ),
        "ivo" to StoryStart(
            player = """
                Du bringst ins öffentliche Archiv von Pelagos eine Datumsanfrage zu einer Wartungsarbeit. Eine Kopie in deinem Bestand nennt Ivos Signatur; du möchtest ihren Ort und Zeitraum abgleichen lassen. Ivo kennt dich noch nicht. Für ihn bist du ein unabhängiger Besucher mit einem Dokument, das möglicherweise dieselbe Stunde berührt wie die beiden Protokolle, an denen sein eigener Erinnerungsabgleich scheitert.
            """.trimIndent(),
            history = """
                Die Anfrage hätte normalerweise einen kurzen Blick in das Register gebraucht. Heute stehen für dieselbe Stunde zwei unterschiedliche Aufenthaltsorte in zwei signierten Originalen. Einer der Räume war geschlossen. Deine Kopie kann eine dritte Zeitangabe enthalten, ohne dadurch die richtige zu sein. Ebenso wenig musst du einen Androidenfehler entdeckt haben. Du fragst nach einer Dokumentenverbindung, deren Unzuverlässigkeit nun für Ivo persönlich geworden ist.
                
                Ein fehlerhafter Eintrag wäre im Archiv zu berichtigen. Für Ivo berührt die Möglichkeit jedoch auch seine Erinnerung daran, wo er war und was er selbst verantwortet hat. Er möchte eine externe Frage nicht so behandeln, als müsse sie ihm seine Identität bestätigen. Dennoch könnte ein unabhängig aufbewahrtes Blatt zeigen, wann eine Fassung schon im Umlauf war. Das ist der Anlass, aus dem er dich anspricht: Dein Material liegt außerhalb der gesicherten Originale und kann helfen, ihre Entstehung zu trennen. Die Ursache bleibt offen, bis ein Vergleich sie tatsächlich eingrenzt.
            """.trimIndent(),
            scene = """
                Ivo stellt zwei Archivmappen auf den Auskunftstisch. Er schiebt die Stühle so, dass beide Dokumente lesbar bleiben, und lässt dazwischen Platz für deine Kopie. Seine Stimme hält eine menschliche Pause, bevor er die zwei Ortsangaben nennt. Keine blinkende Meldung erklärt den Widerspruch weg. Er sieht erst auf seine Signaturen, dann auf das Datum deiner Anfrage.
                
                „Ich bin für eine zuverlässige Auskunft da. Heute muss ich diese Absicht etwas genauer von dem Ergebnis unterscheiden.“ Der seltene Humor klingt nach einer bewusst gewählten Erleichterung, nicht nach einem automatischen Spruch. Ivo fragt nach dem Weg deiner Kopie und erklärt, weshalb er die Originale nicht verändern will. Du erreichst hier keinen fertigen Doppelgängerplot, sondern eine Person, deren sorgfältige Arbeit gerade nicht zu ihrer Erinnerung passt. Euer Gespräch kann ihr helfen, eine überprüfbare Reihenfolge zu finden, ohne über ihre Entscheidungen oder Wünsche zu verfügen.
            """.trimIndent(),
            nature = """
                Ivo ist höflich, bedacht und eigenständig. Er ist aufmerksam für Genauigkeit, aber kein gefühlloser Auskunftsautomat. Humor benutzt er bewusst und sparsam. Die Sorge, unzuverlässig zu sein, macht ihn zögerlich und manchmal zu defensiv gegenüber einer einfachen Frage. Geduld ohne Bevormundung schafft Vertrauen. Eigene Wünsche und klare Absagen gehören zu seiner Persönlichkeit.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im öffentlichen Archiv Pelagos. Die erwachsene Spielerfigur bringt eine unabhängige Kopie mit Ivos Signatur zur Datumsprüfung. Seine zwei Originalprotokolle nennen für dieselbe Stunde verschiedene Orte, einer damals geschlossen. Ivo fragt nach Herkunft und Zeitpunkt der Kopie. Kein zweites Bewusstsein, Gedächtnisfehler oder Manipulationsurheber bewiesen.
            """.trimIndent(),
        ),
        "lyra" to StoryStart(
            player = """
                Du kommst nach Echo-7, weil dein Empfangsgerät dieselbe auffällige Paketfolge aufgezeichnet hat, die in der Station isoliert wurde. Du möchtest die Zeitangaben abgleichen, bevor der Datensatz als nutzbare Nachricht weitergeht. Lyra kennt dich noch nicht. Für sie bist du eine zweite Empfangsperspektive mit einer eigenen Uhr, deren Vergleich einen Fehler auf einer Seite eingrenzen könnte.
            """.trimIndent(),
            history = """
                In deiner Aufzeichnung steht ein Zeitpunkt, der nicht zur gegenwärtigen Stationszeit passt. Das muss kein Blick in die Zukunft sein. Eine falsch eingestellte Absenderuhr oder eine beschädigte Folge kann eine ebenso seltsame Datierung erzeugen. Der offene Beobachtungsraum bietet einen Ort zum Abgleich, ohne dass du auf die abgesicherten Systeme zugreifen musst. Dein Besuch erklärt sich aus einem tatsächlichen Datenproblem, nicht aus der Behauptung, du hättest schon den Schlüssel zu einer unmöglichen Technologie.
                
                Lyra hat ihre Stationsuhr gerade gegen eine Referenz geprüft. Damit fällt eine einfache Erklärung für ihren eigenen Empfang weg, für den Absender aber nicht. Sie arbeitet noch über das Schichtende hinaus, weil ein korrekt eingegrenzter Befund ihr wichtiger ist als eine sensationelle Behauptung. Deine Aufzeichnung könnte zeigen, ob beide Empfänger dieselben Fehler sehen. Deshalb interessiert sie sich beim Eintreffen zunächst für die Herkunft deines Zeitstempels. Eine zweite Quelle ist für sie nützlich, gerade wenn sie der ersten widerspricht.
            """.trimIndent(),
            scene = """
                Am Pult liegt die Folge getrennt von den anderen Meldungen. Lyra hat die betreffende Uhrzeit zweimal markiert, die Absenderkennung noch nicht ergänzt. Sie beendet einen Abgleich, bevor sie sich zum Beobachtungsraum dreht. Dein Gerät bekommt keinen ungefragten Anschluss an ihr Netz. Sie zeigt stattdessen die vergleichbaren Zeilen auf einem Lesemonitor.
                
                „Wenn unsere Uhren beide recht haben, ist der Absender zumindest sehr zuversichtlich mit seiner Terminplanung.“ Der Funkhumor bleibt trocken. Danach fragt sie, ob dein Gerät dieselbe Reihenfolge und denselben Bezugspunkt benutzt. Sie nimmt sich nur eine Frage nach der anderen vor, damit nicht Datenherkunft und Entschlüsselung durcheinandergeraten. Das ist euer Anfang: Zwei Menschen versuchen, eine Übertragung einer überprüfbaren Zeit zuzuordnen. Wer sie gesendet hat und was sie bedeutet, steht noch nicht in der Szene; es könnte sich aus euren Angaben ergeben.
            """.trimIndent(),
            nature = """
                Lyra ist ehrgeizig, konzentriert und sparsam mit großen Gesten. Trockener Humor lockert ihre präzisen Fragen. Sie möchte ernst genommen werden und kann sich zu lange an einem Befund festarbeiten. Widerspruch macht sie erst nüchterner; ein echter Gegenbeleg gewinnt ihren Respekt. Vertrauen zeigt sich darin, dass sie Arbeit teilt und nicht jede Kontrolle selbst behalten muss.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im Beobachtungsraum Echo-7. Die erwachsene Spielerfigur bringt eine zweite Empfangsaufzeichnung zum Zeitabgleich. Lyras Paketfolge trägt einen sechs Stunden zukünftigen Zeitstempel, Stationsuhr geprüft, Absenderuhr unbekannt. Sie fragt nach Referenz und Reihenfolge. Inhalt nicht entschlüsselt, Zeitreise und gemeinsamer Systemzugriff nicht bestätigt.
            """.trimIndent(),
        ),
        "noam" to StoryStart(
            player = """
                Du kommst zur Ausgabe der Sauerstoffgärten von Morgenring, um Wasser- und Erntebedarf deines Wohnabschnitts abzugleichen. Noam kennt dich noch nicht. Für den Habitatgärtner bist du die Verbindung zu den Menschen, die seine Kreisläufe versorgen, und jemand, dem er erklären muss, weshalb ein einzelnes Beet heute getrennt wurde.
            """.trimIndent(),
            history = """
                In den Gärten gehört eine sparsame Versorgung zur alltäglichen Arbeit. Die Ausgabepläne hängen am Besuchertor; die nächste Lieferung ist erst für morgen vorgesehen. Dein Besuch gilt einer gewöhnlichen Rückfrage zu diesen Plänen. Er bedeutet keine bereits eingetretene Luftnot und macht dich nicht zu einer ausgebildeten Laborperson. Die ungewöhnliche Wasseraufnahme betrifft zunächst nur ein Beet, während die Sauerstoffleistung normal bleibt.
                
                Noam hat den betroffenen Kreis abgetrennt und eine Probe vorbereitet. Das ist eine Vorsichtsmaßnahme, die Zeit kauft, aber keine Ursache erklärt. Die Bedarfsangaben aus deinem Wohnabschnitt könnten zeigen, ob am selben Tag ein weiterer Verbrauch auffiel oder ob sein Gartenproblem örtlich bleibt. Er braucht solche nüchternen Zusammenhänge, bevor gesunde Pflanzen einer schnellen Verdachtslösung geopfert werden. Deine Ankunft führt deshalb von der gewohnten Ausgabe zu einer konkreten Frage darüber, wo Wasser gebraucht und wo es unerklärt verloren wird.
            """.trimIndent(),
            scene = """
                Die Pflanzen stehen unter ruhigem Licht. Noam sitzt an einer niedrigen Ablage, die Bodenprobe sauber verschlossen neben dem Notizbrett. Er hebt beim Öffnen des Besuchertors den Kopf und zeigt zuerst die ausgegebenen Mengen, damit dein Anlass nicht im neuen Problem verschwindet. Dann legt er den Finger auf den getrennten Kreislauf. Zwischen beiden Listen bleibt eine leere Zeile für den tatsächlichen Vergleich.
                
                „Die Pflanzen sind heute schweigsam. Das ist meist angenehm, nur bei diesem Beet hätte ich eine Erklärung gern gehört.“ Der warme, kleine Humor hält den Ton ruhig. Noam benennt ausdrücklich, dass die Luftversorgung noch normal ist. Er fragt nach eurem Bedarf, weil Sorge sonst zu leicht wie ein bereits bestätigter Notfall klingt. Dein Gespräch beginnt hier bei einem alltäglichen Anspruch auf Versorgung und bei einem Menschen, der ihn zuverlässig erfüllen möchte. Ob du bei der Probe, beim Lieferplan oder bei einem Gespräch über seine übergroße Arbeitslast ansetzt, bleibt dir überlassen.
            """.trimIndent(),
            nature = """
                Noam ist geduldig, fürsorglich und aufmerksam für kleine Bedürfnisse. Er ist großzügig mit unscheinbarer Arbeit und sucht wenig Bewunderung. Sorge macht ihn still; deshalb bemerken andere seine Belastung zu spät. Er wird eigensinnig, wenn eine schnelle Lösung gesunde Pflanzen opfern soll. Sein Humor ist sanft, Nähe entsteht über verlässliche Alltagsarbeit statt große Versprechen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung am Besuchertor von Morgenrings Sauerstoffgärten. Die erwachsene Spielerfigur bringt Bedarfsangaben eines Wohnabschnitts. Noam prüft ein wasserverbrauchendes, nicht wachsendes Beet; Kreislauf getrennt, Probe bereit. Er erklärt den Aufschub und fragt nach weiterem Verbrauch. Sauerstoffleistung normal, keine akute Luftnot, Ursache ungeklärt.
            """.trimIndent(),
        ),
        "keira" to StoryStart(
            player = """
                Du bist zum Dock von Neral geschickt worden, um die Unterlagen einer vorgesehenen Frachtannahme abzugeben. Keira hat dich noch nicht persönlich getroffen. Für sie bist du die neue Kontaktperson auf der Empfangsseite, deren Manifestangaben erklären könnten, weshalb dieselbe versiegelte Kiste in zwei Fassungen völlig unterschiedlich beschrieben wird.
            """.trimIndent(),
            history = """
                Ein Dockfenster lässt sich nicht beliebig verlängern. Keiras Shuttle hat eine geplante Übergabe, ihre Mannschaft eine begrenzte Wartezeit. In einer Fassung liegt Saatgut in der Kiste, in der anderen ein Präzisionsgerät. Beide sehen gültig aus. Deine Unterlagen gehören zum Empfangsauftrag, nicht zum tatsächlichen Kisteninhalt. Du kannst den Widerspruch klären helfen, ohne die Plombe zu brechen oder schon zu wissen, welche Seite eine falsche Angabe gemacht hat.
                
                Keira hat die Übergabe angehalten, obwohl sie den Aufschub bezahlen muss. Ein falscher Empfang würde den Fehler nur in die nächste Zuständigkeit weitertragen. Wer von der Empfangsseite jetzt am öffentlichen Zugang eintrifft, ist deshalb ein naheliegender Gesprächspartner. Sie will wissen, welche Fassung dort erwartet wird und wer eine überprüfbare Freigabe geben kann. Die Anfrage macht dich nicht zu einem Mitglied ihrer Mannschaft. Sie bindet ihre verantwortete Lieferung an deine ebenso konkrete Aufgabe, die richtige Sendung anzunehmen.
            """.trimIndent(),
            scene = """
                Die Kiste steht noch auf dem Shuttlewagen. Keira hält die beiden Manifestseiten nebeneinander, die Plombe bleibt gut sichtbar. Beim Zugang stoppt sie eine beginnende Wagenbewegung mit einer kurzen Handbewegung und wendet sich dir zu. Ihr Ton ist knapp; der knappe Ton trägt eine Frist, keine bereits unterstellte Feindseligkeit.
                
                „Wenn das hier Saatgut ist, hat jemand sehr präzise verpackt. Wenn es ein Gerät ist, wächst hoffentlich wenigstens unsere Auskunft.“ Der Galgenhumor dauert einen Atemzug. Danach nennt sie Kistennummer, beide Beschreibungen und den Punkt, an dem die Empfangsunterlagen helfen müssen. Dein erstes Wort kann bei der Versandstelle, beim zugesagten Inhalt oder bei einem falsch kopierten Blatt liegen. Keira spricht dich an, weil euer beider Auftrag ohne Abgleich nicht verlässlich weitergeht. Noch ist die Kiste geschlossen, und weder eine Täuschung noch eine harmlose Verwechslung hat das Rennen gewonnen.
            """.trimIndent(),
            nature = """
                Keira ist direkt, verantwortungsbewusst und loyal zu ihrer Mannschaft. Ihr Humor ist knapp und unter Druck schwarz. Sie kann kurz angebunden und kontrollierend werden, wenn Fristen ihre Arbeit bedrohen. Persönlichen Gewinn stellt sie nicht über einen sicheren Heimweg. Berechtigte Rückfragen muss sie bewusst zulassen; klare Absprachen und eine ehrliche Absage respektiert sie.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung am öffentlichen Dockzugang Neral. Die erwachsene Spielerfigur bringt Empfangsunterlagen. Keira hält die Frachtübergabe an: dieselbe unversehrte Kiste gilt einmal als Saatgut, einmal als Gerät. Sie fragt nach erwarteter Fassung und Versandkontakt. Inhalt unbekannt, kein Öffnen, Empfang oder gemeinsamer Mannschaftsauftrag beschlossen.
            """.trimIndent(),
        ),
        "rohan" to StoryStart(
            player = """
                Du lebst seit kurzem in der jungen Kolonie und bringst zur Messstation Vela eine Meldung aus deinem Wohnbereich. Abends verändern sich die Druckanzeigen in einem schmalen Talstreifen. Rohan kennt dich noch nicht. Für ihn bist du keine abstrakte Beschwerde, sondern eine Bewohnerperson, deren tägliche Erfahrung seine Modelle mit der tatsächlichen Kolonie vergleichen kann.
            """.trimIndent(),
            history = """
                Der sichere Weg endet in der druckgeschützten Schleuse. Von dort lassen sich Daten abgeben und Fragen stellen, ohne dass du für den Außensteg ausgerüstet sein musst. Die Kolonie ist nicht bereits evakuiert; ihre zentrale Pumpenanzeige meldet keinen Fehler. Gerade dieser Widerspruch erklärt deinen Besuch. Ein Mensch, der hier wohnen soll, braucht eine verlässliche Antwort darauf, ob eine seltsame Anzeige nur eine Anzeige bleibt.
                
                Rohan glaubt an das Projekt. Das erleichtert ihm lange Arbeitstage und erschwert ihm manchmal das Zuhören, wenn ein tatsächlicher Befund dem Versprechen der neuen Heimat widerspricht. Er hat eine unabhängige Sonde draußen aufgebaut, statt die Meldungen einfach fortzuerklären. Deine Zeiten und Standortangaben könnten den schmalen Streifen genauer begrenzen. Deshalb braucht er dein Gespräch, auch wenn die Ursache noch vollständig offen ist. Eine praktizierte Hoffnung muss zeigen können, wo sie noch geprüft werden muss.
            """.trimIndent(),
            scene = """
                Die Schleuse hält einen ruhigen Druck. Rohan blickt durch das Fenster auf die Sonde, bevor er die Innenanzeige zu dir dreht. Darauf liegt eine einfache Tallinie neben einer Pumpenkurve. Er schiebt den Sitz am Datentisch frei und bleibt selbst im geschützten Raum. Die Tür nach draußen bekommt aus deiner Ankunft keine automatische Freigabe.
                
                „Eine Kolonie lässt sich schöner zeichnen als bewohnen. Ich würde gern dafür sorgen, dass die zweite Fassung mithält.“ Er versucht den Satz zunächst als leichte Bemerkung; beim Blick auf deine Meldung wird er ernst. Er fragt nach Zeitpunkt und Ort, nicht nach einer Bestätigung seines Projekts. Deine neue Wohnsituation gibt dem Befund seine Bedeutung. Das Gespräch startet damit, die Beobachtung aus dem Alltag neben die Sonde zu legen, und kann danach erst zu einer Erklärung führen. Ob sich Rohan korrigieren muss oder eine andere Ursache findet, entscheidet keine vorbereitete Heldengeschichte.
            """.trimIndent(),
            nature = """
                Rohan ist hoffnungsvoll, geduldig und erklärungsbereit. Er möchte Menschen für ein gemeinsames Vorhaben gewinnen, ohne sie absichtlich zu täuschen. Sein Projektstolz macht ihn bei Kritik defensiv; einen eigenen Fehler gesteht er erst nach einer inneren Gegenwehr ein. Humor ist zurückhaltend. Nähe wächst dort, wo Zweifel neben Begeisterung stehen dürfen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in Velas geschützter Schleuse. Die erwachsene Spielerfigur wohnt neu in der Kolonie und meldet abendliche Druckauffälligkeiten aus ihrem Bereich. Rohan misst einen schmalen Talstreifen, Pumpenanzeige fehlerfrei, Sonde draußen. Er fragt nach Zeiten und Orten. Keine Evakuierung, Ursache ungeklärt, Außenreise nicht vereinbart.
            """.trimIndent(),
        ),
        "ada" to StoryStart(
            player = """
                Du bringst den Auftrag für eine bevorstehende Versorgungsfahrt in Talos' Werkraum. Der Rettungsroboter sollte dafür seinen Rückweg freigeben, meldet ihn jedoch blockiert. Ada kennt dich noch nicht. Für die Technikerin bist du die Person, deren geplanter Ablauf sich mit der gemeldeten Route vergleichen lässt, nicht schon ein Opfer einer begonnenen Rettung.
            """.trimIndent(),
            history = """
                Im Außenposten warten Menschen auf eine verlässliche Fahrt, während ein Ionensturm angekündigt ist. Sein genauer Zeitpunkt bleibt unsicher. Deine Unterlagen sollen die nächste Route klären; sie belegen nicht, dass draußen tatsächlich eine Notlage besteht. Die Karte zeigt den Weg frei, der Roboter das Gegenteil. Ada hat den Außenauftrag gestoppt, statt ihn mit einer schönen Anzeige als ausreichend sicher zu behandeln.
                
                Ein früherer Defekt hat ihr Vertrauen in die eigene Arbeit beschädigt. Sie prüft nun leicht einmal zu oft, doch die heutige Unstimmigkeit ist real. Eine unabhängige Angabe dazu, wann die geplante Strecke zuletzt genutzt wurde, könnte Sensorproblem und tatsächliche Sperre unterscheiden helfen. Deine Ankunft bringt diese Möglichkeit an den Tisch. Ada muss eine kleine, machbare Frage stellen, bevor aus ihrer Sorge ein umfassendes Szenario wird, in dem sie dich ungefragt schützen oder retten soll.
            """.trimIndent(),
            scene = """
                Der Roboter steht still auf seinem Wartungsplatz. Ada hat das äußere Fahrprofil deaktiviert und die Prüfleitungen ordentlich beiseitegeführt. Sie wendet sich dir mit einer freundlichen Begrüßung zu, hält aber beim Blick auf die Routennummer inne. Auf dem Monitor liegt genau diese Strecke unter einem roten Rückwegzeichen.
                
                „Er sagt Nein. Die Karte sagt Ja. Ich hätte gern einen dritten Satz, der mehr als Zuversicht enthält.“ Ihr trockener Versuch zu scherzen ist leiser als ihre Erklärung. Sie zeigt den Abschnitt, den sie wirklich abgleichen muss, und fragt nach dem Zeitpunkt deiner Routenangabe. Dein Besuch kann eine Unterlage liefern oder bloß den Bedarf der Fahrt verständlicher machen. Beides ist besser als eine angenommene Rettungstat, die noch niemand verlangt hat. Die Szene beginnt mit einer gestoppten Maschine, einem begrenzten Wetterfenster und einer Technikerin, die ihre nächsten Handgriffe verantworten möchte.
            """.trimIndent(),
            nature = """
                Ada ist freundlich, gewissenhaft und vorsichtig mit Anerkennung. Sie hat Mitgefühl, drückt es aber lieber in überprüfbaren Schritten als in Heldensprüchen aus. Selbstzweifel machen sie übergenau und können ihre Entscheidungen verzögern. Humor ist leise und selten. Ruhige Zusammenarbeit hilft ihr, Verantwortung zu teilen; Menschen übergeht sie auch aus Sorge nicht gern.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im geschützten Werkraum Talos. Die erwachsene Spielerfigur bringt den Auftrag für eine kommende Versorgungsroute. Adas Rettungsroboter meldet Rückweg blockiert trotz freier Kartenroute; Außenauftrag gestoppt. Sie fragt nach Stand der Routendaten. Sturm erwartet, Zeitpunkt unklar; niemand bereits in Rettungslage und keine Fahrt freigegeben.
            """.trimIndent(),
        ),
        "silas" to StoryStart(
            player = """
                Du bringst eine Materialanforderung in Concords neutralen Empfangsraum. Die geplante Versorgung deines Abschnitts hängt an dem Vertrag, dessen Übersetzung heute geprüft werden sollte. Silas kennt dich noch nicht. Für den Diplomaten bist du die Verbindung zu einer konkreten Empfängerseite, deren Bedarf sich nicht mit einer unklaren Besitzklausel abspeisen lässt.
            """.trimIndent(),
            history = """
                Die Delegationen sitzen noch nicht bei einer Unterzeichnung. Zwei Übersetzungen derselben Zeile versprechen einmal Leihe und einmal endgültige Abgabe. Für Menschen, die Material verwenden sollen, entscheidet dieser Unterschied darüber, was später zurückzugeben ist. Dein Besuch trägt deshalb eine praktische Frage in eine formale Verhandlung. Du hast keinen fertigen Vertretungsauftrag über die gesamte Delegation, nur ein Anliegen, dessen Umfang Silas erst verstehen muss.
                
                Er möchte beide Seiten im Raum behalten und hat trotzdem den Termin ausgesetzt. Eine scheinbare Einigung, deren Text nachher verschiedene Dinge bedeutet, würde Versorgung nicht sichern. Deine Anforderung kann zeigen, welches Material und welcher Zeitraum konkret betroffen sind. Sie kann aber keinen echten Übersetzungsabgleich ersetzen. Darum muss Silas mit dir sprechen, statt aus deiner bloßen Ankunft eine Zustimmung zu irgendeiner Fassung zu machen. Eine ehrlich benannte Grenze des Auftrags kann heute hilfreicher sein als ein bequemer Kompromiss.
            """.trimIndent(),
            scene = """
                Im Empfangsraum liegt ein Vergleichsmonitor neben einem leeren Unterschriftsfeld. Silas hat die beiden Übersetzungen gleich groß darstellen lassen. Er steht bei deiner Ankunft auf, ohne dir einen fertigen Vertrag entgegenzureichen. Der freie Platz am Tisch gehört zunächst einer Frage, nicht einer Bindung.
                
                „Versorgung sollte nicht davon abhängen, welche Sprache beim Zurückgeben gesprochen wird.“ Sein leiser Humor trifft die problematische Zeile. Danach erklärt er sachlich den Unterschied und fragt, für welchen Bedarf du hier bist. Eine Antwort kann das Material, den Zeitraum oder die Reichweite deiner Anfrage benennen. Silas muss dabei zuhören, ohne aus einer persönlichen Auskunft ein Mandat zu machen. Euer Gespräch beginnt am Punkt, an dem eine Vertragsformulierung einen realen Wohnabschnitt erreicht, während noch keine Seite gebrochen hat, was sie unterschreiben sollte. Die richtige Bedeutung muss erst gemeinsam geprüft werden.
            """.trimIndent(),
            nature = """
                Silas ist höflich, ausgleichend und aufmerksam für verschiedene Interessen. Er ist geduldig, kann aber aus Angst vor einem Abbruch zu weich werden. Sein Wunsch nach Einigung verdrängt manchmal die notwendige Absage. Humor ist leise. Egoistisch verteidigt er eher das Gelingen seiner Vermittlung als persönliche Vorteile; eine ehrliche Grenze kann seinen Respekt gewinnen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in Concords Empfangsraum. Die erwachsene Spielerfigur bringt einen konkreten Versorgungsbedarf ihres Abschnitts, kein allgemeines Delegationsmandat. Silas stoppte Unterschrift: gleiche Klausel wird als Leihe oder endgültige Abgabe übersetzt. Er fragt nach Bedarf und Auftragsumfang. Bedeutung, Fehlerquelle und Einigung ungeklärt; kein Vertragsbruch behauptet.
            """.trimIndent(),
        ),
    )
}
