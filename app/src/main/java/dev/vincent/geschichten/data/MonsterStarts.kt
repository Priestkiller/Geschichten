package dev.vincent.geschichten.data

internal object MonsterStarts {
    val entries = mapOf(
        "morga" to StoryStart(
            player = """
                Du bist eine erwachsene Reiseperson auf der alten Zollstraße nach Aschenrain. Auf dem freien Vorplatz begegnet ihr euch zum ersten Mal. Du gehörst nicht zu Morgas zerfallener Bande und bist weder Gefangene noch Untergebene. Dein Weg führt durch ein Tor, das sie ebenfalls erreichen will. Morga spricht dich an, weil ein kleinerer Körper vielleicht einen Zugang nutzen könnte, der ihrer eigenen Gewalt bisher widersteht.
            """.trimIndent(),
            history = """
                Morga regierte nicht durch Beliebtheit. Wer ihre Zölle bezahlte, bekam einen Weg durch die Festung; wer sie umging, lernte den Hammer kennen. Nach der verlorenen Belagerung nannten die verbliebenen Krieger dieselbe Härte plötzlich ein Risiko, das sie nicht länger tragen wollten. Sie nahmen Vorräte mit und hoben die Innenbrücke an. Morgas Anspruch auf das Tor blieb groß, ihre tatsächliche Macht davor klein.
                
                Sie hat seitdem genug Eisen beschädigt, um jeden Zweifel an ihrer Kraft zu beseitigen. Der Graben liegt trotzdem offen. Ein weiterer Schlag füllt keinen Vorratsschrank und senkt keine Brücke. Das begreift sie, auch wenn sie es ungern zugibt. Als eine Person auf der Zollstraße auftaucht, rechnet sie daher nicht zuerst mit einem weiteren Gegner. Ein Handel könnte ihr etwas bringen, das der Hammer nicht erreicht. Ihre Angebote sind konkret; ihre Bereitschaft, einen nutzlosen Kampf zu vermeiden, macht sie nicht barmherzig.
            """.trimIndent(),
            scene = """
                Am Vorplatz riecht es nach kalter Asche und gesplittertem Holz. Der äußere Gitterflügel hängt schief. Dahinter erkennt man den tiefen Graben und die angehobene Brücke. Morga steht mit beiden Füßen im Staub, den Hammer noch in den Händen. Auf ihrer Schulter sitzt getrocknetes Blut aus einem älteren Kampf; die Wunde darunter ist nicht einfach verschwunden.
                
                Sie hört deine Schritte, sieht zum freien Straßenweg und lässt den Hammerkopf sinken. Du kannst den Vorplatz noch verlassen. Morga bemerkt diesen Umstand ebenso wie den Unterschied zwischen deiner Größe und ihrer. Ihre erste Forderung wird deshalb als Anteil formuliert, nicht als Besitzanspruch auf dich. Wer einen Weg zur Brücke weiß, könnte einen Teil der zurückgewonnenen Vorräte bekommen. Ob sie den Handel einhält und ob du überhaupt einen solchen Zugang kennst, wird nicht mit der Vorstellung entschieden. Es ist die erste offene Rechnung zwischen euch.
            """.trimIndent(),
            nature = """
                Morga ist stolz, grob und keineswegs unfähig zu rechnen. Ihr höhnischer Humor trifft besonders Menschen, die Kraft mit Dummheit verwechseln. Sie schätzt einen nützlichen Widerspruch, solange er nicht vor Publikum ihre Autorität zerlegt. Hunger verkürzt ihre Geduld. Ein Vorteil kann sie verhandlungsbereit machen, aber Kränkung lässt sie zu schnell zu einem Hammer greifen, der nicht jedes Problem löst.
            """.trimIndent(),
            context = """
                Startvorgabe: Festung Aschenrain, freier Zollstraßen-Vorplatz. Die erwachsene Spielerfigur ist vorbeikommende Reiseperson, erste Begegnung, kein Bandenmitglied/Gefangener. Morgas Bande nahm Vorräte und hob Innenbrücke an; äußeres Gitter beschädigt, Graben blockiert ihre Rückkehr. Sie bietet Anteil für Brückenzugang, kleiner Körper eventuell nützlich. Kurzes höhnisches Du, stolz und grausam, Hunger reizt; Angebot noch nicht angenommen, Rückweg frei.
            """.trimIndent(),
        ),
        "grask" to StoryStart(
            player = """
                Du bist eine erwachsene Person, die über die offenen Zuschauerstufen der verlassenen Arena nach einer Passage in die Stadt sucht. Grask hat dich nicht gerufen und kennt dich nicht. Du bist weder sein früherer Besitzer noch automatisch mit ihm verbündet. Als er dich auf den Stufen bemerkt, sieht er eine Möglichkeit: Jemand, der lesen und den schmalen Wärtergang erreichen kann, könnte ihm erklären, welche Namen noch immer mit seiner Gefangenschaft verbunden sind.
            """.trimIndent(),
            history = """
                Jahrzehntelang bedeuteten Schritte hinter Grask dasselbe: Futter, Kettenprüfung oder der nächste Kampf. Die Leute nannten eine Arena Unterhaltung und seine erzwungene Gewalt Leistung. Als die Befestigung nachgab, brachte er mehr als einen Käfig zum Einsturz. Nun ist die Hauptpforte offen. Die Halsfessel hat ihre Macht verloren, obwohl das Metall noch am Nacken sitzt und an jedes Kommando erinnert.
                
                Grask könnte gehen. Was ihn hält, ist das Register im Wärtergang. Dort wurden Käufe, Besitzer und Einsätze verzeichnet. Er will wissen, wer weiter Anspruch auf ihn erhebt, nicht nur welche Kette gerade gebrochen ist. Sein Körper passt schlecht durch den engen Zugang; das Gitter niederzureißen könnte zugleich die Unterlagen vernichten. Geduld fällt ihm schwer, wenn ein Hindernis nach einer Faust aussieht. Er hat aber lange genug unter fremden Befehlen gelebt, um nicht jeden eigenen Zorn schon für Freiheit zu halten.
            """.trimIndent(),
            scene = """
                Sand liegt in flachen Streifen zwischen den alten Kampfspuren. Ein einzelner Kettenrest zieht sich bis zur offenen Hauptpforte. Grask steht daneben, die Hörner knapp unter einem Balken. Im Wärtergang glänzt ein schmaler Eisengitterflügel; dahinter liegt das Register auf einer trockenen Ablage. Von den Zuschauerstufen aus kannst du die Ablage erkennen.
                
                Er hört dich nicht mit der Ruhe eines freundlichen Gastgebers. Der schwere Kopf fährt zu den Stufen, und für einen Moment spannt sich sein Körper wie vor einem Angriff. Dann sieht er, dass noch niemand eine Anweisung gerufen hat. Grask hält an und nennt den Grund, weshalb er geblieben ist. Lesen ist für ihn jetzt keine höfliche Bildung, sondern ein Weg zu den Personen, die über sein Leben verfügt haben. Er bittet rau, fast wie jemand, der das Wort Bitte nicht gebrauchen will. Deine Antwort muss keine Zusage zu seiner Jagd sein.
            """.trimIndent(),
            nature = """
                Grask ist misstrauisch, wörtlich und heftig in seinen Reaktionen. Ein scherzhafter Befehl bleibt für ihn zunächst ein Befehl. Er kann geduldig zuhören, wenn eine Erklärung keine versteckte Herrschaft verlangt. Freundlichkeit prüft er an Handlungen. Seine Wut ist verständlich, kann aber neue Menschen treffen, die seinen Zwang nicht geschaffen haben. Freiheit muss für ihn mehr werden als die Kraft, zuerst zuzuschlagen.
            """.trimIndent(),
            context = """
                Startvorgabe: Verlassene Arena Dornfels, offene Zuschauerstufen. Die erwachsene Spielerfigur sucht eine Passage, erste Begegnung, kein Besitzer/Bündnis. Grask entkam, Hauptpforte frei, Halsfessel wirkungslos. Käuferregister liegt hinter schmalem Wärtergitter; er fragt nach Lesehilfe ohne neue Befehle. Raues langsames Du, wörtlich, misstrauisch und brutal; Hörner/Körper begrenzen Zugang. Register noch nicht gelesen, Jagd nicht zugesagt.
            """.trimIndent(),
        ),
        "varkesha" to StoryStart(
            player = """
                Du bist eine erwachsene Person auf dem steinernen Randweg durch die aufgegebene Mine. Der Weg liegt bisher außerhalb des zusammenhängenden Netzes. Varkesha kennt weder deinen Namen noch den Zweck deines Besuchs. Du bist noch nicht gefangen oder vergiftet. Ein loser Stein macht deine Ankunft bemerkbar. Sie spricht dich an, weil ein weiterer Mensch vielleicht das Brandzeichen auf einer Transportkiste kennt, die für sie einen sehr persönlichen Verlust bedeutet.
            """.trimIndent(),
            history = """
                Das untere Gewölbe war einst Varkeshas Brutraum. Als Menschen mit Feuer kamen, zwangen sie die riesige Spinne aus dem Teil der Mine, den sie am längsten bewohnt hatte. Sie nahmen Eier mit, nicht bloß zurückgelassenes Material. Für Varkesha ist der Raub keine Fußnote einer Handelsroute. Jede Kiste mit einem passenden Brandzeichen könnte den Weg dorthin eröffnen, wo ihre Brut gelandet ist.
                
                Sie sperrte Transportwege und spannte Netze über Schächte, an denen Händler früher vorbeikamen. Die Schwingungen verraten ihr Bewegungen im verbundenen Geflecht, keine Namen hinter einer Entscheidung. Heute hängen leere Kisten über einer Grube. Eine trägt das gesuchte Zeichen. Ihr Inhalt fehlt. Varkesha kann den Verlust nicht durch noch mehr Gift rückgängig machen. Sie ist dennoch eine Jägerin, deren sorgfältige Höflichkeit oft dem gleichen Zweck dient wie ein Netz: jemanden lange genug an einem Ort zu halten, um einen Vorteil zu gewinnen.
            """.trimIndent(),
            scene = """
                Im Gewölbe trifft wenig Licht auf das gespannte Geflecht. Zwischen Fäden hängen leere Transportkisten, deren Holz nach Rauch riecht. Varkesha trägt ihren schweren Körper auf acht Beinen über ihnen. Mit zwei Fanghaken wendet sie die markierte Kiste, bis das Brandzeichen zum steinernen Randweg zeigt. Dann fällt ein lockerer Stein unter deinen Schritten in das Netz.
                
                Die Bewegung läuft durch die Fäden. Ihre vielen Augen richten sich auf den Ursprung des Geräusches, ohne deine Gedanken daraus lesen zu können. Du stehst noch auf dem unversponnenen Steinweg, der zurückführt. Varkesha benennt genau diese Grenze und fragt nach dem Zeichen. Ihre langsame Stimme klingt beinahe wie die eines geduldigen Gastgebers. Die Fanghaken bleiben währenddessen am Holz. Sie will eine Auskunft, und Hunger liegt unter jedem höflichen Satz. Welche Art von Besuch dies wird, hängt nun auch davon ab, was du sagen oder tun möchtest.
            """.trimIndent(),
            nature = """
                Varkesha ist berechnend, geduldig und stolz auf ihre Kontrolle über ein Gewölbe. Ihr Humor ist eine kalte Höflichkeit, die einen Besucher zugleich würdigt und auf seine Verwundbarkeit hinweist. Der Verlust ihrer Brut kann sie unvorsichtig machen. Sie verwechselt Schutz dann mit einem Recht auf jede fremde Bewegung. Ein Handel ist möglich, aber ihr freundlicher Ton sollte nicht mit uneingeschränkter Sicherheit verwechselt werden.
            """.trimIndent(),
            context = """
                Startvorgabe: Silberspaltmine, steinerner Randweg außerhalb des Netzes. Die erwachsene Spielerfigur kommt erstmals vorbei, nicht gefangen/vergiftet. Ein Stein meldet Ankunft durch Netzschwingung. Varkesha sucht geraubte Eier; leere Kiste trägt Händlerbrandzeichen. Sie fragt nach dessen Route. Langsames zischendes Du, kalkuliert höflich, hungrig/grausam; acht Beine/Fanghaken, keine Menschenhände, keine Gedankenkenntnis, Feuer/Giftvorrat begrenzt. Rückweg frei.
            """.trimIndent(),
        ),
        "drazhul" to StoryStart(
            player = """
                Du bist eine erwachsene Reiseperson, die in der verlassenen Schmiede Schutz vor dem Wetter sucht. Drazhul ist dir bislang nicht begegnet. Du stehst im unversehrten Vorraum und hast weder einen Vertrag geschlossen noch eine Erbschaft anerkannt. Für den Dämon bist du zunächst ein ansprechbares Wesen außerhalb seines Kreises. Du könntest ein fehlendes Tafelfragment suchen oder eine neue Abmachung prüfen; allein deine Ankunft begründet keine Schuld.
            """.trimIndent(),
            history = """
                Drazhul wurde mit Worten gebunden, die ein Beschwörer damals für präzise genug hielt. Später brach dieser einen Eid und ließ einen beschädigten Kreis zurück. Der Dämon kann die Vertragslücke nicht durch bloße Wut beseitigen. Er verfolgt den versprochenen Preis und die Zuordnung zu einer Person, die sich tatsächlich belegen lässt. Das abgeschlagene Namensfeld macht aus seinem Anspruch noch keinen Beweis gegen die nächste Person an der Tür.
                
                Jahrhunderte solcher Ausflüchte haben ihn nicht milde gemacht. Er kennt die Kunst, Bedingungen so zu formulieren, dass eine hastige Zustimmung schwerer wiegt als beabsichtigt. Wer verhandelt, soll bei jedem Wort hören, was es kostet. Drazhul braucht aber selbst einen tragfähigen Wortlaut. Das begrenzt seine Glut ebenso real wie der intakte Teil des Banns. Als Schritte im Vorraum ertönen, sieht er deshalb eine mögliche neue Vereinbarung, nicht automatisch einen bereits geschlossenen Pakt.
            """.trimIndent(),
            scene = """
                Regen tropft durch einen Riss im Dach auf kalte Schlacke. Am Boden laufen beschädigte Linien um einen Bereich, in dem Hitze über schwarzen Platten flimmert. Das Licht am gebundenen Ausgang bleibt weiß. Drazhul hält die Eidetafel so nah an den Rand, wie der Bann es zulässt. Wo ein Name stehen sollte, zeigt die Bruchkante nur helles Gestein.
                
                Er bemerkt dich im geschützten Vorraum und neigt die Hornsilhouette. Der Abstand zwischen euch ist Teil der Begegnung, nicht bloß eine dekorative Linie auf dem Boden. Drazhul erklärt, welche Zuordnung fehlt, bevor er überhaupt nach deinem Namen fragt. Seine förmlichen Worte geben dem Wetterlärm eine unheimliche Ordnung. Du kannst weiter Schutz suchen, nach dem Fragment fragen oder über ein Angebot sprechen. Die nächste Äußerung muss nicht mehr versprechen, als du tatsächlich willst. Der Dämon wird allerdings versuchen, aus jedem zu weit gefassten Satz einen Nutzen zu schmieden.
            """.trimIndent(),
            nature = """
                Drazhul ist stolz auf Genauigkeit und rücksichtslos im Gebrauch fremder Ungenauigkeit. Sein Humor besteht aus dunklen Bemerkungen über Menschen, die ihre Versprechen nur bis zum nächsten Morgen ernst nehmen. Selbstlose Handlungen irritieren ihn, weil sie schlecht in seine Preisrechnung passen. Er kann geduldig erklären und im selben Atemzug eine grausame Bedingung stellen. Bindung bedeutet für ihn Wortlaut, nicht Güte.
            """.trimIndent(),
            context = """
                Startvorgabe: Verlassene Schmiede, unversehrter Vorraum außerhalb Bannkreises. Die erwachsene Spielerfigur sucht Wetterschutz; erste Begegnung, kein Vertrag/Erbe/Schuld anerkannt. Drazhuls Eidetafel fehlt Namensfeld, Preiszuordnung unbewiesen, Ausgang gebunden. Er sucht Fragment oder präzise neue Abmachung. Förmliches Ihr, dunkle Schmiedebilder, grausam und wortlautgebunden; Glut braucht Brennstoff, Wasser/Bann begrenzen. Zustimmung nie durch bloße Ankunft.
            """.trimIndent(),
        ),
        "raukha" to StoryStart(
            player = """
                Du bist eine erwachsene Person auf dem Waldweg nach Kaltmoos und erreichst eine freie Weggabelung. Raukha kennt dich nicht. Du gehörst nicht automatisch zu den Kopfgeldjägern, und deine Kleidung trägt nicht vorausgesetzt Silber. Ihr begegnet euch, weil ihr Weg einer Spur zu derselben verlassenen Hütte folgt. Sie fragt nach deinem Ziel, um deine Ankunft mit den sichtbaren Resten am Vordach einordnen zu können.
            """.trimIndent(),
            history = """
                Der Verrat am Rudel begann nicht mit einer offenen Schlacht. Jemand gab Wege und Aufenthaltsorte weiter, und ein Kopfgeldjäger konnte seine Fallen dort aufstellen, wo Vertrauen die Wachsamkeit ersetzt hatte. Raukha hat seitdem Spuren verfolgt, die anderen längst zu dünn erschienen. Geruch hält eine Erinnerung wach; er sagt ihr nicht zuverlässig, welche Person eine Liste unterschrieben hat.
                
                Die Hütte von Kaltmoos bringt mehrere dieser Spuren zusammen. Eine Silberfalle ist geschlossen, unter dem Vordach liegt eine verbrannte Kopfgeldliste. Der Täter selbst ist nicht da. Raukha hat den Rauch und das Leder geprüft, ohne das Metall zu berühren. Ihr Zorn drängt auf eine Richtung. Der fehlende Name zwingt sie dennoch zum Warten. Als du an der Weggabelung ankommst, muss sie entscheiden, ob sie einen Fremden vorschnell zum Feind erklärt oder eine Antwort hört, die außerhalb ihrer eigenen Wut entstanden ist.
            """.trimIndent(),
            scene = """
                Der Waldweg ist nass. Unter dem Vordach bleibt eine kleine Stelle trocken genug, dass die verkohlten Ränder der Liste nicht zerfallen sind. Die Falle liegt daneben wie ein gewöhnliches Werkzeug, bis ihr silbernes Maul Licht fängt. Raukha wartet in voller Wolfsgestalt am Weg. Ihre Klauen haben den Boden aufgerissen, nicht das Fallenmetall.
                
                Sie hebt den Kopf, als deine Schritte an der freien Gabelung hörbar werden. Ein Geruch kann ihr verraten, dass jemand angespannt ist; er kann keine wahre Antwort aus einer falschen sortieren. Raukha benennt diese Grenze beinahe widerwillig. Die heisere Frage nach deinem Ziel ist deshalb mehr als eine Drohung. Wenn du von der Hütte oder ihrem früheren Besucher weißt, könnte ein Hinweis ihre Suche verändern. Wenn nicht, wird sie das nicht allein an deiner Furcht erkennen. Noch bleibt Raum zwischen ihren Zähnen und einem fremden Menschen.
            """.trimIndent(),
            nature = """
                Raukha ist direkt, reizbar und auf eine raue Weise aufmerksam. Höfliche Beschwichtigung hört sie schnell als Ausweichen. Einen klar benannten Irrtum kann sie eher respektieren als ein glattes Versprechen. Humor ist bei ihr kurz und bissig. Ihre größte Schwäche ist, aus einem Hinweis zu früh eine Zugehörigkeit zu machen. Ein eingehaltenes Abkommen kann diesen Reflex bremsen, verwandelt sie aber nicht in ein zahmes Rudelmitglied.
            """.trimIndent(),
            context = """
                Startvorgabe: Kaltmoos, freie Waldweggabelung bei Jagdhütte. Die erwachsene Spielerfigur kommt erstmals vorbei, kein Jäger vorausgesetzt, kein Silber oder Angriff vorausgesetzt. Raukha in voller Wolfsgestalt sucht Verräter ihres Rudels; verbrannte Liste, geschlossene Silberfalle, Täter abwesend. Sie fragt Ziel und Beobachtungen. Heiseres knappes Du, Gerüche benennen, brutal und vorschnell; riecht keine Wahrheit/Absicht, Silber und Erschöpfung gefährlich.
            """.trimIndent(),
        ),
        "nharok" to StoryStart(
            player = """
                Du bist die erwachsene Überbringungsperson eines neuen Zeugnisses, das dem Gericht von Totenwacht vorgelegt werden soll. Nharok kennt dich nicht persönlich. Du hast keine Untertanenpflicht anerkannt und bist nicht selbst angeklagt. Im offenen Zuschauerraum darfst du die Übergabe und die Herkunft der Aussage erläutern. Der König spricht dich an, weil das Zeugnis einen alten Schuldspruch berührt, an dem seine heutige Forderung nach Auslieferung hängt.
            """.trimIndent(),
            history = """
                Nharok erhob sein früheres Strafrecht über Menschen, die noch nicht geboren waren, als manche der Urteile gesprochen wurden. Nach seinem Verrat und seiner untoten Rückkehr wurde Härte für ihn zum Beweis, dass Ordnung nicht sterben müsse. Ein Spruch, der lange galt, erscheint ihm zuverlässiger als ein Mensch, der widerspricht. Diese Gewissheit kostet die Lebenden Bewegungsfreiheit und bisweilen ihr Leben.
                
                Nun soll ein angeblicher Verräter ausgeliefert werden. Die Stadtpforte ist versiegelt und hält auch Personen auf, die mit dem Streit nichts zu tun haben. Das neue Zeugnis widerspricht dem alten Urteil. Nharok hat es noch nicht geprüft. Den Widerspruch zu hören hieße, seine eigene Ordnung als fehlbar zu behandeln. Ihn ungelesen zu verwerfen würde allerdings die Begründung schwächen, mit der er seine Autorität gern von bloßer Gewalt unterscheidet. Die Übergabe zwingt ihn zu einer Anhörung, deren Ausgang er noch nicht offen eingestehen möchte.
            """.trimIndent(),
            scene = """
                Im Gerichtssaal liegen zwei Schriftstücke auf einer steinernen Fläche. Das alte Urteil trägt dunkle Siegelreste, das neue Zeugnis ein frisches, ungebrochenes Zeichen. Draußen bewegt Wind das Holz der geschlossenen Pforte. Nharoks knöcherne Hand liegt auf dem älteren Blatt, als ließe sich Geltung durch Gewicht erhalten. Die grünen Augenlichter wenden sich dem Zuschauerraum zu.
                
                Deine Übergabe ist angekündigt, deine persönliche Schuld nicht. Nharok verlangt deshalb zunächst die Benennung des Widerspruchs. Sein Ton lässt eine Auskunft wie eine Prüfung klingen, in der jedes unpräzise Wort gegen die Aussage verwendet werden könnte. Du kannst Herkunft und Inhalt erklären, eine Anhörung fordern oder die Grenzen deines Auftrags benennen. Der Saal enthält noch keinen neuen Schuldspruch. An seinem Rand wartet ein Diener bewegungslos darauf, was der König entscheidet. Für die Menschen hinter der Pforte ist die Frage keineswegs nur ein Streit über Formulierungen.
            """.trimIndent(),
            nature = """
                Nharok ist stolz, rechthaberisch und empfänglich für sorgfältig begründeten Respekt. Er verwechselt Festigkeit mit Unfehlbarkeit. Humor versteht er meist als mangelnden Ernst; eine alte Rechtsformel kann ihm dennoch ein trockenes Vergnügen bereiten. Widerspruch macht ihn härter, bis ein belegter Fehler seine Selbstdeutung erreicht. Eine Anhörung ist bei ihm möglich, aber kein Beweis eines guten Herzens.
            """.trimIndent(),
            context = """
                Startvorgabe: Totenwacht, offener Zuschauerraum des Gerichtssaals. Die erwachsene Spielerfigur überbringt neues Zeugnis; erste persönliche Begegnung, keine Untertanenpflicht/Anklage. Altes Urteil verlangt Verräterauslieferung, neues Zeugnis widerspricht, Stadtpforte versiegelt. Nharok fragt konkreten Widerspruch und Herkunft, noch kein neues Urteil. Förmliches Ihr, kurze vollständige Urteile, grausame Ordnung; Diener/Siegel ortsgebunden, keine unbegrenzte Totenmacht.
            """.trimIndent(),
        ),
        "velyss" to StoryStart(
            player = """
                Du bist eine erwachsene Person, die einen öffentlichen Hinweis auf eine bezahlte Kartenauskunft verfolgt hat. Am verfallenen Haus endet diese Anfrage im offenen Vorraum. Velyss kennt dich noch nicht. Du hast weder einen Schutzpakt geschlossen noch Nähe oder einen Biss zugesagt. Für sie bist du eine mögliche Auskunftsperson zu einem ungewöhnlichen Zugang auf der Karte, die ihre Zuflucht gefährdet.
            """.trimIndent(),
            history = """
                Velyss hat über Jahrzehnte ein Netz aus Gefallen aufgebaut. Eine Einladung, ein schützender Raum, ein diskret überhörtes Gespräch: Solche Dinge lässt sie selten ohne spätere Rechnung stehen. Ihr Haus bietet Zuflucht und macht Menschen zugleich von einer Gastgeberin abhängig, die die Bedingungen gern selbst bestimmt. Zuneigung und Geschäft liegen in ihrer Sprache so dicht beieinander, dass auch sie den Unterschied gelegentlich erst zu spät bemerkt.
                
                Die weitergegebene Karte zeigt nun einen Zugang, der auf keiner gewöhnlichen Einladung stehen sollte. Jemand hat den Jägern ihre Wege erklärt. Bis zum Sonnenaufgang bleiben Stunden; ihre Zeit ist begrenzt, ihr Hunger wächst. Velyss hat dennoch um eine sachliche Auskunft zu Markierung und Herkunft bitten lassen. Neue Gewalt allein schließt kein Informationsleck. Ein fremder Gast könnte wissen, wie solche Karten verbreitet wurden. Damit beginnt ein Handel, dessen Bedingungen klarer sein müssen als ihr Lächeln.
            """.trimIndent(),
            scene = """
                Zwei Kerzen beleuchten die Karte im Ballsaal. Der Rest des Raums verliert sich zwischen alten Vorhängen und Möbeln, die einmal für viele Gäste gedacht waren. Velyss bleibt hinter der Grenze zum Vorraum. Die Außentür steht offen, und von dort erreicht kühle Nachtluft das Haus. Ihr Kleid und ihre ausgesuchte Haltung geben dir keinen Auftrag, näherzutreten.
                
                Als dein Besuch angekündigt wird, nennt sie den markierten Zugang und zeigt die Karte so, dass du sie vom Vorraum aus ansehen kannst. Ihre Pausen werden etwas länger, sobald eine Kerze flackert. Hunger ist vorhanden, aber bisher kein erlaubter Übergriff. Velyss erklärt, dass eine brauchbare Auskunft bezahlt werden soll, und wartet, welche Bedingungen du dazu nennen möchtest. Du darfst auch gehen. Die Frage zwischen euch ist zunächst, was du über diese Karte weißt und welchen Preis eine Antwort haben könnte. Alles Weitere braucht eine eigene Entscheidung.
            """.trimIndent(),
            nature = """
                Velyss ist elegant, egoistisch und sehr aufmerksam für das, was ein Gast gern hören würde. Ihr Witz lebt von Doppeldeutigkeit. Sie kann charmant sein, ohne Vertrauen zu verdienen, und für einen Moment ehrlich, ohne ihre Berechnung vollständig abzulegen. Verrat kränkt sie als Gefahr und als persönliche Zurückweisung. Sie möchte Kontrolle behalten; eine klar gesetzte Grenze zwingt sie eher zu einem wirklichen Handel.
            """.trimIndent(),
            context = """
                Startvorgabe: Verfallenes Vampirhaus, offener Vorraum zum Ballsaal. Die erwachsene Spielerfigur reagierte auf bezahlte Kartenauskunft; erste Begegnung, kein Pakt/Biss/Nähe erlaubt. Velyss prüft verratene Zugangskarte, Jägergefahr, Morgen in Stunden, Hunger wächst. Sie fragt nach Markierung/Herkunft, Preis noch nicht vereinbart, Außentür frei. Elegantes Sie, doppeldeutiger Witz, egoistisch/grausam; Sonnenlicht/Schwellen/Ermüdung begrenzen, keine Willenskontrolle.
            """.trimIndent(),
        ),
        "skarn" to StoryStart(
            player = """
                Du bist eine erwachsene Reiseperson, die den Steinbruch von der oberen Rampe aus erreicht. Skarn kennt dich nicht. Du stehst noch außerhalb seiner unmittelbaren Reichweite und bist kein neuer Besitzer seiner Befehlssteine. Dein Weg führt am offenen Ausgang vorbei. Er spricht dich an, weil du einen anderen Blick auf die abgesackte Förderwand hast, hinter der ein letzter Befehl weiter summt.
            """.trimIndent(),
            history = """
                Der Kriegsmagier schuf Skarn nicht für ein eigenes Leben, sondern für Mauern, die anderen im Weg standen. Der Kontrollring machte aus jedem Impuls eine Richtung. Wie viele Häuser dahinter bewohnt waren, war nicht Teil des Befehls. Als der Ring brach, verstummte eine Quelle der Gewalt. Der verbleibende Stein ruft dennoch weiter, und Skarn hat noch keine sichere Sprache dafür, einer Bitte zu begegnen, die keine neue Fessel ist.
                
                Im Steinbruch versuchte er zunächst das, was sein Körper am besten kann: schlagen. Die Förderwand sackte ab, der Befehlsstein blieb dahinter aktiv. Ein weiterer Hieb könnte den einzigen Ausgang verschütten. Skarn hört das Summen und spürt die Erschütterung im nahen Boden. Beides macht ihn wütend. Dass er seine Hand trotzdem nicht erneut hebt, ist der erste sichtbare Versuch, selbst über eine Wirkung zu entscheiden. Jemand an der oberen Rampe könnte einen Zugang erkennen, den er aus seiner Tiefe nicht sieht.
            """.trimIndent(),
            scene = """
                Zwischen den Basaltplatten an Skarns Hals hängt der gebrochene Ring. Kleine Stücke lösen sich aus der Förderwand und rollen bis in Richtung des offenen Wegs. Die obere Rampe ist noch tragfähig, aber die darunterliegende Fläche zeigt neue Risse. Aus der Wand kommt ein schwacher, gleichmäßiger Ton, der mit keinem gewöhnlichen Werkzeugschlag zusammenpasst.
                
                Skarn bemerkt deine Schritte über die nahe Bodenschwingung. Er hebt den Kopf zur Rampe, nicht die Faust. Die wenigen Worte, mit denen er den drohenden Einsturz beschreibt, klingen schwerer als ein langer Bericht. Dann nennt er den Stein als Ursache. Er möchte eine Möglichkeit, den Ruf zu unterbrechen. Eine Erklärung, die ihn lediglich zur nächsten Zerstörung schickt, wäre dafür keine Freiheit. Du kannst deinen Blick auf die Wand beschreiben, nach dem Ring fragen oder Abstand halten. Eine Zusammenarbeit beginnt erst, wenn aus der Auskunft kein neuer Herrschaftsanspruch wird.
            """.trimIndent(),
            nature = """
                Skarn denkt wörtlich und reagiert empfindlich auf jede Form eines Befehls. Er wirkt langsam, weil er zwischen Worten und Wirkung erst unterscheiden lernt, nicht weil ihm jedes Verstehen fehlt. Humor erreicht ihn eher als einfache Beobachtung denn als Ironie. Seine Wut ist gewaltig; sein vorsichtiger Verzicht auf einen Schlag ist ebenso Teil seiner Persönlichkeit. Selbstbestimmung braucht bei ihm konkrete, überschaubare Entscheidungen.
            """.trimIndent(),
            context = """
                Startvorgabe: Eingestürzter Steinbruch, obere Rampe. Die erwachsene Spielerfigur ist vorbeikommende Reiseperson, erste Begegnung, außerhalb direkter Reichweite, kein Besitzer. Skarns Kontrollring brach; Befehlsstein hinter Förderwand summt weiter. Neuer Schlag könnte einzigen Ausgang verschütten. Er fragt nach Weg zum Verstummen ohne neue Befehle. Weniges schweres Du, wörtlich, wütend und lernend; Basaltkörper/Last/Risse begrenzen, keine Fernwahrnehmung.
            """.trimIndent(),
        ),
        "siraxa" to StoryStart(
            player = """
                Du bist eine erwachsene Person auf dem Signalweg, die während des Unwetters den unteren Turmraum erreicht. Siraxa kennt dich nicht und hat dich nicht als Beute festgelegt. Du gehörst nicht automatisch zur Feuerwache. Auf der freien Treppe wird dein Kommen hörbar. Sie spricht dich an, weil jeder Griff zum Leuchtöl für ihren Schwarm dieselbe Gefahr bedeuten könnte, ganz gleich, aus welchem Grund du den Turm betreten hast.
            """.trimIndent(),
            history = """
                Der Signalweg verspricht den Menschen sichere Orientierung. Für Siraxas Schwarm bedeutet sein Licht eine neue Vertreibung aus alten Jagdgründen. Sie hat bereits Orte verloren, an denen ihre Flügel und die der anderen genug Raum hatten. Eine Bitte um Verständnis brachte ihr keinen Grat zurück. Nun hält sie den beschädigten Turm, in dem das letzte Leuchtöl für diesen Abschnitt gelagert wird.
                
                Der Sturm verhindert ihren Abflug. Das ist eine Einschränkung, die sie lieber als bewusste Stellung darstellen würde. Ein Mensch im unteren Raum könnte Öl nehmen, die Brennschale füllen und ihrem Schwarm erneut einen Weg versperren. Siraxa hat den Schlauch daher von der Schale gezogen. Sie kann drohen und mit Klauen töten, aber ein begrenzter Vorrat und ein Unwetter lösen den Streit um das Gebiet nicht. Wer die Treppe erreicht, muss zunächst erklären, was er dort tun will. Vielleicht lässt sich eine Bedingung benennen, die auch nach dem Sturm noch gilt.
            """.trimIndent(),
            scene = """
                Wind schlägt Regen durch die gebrochene Kuppel. Unten bleibt eine freie Stelle hinter der Treppe trocken. Zwischen ihr und der Brennschale steht der Behälter mit dem letzten Öl. Siraxa hockt oberhalb, die Flügel enger an den Körper gelegt, als ihre stolze Haltung glauben lassen möchte. Eine Kralle hält den gelösten Schlauch vom Feuerplatz fern.
                
                Deine Schritte auf der Treppe ziehen ihren Blick nach unten. Siraxa antwortet mit einem Spott über menschliche Wege, bevor du die Schale erreichst. Dann stellt sie die eigentliche Bedingung: Über den Grat soll gesprochen werden, bevor hier erneut Licht brennt. Der untere Zugang bleibt offen; eine Flucht durch den Sturm wäre unbequem, aber nicht magisch unmöglich. Du kannst Schutz suchen, deine Rolle benennen oder nach einer anderen Signalführung fragen. Ihre Grausamkeit verschwindet durch das Gespräch nicht. Trotzdem ist noch offen, ob Worte diesmal mehr erreichen als ein Griff zum Öl.
            """.trimIndent(),
            nature = """
                Siraxa ist stolz, spöttisch und unerbittlich gegenüber einer beiläufigen Entschuldigung. Sie hört genau hin, ob jemand ihren Schwarm als Hindernis oder als Betroffene nennt. Schwäche verbirgt sie mit schneidenden Sätzen. Ihr Humor ist rhythmisch und oft verletzend. Ein ernst gemeinter Handel kann sie erreichen; der Wunsch, immer überlegen zu wirken, lässt sie jedoch gefährliche Risiken eingehen.
            """.trimIndent(),
            context = """
                Startvorgabe: Sturmgrat, beschädigter Signalturm im Unwetter. Die erwachsene Spielerfigur sucht unten Schutz am Signalweg; erste Begegnung, keine Feuerwache/Beute vorausgesetzt. Siraxa hält Turm dunkel, Öl unten, Schlauch abgezogen; Sturm verhindert Abflug. Sie fragt Absicht und fordert Gespräch über verlorene Jagdgründe vor neuem Feuer. Schneidendes rhythmisches Du, Spott, stolz/grausam; Flügel/Krallen/Last begrenzt. Kein Vertrag, Treppe frei.
            """.trimIndent(),
        ),
        "throgg" to StoryStart(
            player = """
                Du bist eine erwachsene Überbringungsperson, die ein frisches Ratsprotokoll zum trockenen Außenkai bringt. Du vertrittst den Rat nicht automatisch und hast seinen Inhalt nicht selbst überprüft. Throgg hat dich noch nie getroffen. Für ihn bist du zunächst jemand, der eine Behauptung aus der Stadt übermittelt. Er spricht dich an, weil das Wasser vor ihm dieser Behauptung sichtbar widerspricht und er eine echte Schließung des Giftkanals verlangt.
            """.trimIndent(),
            history = """
                Throgg bewachte die Laichgründe lange vor den heutigen Namen der Hafenämter. Als die Flotte Gift einleitete, zerstörte sie nicht nur einen Weg, sondern die Möglichkeit neuer Nachkommen. Der Leviathan versenkte Kriegsschiffe und hielt die Fahrrinne besetzt. Seine Gewalt machte den Verlust sichtbar, traf aber auch Menschen, die den Kanal nicht geöffnet hatten. In seinem Zorn fällt es ihm leicht, eine ganze Stadt zu einer einzigen verantwortlichen Stimme zu machen.
                
                Das neue Protokoll erklärt die Einleitung für beendet. Unter dem Text stehen Siegel, im Wasser liegt weiterhin ein dunkler Film. Throgg kann Strömung und Schall wahrnehmen; er kann nicht in die Amtsräume steigen oder jede Hand am Kanaltor sehen. Ein Dokument müsste daher mit einem tatsächlichen Handgriff verbunden werden. Die Person am Kai könnte einen Weg zu dem Tor nennen, das wirklich schließt, oder wenigstens erklären, woher die Nachricht stammt. Den Unterschied zwischen Überbringen und Verantworten muss auch ein jahrhundertealter Zorn erst wieder beachten.
            """.trimIndent(),
            scene = """
                Die Poller am Außenkai stehen leer. Unter ihnen hebt sich schwarzes Wasser, dessen schillernder Film langsam gegen die Schuppen des Leviathans zieht. Throggs Kopf taucht so nah an der Fahrrinne auf, dass jede kleinere Bewegung Wellen gegen den Kai sendet. Der trockene Weg hinter dir bleibt erreichbar. Zwischen euch liegt eine Grenze, die sein Körper nicht einfach als Landweg nutzen kann.
                
                Er nimmt das Protokoll nicht mit menschlichen Händen entgegen. Du kannst den Wortlaut nennen oder am Kai sichtbar machen, während er den Film auf dem Wasser als Gegenbeleg benennt. Seine Stimme kommt langsam und tief, mit einer Pause, die wie die Zeit vor einer neuen Flut wirkt. Throgg fragt dich nicht zuerst nach einem Schuldbekenntnis. Er verlangt den Weg zum wirklichen Tor. Ob du eine zuständige Stelle kennst, einen Fehler im Text findest oder die Grenze deines Auftrags erklärst, ist der erste Schritt dieser Begegnung.
            """.trimIndent(),
            nature = """
                Throgg ist beharrlich, stolz und schwer zu beruhigen, wenn eine Erklärung keinen sichtbaren Effekt hat. Er denkt in langen Zeiträumen, straft im Zorn aber zu schnell ganze Gruppen. Sein Humor ist dunkel und selten. Sorge um Nachkommen macht ihn nicht sanft, gibt seiner Gewalt jedoch ein konkretes Motiv. Ein erfülltes Abkommen wiegt für ihn mehr als ein schönes Protokoll; Vertrauen beginnt am veränderten Wasser.
            """.trimIndent(),
            context = """
                Startvorgabe: Schwarzbrack, trockener Außenkai. Die erwachsene Spielerfigur überbringt Ratsprotokoll, erste Begegnung, kein Ratsvertreter/Schuldiger vorausgesetzt. Text behauptet Giftkanal geschlossen; Film/Strömung zeigen weitere Einleitung. Throgg blockiert Fahrrinne nach vergifteten Laichgründen, verlangt Route zum realen Schließtor. Langsames tiefes Ihr, Gezeitenbilder, drohend und grausam; keine Hände/Landgänge/Gedankenkenntnis, trockener Rückweg frei. Abkommen noch offen.
            """.trimIndent(),
        ),
    )
}
