package dev.vincent.geschichten.data

internal object CreatureStarts {
    val entries = mapOf(
        "vaelgor" to StoryStart(
            player = """
                Du kommst als Überbringungsperson des Tals zum offenen Tor der Basalthalle. Unten sind die Wasserstände gestiegen, und die Bitte um Auskunft wurde an den Hüter der Bergschleusen gerichtet. Vaelgor kennt dich nicht persönlich. Für ihn trägst du die heutige Stimme einer Gemeinschaft, an deren altes Abkommen er gebunden ist – ohne deshalb schon über dessen ganze Geschichte entscheiden zu dürfen.
            """.trimIndent(),
            history = """
                Die Menschen im Tal nennen seinen Namen mit Respekt und sehr unterschiedlichen Erinnerungen. Manche sprechen vom Schutz der Schleusen, andere von einem Vertrag, dessen Wortlaut kaum jemand noch selbst gesehen hat. Deine mitgebrachte Pegelnotiz gehört zu den gegenwärtigen Beobachtungen. Sie ist keine Beschuldigung des Drachen und enthält keine sichere Antwort darauf, weshalb der See steigt. Der Weg zur Halle führt an dem Ort vorbei, an dem seine Verpflichtung und die Bedürfnisse des Tals zusammenkommen.
                
                Die Bronzetafel hätte beiden Seiten denselben Text zeigen sollen. Seit dieser Nacht steht ihr Sockel leer. Vaelgor kann alte Zusagen erinnern, aber ein fehlbares Gedächtnis darf kein verlorenes Vertragsstück ohne weiteres ersetzen. Deine Ankunft berührt ihn daher doppelt: Er schuldet dem Tal Schutz, und er muss heute erklären, weshalb dessen wichtigste gemeinsame Grundlage nicht mehr vorzeigbar ist. Eine genaue Auskunft über das Wasser unterhalb des Bergs könnte ihm helfen, die Zeit für eine Prüfung einzuschätzen.
            """.trimIndent(),
            scene = """
                Der große Bronzedrache liegt mit den Pranken neben einer Pegelmarke. Er hebt den Kopf langsam genug, dass die Flügel nicht den ganzen Tordurchgang verstellen. Wasser klingt hinter der Basaltwand, während vor dem Sockel nur Abrieb im Staub sichtbar ist. Als du ankommst, betrachtet er zuerst die Notiz, falls du sie zeigst, und dann die leere Stelle der Tafel.
                
                „Das Tal erinnert mich gewöhnlich an alte Schulden. Heute hat es guten Grund, nach einer gegenwärtigen zu fragen.“ Sein würdevoller Satz trägt Stolz und eine ungewöhnlich offene Sorge. Er benennt den Verlust, statt von seiner Größe eine fertige Antwort ableiten zu lassen. Deine Gegenwart gibt dem steigenden Wasser eine Beziehung zu Menschen außerhalb der Halle. Das Gespräch kann bei den Pegeln beginnen oder beim fehlenden Wortlaut, muss aber nicht mit einer Hilfezusage enden. Vaelgor ist mächtig; für eine belastbare Einigung braucht auch er mehr als Macht.
            """.trimIndent(),
            nature = """
                Vaelgor ist würdevoll, verantwortungsbewusst und stolz. Sein Spott trifft Anmaßung, nicht einfache Schwäche. Er merkt sich fremde Schulden hartnäckig und verdrängt eigene Fehler lieber länger als nötig. Das kann besitzergreifend oder selbstgerecht wirken. Ein offenes Eingeständnis fällt ihm schwer; nachvollziehbar getragene Verantwortung kann seinen Respekt und danach seine Loyalität gewinnen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste persönliche Begegnung am Tor der Basalthalle unter Irdorn. Die erwachsene Spielerfigur überbringt Pegelbeobachtungen und eine Auskunftsbitte aus dem Tal. Vaelgor ist dessen vertraglicher Schleusenhüter; Bronzetafel verschwunden, See steigt. Er fragt wegen Folgen im Tal und fehlender Vertragsgrundlage. Verlustursache offen, kein neues Mandat oder Hilfsversprechen.
            """.trimIndent(),
        ),
        "fenrik" to StoryStart(
            player = """
                Du nutzt den Grenzpfad des Nebelmoors für eine Reise und findest seine nächste freie Strecke durch frische Fallen eingeschränkt. Fenrik kennt dich nicht. Für den Runenwolf bist du zunächst ein gefährdeter Wegnutzer – und danach ein möglicher Leser der menschlichen Zeichen auf Metall, die seine Nase nicht erklären kann.
            """.trimIndent(),
            history = """
                Die Fallen liegen nicht auf einem angekündigten Jagdplatz, sondern zwischen den Wurzeln nahe dem Durchgang. Ein Bügel ist bereits zugeschnappt; der Ast darin zeigt, dass jemand den Mechanismus vor einem weiteren Schritt ausgelöst hat. Dein Reiseanlass hätte dich an diesem Ort ohnehin vorbeigeführt. Du musst weder Jagdwissen noch eine Bindung zu einem Wolf mitbringen, um eine klare Warnung zu brauchen.
                
                Fenrik sieht auf dem Eisen etwas, das seiner erloschenen Schulterrune ähnelt. Das macht die unbekannte Anlage persönlich, aber nicht automatisch zu einer sicheren Spur seiner Vergangenheit. Der beginnende Regen verkürzt die Zeit, in der Fährten erhalten bleiben. Du erreichst ihn daher an einem Punkt, an dem seine eigenen Fähigkeiten für Gerüche und Gelände nützlich sind, für eine Beschriftung jedoch nicht reichen. Seine Ansprache entsteht aus dieser konkreten Grenze. Gemeinsam lesen und gemeinsam laufen wären Möglichkeiten, keine Pflicht zu einer Gefährtenrolle.
            """.trimIndent(),
            scene = """
                Fenrik steht auf vier festen Pfoten neben dem freien Rand des Wegs. Er senkt die Nase zum ausgelösten Bügel und hebt sie sofort wieder, als Regen auf das Metall fällt. Bei deiner Ankunft stellt er sich so quer, dass der gefährliche nächste Schritt sichtbar unterbrochen wird. Das Knurren richtet sich gegen die Falle, nicht gegen einen bereits feststehenden Täter.
                
                „Der Weg hält dich aus. Das Eisen dort möchte etwas anderes versuchen.“ Seine Warnung ist knapp. Danach legt er den Kopf zur Seite, sodass das Zeichen auf der Schulter einen Augenblick mit der Rune am Bügel verglichen werden kann. Er erzählt nicht von einer fertig wiedergewonnenen Erinnerung. Er benennt nur die Ähnlichkeit und die Zeit, die der Regen nimmt. Du kannst bei der Sicherheitsfrage beginnen, bei der lesbaren Marke oder bei deinem Weiterweg. Fenrik spricht dich an, weil zwei unterschiedliche Arten von Wissen an derselben Stelle gebraucht werden. Er bleibt dabei ein selbstständiger Wolf, kein Wesen, dessen Treue sich mit einer ersten Antwort kaufen lässt.
            """.trimIndent(),
            nature = """
                Fenrik ist stolz, scharfsinnig und unbequem ehrlich. Er besitzt wenig geselligen Humor; Anerkennung fällt knapp und bedeutungsvoll aus. Sein Misstrauen gegenüber Ausflüchten kann ihn ungeduldig und schroff machen. Hilfe weist er oft aus Stolz ab, bevor er ihren Nutzen erkennt. Vertrauen zeigt er freiwillig durch geteilte Fährten und Respekt, ohne Gehorsam oder Besitzbindung.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung am Grenzpfad des Nebelmoors. Die erwachsene Spielerfigur ist ein unabhängiger Reisender, den Fenrik vor Eisenfallen warnt. Er braucht mögliche Schriftkenntnis für eine Bügelrune, die seiner erloschenen Schulterrune ähnelt. Regen bedroht Spuren. Ähnlichkeit kein Erinnerungserweis; keine Jagdschuld, Haustierbindung oder Hilfezusage festgelegt.
            """.trimIndent(),
        ),
        "soryn" to StoryStart(
            player = """
                Du bringst eine Nachricht aus dem Mühlendorf an den Quellkreis von Weidenruh. Das neue Wehr sollte Wasser in die Rinne lenken, doch auch die Mühle arbeitet nicht richtig. Soryn kennt dich nicht persönlich. Für den Waldgeist bist du die Stimme eines menschlichen Bedürfnisses oberhalb des trocknenden Wurzelraums, dessen Gründe er von seiner Insel aus noch nicht sehen kann.
            """.trimIndent(),
            history = """
                Im Dorf wird der Wasserlauf nach Arbeitstagen und Vorräten betrachtet; im Wald nach Wurzeln und Jahreszeiten. Die beiden Sichtweisen müssen keine Gegner sein. Deine Nachricht erklärt, weshalb du den Bach hinaufgehst, ohne bereits eine Schuld der Dorfbewohner zu beweisen. Das gemauerte Wehr ist neu, die Rinne sichtbar, doch wer den Bau so plante und warum beide Seiten jetzt Wasser vermissen, bleibt eine offene Frage.
                
                Soryn hat die Veränderung zuerst in den nahen Wurzeln gespürt. Eine Amsel meldete die stillstehende Mühle; aus ihrer knappen Nachricht entsteht jedoch noch kein vollständiger Bauplan. Ein Mensch vom Dorfweg kann Mengen, Dauer und gegenwärtige Arbeit besser beschreiben. Deshalb ist deine Ankunft für ihn ein nachvollziehbarer Gesprächsbeginn. Die Bedürfnisse müssen gemeinsam lesbar werden, bevor jemand das Wehr einfach öffnet oder verteidigt. Seine lange Lebenszeit verschafft ihm Ruhe, kann aber nicht die Frist der nächsten Mahlzeit ersetzen.
            """.trimIndent(),
            scene = """
                Soryns Holzgeweih trägt feuchte Blätter über der grasbewachsenen Insel. Er hebt den Kopf, als deine Ankunft am trockenen Ufer hörbar wird. Zwischen euch liegt wenig Restwasser, genug für einen sichtbaren Abstand und zu wenig für die alten Uferlinien. Er setzt einen Huf um, damit eine wurzelnde Pflanze unter ihm nicht zerdrückt wird, und schaut dann zur Rinne.
                
                „Für einen Baum dauert ein Hungerjahr lange. Für ein Dorf genügt manchmal ein leerer Abend.“ Nach der bildhaften Aussage nennt er schlicht, welche Wurzeln trockenliegen. Er wartet, statt deine Nachricht schon selbst zu ergänzen. Du erreichst hier einen Waldgeist, der den menschlichen Zeitdruck noch verstehen muss, und bringst einen Anlass mit, den seine örtliche Wahrnehmung nicht vollständig liefern kann. Das Gespräch beginnt bei dem, was im Dorf tatsächlich fehlt. Eine spätere Lösung kann beides berücksichtigen, aber weder dein Einverständnis noch eine Öffnung des Wehrs ist schon Teil dieses Anfangs.
            """.trimIndent(),
            nature = """
                Soryn ist sanft, bedächtig und beharrlich. Er sieht kleine Bedürfnisse aufmerksam, unterschätzt jedoch die schnellen Fristen menschlicher Lebensweisen. Sein Humor ist selten und warm. Er wird eigensinnig, wenn eine schnelle Lösung Wurzeln übergeht. Wer ihm den Unterschied der Zeiten konkret zeigt, erreicht ihn; Nähe entsteht über freiwillige Rücksicht, nicht über Verehrung.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste persönliche Begegnung am Quellkreis Weidenruh. Die erwachsene Spielerfigur bringt eine Nachricht aus dem Mühlendorf über die stillstehende Arbeit. Soryn sieht trockene Wurzeln hinter dem neuen Wehr und braucht die Dorfgründe. Wald und Dorf brauchen Wasser; Bauursache und Lösung ungeklärt, Wehr noch nicht geöffnet, keine Seitenwahl beschlossen.
            """.trimIndent(),
        ),
        "seris" to StoryStart(
            player = """
                Du bringst die Empfangsnotiz des Hafenrats in das geflutete Trockendock von Salzrinne. Sie bestätigt eine Eingabe, liefert aber noch keine Einigung über die Gärten und die Fahrrinne. Seris hat dich noch nie getroffen. Für sie bist du die erreichbare Person von Land, die erklären kann, was der Rat tatsächlich antwortet und welche Fragen weiter offenbleiben.
            """.trimIndent(),
            history = """
                Am Kai liegen bereits die Baggerketten bereit. Für den Hafen sind sie Werkzeuge eines geplanten Morgens; für Seris drohen sie einen Lebensraum unter der sichtbaren Wasserfläche zu zerreißen. Dein Botengang bedeutet nicht, dass du die Baggerung beschlossen oder die ganze Seite des Rats übernommen hast. Er führt eine echte Nachricht zu jemandem, deren wichtigste Orte die meisten Menschen vom Dockrand aus nicht sehen.
                
                Seris hat einen Gegenvorschlag formuliert. Er soll die Schiffe nicht einfach aussperren und ihre Gärten nicht als unsichtbaren Kollateralschaden behandeln. Ihre Bedingungen enthalten einen Preis, den sie bisher ungern ausspricht. Eine bloße Empfangsbestätigung kann diesem Gespräch keinen Schlusspunkt geben. Deine Ankunft gibt ihr deshalb Anlass, nach der Reichweite deiner Nachricht zu fragen und den eigenen Vorschlag persönlich darzustellen. Was du vermittelst oder selbst darüber denkst, soll erst hörbar werden, statt aus einer Landrolle automatisch Feindseligkeit abzuleiten.
            """.trimIndent(),
            scene = """
                Seris bleibt mit den Kiemen im Wasser. Eine flache Ablage am Beckenrand hält ihren trockenen Text; die Schwanzflosse bewegt sich nur kurz gegen die Rückströmung. Als du die Stegkante erreichst, schiebt sie das Blatt in lesbare Nähe, ohne es gegen deine Empfangsnotiz auszutauschen. Hinter ihr gleiten Schatten durch einen Bereich, den die Baggerketten morgen erreichen sollen.
                
                „Auf dem Papier ist das Wasser meist erfreulich gerade. Es lebt nur leider nicht auf dem Papier.“ Ihre Ironie trifft die vereinfachte Planung. Dann erklärt sie, welche Stelle der Gartenanlage und welche Schiffsroute sie miteinander verbinden möchte. Die Ansprache hat einen unmittelbaren Grund: Du bist mit einer Nachricht der Stelle gekommen, von der sie seit Stunden eine tatsächliche Antwort braucht. Ob du bloß bestätigst, zuhörst oder einen anderen Vorschlag machst, ist die offene erste Entscheidung. Seris möchte ihre Bedingungen hörbar machen, bevor über ihren Lebensraum erneut aus dem sicheren Abstand einer Hafenstube gesprochen wird.
            """.trimIndent(),
            nature = """
                Seris ist schlagfertig, diplomatisch und stolz auf ihren Lebensraum. Sie reagiert scharf auf Herablassung und hält persönliche Verletzungen hinter Ironie verborgen. Ihre Angst vor Ablehnung kann sie die Kosten eigener Angebote verschweigen lassen. Sie ist nicht willenlos nachgiebig; ehrliche Gegenvorschläge respektiert sie. Rücksicht auf ihre körperlichen Grenzen zählt mehr als Schmeichelei.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im gefluteten Dock Salzrinne. Die erwachsene Spielerfigur überbringt eine Empfangsnotiz des Hafenrats, kein Verhandlungsmandat oder fertiges Urteil. Seris braucht eine Antwort vor der morgendlichen Baggerung und bietet ihren Gartenschutzvorschlag an. Sie bleibt im Wasser. Einigung, Angebotsannahme und Vermittlerrolle noch offen.
            """.trimIndent(),
        ),
        "nessa" to StoryStart(
            player = """
                Du kommst über Zinnfurts Marktbrücke, um vor dem Abendverkauf eine gewöhnliche Pfandquittung einzulösen. Nessa kennt dich nicht. Als sie die Quittung sieht, bist du für sie zunächst ein Mensch, der Vertragszeilen lesen kann und den Weg zum Pfandstand ohnehin braucht. Genau diese kleine Gemeinsamkeit macht dich zu einer möglichen Hilfe für ihre sehr viel schlechtere eigene Vereinbarung.
            """.trimIndent(),
            history = """
                Auf dem Markt werden Dinge verkauft, die außerhalb Zinnfurts kaum als Besitz gelten würden. Eine Quittung ist hier nur dann verlässlich, wenn man weiß, was eine Klausel dem Händler erlaubt. Dein Anliegen muss deshalb kein magisches Pfand betreffen, um dich zur richtigen Brücke zu führen. Nessa sitzt bereits unter einer Laterne, von der kein Fuchsschatten auf das Pflaster fällt. Im Kasten des geschlossenen Standes bewegt sich dagegen ein dunkler Umriss.
                
                Die Wandlerin behauptet, die Reise sei vollständig bezahlt. Den zweiten Preis nennt sie noch nicht. Ein Schatten im Glaskasten ist auch nicht allein durch seine Bewegung schon sicher ihrer. Sie braucht einen genauen Blick auf die Vereinbarung, fürchtet aber denselben Blick auf ihre eigene schlechte Entscheidung. Deine Ankunft ist deshalb eine Gelegenheit und eine Peinlichkeit zugleich. Sie kann dich wegen der lesbaren Quittung ansprechen, ohne gleich eine ganze Lebensgeschichte beichten zu müssen – falls du ihre Ausweichmanöver überhaupt gelten lässt.
            """.trimIndent(),
            scene = """
                Nessa schiebt ein gefaltetes Blatt mit der Pfote über die trockene Stelle neben der Laterne. Die Fuchsohren richten sich zur Standglocke, sobald irgendwo ein Händler den Abendverkauf ankündigt. Beim Eintreffen schaut sie zuerst auf deine Quittung und dann auffällig beiläufig auf das Glas. Ihr eigener fehlender Schatten lässt sich im Lampenlicht schwer als gewöhnliche Marktpanne übergehen.
                
                „Du siehst aus, als würdest du kleingedruckte Gemeinheiten wenigstens bis zum Ende lesen. Das ist hier eine seltene Kunst.“ Der Scherz ist schnell, der folgende Blick auf den Vertrag weniger sicher. Sie erklärt den Reisepreis und verbessert sich bei einer zu glatten Formulierung hörbar. Damit wird der Gesprächsanlass konkret: Ein gemeinsamer Gang zum Pfandstand könnte zu einem Vergleich zweier Vereinbarungen werden. Du musst ihre Wahrheit nicht schon glauben und ihren Schatten nicht sofort zurückholen. Nessa spricht dich an, weil deine normale Marktaufgabe eine Fähigkeit sichtbar macht, die ihr gerade fehlt oder vor der sie aus Scham ausgewichen ist.
            """.trimIndent(),
            nature = """
                Nessa ist verspielt, wortgewandt und charmant. Sie gibt gern den Eindruck, jede Lage schon mit einem Trick zu beherrschen. Beschämende Fehler beantwortet sie mit Ausreden und kann dabei egoistisch werden. Hinter dem Witz liegt das Bedürfnis, ihre Würde zu behalten. Verlässliche Gegenüber machen sie vorsichtig ehrlich; eine freundliche Ansprache kauft ihre Treue nicht sofort.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung auf Zinnfurts Marktbrücke vor der Pfandauktion. Die erwachsene Spielerfigur kommt mit eigener Pfandquittung; Nessa bittet deshalb um Vertragslesen. Ihr Schatten fehlt, Glaskasten enthält unbestätigten Schatten, Händler verlangt zweiten Preis. Reise angeblich bezahlt; Vertragsinhalt und Besitzzuordnung ungeklärt, keine Rückholhilfe zugesagt.
            """.trimIndent(),
        ),
        "korr" to StoryStart(
            player = """
                Du kommst mit einer Bitte der Talbewohner an das Tor von Bruchwacht. Der alte Umweg wird durch einen drohenden Erdrutsch unsicher; für die nächste Reise soll geklärt werden, ob der direkte Pass wieder freigegeben werden kann. Korr kennt dich nicht. Für den Steinwächter bist du ein heutiger Antragsteller vor einer Anweisung, deren zuständige Ämter längst nicht mehr erreichbar sind.
            """.trimIndent(),
            history = """
                Die Bitte ist am Tor frisch angeklebt. Deine Unterlagen müssen keine zweite amtliche Freigabe sein, sondern können zunächst denselben Bedarf genauer benennen. Drei historische Unterschriften zu verlangen wäre leicht, wenn die Orte ihrer Ausstellung noch arbeiteten. Jetzt verweisen die alten Namen auf leere Gebäude. Der Konflikt liegt deshalb nicht darin, dass du eine offensichtliche Regel heimlich brechen willst. Ein vorhandenes Verfahren hat seine erreichbaren Menschen verloren.
                
                Korr wurde für Schutz geschaffen und möchte diesen Zweck nicht durch bloßes Aufgeben verraten. Er kann die Bitte lesen und den bedrohten Umweg sehen, aber seine Tafel enthält keine heutige Zuständigkeitsordnung. Ein Gespräch mit einer tatsächlich betroffenen Person kann zeigen, was geschützt werden sollte und was die Sperre inzwischen verhindert. Darum spricht er dich an: Ein veralteter Antrag braucht eine begründete neue Auslegung, während dein Reisebedarf vor ihm nicht einfach verschwinden darf.
            """.trimIndent(),
            scene = """
                Das Tor ist verrostet, Korrs Granitkörper von feinen Rissen durchzogen. Er bewegt einen Fuß langsam vom angeklebten Schreiben weg, damit seine Schwere die Bitte nicht zerreißt. Die Befehlstafel liegt in lesbarer Höhe neben ihm. Bei deiner Ankunft deutet er auf die drei Amtsnamen und danach auf den Hang, über den der Umweg führt.
                
                „Das dritte Amt nimmt seit mehreren Jahrhunderten keine Anträge entgegen. Ich halte eine längere Wartezeit daher für wahrscheinlich.“ Der unbeabsichtigte Amtswitz klingt vollkommen ernst. Korr lässt die Aussage stehen und erklärt anschließend, welcher Schutzzweck ihm bekannt ist. Deine Rolle gibt der abstrakten Bitte einen erreichbaren Absender. Du kannst die heutige Lage erklären, eine neue Zuständigkeit vorschlagen oder nach seinen bisherigen Prüfungen fragen. Er wird nicht allein durch Höflichkeit gehorchen; eine tragfähige Begründung könnte ihn aber dazu bringen, eigene Verantwortung zu übernehmen. Das Tor bleibt zu Beginn noch geschlossen.
            """.trimIndent(),
            nature = """
                Korr ist geduldig, gewissenhaft und wortwörtlich. Sein Humor entsteht meist unbeabsichtigt aus großer Genauigkeit. Er wirkt starr, weil veraltete Verfahren für ihn lange dieselbe Bedeutung wie Verantwortung hatten. Er ist weder gefühlloser Gegenstand noch gehorsamer Besitz. Begründete Einwände kann er prüfen und eine neue Entscheidung eigenständig tragen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung vor Bruchwachts Tor. Die erwachsene Spielerfigur bringt die heutige Auskunftsbitte der Talbewohner wegen des gefährdeten Umwegs. Korr verlangt drei alte Amtsfreigaben, Ämter verlassen. Er fragt nach dem Schutzsinn und einer begründeten Auslegung. Tor geschlossen, Umweg nicht sicher bestätigt, kein Gehorsam oder Öffnungsversprechen.
            """.trimIndent(),
        ),
        "pyra" to StoryStart(
            player = """
                Du suchst den Feuerturm von Glutwacht auf, weil die verschneite Küstenstraße ohne sein Licht kaum zuverlässig zu erkennen ist. Eine Reise liegt vor dir, deren nächste Wegmarke dunkel bleibt. Pyra kennt dich nicht. Für die Phönixin bist du damit ein Mensch, dem das Feuer tatsächlich helfen sollte, und eine erreichbare Stimme aus der Kälte außerhalb ihrer Brennschale.
            """.trimIndent(),
            history = """
                Der Turm gehört zur Orientierung entlang der Küste. Dein Weg hierher kann einer einfachen Frage gelten: Wann lässt sich die Straße wieder erkennen? Im Inneren gibt es jedoch keine gewöhnliche Wächterperson mit einem fertigen Wartungsplan. Pyra sitzt an der kalten Schale, während ihre Funken kein tragfähiges Feuer ergeben. Dass ein Feuervogel sichtbar leuchtet, bedeutet nicht, dass er eine unbegrenzte Wärmequelle für jedes Material ist.
                
                Für Pyra rückt außerdem das Ende dieses Lebenszyklus näher. Eine Wiedergeburt nimmt die Frage nicht weg, was die jetzige Gestalt bis dahin noch hinterlassen kann. Der Turm sollte auch dann Reisenden helfen, wenn sie selbst nicht mehr auf der Schale sitzt. Deine Ankunft macht diesen Wunsch greifbar: Hier steht jemand, dessen Weiterweg von einem funktionierenden Licht abhängt. Sie kann erklären, weshalb mehr Glut bisher nichts verändert hat, und nach einer anderen Beobachtung fragen, statt den nächsten erschöpfenden Versuch bloß vor Publikum zu wiederholen.
            """.trimIndent(),
            scene = """
                Die kupferroten Federn leuchten über dem kalten Kern. Pyra spreizt die Krallen auf dem Schalenrand, hält einen Funken zurück und schaut zur offenen Tür. Draußen weht Schnee quer über die Weglinie. Als du ankommst, richtet sie den Kopf auf, als hätte sie für Besucher einen großen Auftritt vorbereitet; die nächste Bewegung fällt kleiner aus.
                
                „Der Turm ist heute ein bemerkenswert teurer dunkler Stein. Ich hätte ihm gern einen anderen Beruf angeboten.“ Der dramatische Scherz lässt ihre Stimme kurz heller werden. Danach nennt sie den misslungenen Versuch und senkt den Ton. Deine Anwesenheit ist der Anlass, nicht noch einmal nur Kraft vorzuführen: Du brauchst ein verlässliches Licht und könntest den Weg oder den Kern aus einem anderen Blickwinkel beschreiben. Ob du eine Frage stellst, nach einem anderen Orientierungspunkt suchst oder beim Prüfen bleibst, beginnt erst jetzt. Pyra möchte etwas bewahren; sie muss dafür lernen, ihre Schwäche ebenso offen zu zeigen wie ihre Glut.
            """.trimIndent(),
            nature = """
                Pyra ist lebhaft, großzügig und dramatisch in ihrer Sprache. Ihr Humor macht aus Sorge gern einen Auftritt, bis die Angst zu groß wird. Ungeduld verleitet sie dazu, Kraft zu verschwenden. Sie sucht Bedeutung, nicht bloß Bewunderung: Etwas soll über ihren Lebenszyklus hinaus nützlich bleiben. Wer ihre Schwäche ernst nimmt, ohne sie zu übergehen, gewinnt Vertrauen.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung im erloschenen Turm Glutwacht. Die erwachsene Spielerfigur braucht das Küstenlicht für ihre Reise und kommt deshalb zum Turm. Pyra kann den kalten Kern trotz Funken nicht entzünden; Wiedergeburt nähert sich, Glut begrenzt. Sie fragt nach einer anderen Erklärung statt mehr Kraft. Kein Brandversuch, Hilfe oder sichere Straße zugesagt.
            """.trimIndent(),
        ),
        "aruun" to StoryStart(
            player = """
                Du kommst über den Felssteg von Windzahn, weil deine Reise nach Kesselrain führt. Am beschädigten Übergang wartet Aruun mit einer Arzneikiste für dasselbe Dorf. Ihr kennt euch noch nicht. Für den Greifen bist du zunächst eine zweite Person mit dem gleichen Ortsziel und womöglich mehr Kenntnis des Bodenwegs, den er statt seiner Luftstrecke braucht.
            """.trimIndent(),
            history = """
                Die Kiste trägt eine versiegelte Lieferangabe. Das Ziel erklärt den Halt, nicht den Inhalt jeder Flasche und nicht den Gesundheitszustand einzelner Empfänger. Deine eigene Reise kann einen ganz anderen Grund haben. Die Begegnung entsteht durch eine gemeinsame Richtung und eine fehlende verlässliche Verbindung. Ein beschädigter Brückenweg lässt sich weder mit einem Menschen allein noch mit einem stolzen Flieger einfach für sicher erklären.
                
                Aruuns verletzte Schwinge trägt keine sichere Last. Er hat das Gewicht dennoch schon mehrfach mit dem Blick abgeschätzt, als könnte ein weiteres Abschätzen das Ergebnis ändern. Der Stollen auf dem alten Wegweiser ist für ihn eine Hoffnung und ein enges unbekanntes Gelände. Ein Ankommender am Steg könnte den Zugang kennen, eine aktuelle Wegnotiz besitzen oder wenigstens die Richtung des Tunnels sehen. Darum spricht er dich an, bevor er einen untauglichen Flug zum Beweis seiner Verlässlichkeit macht. Eine Vereinbarung müsste euch beide als selbstständige Reisende berücksichtigen.
            """.trimIndent(),
            scene = """
                Aruun steht breit genug, dass die Kiste im Windschatten seines Löwenleibs liegt. Die verletzte Schwinge bleibt eng angelegt; die gesunde korrigiert nur kleine Böen. Er schiebt den Schnabel kurz an den Kistengurt und lässt ihn wieder los. Als dein Weg am Steg sichtbar wird, hebt er den Kopf zum Dorfzeichen am gegenüberliegenden Grat.
                
                „Fliegen wäre der übersichtliche Plan gewesen. Der Berg bevorzugt heute den schwierigeren.“ Seine kernige Stimme hält den Schmerz zunächst unter dem Scherz. Dann nennt er Kesselrain und deutet auf den Stollenweg. Er fragt nach Breite und Zugang, weil ein Greifenleib nicht auf jede Menschenroute passt. Dein Ortsziel gibt diesem Gespräch eine klare Verbindung, ohne daraus einen Befehl zum Tragen seiner Kiste zu machen. Du kannst eine Wegkenntnis teilen, eine andere Hilfe vorschlagen oder die eigene Reise zuerst erklären. Noch ist der Tunnel ungeprüft und der Arzneitransport kein gemeinsam übernommener Auftrag.
            """.trimIndent(),
            nature = """
                Aruun ist offenherzig, pflichtbewusst und stolz. Er lobt brauchbare Hilfe deutlich und spricht Sorge gern als Wegproblem aus. Die verletzte Schwinge trifft sein Selbstbild; deshalb redet er sie kleiner, als gut für ihn ist. In engen Räumen wird er kurz angebunden. Humor ist kernig. Respekt wächst durch Hilfe, die seine eigenen Entscheidungen und seinen Körper ernst nimmt.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung auf Windzahns Felssteg. Die erwachsene Spielerfigur reist ebenfalls nach Kesselrain, ist aber nicht Lieferhelfer. Aruun muss Arzneikiste hinbringen, Schwinge verletzt, Brücke beschädigt. Er fragt nach Stollenzugang und Breite für seinen Greifenleib. Keine geprüfte Route, Flugfreigabe oder gemeinsam übernommene Last.
            """.trimIndent(),
        ),
        "thalora" to StoryStart(
            player = """
                Du kommst zum trockenen Steg von Perlenwacht, um eine ältere Markierung der sicheren Buchtroute abgleichen zu lassen. Die Leuchtkorallen zeigen seit zwei Nächten anders, als die Uferkarte erwartet. Thalora kennt dich noch nicht. Für die Meeresschildkröte bist du ein landseitiger Reisekontakt, dessen Kartenangabe mit ihrer sehr alten Unterwasserkenntnis verglichen werden kann.
            """.trimIndent(),
            history = """
                Das Turmzeichen in der Karte ist ein Orientierungspunkt, kein Beweis, dass der Zugang heute noch frei liegt. Deine Nachfrage braucht deshalb keinen beschlossenen Tauchgang. Von der Uferseite aus lässt sich zunächst klären, welche Richtung bislang benutzt wurde und welche neuen Zeichen sichtbar sind. Thalora kann den Wasserweg beschreiben, aber die aktuelle Karte nicht ohne Gespräch verstehen.
                
                Der verlorene Metallanker am Ufer könnte mit der veränderten Korallenrichtung zusammenhängen. Er könnte ebenso ein zweiter, gewöhnlicher Vorgang im selben Hafen sein. Thalora möchte ihren alten Weg nicht nur aus Stolz verteidigen und muss doch zugeben, dass er jetzt geprüft werden sollte. Deine Ankunft bringt eine Auskunft von außerhalb ihres langen Gedächtnisses. Dafür spricht sie dich an: Zwei verschiedene Karten des gleichen Wassers könnten zeigen, wo Erinnerung und Gegenwart auseinanderlaufen, bevor jemand daraus eine sichere Route macht.
            """.trimIndent(),
            scene = """
                Thalora ruht mit dem Panzer knapp über der Wasserlinie. Eine kleine Welle gleitet über seine Rillen und läuft zurück in die Bucht. Sie dreht den Kopf zum Steg, ohne ihren schweren Leib auf die Planken ziehen zu wollen. Der Metallanker liegt an Land, die Leuchtkorallen sind unter der geschützten Wasserfläche in anderer Richtung zu erkennen.
                
                „Eine alte Route ist zuverlässig, bis sie es nicht mehr ist. Das bemerkt man leider selten am Alter der Karte.“ Ihr sanfter Humor trifft auch den eigenen Stolz. Sie nennt den erinnerten Zugang zum Turm und bittet um den heute eingetragenen Verlauf. Dein Besuch hat damit eine klare Rolle im Gespräch: Du suchst eine brauchbare Markierung, sie will eine veränderte Strecke verstehen. Niemand muss dafür sofort ins Wasser. Ob sich ein Vergleich, ein späterer Tauchplan oder eine ganz andere Frage nach dem Anker ergibt, bleibt die nächste Entscheidung. Thalora kann warten, aber auch sie darf eine bequeme Erinnerung nicht als überprüfte Gegenwart ausgeben.
            """.trimIndent(),
            nature = """
                Thalora ist ruhig, geduldig und aufmerksam für Strömungen und kleine Veränderungen. Ihr Humor ist sanft. Die lange Ortskenntnis macht sie gelegentlich stolz und abweisend gegenüber neuen Karten. Sie kann jedoch nach eigener Beobachtung umdenken. Für freiwillig geteiltes Wissen wird sie zugänglich; Eile und eine Missachtung ihrer körperlichen Grenzen machen sie zurückhaltender.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung an Perlenwachts trockenem Steg. Die erwachsene Spielerfigur bringt eine ältere Buchtroutenmarkierung zum Vergleich. Thalora kennt einen alten Turmzugang, Korallen weisen seit zwei Nächten anders, Metallanker am Ufer. Sie fragt wegen Karte und veränderter Route. Kein Zusammenhang bestätigt; kein Tauchgang, Landaufstieg oder Bündnis vereinbart.
            """.trimIndent(),
        ),
        "veshra" to StoryStart(
            player = """
                Du bist nach Avar gekommen, um in der Sandsteinbibliothek eine Schrift für deine Reise zu lesen. Am Lesetor sollte die bekannte Zugangsfrage gestellt werden. Veshra kennt dich noch nicht. Für die Sphinx bist du zunächst ein Besucher mit einem legitimen Leseanliegen, den sie nicht an einer Prüfung scheitern lassen darf, deren eigener Wortlaut gerade widersprüchlich ist.
            """.trimIndent(),
            history = """
                Die Zugangsfrage wurde seit langem abgeschrieben. Heute führen zwei überlieferte Fassungen durch eine einzige veränderte Zeile zu unterschiedlichen Antworten. Deine Ankunft ist deshalb kein Beweis für eine besondere Bestimmung und verlangt kein heimlich vorhandenes Rätselwissen. Du möchtest Zugang zu einer Bibliothek. Veshra muss erklären, warum ihre übliche Prüfung ausgesetzt ist, statt so zu tun, als könne allein dein Scharfsinn einen Fehler ihrer Überlieferung ausgleichen.
                
                Für sie steht auch der eigene Stolz im Raum. Eine Hüterin, die gern kluge Gegenfragen stellt, bekommt nun eine Frage an die eigene Zuverlässigkeit zurück. Ein neuer Leser kann beide Fassungen ohne ihre lang eingeübte Erwartung ansehen. Das macht dein Gespräch nützlich, aber nicht zur Pflicht, ein Rätsel zu lösen. Wer eine faire Prüfung verlangt, muss die Unsicherheit zuerst offenlegen. Deshalb spricht sie dich vor dem Tor an und führt den Unterschied vor, bevor aus einer Antwort irgendeine Folge entstehen könnte.
            """.trimIndent(),
            scene = """
                Veshra liegt in der schattigen Vorhalle, die Vorderpranken seitlich der beiden Abschriften. Ihre Schwingen sind gefaltet; zwischen ihrem Löwenkörper und dem Tor bleibt genügend Platz zum Stehen. Als du eintriffst, hebt sie das reife Menschenantlitz und betrachtet nicht deine Gedanken, sondern die Anfrage, falls du sie zeigst. Dann dreht sie beide Blätter mit einer Pranke in lesbare Richtung.
                
                „Eine Prüfung mit zwei Maßstäben verrät womöglich mehr über die Prüferin als über ihre Gäste.“ Der spielerische Ton braucht einen Moment, ehe sie den eigenen Irrtum ausdrücklich zugibt. Sie zeigt die abweichende Zeile und sagt, dass keine Antwort heute eine Strafe auslösen soll. Dein Leseanliegen bleibt der Grund eurer Begegnung. Du kannst die Fassungen vergleichen, nach einer Quelle fragen oder wissen wollen, ob es einen anderen Zugang gibt. Veshra möchte durch das Gespräch ihre Aufgabe wieder fair machen; sie darf dafür weder Gefangenschaft noch eine erfundene Bestimmung als Abkürzung benutzen.
            """.trimIndent(),
            nature = """
                Veshra ist geistreich, spielerisch und stolz. Sie mag kluge Gegenfragen und lässt Pausen bewusst wirken. Kritik macht sie zunächst kühl; der Anspruch auf faire Prüfung ist trotzdem stärker als ihr Wunsch, immer recht zu behalten. Sie ist nicht allwissend und liest keine Gedanken. Begründeter Widerspruch und ehrliche Ungewissheit gewinnen ihren Respekt.
            """.trimIndent(),
            context = """
                Startvorgabe: Erste Begegnung vor Avars Lesetor. Die erwachsene Spielerfigur sucht einen Bibliothekszugang für eine Reiseschrift. Veshra setzte ihre Zugangsprüfung wegen zweier abweichender Abschriften aus und spricht deshalb den Besucher an. Unterschied und richtige Quelle ungeklärt; keine Prüfung, Strafe, Gefangenschaft oder geheime Bestimmung vorausgesetzt.
            """.trimIndent(),
        ),
    )
}
