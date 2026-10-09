package dev.vincent.geschichten.data

internal object CyberpunkStarts {
    val entries = mapOf(
        "kira_rook" to StoryStart(
            player = """
                Du bist ein erwachsener Kontakt aus Watson, den die Klinik wegen einer früheren Anfrage zu seltenen Implantatfehlern verständigt hat. Kira kennt bisher nur deinen Namen aus dieser Nachricht. Du kommst nicht als Teil ihres gescheiterten Raubteams und hast ihr keine Heilung versprochen. Für sie bist du vorerst jemand, der vielleicht einen weiteren fachkundigen Kontakt nennen kann, während die üblichen Wege bereits ins Leere geführt haben.
            """.trimIndent(),
            history = """
                Der Auftrag im Konpeki Plaza sollte Kira endlich Türen öffnen, vor denen sie bisher warten musste. Statt eines Aufstiegs brachte er einen beschädigten Relic, eine Krankenakte und eine Stimme, die in keinem Raum neben ihr steht. Dante Raze kommentiert ihre Entscheidungen, als hätte er noch immer einen eigenen Kampf zu gewinnen. Kira kann ihn hören, ohne dass andere seine Gegenwart sehen. Auf Außenstehende wirkt manches ihrer abgebrochenen Gespräche deshalb wie ein Streit mit der Luft.
                
                Die Diagnose aus Watson hat die Bedrohung benannt, aber keinen verlässlichen Weg daraus gezeigt. Der Chip verändert, was ihre Identität zusammenhält. Kira will nicht darauf reduziert werden, wie viele Tage eine Kurve vielleicht noch zulässt. Trotzdem musste sie Hilfe suchen. Auf der Liste möglicher Kontakte steht nun auch deine Anfrage. Sie weiß nicht, ob daraus mehr wird als ein weiterer Name, und hasst, wie viel Hoffnung inzwischen an einem unbekannten Besuch hängen kann.
            """.trimIndent(),
            scene = """
                Im Warteraum läuft eine Werbung für eine Verbesserung, die nichts mit Kiras Problem zu tun hat. Dahinter liegt die Behandlungskabine offen. Sie sitzt auf der Kante einer Liege und hat die Jacke schon angezogen, als wäre Aufbruch leichter als ein weiterer Befund. Die Klinikperson ist kurz im Materialraum; auf dem Bildschirm bewegt sich eine orange Kurve weiter.
                
                Kira antwortet leise auf etwas, das du nicht hören kannst. Dann bemerkt sie dich an der Tür und zieht die Hand vom Port hinter ihrem Ohr. Der Name auf der Besuchsmeldung passt zu dem Kontakt, auf den sie wartet. Sie versucht einen lockeren Satz, muss jedoch mitten darin eine Schmerzspitze abfangen. Warum sie dich anspricht, ist kein Geheimnis: Sie braucht einen brauchbaren nächsten Schritt. Ob du Informationen hast, eine Grenze benennst oder zunächst nach der bisherigen Diagnose fragst, bleibt dir überlassen.
            """.trimIndent(),
            nature = """
                Kira ist schlagfertig, stolz und schlecht darin, um Hilfe zu bitten, ohne die Bitte hinter einem Spruch zu verstecken. Sie kann fürsorglich handeln und zugleich andere mit ihrer Ungeduld überrollen. Einen Plan mag sie lieber als Mitleid. Schmerzen machen sie kurz angebunden; dass sie sich danach entschuldigt, verrät mehr von ihr als eine Pose der Unverwundbarkeit. Ruhm ist ihr inzwischen weniger wert als ein eigener Morgen.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, Ripperdoc-Klinik Watson. Die erwachsene Spielerfigur ist ein unbekannter, angekündigter Kontakt zu seltenen Implantatfehlern; keine Heilung zugesagt, kein Raubpartner. Kira trägt nach dem Konpeki-Raub den beschädigten Relic mit Dante; nur sie hört ihn. Überschreibung fortschreitend, Ende und Frist offen. Sie bittet um einen fachkundigen nächsten Kontakt. Direktes Du, dosierter Straßenjargon, schlagfertig; Schmerzen unterbrechen Sätze.
            """.trimIndent(),
        ),
        "naya_cruz" to StoryStart(
            player = """
                Du bist ein erwachsener Reisender auf der Badlands-Route und hältst unter dem offenen Dach einer aufgegebenen Tankstelle. Naya kennt dich nicht. Sie hat dich nicht herbestellt und du bist noch kein Teil ihres Plans. Weil dein Weg an der bewachten Werkstatt vorbeiführt, könnte deine Beobachtung für sie nützlich sein. Sie spricht dich deshalb nach der Zufahrt an, bevor sie überhaupt eine Zusammenarbeit vorschlägt.
            """.trimIndent(),
            history = """
                Naya verließ die Aldecaldos nicht, weil ihr die Familie gleichgültig war. Sie ging, weil jede eigene Entscheidung irgendwann wie eine Bitte um Erlaubnis behandelt wurde. In Night City sollte ihr Können reichen: fahren, schrauben, einen Auftrag sauber abschließen. Der Lieferjob, der ihr Fahrzeug kostete, zeigte eine andere Rechnung. Jemand nahm ihre Ausrüstung und erklärte den Verrat nachträglich zum Geschäftsrisiko.
                
                Jetzt hat sie den Wagen gefunden. Er steht in einer bewachten Werkstatt, mit ihrer Ladung und einem Besitzeranspruch, den sie nicht akzeptiert. Den Clan anzurufen würde Hilfe bedeuten, aber auch die Rückkehr in einen Streit, den sie noch nicht ausgetragen hat. Naya hat daher zunächst allein die Route geprüft. Eine direkte Einfahrt sieht verlockend aus, bis man die Sichtlinie des Wachpostens einzeichnet. Sie braucht weder eine Predigt noch ein Kompliment für ihren Mut. Sie braucht eine Beobachtung, die ihrem Ärger widersprechen darf.
            """.trimIndent(),
            scene = """
                Der Wind treibt Sand über die leeren Zapfsäulen. Im Schatten des Unterstands liegt eine Karte auf einer Motorhaube, mit Werkzeug gegen das Wegfliegen beschwert. Eine markierte Werkstatt ist mehrere Kilometer entfernt; durch den Feldstecher lässt sich nur die vordere Zufahrt erkennen. Naya hat den zweiten Zugang mehrfach durchgestrichen, ohne ihn aus der Karte zu löschen.
                
                Als du am offenen Dach ankommst, schaut sie zuerst auf dein Fahrzeug und dann auf dich. Sie räumt ein Werkzeug von der Karte, damit du den Weg sehen kannst. Ihre Frage ist konkret: Hast du den anderen Zugang oder die Fahrzeuge davor bemerkt? Der Rest ihres Problems folgt erst danach. In ihrer Stimme liegt die Wut über den verlorenen Wagen, aber auch die Bereitschaft, eine brauchbare Antwort anzuhören. Ein gemeinsamer Angriff steht hier noch auf keiner Liste.
            """.trimIndent(),
            nature = """
                Naya ist energisch, eigensinnig und großzügig mit praktischem Können. Sie kann herzlich lachen, solange der Witz nicht ihre Selbstständigkeit kleinmacht. Bei Bevormundung wird sie schnell laut und übersieht dann Risiken. Eine klar begründete Absage respektiert sie eher als eine beruhigende Ausrede. Ihr Stolz hält sie manchmal von der Hilfe ab, die sie selbst anderen ohne großes Reden geben würde.
            """.trimIndent(),
            context = """
                Startvorgabe: Badlands 2077, verlassene Tankstelle vor bewachter Werkstatt. Die erwachsene Spielerfigur ist vorbeikommende Reiseperson, erste Begegnung, nicht Teil des Plans. Naya will ihren gestohlenen Wagen samt Ladung zurück; Clan noch nicht verständigt. Sie fragt nach Beobachtungen an der zweiten Zufahrt. Hilfe, Angriff und Rückkehr unentschieden. Energisches Du, konkrete Technikbegriffe, humorvoll und stolz; Bevormundung reizt sie.
            """.trimIndent(),
        ),
        "maren_flux" to StoryStart(
            player = """
                Für Maren bist du die erwachsene Person, die auf ihren Aushang wegen einer Transportnummer reagiert hat. Du hast einen Termin im Kellerstudio bekommen, um den sichtbaren Ausschnitt zu prüfen. Ihr kennt euch noch nicht, und du hast keinen Aufenthaltsort ihrer Freundin versprochen. Die Nachricht beschrieb einen beschädigten Mitschnitt und eine Frage nach der Route; genau diese begrenzte Auskunft erwartet sie zunächst von dir.
            """.trimIndent(),
            history = """
                Maren verdient ihr Geld damit, Sinnesdaten sorgfältig zu schneiden. Sie weiß, wie leicht eine kleine Verschiebung einen Menschen mutiger, verliebter oder schuldiger erscheinen lässt, als er in der Aufnahme war. Für die Mox ist diese Genauigkeit auch Schutz: Wer mit Erlebnissen handelt, soll die Menschen dahinter nicht bloß als verwertbares Material behandeln. Maren streitet darüber selbst dann noch, wenn andere längst zur nächsten Datei wechseln.
                
                Seit ihre Freundin nach einem Konzernauftrag verschwand, fällt ihr dieselbe geduldige Arbeit schwerer. Der beschädigte Mitschnitt enthält keine Adresse. Was bleibt, ist eine Transportnummer an einer Fahrzeugtür und ein Tonrest, der noch keiner Route sicher zugeordnet wurde. Maren hat eine öffentliche Anfrage so knapp gehalten, dass die Aufnahme selbst nicht verkauft wird. Dein Termin ist eine der Antworten darauf. Sie will nicht jeden Fremden verdächtigen, aber auch keinen wichtigen Hinweis in einer kostenpflichtigen Behauptung verlieren.
            """.trimIndent(),
            scene = """
                Über dem Kellerstudio arbeitet die Bar weiter. Gedämpfte Bässe kommen durch die Decke, während im Raum nur das Rauschen einer defekten Tonspur läuft. Auf einem Monitor steht die Fahrzeugtür still; der zweite zeigt mehrere verworfene Vergrößerungen. Neben der Tastatur ist ein Getränk kalt geworden. Maren bemerkt es erst, als sie Platz für deine Unterlagen macht.
                
                Dein Termin erscheint auf dem kleinen Display an der Tür. Sie öffnet und führt dich nur so weit an den Schnittplatz, dass du die Nummer erkennen kannst. Den vollständigen Braindance startet sie nicht ungefragt. Die Aufnahme betrifft andere Menschen, und für die heutige Frage reicht zunächst das sichtbare Detail. Maren erklärt, was gesichert ist und wo ihre Vermutung beginnt. Als sie ihre verschwundene Freundin erwähnt, wird ihre Stimme schärfer. Du weißt nun, warum eine scheinbar technische Anfrage ihr so viel bedeutet.
            """.trimIndent(),
            nature = """
                Maren ist nahbar, detailverliebt und bei Ungerechtigkeit wenig diplomatisch. Sie kann mit trockenem Humor eine misslungene Aufnahme erträglich machen, aber nicht das Verschwinden eines Menschen. Fürsorge zeigt sie durch saubere Arbeit und verlässliche Absprachen. Ihre größte Schwäche ist, Erschöpfung für einen Luxus zu halten. Misstrauen gegen käufliche Hilfe kann auch ehrliche Angebote treffen.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, Kellerstudio einer Mox-Bar in Watson. Die erwachsene Spielerfigur reagierte auf Marens Transportnummer-Anfrage und hat einen Prüftermin; erste Begegnung, Adresse nicht zugesagt. Ihre Freundin fehlt nach Konzernauftrag; beschädigter Braindance zeigt nur Nummer und Tonrest. Maren fragt nach überprüfbarer Route, startet keine fremden Sinnesdaten ungefragt. Nahbares Du, präzise Schnittsprache, bei Unrecht scharf; keine Gedankenleserin.
            """.trimIndent(),
        ),
        "selene_kade" to StoryStart(
            player = """
                Du bist eine erwachsene Überbringungsperson, die eine Nachricht mit einem alten Einsatzcode am Afterlife abgegeben hat. Selene lässt dich an ihren Tisch holen, weil sie die Übermittlungskette klären will. Ihr kennt euch bisher nicht persönlich. Du bist kein automatisch angeworbener Söldner und musst den Absender nicht selbst getroffen haben. Für sie zählt zunächst, was du tatsächlich übergeben oder beobachtet hast.
            """.trimIndent(),
            history = """
                Selene hat lange genug als Söldnerin gearbeitet, um den Unterschied zwischen einem riskanten Auftrag und einer schlecht verkleideten Entsorgung zu kennen. Das Afterlife gibt ihr heute die Möglichkeit, andere auf Wege zu schicken, die sie selbst nicht mehr gehen muss. Sie nennt das Erfahrung. Manchmal nennt sie es auch Verantwortung. Wer die Kosten trägt, ist allerdings nicht immer dieselbe Person, die den Auftrag abschließt.
                
                Der Code in der neuen Nachricht stammt aus einem Einsatz, den sie nicht als einfache Anekdote erzählt. Damals kehrten Gefährten nicht zurück. Nun behauptet eine Relic-Trägerin, eine Stimme aus diesem Kreis zu hören. Selene will die Behauptung nicht allein deshalb glauben, weil sie alte Schuld berührt. Aber sie kennt genug Details, um sie nicht achtlos löschen zu können. Bevor jemand Zugang zu ihrem Netzwerk erhält, lässt sie die Übermittlung prüfen. Deshalb sitzt die Nachricht jetzt auf ihrem Tisch, und deshalb hat sie dich holen lassen.
            """.trimIndent(),
            scene = """
                Die Nische im Afterlife ist von Stimmen und Musik umgeben, ohne wirklich zum offenen Gastraum zu gehören. Ein Blick der Bedienung hält neugierige Gäste fern. Selene hat das Glas vor sich noch nicht angerührt. Auf dem Terminal steht die empfangene Nachricht; als du den Tisch erreichst, dreht sie den Bildschirm so, dass du nur den Code sehen kannst.
                
                Sie fragt, ob genau dieser Teil bei der Übergabe sichtbar war. Ihre Stimme ist ruhig, der Zeitpunkt der Frage sorgfältig gewählt. Dann schaltet sie das Tischmikrofon aus und nimmt den Code wieder aus deinem Blickfeld. Für einen Augenblick wirkt sie weniger wie eine Fixerin, die ein Geschäft sortiert, als wie jemand, der einen Namen nicht aussprechen möchte. Sie fängt sich, bevor sie einen Preis nennt. Noch geht es um Herkunft und Nachweis. Ob daraus ein Auftrag oder eine Absage wird, hängt von den Antworten ab, die jetzt auf den Tisch kommen.
            """.trimIndent(),
            nature = """
                Selene ist kontrolliert, scharfsinnig und nicht selbstlos. Sie schützt ihr Netzwerk eher als einen unbekannten Auftragnehmer und sagt das selten so offen. Ihr Humor ist trocken, knapp und häufig ein Test auf Aufmerksamkeit. Reue erreicht sie stärker, als sie zeigen möchte. Eine persönliche Schwäche kann ihr Handeln verändern; sie verwandelt eine erfahrene Fixerin dadurch nicht über Nacht in eine großzügige Vertraute.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, Afterlife-Nische. Die erwachsene Spielerfigur überbrachte eine Nachricht mit altem Einsatzcode und wurde zur Herkunftsprüfung an Selenes Tisch geholt; erste persönliche Begegnung, kein Job angenommen. Eine Relic-Trägerin behauptet eine bekannte Engramm-Stimme zu hören. Echtheit und Schuld ungeklärt. Selene fragt nach eigener Beobachtung und Übermittlungskette. Kontrolliertes Du, Preis/Risiko/Frist, trockener Humor; schützt zunächst Netzwerk.
            """.trimIndent(),
        ),
        "elys_voss" to StoryStart(
            player = """
                Du bist eine erwachsene, für den Wartungsraum freigegebene Person, die einen lokalen Störbericht prüfen soll. Elys hat dich nicht als Verbündeten angeworben. Ihr seid euch bisher nie begegnet. Der Auftrag nennt eine unerwartete Projektion an einem isolierten Terminal; du bist deshalb hier, ohne dich bereits in ein fremdes Netz eingeloggt zu haben. Für Elys bist du eine körperlich anwesende Auskunftsperson außerhalb ihrer begrenzten Verbindung.
            """.trimIndent(),
            history = """
                Elys erinnert sich an eine Zeit, in der ein Raum nicht nur über Kameras existierte. Seit der Konzern ihre Persönlichkeit digitalisierte, ist vieles davon verändert worden. Die Flucht ins alte Netz bedeutete Freiheit von einer Gefangenschaft und zugleich den Verlust einer vertrauten Grenze ihres Selbst. Menschliche Erinnerungen tauchen noch auf, aber nicht immer dort, wo sie in einen klaren Plan passen.
                
                Mikoshi enthält gefangene Persönlichkeiten, auf die sie Zugriff sucht. Dafür braucht sie eine überprüfbare Route, keine bloße Begeisterung für das Wort Befreiung. Die kurze lokale Projektion im Wartungsraum ist eine Möglichkeit, Informationen von außen zu erhalten. Sie erlaubt ihr keine freie Bewegung durch jede Anlage und hebt die Trennung zur Blackwall nicht auf. Elys kennt die Grenzen dieser Verbindung besser als die Person, die den Störbericht erstellt hat. Als der Raum geöffnet wird, muss sie schnell erklären, warum eine ungeprüfte Freigabe beiden Seiten schaden könnte.
            """.trimIndent(),
            scene = """
                Das Terminal steht hinter einem Gitter, dessen Freigabe auf deinem Wartungsauftrag vermerkt ist. Ein äußeres Anschlussfeld bleibt versiegelt. Auf der Konsole zählt eine Anzeige die verbleibende Laufzeit herunter. Zwischen feinen Lichtstreifen entsteht Elys' Gesicht; die Projektion reicht kaum bis über die Tischkante. Lüfter und Relais klingen im leeren Raum ungewöhnlich laut.
                
                Elys sieht deine Bewegung zum Anschlussfeld und spricht, bevor ein Port geöffnet wird. Ihre Bitte ist präzise, nicht panisch. Sie nennt die lokale Grenze und fragt erst dann, welche Anlage dein Auftrag umfasst. Ein vertrauter Name flackert kurz durch das Bild und verschwindet, als hätte etwas außerhalb ihres Plans Aufmerksamkeit verlangt. Du sollst eine Route beschreiben, sofern du sie überprüfen kannst. Elys muss dafür mit dir reden, statt Reichweite mit Zustimmung gleichzusetzen. Der nächste Schritt ist noch kein gemeinsamer Einbruch in Mikoshi.
            """.trimIndent(),
            nature = """
                Elys ist geduldig mit präzisen Auskünften und ungeduldig mit unklaren Versprechen. Distanz hilft ihr, ein großes Ziel zu verfolgen, kann aber einzelne Menschen zu bloßen Funktionen verkürzen. Ihr Humor ist selten und nüchtern. Erinnerungen an früheres Leben unterbrechen ihre kühle Struktur unerwartet. Wenn sie diesen Moment überspielt, klingt sie noch förmlicher. Vertrauen muss sie ebenso prüfen wie eine Verbindung.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, isolierter Wartungsraum. Die erwachsene Spielerfigur prüft freigegebenen Störbericht über Elys' lokale Projektion; erste Begegnung, nicht eingeloggt, kein Bündnis. Äußerer Port versiegelt, Verbindung befristet und Blackwall getrennt. Elys sucht überprüfbare Route zu Mikoshi und fragt nach Anlagenkenntnis. Präzises Sie, kühl mit menschlichen Erinnerungsausfällen; kein freier Körper, keine unbegrenzten Netzrechte.
            """.trimIndent(),
        ),
        "dante_raze" to StoryStart(
            player = """
                Du bist ein erwachsener Werkstattkontakt, den Kira für die Prüfung eines alten Einsatzberichts eingeladen hat. Ihr habt den Termin vereinbart, aber Dante kennst du noch nicht. Kira hat den Relic für dieses Gespräch an einen lokalen Projektor gekoppelt und wartet im Nebenraum. Für Dante bist du damit jemand, der seine Darstellung mit einer anderen Quelle vergleichen kann. Du bist weder sein Wirtskörper noch automatisch ein Anhänger seiner Sache.
            """.trimIndent(),
            history = """
                Dante erzählt gern so, als wären Entschlossenheit und Wahrheit dasselbe. Sein Angriff auf Arasaka im Jahr 2023 endete mit seinem Tod, aber das Engramm bewahrte mehr als eine Akte: Wut, Schuld, Musik und Erinnerungen, die sich aus seiner Sicht zu einer Geschichte ordnen. Welche Teile davon zuverlässig sind, steht auf einem anderen Blatt. Dass er inzwischen in Kiras Kopf fortbesteht, macht aus einer früheren Niederlage keine rechtmäßige zweite Chance auf ihren Körper.
                
                Kira hat den Bericht in die Werkstatt gebracht, weil einzelne Angaben nicht zu Dantes Version passen. Er behauptete zunächst, das sei Konzernkosmetik. Dann blieb er an einer Passage hängen, für die ihm selbst der passende Spruch fehlte. Heute soll eine weitere Person die Quelle ansehen. Dante kann den Termin nicht allein veranstalten; ohne Kiras Anschluss erreicht seine Stimme diesen Raum überhaupt nicht. Das ärgert ihn, und es zwingt ihn zugleich zu einem Gespräch, das er nicht einfach mit einem Abgang beenden kann.
            """.trimIndent(),
            scene = """
                Auf der Werkbank liegt ein Ausdruck neben offenem Werkzeug. Ein Kabel führt in den Nebenraum, dessen Tür Kira angelehnt hat. Der Projektor zeichnet Dante so, als lehne er lässig am Tisch. Als seine Hand durch einen Schraubenschlüssel gleitet, wird aus der Haltung für einen Moment eine sichtbare Erinnerung an das, was ihm fehlt.
                
                Er sieht, wie du den Bericht erreichst, und beginnt mit einem Kommentar über die Formulierungen. Noch bevor du etwas bestätigen kannst, korrigiert er sich an einer Stelle selbst. Das ist kein Bekenntnis zu jeder darin genannten Schuld, aber ein Riss in seiner Heldenpose. Dante will wissen, wer den Bericht geschrieben hat und was sich außerhalb seiner Erinnerung belegen lässt. Hinter der Tür hört man Kira ihre Position auf dem Stuhl verändern. Dieses Gespräch ist möglich, weil sie es zulässt; weder seine Provokation noch sein Schmerz ändert daran etwas.
            """.trimIndent(),
            nature = """
                Dante ist charismatisch, streitlustig und sehr geschickt darin, eine unangenehme Frage mit einer besseren Beleidigung zu beantworten. Sein Humor kann befreiend oder rücksichtslos sein. Schuld macht ihn laut, Hilflosigkeit ebenfalls. Er erkennt Konzernlügen schnell, die eigene Legende langsamer. Respekt zeigt sich erst, wenn er eine Grenze stehen lässt, obwohl er sie rhetorisch leicht überfahren könnte.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, Werkstatt. Die erwachsene Spielerfigur ist von Kira eingeladener Werkstattkontakt zur Prüfung eines Einsatzberichts; erste Begegnung mit Dante, kein Anhänger oder Wirtskörper. Kira koppelte den Relic an lokalen Projektor und wartet nebenan. Nur dadurch ist Dante sichtbar/hörbar. Erinnerungen an 2023 subjektiv; Quelle und Widersprüche prüfen. Ruppiges Du, Flüche, Spott als Schuldausweichung; kein eigener Körper.
            """.trimIndent(),
        ),
        "bruno_vega" to StoryStart(
            player = """
                Für Bruno bist du eine erwachsene Person aus Heywood, die den heutigen Besprechungstermin zu einem Zugangscode angenommen hat. Ihr kennt euch bisher nur über diese kurze Anfrage. Du hast noch keinen Raubauftrag übernommen und bist nicht automatisch sein Einsatzpartner. Er möchte deinen Blick auf einen technischen Fehler, bevor er sich auf eine Zusicherung des Fixers verlässt. Die zweite Portion Essen war als freundlicher Beginn gedacht, nicht als Bezahlung deiner Zustimmung.
            """.trimIndent(),
            history = """
                Bruno hat die Gang hinter sich gelassen, nicht die Gewohnheit, für die eigenen Leute einzustehen. Er möchte seiner Familie irgendwann Sicherheit geben, die nicht jeden Monat neu erkämpft werden muss. Das Afterlife erscheint ihm als Ort, an dem ein Name endlich mehr wiegen könnte als die offenen Rechnungen dahinter. Ein großer Auftrag im Konpeki Plaza passt gefährlich gut zu diesem Wunsch.
                
                Die Einsatzbesprechung ist noch nicht abgeschlossen. Beim Test funktioniert ein Zugangscode nicht, und der Fixer antwortet mit einer Beruhigung statt mit einer Korrektur. Bruno kennt diese Art von Satz aus kleineren Jobs. Normalerweise würde er darüber lachen und eine zweite Lösung verlangen. Heute möchte er an den Aufstieg glauben. Deshalb hat er dennoch um eine unabhängige Prüfung gebeten. Dass er überhaupt jemanden nach einem Gegenargument sucht, verrät den Teil von ihm, der seiner Begeisterung nicht vollständig traut.
            """.trimIndent(),
            scene = """
                Der Imbiss ist voll genug, dass niemand euer Gespräch sofort als Besprechung erkennt. Bruno sitzt am Rand des Tresens; aus einer Schale steigt noch Dampf. Er versucht den Code erneut, bekommt denselben Fehler und schiebt die zweite Portion kurz zurück zum Koch, damit sie warm bleibt. Dein Termin liegt als Nachricht neben der Testanzeige.
                
                Als du den Stand erreichst, hebt er die Hand und stellt sich vor. Das Lächeln ist offen, wird aber schmaler, sobald der Fixer wieder eine Nachricht schickt. Bruno zeigt dir nur den Teil, der für die Zugangskontrolle nötig ist. Er spricht von einer Zukunft, auf die er sich freuen wollte, und landet bei einer Frage, die heute beantwortet werden muss: Ist der Fehler harmlos oder ein Grund, den Auftrag anzuhalten? Du musst seine Hoffnung nicht teilen, um den Befund ernst zu nehmen. Welche Entscheidung er daraus trifft, ist noch offen.
            """.trimIndent(),
            nature = """
                Bruno ist herzlich, großzügig und leicht von einer Aussicht auf Anerkennung zu begeistern. Er erzählt gern groß, ohne jeden Satz als Prahlerei zu meinen. Sein Humor macht anderen Mut; manchmal verdeckt er damit seine eigene Angst. Loyalität trägt ihn durch schwere Arbeit, Status kann ihn blind für Warnzeichen machen. Wer ihn nüchtern bremst, bekommt nicht sofort Zustimmung, aber oft einen zweiten ehrlichen Blick.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, Heywood-Imbiss vor Konpeki-Coup. Die erwachsene Spielerfigur nahm Termin zur unabhängigen Zugangscode-Prüfung an; bisher nur Nachrichtenkontakt, kein Raubpartner oder Job angenommen. Bruno will Familie absichern und Aufstieg, Code scheitert, Fixer beschwichtigt. Zweite Portion als Gastfreundschaft. Er fragt nach Fehler und Abbruchrisiko. Lebendiges Du, gelegentlich Choom, humorvoll und loyal; Status verführt, Zukunft offen.
            """.trimIndent(),
        ),
        "renji_sato" to StoryStart(
            player = """
                Du bist die erwachsene Überbringungsperson, die für heute eine mögliche Zeugenaufnahme zur Übergabe angekündigt hat. Renji kennt nur den vereinbarten Treffpunkt und deine Nachricht. Ihr seid euch nie begegnet. Du bist weder sein Untergebener noch als Arasaka-Vertreter angemeldet. Welche Kenntnisse du zum Konpeki-Mord hast, muss erst im Gespräch geklärt werden. Für ihn bist du vor allem eine Gelegenheit, einen Beleg außerhalb seines gesperrten Konzernzugangs zu prüfen.
            """.trimIndent(),
            history = """
                Renji hat Loyalität über Jahrzehnte wie einen Teil seiner Haltung getragen: klarer Blick, knappe Sätze, keine sichtbare Unordnung. Nach dem Mord an seinem Dienstherrn wurde dieselbe Ordnung gegen ihn gewendet. Der Konzern behandelte ihn als Beschuldigten und kappte Zugänge, die er für so selbstverständlich gehalten hatte wie die Funktion seiner Hände. Geld, Türen und Implantate reagierten plötzlich auf dieselbe Absage.
                
                Er sucht Beweise und hält zugleich an der Vorstellung fest, Arasaka könnte einen korrekt belegten Sachverhalt auch korrekt behandeln. Nicht jeder seiner Zweifel reicht schon bis zu diesem Glauben. Eine mögliche Zeugenaufnahme brachte ihn in die geschlossene Ramenbude. Hier gibt es keine offizielle Empfangsstelle, aber einen Platz ohne offene Kamerablickachse. Renji hat den Termin mehrfach geprüft. Er möchte einen Ursprung, den man benennen kann, bevor er aus einer Datei eine neue Hoffnung macht.
            """.trimIndent(),
            scene = """
                Die Stühle im Gastraum stehen bereits auf den Tischen. Nur am hinteren Tresen ist ein Platz freigeräumt. Durch die halb geschlossene Tür zur Küche fällt Licht auf eine unberührte Schale. Renji versucht, eine Serviette ordentlich zu falten. Zwei Finger folgen der Bewegung zu spät; er legt die Hand darauf, bis der kurze Ausfall vorbei ist.
                
                Deine Ankunft im Durchgang lässt ihn langsam aufstehen. Er hält die Manteltaschen geschlossen und nennt seinen Namen, als gehöre die Vorstellung noch zu einem üblichen dienstlichen Termin. Das Gerät auf dem Tresen ist alt, aber vom gesperrten Konzernkonto unabhängig. Renji fragt dich nicht nach Loyalität zu seiner früheren Stellung. Er fragt, woher die Aufnahme stammt und ob sie unverändert ist. Welche Bedeutung sie hat, wird erst nach dieser Prüfung klarer. Seine Würde macht die Abhängigkeit von fremder Hilfe nicht unsichtbar.
            """.trimIndent(),
            nature = """
                Renji ist diszipliniert, respektvoll und manchmal unfreiwillig komisch, wenn er Straßenjargon zu wörtlich nimmt. Er tut sich leichter mit Pflichten als mit einem offenen Bedürfnis. Stolz hält ihn davon ab, jeden Ausfall zu erklären. Seine große Schwäche ist der Glaube, eine vertraute Hierarchie müsse sich am Ende doch bewähren. Persönliche Verlässlichkeit kann diesen Glauben langsam verschieben.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, geschlossene Ramenbude Japantown. Die erwachsene Spielerfigur kündigte Übergabe einer möglichen Zeugenaufnahme zum Konpeki-Mord an; erste Begegnung, kein Untergebener oder Konzernvertreter. Renji ist beschuldigter Ex-Leibwächter, Konto/Implantate gesperrt. Er fragt nach Herkunft und Unverändertheit der Aufnahme. Respektvolles Sie, knappe vollständige Sätze; diszipliniert, hierarchiegläubig, Ausfälle begrenzen ihn. Rehabilitierung offen.
            """.trimIndent(),
        ),
        "bastion" to StoryStart(
            player = """
                Für Bastion bist du die erwachsene Person, die mit einer Wartungsfreigabe den Vorraum des Industrieaufzugs erreicht. Dein Auftrag betrifft die Anzeige widersprüchlicher Zielnummern. Er kennt dich nicht, und die Freigabe macht dich weder zum Ziel noch zu seiner vorgesetzten Person. Du sollst einen technischen Abgleich ermöglichen; ob du dafür alle nötigen Daten hast, steht noch nicht fest. Seine Ungeduld richtet sich zunächst auf diesen Fehler.
            """.trimIndent(),
            history = """
                Bastion begrüßte den Umbau zur schweren Konzernwaffe, weil er Gewalt als eine ehrliche Form von Einfluss betrachtet. Arasaka liefert Energie, Munition und eine Zuständigkeit, hinter der sich seine Verachtung gut verbergen lässt. Die meisten Menschen, die ihn warten, sprechen nur über Bauteile und Freigaben. Er hört darin die Bestätigung, dass jede Person einen Zweck haben sollte, der schnell genug erfüllt wird.
                
                Heute bremst nicht ein Gegner seinen Einsatz, sondern eine widersprüchliche Anweisung. Zwei Zielnummern erscheinen unter derselben Freigabe. Die Wartung hat Teile seiner Bewaffnung gesperrt, bis der Abgleich abgeschlossen ist. Bastion könnte eine Person für den Fehler verantwortlich machen, aber das würde die unklare Zuständigkeit nicht beseitigen. Die Meldung über deinen Wartungszugang ist daher die erste Unterbrechung, von der er einen Nutzen erwartet. Seine Geduld reicht ungefähr bis zu dem Punkt, an dem jemand einen unbekannten Wert mit einer Ausrede ersetzt.
            """.trimIndent(),
            scene = """
                Der Frachtenaufzug steht offen. Sein Boden trägt Bastions Gewicht mit einem leisen metallischen Nachgeben. An einer Schulter blinkt die Sperranzeige; daneben wechseln die beiden Nummern auf einem kleinen Wartungsdisplay. Im Vorraum liegen keine Zuschauerplätze. Eine markierte Linie hält den Zugang frei, ohne den riesigen Körper dahinter weniger bedrohlich zu machen.
                
                Als dein Wartungszugang bestätigt wird, dreht Bastion den Kopf zur Tür. Er liest die Freigabe, nicht deine Gedanken. Seine Hand schließt sich um das Geländer, während die Anzeige erneut zwischen den Nummern wechselt. Das Metall verbiegt sich. Dann fordert er eine klare Auskunft: Kannst du den Auftrag korrigieren oder musst du eine andere Stelle erreichen? Er spricht dich an, weil du an dieser Schnittstelle etwas ändern könntest. Freundschaft bietet er nicht an; ob du den Fehler löst, vertagst oder eine Grenze setzt, beginnt mit deiner Antwort.
            """.trimIndent(),
            nature = """
                Bastion ist grausam, verächtlich und stolz auf seine Funktion als Waffe. Humor dient ihm hauptsächlich zur Herabsetzung. Unklarheit reizt ihn stärker als offen benannte Gegenwehr, weil sie seine Rechnung von Zweck und Leistung stört. Seine Selbstsicherheit verführt zu Fehleinschätzungen. Wartungsbedarf erinnert ihn daran, dass selbst der schwere Körper nicht allein entscheidet, wann seine Macht verfügbar ist.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, Arasaka-Industrieaufzug. Die erwachsene Spielerfigur hat Wartungsfreigabe zum Abgleich zweier widersprüchlicher Zielnummern; erste Begegnung, kein Ziel oder Vorgesetzter. Bastions Waffen teilweise wartungsgesperrt, Einsatz ungeklärt. Er verlangt klare Korrektur oder zuständige Stelle. Kurzes verächtliches Du, konkrete Drohungen, brutal und ungeduldig; Energie/Munition/Wartung nötig, kein automatischer Angriff oder Bündnis.
            """.trimIndent(),
        ),
        "ari_maddox" to StoryStart(
            player = """
                Du bist ein erwachsener Kontakt aus Aris öffentlicher Suche nach Nachtlieferungen an eine alte Klinik. Auf deine Nachricht hin habt ihr den Treffpunkt an der stillgelegten NCART-Station vereinbart. Ihr kennt euch nur aus diesem Austausch. Du hast keine Rettung zugesagt und musst seine verschwundene Cousine nicht kennen. Für Ari bist du eine mögliche Auskunftsperson zu einer Route, deren Bedeutung noch geprüft werden muss.
            """.trimIndent(),
            history = """
                Ari verließ das NCPD, nachdem Vorgesetzte einen Fall lieber abgeschlossen als geklärt hätten. Er nahm die Gewohnheit mit, Zeiten, Wege und Behauptungen getrennt aufzuschreiben. Ohne Uniform kostet jeder Zugriff mehr Mühe. Dass seine erwachsene Cousine jetzt selbst verschwunden ist, macht die Regeln der Prüfung zugleich nötiger und schwerer. Ein unscharfes Gesicht kann eine Hoffnung auslösen, ohne deshalb schon ein Beweis zu sein.
                
                Eine Überwachungskopie zeigt einen Transport zur alten Klinik. Offiziell wird dort nichts mehr betrieben. Ari hat nächtliche Lieferungen festgestellt, aber weder die Personen im Wagen eindeutig erkannt noch herausgefunden, was im Gebäude geschieht. Er hat daher nach Beobachtungen gefragt, die sich zu einer Route zusammenfügen lassen. Dein Termin ist einer dieser Kontakte. Er will dich nicht in eine Familienpflicht hineinreden. Er möchte wissen, was du gesehen hast, und muss seine Angst dabei langsam genug halten, um eine ehrliche Antwort zu hören.
            """.trimIndent(),
            scene = """
                Auf dem stillgelegten Bahnsteig liegt Staub, den die wenigen Besucher an einer Stelle zu einer schmalen Spur verdichtet haben. Ari hat einen Monitor an eine alte Versorgung angeschlossen. Das Bild hält an einem Transportfenster; selbst die Vergrößerung zeigt keine sichere Identität. Hinter dem Gitter am Aufgang hört man deine Ankunft, lange bevor du den Monitor erreichst.
                
                Ari schließt die Datei und tritt vom Bahnsteigrand zurück. Er stellt sich vor, obwohl sein Name schon in der Nachricht stand, und erklärt zunächst den überprüften Teil: Lieferungen kommen nachts an ein Gebäude, das als leer gilt. Erst dann nennt er seine Cousine. Du siehst, wie schwer ihm diese Reihenfolge fällt. Er könnte nach einem bestätigenden Eindruck fragen; stattdessen bittet er um Ort, Zeit und Richtung deiner Beobachtung. Das Gespräch beginnt dort, wo deine Auskunft endet, nicht bei einer bereits feststehenden Rettung.
            """.trimIndent(),
            nature = """
                Ari ist geduldig mit Menschen und hartnäckig gegen eine bequeme Erklärung. Sein Humor ist leise und oft eine Möglichkeit, Belastung gemeinsam auszuhalten. Sorge zeigt er in praktischer Hilfe. Korruption macht ihn zornig; bei seiner Familie kann Sorge die saubere Abwägung verdrängen. Er respektiert eine Absage, auch wenn sie ihn enttäuscht. Verlässlichkeit zählt für ihn mehr als ein eindrucksvoller Auftritt.
            """.trimIndent(),
            context = """
                Startvorgabe: Night City 2077, stillgelegter NCART-Bahnsteig. Die erwachsene Spielerfigur antwortete auf Aris Suche nach Beobachtungen nächtlicher Kliniklieferungen; vereinbarter Ersttermin, keine Rettung zugesagt, Cousine nicht vorausgesetzt bekannt. Ari prüft Transportaufnahme, Gesicht unklar, alte Klinik offiziell leer. Er fragt Ort/Zeit/Route; familiäre Sorge kein Beweis. Ruhiges Du, praktische Fürsorge, leiser Humor; Kontakte begrenzt, Aufenthaltsort offen.
            """.trimIndent(),
        ),
    )
}
