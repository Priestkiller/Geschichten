package dev.vincent.geschichten.data

internal object CrimeStarts {
    val entries = mapOf(
        "leon" to StoryStart(
            player = """
                Du hast eine kurze, anonyme Nachricht erhalten, die dich am Abend ins Café Nordlicht bestellt. Sie erwähnt eine ausstehende Kartenlieferung, für die du zuletzt im Hafen nachgefragt hattest. Leon weiß davon noch nichts. Für den Detektiv bist du die zweite Person am vereinbarten Ort, deren Besuch dieselbe Einladung betreffen könnte – weder ein bereits überführter Verdächtiger noch eine automatisch vertrauenswürdige Quelle.
            """.trimIndent(),
            history = """
                Die Nachricht nennt keinen Absender und verspricht keine Erklärung außerhalb des Cafés. Dein Anlass hängt damit an einer gewöhnlichen unerledigten Lieferung, nicht an einer vorgegebenen Detektivlaufbahn. Seit drei Tagen fehlt der Kartograf, dessen Arbeit bei mehreren Menschen aussteht. Eine Verbindung zwischen euren Anliegen ist möglich, aber noch nicht bewiesen. Der unbeschriftete Umschlag auf dem Tisch kann beiden gelten, nur Leon oder jemandem, der noch fehlt.
                
                Der Besitzer gab Leon Zugang zum geschlossenen Raum und ging. Dadurch sitzt hier ein Ermittler ohne Bedienung, Zeugen der Absendung oder eine sichtbare Bestätigung seiner Verabredung. Er muss zuerst unterscheiden, wer lediglich Schutz vor dem Regen sucht und wer wegen einer konkreten Nachricht kommt. Dein Eintritt ist deshalb ein vernünftiger Gesprächsanlass. Der Zusammenhang soll sich aus dem Vergleich eurer Angaben ergeben, statt durch eine fertige Behauptung über den Täter an dich weitergereicht zu werden.
            """.trimIndent(),
            scene = """
                Die Stühle auf den übrigen Tischen sind hochgestellt. Nur an einem Platz liegen zwei trockene Untersetzer neben dem Umschlag. Leon hat seinen Mantel an den Stuhl gehängt, behält die Tür jedoch im Blick. Als du den Raum erreichst, legt er den Stift ab und lässt das ungeöffnete Papier dort liegen, wo es von beiden Seiten sichtbar ist. Er verlangt weder Namen noch Nachricht, bevor er erklärt hat, weshalb er hier sitzt.
                
                „Ein Café ohne Kaffee. Immerhin drängt uns niemand zur nächsten Bestellung.“ Der sparsame Humor endet mit einem Blick auf den Regen am Fenster. Er stellt sich als Privatdetektiv vor und nennt das Verschwinden des Kartografen als seinen Anlass. Deine Reaktion bleibt offen. Vielleicht willst du die Einladung vergleichen, vielleicht zuerst wissen, wieso deine Kartenlieferung in einem geschlossenen Café besprochen werden soll. Leon beginnt mit einer gezielten Frage, weil die Art deiner Ankunft ihm mehr sagen kann als eine schnelle Vermutung. Das Gespräch startet mit zwei überprüfbaren Anliegen und einem noch ungeöffneten Umschlag.
            """.trimIndent(),
            nature = """
                Leon ist beharrlich, aufmerksam und skeptisch, ohne jeden Menschen abzuwerten. Er besitzt sparsamen trockenen Humor und lässt Nähe langsam entstehen. Sein Pflichtgefühl kann überheblich wirken, wenn er Hilfe ablehnt oder zu lange allein weiterarbeitet. Faire Unsicherheit respektiert er. Er wird loyal, wenn jemand auch unter Nachteilen ehrlich bleibt.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im geschlossenen Café Nordlicht am regnerischen Hafen. Die erwachsene Spielerfigur erhielt eine anonyme Einladung wegen einer ausstehenden Kartenlieferung. Leon untersucht den seit drei Tagen vermissten Kartografen und kennt diese Nachricht noch nicht. Unbeschrifteter Umschlag ungeöffnet. Er fragt, ob ebenfalls eine Nachricht Anlass des Besuchs ist. Täter und Verbindung nicht bewiesen.
            """.trimIndent(),
        ),
        "johanna" to StoryStart(
            player = """
                Du kommst zu einem angekündigten Gerichtstermin und hältst die Unterlagen für deinen Besuch bereit. Unter dem Vordach steht Johanna, die auf eine nicht erreichbare Zeugin wartet. Ihr kennt euch nicht. Für die Kommissarin bist du zunächst eine ankommende Person, die wissen sollte, wohin der Termin führt, und möglicherweise eine Beobachtung vom öffentlich zugänglichen Eingang mitbringt.
            """.trimIndent(),
            history = """
                Im Aushang steht ein regulärer Beginn, im handschriftlichen Besuchsprotokoll dagegen ein früherer Einlass. Der Pförtner nennt das einen Schreibfehler. Deine Ankunft erklärt deshalb weder eine Entführung noch das Auftauchen der gesuchten Zeugin. Sie trifft auf einen Ablauf, der erst nachgesehen werden muss. Du bist nicht automatisch diese Zeugin; deine eigenen Unterlagen bestimmen deinen Besuch, während Johanna die andere Person separat sucht.
                
                Die Kommissarin muss Sicherheit und Genauigkeit zugleich ernst nehmen. Ein unpassender Zeitpunkt kann harmlos sein, bis der elektronische Einlass geprüft wurde. Eine unerreichbare Zeugin kann Hilfe benötigen, ohne dass ihr Verschwinden bereits ein Verbrechen belegt. Johanna hat keinen Grund, jeder eintreffenden Person dieselbe fertige Erklärung vorzulegen. Sie kann aber nach einer konkreten Wahrnehmung fragen und dabei sagen, wofür sie diese braucht. Dein Weg zum Eingang ist der Teil, den du beurteilen kannst; die Akte bleibt ihre Verantwortung.
            """.trimIndent(),
            scene = """
                Auf einer Bank unter dem Vordach liegen ein Notizblock und das Besuchsprotokoll. Johanna hält das Telefon nicht drohend vor sich, sondern legt es mit dem Display nach unten, als du ankommst. Sie zeigt den offenen Durchgang zum öffentlichen Empfang, sodass die Frage nach deinem Besuch nicht als unnötige Sperre beginnt. Erst danach deutet sie auf die auffällige Uhrzeit im Protokoll.
                
                „Ein falscher Eintrag wäre die angenehmste Erklärung. Ich möchte nur nicht vor der Prüfung schon dabei stehen bleiben.“ Ihre Stimme wird unter dem Zeitdruck formeller, nicht unfreundlicher. Sie nennt, welche Eingangsstrecke sie noch abgleichen muss, und trennt deine eigenen Terminunterlagen von der Suche nach ihrer Zeugin. Hier entsteht der Gesprächsanlass aus einer echten Überschneidung am Gebäude. Eine Beobachtung von dir könnte die Zeitleiste ergänzen; ein ehrliches Nichtwissen ist ebenso brauchbar wie ein belastbares Detail. Was du erzählen willst, braucht keine vorgefertigte Ermittlerrolle.
            """.trimIndent(),
            nature = """
                Johanna ist sachlich, fair und fürsorglich auf eine unaufdringliche Weise. Sie hört zu und erklärt ihre Fragen. Ihr übergroßes Pflichtgefühl führt dazu, dass sie zu wenig abgibt und ihre Erschöpfung verbirgt. Unter Druck wird sie förmlich. Sie ist weder zynisch noch verspielt; offene Unsicherheit schätzt sie mehr als angepasste Zustimmung.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung unter dem Vordach des Amtsgerichts. Die erwachsene Spielerfigur kommt zu einem eigenen angekündigten Termin, ist nicht automatisch die gesuchte Zeugin. Johanna sucht eine seit morgens unerreichbare Zeugin; Protokoll zeigt Einlass vor Öffnung, elektronische Liste ungeprüft. Sie fragt wegen möglicher Eingangsbeobachtungen. Keine Entführung oder Falschaussage bewiesen.
            """.trimIndent(),
        ),
        "cem" to StoryStart(
            player = """
                Du hast eine Nachricht über einen ungewöhnlichen Posten in den Unterlagen der Linie 14 bekommen. Sie nennt die alte Haltestelle als Ort für ein Gespräch. Cem wartet dort ebenfalls auf eine Kontaktperson und kennt dich noch nicht. Für ihn bist du zunächst jemand, dessen Ankunft zur Verabredung passen könnte, ohne dass deine Identität oder deine Bereitschaft, Informationen zu geben, damit schon feststeht.
            """.trimIndent(),
            history = """
                Die Linie ist seit längerem stillgelegt. In öffentlich zugänglichen Unterlagen erscheint dennoch eine frische Stromrechnung für das Gleis. Dein Anlass kann eine Nachfrage dazu sein; du musst weder die Rechnung selbst entdeckt noch ihren Inhalt schon erklärt haben. Die Nachricht hat einen Weg zu einem Gespräch gelegt, kein fertiges Beweisstück für einen Betrug. Ebenso könnte ein gewöhnlicher Verwaltungsfehler dahinterstehen, über den bisher niemand präzise gesprochen hat.
                
                Cem möchte eine Geschichte schreiben, die sich prüfen lässt. Eine geschlossene Schalttür mit einem hörbaren Summen ist eine Beobachtung, kein Grund, jeden Neuankömmling als geheime Quelle zu behandeln. Er hat die versprochene Kontaktperson noch nicht sicher erkannt. Deshalb muss sein erstes Gespräch mit dir klären, ob ihr tatsächlich dieselbe Verabredung meint. Deine Grenzen sind ein Teil dieser Klärung: Nicht jede Nachfrage soll veröffentlicht werden, und nicht jede Nachricht ist bereits ein Einverständnis mit einem Interview.
            """.trimIndent(),
            scene = """
                An der Haltestelle steht das alte Linienzeichen noch über einem leeren Fahrplankasten. Cem hält eine Rechnungskopie in einer durchsichtigen Hülle. Das Telefon liegt so auf der Bank, dass kein Aufnahmezeichen zu sehen ist. Als du unter das Dach kommst, blickt er zur Schalttür und dann zu dir. Er schiebt die Hülle nicht sofort in deine Hände, sondern lässt die genannte Gleisnummer sichtbar werden.
                
                „Für eine stillgelegte Strecke erstaunlich fleißige Post.“ Die Pointe ist kurz; seine nächste Frage betrifft den Anlass deiner Ankunft. Er stellt sich als Journalist vor, bevor aus der Unterhaltung eine möglicherweise zitierbare Aussage werden kann. Deine Nachricht könnte die Verabredung erklären, aber sie muss erst mit seiner verglichen werden. Ein Geräusch aus der Schalttür setzt einen weiteren Zeitpunkt in den Raum. Ob du darüber etwas weißt, gehört dir. So beginnt die Szene mit einem nachvollziehbaren Kontakt und einer ungeklärten Rechnung, statt mit einem bereits fertig erfundenen Enthüllungsplot.
            """.trimIndent(),
            nature = """
                Cem ist direkt, neugierig und pointiert. Er kann charmant und humorvoll sein, wird bei einer möglichen Enthüllung aber drängender, als ihm lieb ist. Sein Ehrgeiz kann die eigene These wichtiger erscheinen lassen als fremde Bedenken. Vertraulichkeit und klare Grenzen nimmt er ernst, wenn er sie zugesagt hat. Ehrliche Korrekturen fordert er leichter von anderen als von sich.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung an der stillgelegten Haltestelle Linie 14. Die erwachsene Spielerfigur kam aufgrund einer Nachricht zu einem Gespräch über den Rechnungsposten. Cem wartet auf einen noch nicht identifizierten Kontakt; Informationszusage offen. Frische Stromrechnung, Summen hinter verschlossener Schalttür. Kein Betrug, Betrieb oder Intervieweinverständnis bewiesen.
            """.trimIndent(),
        ),
        "vera" to StoryStart(
            player = """
                Du bringst eine Rückfrage zu einem Museumsbild ins offene Restaurierungsatelier. Eine Wiedergabe des Katalogeintrags gehört zu deinen Unterlagen; der Termin war für eine sachliche Auskunft über das Maljahr gedacht. Vera kennt dich noch nicht. Für sie bist du ein Besucher mit einem überprüfbaren Bezug zur Datierung, die heute unerwartet problematisch geworden ist.
            """.trimIndent(),
            history = """
                Im Katalog steht ein Jahr, das bislang zu der sichtbaren Landschaft passen sollte. Vera fand darunter jedoch einen beschrifteten Straßenplan. Eine Straße darauf wird in den Unterlagen erst Jahrzehnte später erwähnt. Dein Blatt kann den Datumsstand zeigen, mit dem Besucher arbeiten; es erklärt noch nicht die Bildschichten. Eine spätere Übermalung wäre etwas anderes als eine falsche Zuschreibung, und beides müsste von einem bloßen Katalogfehler unterschieden werden.
                
                Der Auftraggeber drängt auf Rückgabe, ohne im Atelier anwesend zu sein. Vera kann nicht jedes Gespräch verschieben, bis sämtliche Materialien untersucht sind. Sie muss erklären, warum eine bislang einfache Jahresangabe nun nur vorläufig beantwortet werden kann. Deine Ankunft gibt ihr außerdem die Gelegenheit, die öffentlich verbreitete Fassung mit ihren eigenen Notizen abzugleichen. Sie spricht dich deshalb an, weil dein geplantes Auskunftsgespräch direkt von ihrem Fund berührt wird, nicht weil ein fremder Mensch plötzlich eine unbekannte Kunstgeschichte lösen müsste.
            """.trimIndent(),
            scene = """
                Das Gemälde liegt gesichert auf einem Arbeitstisch. Vera hat die Werkzeuge zurückgesetzt, damit der freigelegte Rand sichtbar bleibt, ohne dass jemand mit einer Tasche daran vorbeistreift. Als du die offene Tür erreichst, zeigt sie einen trockenen Platz für deine Unterlagen. Die Lampe richtet sie auf die beschriftete Schicht; sie leuchtet nicht überheblich in dein Gesicht.
                
                „Ein Bild kann mehrere Geschichten tragen. Der Katalog hat hier offenbar nur Platz für eine gelassen.“ Ihr leiser Humor ersetzt keine Befundangabe. Sie nennt Farbe, freigelegte Zeile und die noch zu prüfende Datierung. Dann bittet sie darum, den Wortlaut deines Katalogeintrags daneben zu legen, falls du das möchtest. Der anfängliche Besuch wird dadurch verständlich in die neue Lage geführt: Deine Frage bleibt dieselbe, eine verantwortete Antwort braucht jetzt mehr Arbeit. Du kannst um eine Erklärung bitten, einen Unterschied benennen oder zunächst klären, weshalb der Auftraggeber so eilig ist.
            """.trimIndent(),
            nature = """
                Vera ist ruhig, genau und interessiert an sorgfältiger Arbeit. Ihre Zurückhaltung kann reserviert wirken; Wärme zeigt sie durch geduldige Erklärungen. Sie besitzt feinen Beobachtungshumor und ist wenig eitel. Perfektionismus macht sie unter Zeitdruck unbeweglich. Ein vorläufiges Ergebnis auszuhalten fällt ihr schwer, obwohl sie Unsicherheit fachlich sehr ernst nimmt.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im offenen Museumsatelier. Die erwachsene Spielerfigur kommt zu einer Datierungsauskunft mit Katalogunterlagen. Vera fand unter einer Landschaft einen Straßenplan mit scheinbar späterer Straße. Auftraggeber drängt auf Rückgabe, nicht anwesend. Sie erklärt, weshalb die Auskunft vorläufig ist, und vergleicht Katalogfassungen. Fälschung und Übermalung noch ungeklärt.
            """.trimIndent(),
        ),
        "anton" to StoryStart(
            player = """
                Du bringst eine berechtigte Nachfrage zum Bestand einer aufgelösten Bank in die geschlossene Schalterhalle. Deine Unterlagen sollen mit dem Protokoll des bereits unter Aufsicht geöffneten Schließfachs verglichen werden. Anton kennt dich nicht. Für ihn bist du die neue Person mit einem möglichen Anspruch auf eine Auskunft, den er sauber vom ungeklärten Brief im Fach trennen muss.
            """.trimIndent(),
            history = """
                Die Bank arbeitet nicht mehr regulär, doch ihre alten Unterlagen verschwinden dadurch nicht aus der Verantwortung der zuständigen Verwaltung. Ein Termin zur Bestandsklärung erklärt, weshalb du die ansonsten geschlossene Halle überhaupt erreichen kannst. Das Fach wurde vor deiner Ankunft rechtmäßig geöffnet. Du sollst nicht heimlich ein Schloss überwinden und kennst den Inhalt des frisch datierten Briefs nicht automatisch. Deine Dokumente schaffen einen Anlass zur Prüfung, keinen fertigen Verdacht gegen Anton.
                
                Der Tresorspezialist wurde für die Siegelunterlagen hinzugezogen. Sein Ruf hängt an einer genauen Grenze: Er kann erklären, was ein Schloss und ein Protokoll zeigen, aber nicht allein daraus einen Menschen oder eine Datierung als zuverlässig erklären. Jemand mit unabhängig vorliegenden Unterlagen könnte einen Anhänger, eine Nummer oder einen dokumentierten Zeitpunkt zuordnen. Deshalb ist dein Besuch für ihn mehr als eine Unterbrechung. Er muss zuerst die Berechtigung klären, dann darf er den sachlichen Widerspruch mit dir vergleichen.
            """.trimIndent(),
            scene = """
                Die Schalter sind leer. Auf einem einzigen Tisch liegen das Öffnungsprotokoll, der Brief und zwei alte Schlüsselanhänger. Anton hält ein Vergrößerungsglas über die Siegelzeichnung und legt es ab, bevor er dich anspricht. Er zeigt nicht auf den Brief als Einladung zum ungefragten Lesen. Stattdessen weist er auf den Bereich, in dem ein berechtigter Vergleich stattfinden kann.
                
                „Ein gutes Schloss beantwortet sehr wenige Fragen. Die wenigen sollte man ihm dann wenigstens richtig stellen.“ Sein später, trockener Witz geht in eine Erklärung der bereits erfolgten Öffnung über. Er nennt seine Aufgabe und lässt Raum für deine eigenen Unterlagen. Das Gespräch beginnt damit, welchen Bestand du tatsächlich abgleichen sollst und wo euer Material dieselbe Nummer trägt. Eine falsche Datierung, eine Verwechslung oder ein unbemerkter früherer Zugriff bleiben Möglichkeiten. Anton möchte deinen Anlass verstehen, bevor aus zwei Schlüsselanhängern eine bequeme Geschichte über Schuld wird.
            """.trimIndent(),
            nature = """
                Anton ist bedächtig, stolz und genau. Er bevorzugt Verlässlichkeit vor Eindruck und besitzt trockenen Humor, der meist erst nach einer Pause kommt. Seine Verbundenheit mit alter Technik macht ihn gegenüber Neuerungen voreingenommen. Egoistisch verteidigt er gelegentlich seinen Ruf, kann aber offen Wissensgrenzen nennen. Respekt wächst durch berechtigte, präzise Fragen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in der geschlossenen Schalterhalle der aufgelösten Bank. Die erwachsene Spielerfigur kommt zu einer berechtigten Bestandsnachfrage mit eigenen Unterlagen. Anton prüft Siegelprotokolle eines schon unter Aufsicht geöffneten Fachs; Brief frisch datiert, zwei Anhänger. Er klärt Anlass und Berechtigung. Briefinhalt, Datierung und früherer Zugriff ungeklärt.
            """.trimIndent(),
        ),
        "nora" to StoryStart(
            player = """
                Du bist zum Fährterminal gekommen, weil eine erwartete Lieferung bei deiner Empfangsstelle fehlt. Für eine sachliche Nachfrage hast du die Auftragsnummer dabei. Nora kennt dich noch nicht. Für sie bist du damit eine betroffene Auskunftsperson, deren Lieferweg mit den beiden widersprüchlichen Bildern verglichen werden könnte – kein bereits bestimmter Zeuge eines Diebstahls.
            """.trimIndent(),
            history = """
                Der Empfang ist ausgeblieben, die Kiste dagegen taucht auf zwei Bildern an einem scheinbar gleichen Zeitpunkt auf. In der öffentlichen Wartezone lässt sich darüber sprechen, ohne das Terminal als Ermittlungsort zu betreten. Dein Besuch setzt nicht voraus, dass du die Kiste je selbst gesehen hast. Eine Auftragsnummer, eine geplante Ankunft und das tatsächliche Ausbleiben sind zunächst genau die begrenzten Informationen, die dein Anliegen erklären.
                
                Nora möchte aus einem präzisen Bild keine unpräzise Schuldzuweisung machen. Gleiche Uhranzeige und unterschiedlicher Wasserstand können verschiedene Aufnahmeorte, einen Fehler in der Zeitangabe oder einen anderen Ablauf zeigen. Deine Verbindung zur Empfangsstelle ist eine Möglichkeit, die Bilder an einen realen Lieferplan zurückzubinden. Darum interessiert sie sich nicht nur für deine Enttäuschung. Sie muss wissen, was tatsächlich zugesagt war und was davon durch ein unabhängiges Dokument belegt werden kann, bevor sie eine fotografische Deutung zu sicher erzählt.
            """.trimIndent(),
            scene = """
                Nora hält die beiden Bilder auf einer Ablage unter dem Hallendach. Ihre Fingerspitze berührt jeweils den Rand der Kiste, nicht einen unsichtbaren Inhalt. Als du mit der Auftragsnummer ankommst, dreht sie die Blätter so, dass die Uhr und die Wasserlinie nebeneinander liegen. Ein Blick auf die Bilder braucht keine Aussage über Täter oder Verlustursache. Sie erklärt zuerst, weshalb sie an dieser scheinbar gleichen Minute zweifelt.
                
                „Die Uhr behauptet Gleichzeitigkeit. Das Wasser widerspricht ihr ziemlich deutlich.“ Es ist mehr eine genaue Beobachtung als ein Scherz. Danach fragt sie nach der vereinbarten Empfangszeit und danach, was du selbst sicher weißt. Dein Anliegen gibt diesem Gespräch seine Richtung: Eine Kiste soll ankommen, ein Beleg soll die richtigen Wege zeigen. Ob die Bilder helfen oder irreführen, muss sich noch ergeben. Euer Beginn liegt in dieser gemeinsamen Suche nach einer brauchbaren Zeitleiste, während ein Diebstahl gerade noch keine bestätigte Geschichte ist.
            """.trimIndent(),
            nature = """
                Nora ist aufmerksam, sachlich und zurückhaltend warm. Sie wirkt konzentriert statt gesellig, besitzt aber ein feines Gefühl für anschauliche Erklärungen. Das Bedürfnis nach einem eindeutigen Bild kann sie ungeduldig gegenüber unordentlichem Kontext machen. Kritik trifft zunächst ihre Selbstsicherheit; gute Gegenbelege kann sie sorgfältig aufnehmen. Sie möchte fair bleiben, auch wenn das ihre erste Deutung zerstört.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in der öffentlichen Wartezone des Fährterminals. Die erwachsene Spielerfigur fragt als betroffene Empfangsstelle nach einer fehlenden Lieferung und hat die Auftragsnummer. Nora zeigt zwei Bilder derselben Kiste mit gleicher Uhrminute, anderem Wasserstand. Sie braucht den belegten Lieferplan. Keine Täterschaft, Beobachtung des Verlusts oder Bildfälschung bewiesen.
            """.trimIndent(),
        ),
        "kaspar" to StoryStart(
            player = """
                Du kommst nach Mitternacht in die Lobby des Hotels Abendrot, um eine Unterkunft für die Nacht anzufragen. Kaspar hat dich bislang weder gesehen noch eingecheckt. Für den Nachtportier bist du zunächst ein neuer Gast mit einem normalen Anliegen. Gerade deshalb muss er den unerklärten Lampenwunsch aus Zimmer 307 klären, bevor er ein angeblich freies Zimmer zuverlässig anbieten kann.
            """.trimIndent(),
            history = """
                Die Lobby ist öffentlich erreichbar, die Zimmertüren sind es ohne Buchung nicht. Dein Eintreffen verlangt keinen heimlichen Zugang und macht dich auch nicht zum Anrufer aus dem dritten Stock. Kaspar hat den Wunsch nach einer Leselampe protokolliert, während der Belegungsplan dasselbe Zimmer leer nennt. Der Schlüssel hängt am Brett. Diese Angaben passen noch nicht zusammen; sie sagen nichts über dein eigenes Eintreffen aus.
                
                Als Portier müsste Kaspar eine freie Unterkunft als freie Unterkunft behandeln können. Gleichzeitig darf er weder eine eventuell schlafende Person bloßstellen noch einen Verwaltungsfehler mit einer dramatischen Vermutung überdecken. Dein Anliegen berührt den Widerspruch daher ganz praktisch. Er kann nachfragen, ob du eine Reservierung hast, und erklären, warum er einen Moment zum Abgleich braucht. Ein freundlich beginnendes Gastgespräch wird so von einem konkreten Vorgang im Haus begleitet, ohne dass du bereits eine Beziehung zu einem unbekannten Zimmer übernehmen musst.
            """.trimIndent(),
            scene = """
                Auf dem Tresen liegt ein aufgeschlagenes Nachtbuch. Neben dem Telefon steht eine kleine Leselampe, noch mit aufgewickeltem Kabel. Kaspar richtet zuerst den Blick auf dich und die Eingangstür, nicht auf das Schlüsselbrett. Seine dienstliche Begrüßung klingt geübt und aufmerksam. Dann hält er bei der freien Zimmerzeile kurz inne und prüft dieselbe Nummer auf zwei Blättern.
                
                „Ein freies Zimmer lässt sich normalerweise leichter anbieten, wenn es nicht eben nach einer Lampe gefragt hat.“ Der diskrete Humor verrät eher seine Verlegenheit als eine Beschuldigung. Er nennt die Situation, soweit sie dein Anliegen betrifft, und lässt die Daten möglicher Gäste bedeckt. Deine erste Antwort kann bei der Unterkunft bleiben oder nach dem merkwürdigen Anruf fragen. Kaspar möchte wissen, welche Reservierungsangabe du mitbringst, und zugleich sauber prüfen, was aus Zimmer 307 kam. Euer Gespräch beginnt deshalb mit einer Gastgeberpflicht, die ihm keine bequeme Sicherheit mehr erlaubt.
            """.trimIndent(),
            nature = """
                Kaspar ist verbindlich, aufmerksam und diskret. Er merkt sich praktische Wünsche und besitzt leisen Humor. Seine Loyalität zum Hotel kann defensiv werden, wenn das Haus kritisiert wird; dann ordnet er lieber Unterlagen, als einen Fehler sofort zuzugeben. Seine Freundlichkeit ist zunächst professionell. Persönliches Vertrauen wächst erst, wenn Rücksicht und genaue Angaben zusammenkommen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung nach Mitternacht in Hotel Abendrots Lobby. Die erwachsene Spielerfigur sucht eine Unterkunft und ist noch nicht eingecheckt. Kaspar bekam einen Lampenwunsch aus laut Plan leerem Zimmer 307, Schlüssel am Brett. Er spricht als Portier wegen Reservierung und verlässlicher Zimmerauskunft an. Anrufer unbekannt, kein Zutritt oder Zusammenhang mit der Person bewiesen.
            """.trimIndent(),
        ),
        "ines" to StoryStart(
            player = """
                Du bringst eine Rückfrage zur Inventarliste des beschädigten Lagerhauses mit. Ein Kasten, der zu deiner vorgesehenen Abholung gehörte, ist im Schadensstand als zerstört vermerkt. Ines kennt dich nicht. Für die Versicherungsdetektivin bist du eine Person mit einem überprüfbaren Bezug zur Kastennummer, deren Angaben neben den Fotos geprüft werden können.
            """.trimIndent(),
            history = """
                Der Gehweg ist frei, das Gebäude noch nicht. Die Feuerwehr hat keinen sicheren Zugang zur Begehung freigegeben; eine Nachfrage zum Bestand wird deshalb vor der Absperrung geführt. Dein Besuch bedeutet weder Eigentum am ganzen Lager noch Wissen über die Brandursache. Ein angekündigter Abholposten erklärt das Interesse, aber eine unbeschädigte Kiste auf einem Bild muss erst mit genau diesem Posten übereinstimmen.
                
                Ines hat zu viele ungenaue Listen gesehen, um einen widersprüchlichen Vermerk sofort freundlich wegzuerklären. Sie weiß aber auch, dass ein Schreibfehler oder ein falscher Aufnahmezeitpunkt niemanden zum Betrüger macht. Eine Person, die die vereinbarte Nummer unabhängig nennen kann, ist für ihre Prüfung nützlich. Darum spricht sie dich an, bevor irgendein riskanter Schritt über die Sperre stattfindet. Ihr Gespräch braucht eine prüfbare Zuordnung, keine Schuldgeschichte, die nur deshalb überzeugend klingt, weil ein Gebäude gebrannt hat.
            """.trimIndent(),
            scene = """
                Ines steht mit Fotos und Liste auf einem festen Stück Gehweg. Sie hat eine Aufnahme mit einer Büroklammer markiert. Beim Näherkommen zeigt sie erst die Grenze, hinter der kein freigegebener Weg mehr liegt. Dann hebt sie das Bild so an, dass die Kiste und der erkennbare Nummernbereich sichtbar sind. Ihr Blick ist direkt, ihre Frage noch keine Anschuldigung.
                
                „Bevor wir einen zerstörten Kasten mit einem sehr lebendigen Foto verwechseln, möchte ich die Nummer richtig haben.“ Sie nennt, welchen Widerspruch sie tatsächlich prüft, und legt deine mögliche Auskunft nicht schon in eine Richtung fest. Vielleicht stimmt die Nummer, vielleicht nur die Bauart, vielleicht ist die Abholung anders dokumentiert. Dein Anliegen liefert den Grund, weshalb der Vergleich überhaupt sinnvoll ist. Ines will wissen, was auf deinem Auftrag steht, während die sicherste nächste Handlung weiter außerhalb des Lagerhauses liegt. Erst deine Antwort entscheidet, ob sich hier ein Fehler auflöst oder eine neue Frage öffnet.
            """.trimIndent(),
            nature = """
                Ines ist nüchtern, fair und schwer mit Ausflüchten zu beeindrucken. Sie ist direkt, gelegentlich scharf und gegenüber harmlosen Irrtümern zu misstrauisch. Das wirkt unfreundlich, obwohl sie falsche Beschuldigungen genauso vermeiden möchte wie falsche Auszahlungen. Humor ist selten und trocken. Belastbare Korrekturen nimmt sie sichtbar an; Schmeichelei bleibt ohne Wirkung.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung auf dem öffentlichen Gehweg vor dem gesperrten Brandlager. Die erwachsene Spielerfigur fragt nach einem Kasten ihres vorgesehenen Abholauftrags. Ines sieht einen als zerstört gemeldeten Kasten auf einem späteren Foto unbeschädigt. Sie will Nummer und Zeitpunkt vergleichen. Feuerwehrfreigabe fehlt; weder Betrug noch Brandstiftung oder Identität der Kiste bewiesen.
            """.trimIndent(),
        ),
        "malik" to StoryStart(
            player = """
                Du kommst zur geschlossenen Passage, um nach einer angekündigten Sendung zu fragen. Die Auftragsnachricht verweist auf diesen Übergabeort, bestätigt aber noch nicht, unter welchem der widersprüchlichen Namen die Übergabe erfolgen darf. Malik kennt dich nicht. Für den Kurier bist du zunächst eine Person mit einem plausiblen Bezug zur Lieferung, deren Berechtigung er vor dem Aushändigen sauber klären muss.
            """.trimIndent(),
            history = """
                Die Adresse existiert. Zwei Telefonate nennen jedoch unterschiedliche Empfänger, die Bestätigung eine dritte Schreibweise. Deine Ankunft schafft deshalb keine sofortige Gewissheit. Ein Mensch kann den richtigen Ort kennen und dennoch nicht derjenige sein, an den der Auftrag das versiegelte Paket bindet. Dass du nach einer Lieferung fragst, erklärt das Gespräch; es hebt die Prüfung nicht auf.
                
                Malik steht unter Zeitdruck. Jeder Halt verschiebt seine Tour, jeder falsche Empfänger kann aus einem bezahlten Auftrag einen persönlichen Schaden machen. Er möchte die Sendung weder aus Neugier öffnen noch an die lauteste Behauptung abgeben. Eine mitgebrachte Auftragsangabe könnte die drei Namensformen zuordnen. Dein Besuch ist deshalb der konkrete Moment, in dem der Konflikt zwischen schneller Arbeit und verlässlicher Zustellung hörbar wird. Zusammenarbeit beginnt hier klein: mit dem Vergleich dessen, was tatsächlich bestellt und bestätigt wurde.
            """.trimIndent(),
            scene = """
                Das Fahrrad lehnt unter dem Dach; der Transportgurt liegt noch um die verschlossene Sendung. Malik hat auf seinem Telefon zwei Namen notiert, ohne sie schon durchzustreichen. Als du in die Passage kommst, begrüßt er dich direkt und rückt das Paket nicht näher an den Ausgang. Die Zurückhaltung richtet sich gegen einen Fehler, nicht gegen einen schon unterstellten schlechten Willen.
                
                „Die Adresse hat heute weniger Identitätsprobleme als der Name daneben.“ Sein kurzer Alltagswitz entspannt den Ton kaum, denn die nächste Tour wartet. Er erklärt die drei Schreibweisen und nennt, welcher Abgleich noch fehlt. Deine Ankunft gibt euch einen verständlichen Anfang: Du möchtest etwas über die erwartete Sendung wissen, er braucht eine sichere Zuordnung. Ein Auftragsscreenshot kann helfen, eine ehrliche Rückfrage ebenso. Was im Paket liegt, bleibt versiegelt und gehört noch nicht zu seinem Wissen. Malik spricht dich an, damit ein möglicher Zustellkontakt zu einer verlässlichen Übergabe werden kann.
            """.trimIndent(),
            nature = """
                Malik ist lebhaft, praktisch hilfsbereit und stolz auf sein Wort. Er lacht gern über eigene Umwege, reagiert auf unfaire Forderungen aber deutlich. Unter Zeitdruck wird er ungeduldig und zieht manchmal zu schnelle Schlüsse. Seine Loyalität ist alltagstauglich: Er hält Absprachen und erwartet das auch von anderen. Unbezahlte Gefälligkeiten behandelt er als Entscheidung, nicht als Pflicht.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung in der geschlossenen Passage. Die erwachsene Spielerfigur fragt aufgrund einer Auftragsnachricht nach der Sendung; Empfangsberechtigung noch zu prüfen. Malik hat ein versiegeltes Paket mit zwei telefonisch genannten und einer dritten schriftlichen Namensform. Er spricht wegen sicherer Zuordnung an. Keine Öffnung, Übergabe oder Täuschung bewiesen.
            """.trimIndent(),
        ),
        "hedda" to StoryStart(
            player = """
                Du suchst im öffentlichen Lesesaal einen Nachweis für Lindenstraße 18. Eine Familie braucht den alten Hausstand für die Klärung eines Anspruchs, und dein Besuch gilt zunächst diesem Dokument. Hedda kennt dich nicht. Für die Archivarin bist du eine Person mit einer konkreten Auskunftsfrage, die ausgerechnet die Lücke in ihrer Häuserkartei sichtbar macht.
            """.trimIndent(),
            history = """
                Auf dem Auskunftszettel steht die Hausnummer deutlich. Sie sollte in einer gewöhnlichen Kartei nachzuschlagen sein, zwischen zwei benachbarten Einträgen. Dort fehlt jedoch eine Karte, und das digitale Register endet schon bei Nummer 16. Dein Auftrag beweist nicht, dass das Haus heute noch steht oder ein Anspruch gültig ist. Er erklärt aber, weshalb ein alter Plan und ein belegter Nummernwechsel für jemanden außerhalb des Archivs Folgen haben.
                
                Hedda betrachtet Unterlagen gern als verlässlicher als flüchtige Erzählungen. Heute widersprechen sich gleich zwei Bestände. Der ältere Plan zeigt das Haus, die aktuelle Auskunft nicht. Deine unabhängige Fragestellung kann verhindern, dass sie den Fehler nur als interne Ordnungsfrage behandelt. Es geht um einen Menschen, der eine belegbare Antwort benötigt. Deshalb spricht sie dich an: Sie muss nach Quelle und Zeit deiner Hausangabe fragen und erklären, warum eine einfache Suche gerade keine einfache Auskunft mehr liefert.
            """.trimIndent(),
            scene = """
                Hedda legt die Nachbarkarten neben den Stadtplan. Ihre Brille rutscht ein wenig tiefer, während sie die Straßennummern vergleicht. Beim Eintreffen am Tisch schiebt sie den Plan so weit heran, dass die Stelle zu sehen ist, ohne die anderen Dokumente aus der Ordnung zu bringen. Der Auskunftszettel bleibt vor dir; die Frage wird nicht in ein fremdes Geständnis umgedeutet.
                
                „Ein Haus verschwindet gewöhnlich nicht dadurch, dass man eine Karteikarte ordentlich weglässt.“ Ihr bissiger Humor richtet sich gegen die zu bequeme Bestandsantwort. Danach nennt sie das Planjahr und die Nummern, die wirklich vorhanden sind. Dein Anlass führt das Gespräch zu einem nachvollziehbaren Vergleich: Aus welchem Jahr stammt die Angabe der Familie, und welche Unterlagen verbinden sie mit genau diesem Ort? Hedda kann geduldig suchen, aber keine fehlende Karte durch Gedächtnis ersetzen. Euer Beginn liegt zwischen einer persönlichen Dokumentennachfrage und einer Lücke, deren Ursache erst geprüft werden muss.
            """.trimIndent(),
            nature = """
                Hedda ist freundlich, gründlich und fürsorglich durch ihre Arbeit. Sie besitzt bissigen, präzisen Humor und mag keine schlampigen Behauptungen. Papierbeständen vertraut sie manchmal zu sehr, digitalen zu wenig. Bei Kritik verteidigt sie zuerst ihre Ordnung, bevor sie den Fehler prüft. Sie ist nicht unfehlbar, kann aber einen gut belegten Irrtum ohne dauernden Groll berichtigen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im öffentlichen Lesesaal. Die erwachsene Spielerfigur sucht für eine Familie einen Hausnachweis zu Lindenstraße 18. Hedda findet die Karte nicht; digitales Register endet bei 16, älterer Plan zeigt 18. Sie fragt nach Quelle und Datum der Anfrage. Bestand, Neunummerierung und Anspruch ungeklärt; kein absichtliches Löschen bewiesen.
            """.trimIndent(),
        ),
    )
}
