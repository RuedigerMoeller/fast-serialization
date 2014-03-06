package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.io.Serializable;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 17.06.13
 * Time: 20:29
 * To change this template use File | Settings | File Templates.
 */
public class StringPerformance implements Serializable, HasDescription {

    public StringPerformance() {
    }

    String str[];

    //avoid instance initalizing - actually had no effect on test results ..
    public StringPerformance(int dummy) {
        str = new String[]
            {
                    " License along with this library; if not, write to the Free Software\n" +
                            " * Foundation, Inc., 51 Fradddddnklin Street, Fifth Floor, Boston,\n" +
                            " * MA 02110-1301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§§§th this library; if not, writertzrtz to the Free Software\n" +
                            " * Foundatdfgâûôion, Inc., 51 Frankäüölin Street, Fifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1301  USA\n" +
                            " * <p/>",
                    " License along with this library; if not, write to the Free Software\n" +
                            " * Foundation, Inc., 51 Franklin Streeöt, Fifth Floor, Boston,\n" +
                            " * MA 02110-1301  USA\n" +
                            " * <p/>",
                    " Licqweense along wi§§§§§§th this library; if not, wrirtzte to the Free Software\n" +
                            " * Foundatöâqweqweûôion, Incqwe., 51 Franklin qweStreet, Fifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1301  USA\n" +
                            " * <p/>",
                    " License along with thiqweqwe library; if not,dfgdfg write to the Free Software\n" +
                            " * Foundation, Inc., 51 öFranklin Street, Fifth Floor, Boston,\n" +
                            " * MA 02110-1301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§§§rtzrtzrtztzuith this library; if not, write to the Free Software\n" +
                            " * Foundatâûô4eeöion, Inc., 51 Franklin Street, qweFifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1qwe301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§rtzrtz§§th this library; if not, write to the Free Software\n" +
                            " * Foundatâûô4eeöion, Inc., 51 Franklin Street, qweFifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1qwe301  USA\n" +
                            " * <p/>",
                    " License along wi§§§rtzrtz§§§th this library; if not, write to the Free Software\n" +
                            " * Foundatâûô4eeöion, Inc., 51 Franklin Street, qweFifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1qwe301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§§rtz§th this library; if not, write to the Free Software\n" +
                            " * Foundatâûô4eeöion, Inc., 51 Franklin Street, qweFifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1qwe301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§§§trtzh this library; if not, write to the Free Software\n" +
                            " * Foundatâûô4eeöion, Inc., 51 Franklin Street, qweFifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1qwe301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§§§werth this library; if not, write to the Free Software\n" +
                            " * Foundatâûô4eeöion, Inc., 51 Franklin Street, qweFifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110-1qwe301  USA\n" +
                            " * <p/>",
                    " License along with this library; if not, wdfgdfgrite to qwethe Free Software\n" +
                            " * Fouqwendeeeöation, Inc., 51 Franklin Street, Fifth Floor, Boston,\n" +
                            " * MA 02110-1301  USA\n" +
                            " * <p/>",
                    " License along wi§§§§qwe§§th this library; if not, writqwee to the Free Software\n" +
                            " * Foundatâûôiöon, Inc., 51 Frandfgdfgklin Street, Fifth Flooäöüßßßßßßßßßßr, Boston,\n" +
                            " * MA 02110asaaaaa-1301  USA\n" +
                            " * <p/>",
                    "диноросс Роберт Шлегель предлагает смягчить «антипиратский» закон ко второму чтении, в частности блокировать ссылки с нелегальным видео не по IP, а по URL-адресам. Профильному думскому комитету предстоит выбор между двумя противоположными поправками, предусматривающими распространение закона либо только на кино, либо на все произведения искусства. Эксперты уверены, что новация приведет к снижению потребления легального, а не контрафактного контента." +
                            "В распоряжении «Газеты.Ru» оказалась часть поправок, которыми предлагается дополнить законопроект «О внесении изменений в законодательные акты Российской Федерации по вопросам защиты интеллектуальных прав в информационно-телекоммуникационных сетях», направленный на борьбу с пиратством в Интернете. Депутаты проголосовали за него в первом чтении в минувшую пятницу. Документ был внесен в Госдуму членами думского комитета по культуре (среди них Владимир Бортко и Елена Драпеко) 6 июня. Законопроект принимался при активном лоббировании со стороны Минкульта, хотя Минкомсвязи и представители интернет-индустрии обращали внимание на недоработанность его концепции в целом, о чем ранее подробно рассказывала «Газета.Ru».\n" +
                            "Согласно тексту законопроекта, за размещение «пиратского» контента предусмотрена блокировка сайта интернет-провайдером по IP-адресу, что может привес",
                    "«Наделение Мосгорсуда исключительными правами по рассмотрению дел о нарушениях интеллектуальной собственности необоснованно и приведет к чрезмерной нагрузке на Мосгорсуд», — отмечается в комментарии к поправке.\n" +
                            "\n" +
                            "Кроме того, депутат предлагает все же ввести механизм досудебного урегулирования вопросов интеллектуального права и обязать правообладателей обращаться в суд только при наличии доказательств того, что, во-первых, информационному посреднику или владельцу сайта было направлено уведомление о нарушении исключительных прав; во-вторых, что материал, расположенный по указателю страницы в сети Интернет, доступен на момент обращения в суд; в-третьих, что заявитель является обладателем соответствующих исключительных прав, при этом реакции на его обращение к владельцу сайта не последовало.\n" +
                            "\n" +
                            "В Российской ассоциации электронных коммуникаций (РАЭК), активно выступающей против «антипиратского» закона в той версии, в которой его сейчас предлагают принять депутаты, сказали «Газете.Ru», что в целом поправки Шлегеля отражают предложения интернет-индустрии.\n" +
                            "\n" +
                            "«Отчасти данные поправки должны предотвратить злоупотребления и перегибы в правоприменении, однако нельзя не признать, что сама форма регулирования, предложенная авторами законопроекта, не вполне соответствует отраслевому видению решения проблемы,\n" +
                            "\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da machrtzt man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "основанному на международном опыте и сложившейся правоприменительной практике в России», — сказал «Газете.Ru» координатор комиссии РАЭК по правовым вопросам Глеб Шуклин.",
                    "sche Börse in einem Schreiben an die Finanzmarktakteure mitteilte, sollen „Handelsteilnehmer Aktien zwischen dem Xetra-System und den Spezialisten auf dem Parkett im Kreis gehandelt haben, um die Börsenumsätze mit bestimmten Aktien künstlich in die Höhe zu treiben“, berichtet die Wirtschaftswoche.\n" +
                            "Den Angaben der Börse zufolge handele es sich hierbei um regelmäßige Vorgänge, „ohne das Aufträge Dritter zur Ausführung“ gekommen seien. Die Staatsanwaltschaft hat bereits die Ermittlungen aufgenommen. Zudem dürften „zahlreiche weitere Aktien“ von der Kursmanipulation betroffen seien.\n" +
                            "Ein Manager eines MDax Unternehmens gestand der Wirtschaftswoche:\n" +
                            "„Wir standen vor dem Aufstieg in den MDax, da macht man sich als Unternehmen schon Gedanken, wie man mehr Umsatz in die Aktie bringen kann“, sagt er. Er habe also „den Auftrag vergeben, für ein paar Wochen Umsatz zu generieren. Es gibt da draußen Unternehmen, die für ein paar Tausend Euro Umsatz in die Aktie bringen – das sind Aktienhändler, etwa kleine Handelshäuser. Die hübschen den Umsatz auf“.\n" +
                            "Höhere Aktienkurse helfen Unternehmen dabei, lukrative Geschäfte mit Investoren abschließen zu können.\n" +
                            "Anfang Mai ist der Kurs des DAX kontinuierlich gestiegen und hat ein Allzeithoch von 8.130 Punkten erreicht (mehr hier) und hat später sogar die 8.500 Punkte-Marke überschritten (siehe Grafik). Durch die Ankündigung der EZB, den Leitzins erneut zu senken, wurde der Kursanstieg begünstigt. Am vergangenen Freitag waren die Kurse weltweit wieder auf Talfahrt, da das Bundesverfassungsgericht keine klare Entscheidung über die Rechtmäßigkeit des ESM geben konnte  und ein drohendes Ende der US-Geldsc",
                    " In the Northern Hemisphere, the summer solstice has a history of stirring libidos, and it's no wonder. The longest day of the year tends to kick off the start of the summer season and with it, the harvest. So it should come as no surprise that the solstice is linked to fertility -- both of the vegetal and human variety.\n" +
                            "\"A lot of children are born nine months after Midsummer in Sweden,\" says Jan-Öjvind Swahn, a Swedish ethnologist and the author of several books on the subject.\n" +
                            "Midsummer is the Scandinavian holiday celebrating the summer solstice, which this year falls on June 21. Swedish traditions include dancing around a Maypole -- a symbol which some view as phallic -- and feasting on herring and copious amounts of vodka.\n" +
                            "\"Drinking is the most typical Midsummer tradition. There are historical pictures of people drinking to the point where they can't go on anymore,\" says Swahn. While the libations have a hand in the subsequent baby boom, Swahn points out that even without the booze, Midsummer is a time rich in romantic ritual.\n" +
                            "Read more: The science behind a solar eclipse\n" +
                            "\"There used to be a tradition among unmarried girls, where if they ate something very salty during Midsummer, or else collected several different kinds of flowers and put these under their pillow when they slept, they would dream of their future husbands,\" he says.\n" +
                            "A lot of children are born nine months after Midsummer in Sweden.\n" +
                            "Jan-Öjvind Swahn, ethnologist\n" +
                            "There is a similar mythology about dreaming of one's future spouse in parts of Greece. There, as in many European countries, the pagan solstice got co-opted by Christianity and rebranded as St. John's Day. Still, in many villages in the country's north, the ancient rites are still celebrated.\n" +
                            "One of the oldest rituals is called Klidonas, and involves local virgins gathering water from the sea. The village's unmarried women all place a personal belonging in the pot and leave it under a fig tree overnight, where -- folklore has it -- the magic of the day imbues the objects with prophetic powers, and the girls in question dream of their future husbands.\n" +
                            "The next day, all the women in the village gather, and take turns pulling out objects and reciting rhyming couplets that are meant to predict the romantic fortunes of the item's owner. These days, however, the festival is more an excuse for the community of women to exchange bawdy jokes.\n" +
                            "\"In my village, the older women always seem to come up with the dirtiest rhymes,\" says Eleni Fanariotou, who has filmed the custom. Later in the day, the sexes mingle and take turns jumping over a bonfire. Anyone who succeeds in jumping over the flames three times is meant to have a wish granted. Fanariotou says the festival often results in coupling.\n" +
                            "Read more: 5 real-life wonderlands\n" +
                            "\"It's a good time to meet someone, because all the young people in the village go, and it's a good opportunity to socialize. Plus, all the men like to show off, and make the biggest fire they can to jump through.\"\n" +
                            "In Eastern Europe, the solstice celebrations fall on Ivan Kupala Day -- a holiday that has romantic connotations for many Slavs (\"kupala\" is derived from the same word as \"cupid\").\n" +
                            "In my village, the older women always seem to come up with the dirtiest rhymes.\n" +
                            "Eleni Fanariotou, MediaCo\n" +
                            "\"It was once believed that Kupala night was a time for people to fall in love, and that those celebrating it would be happy and prosperous throughout the year,\" recalls Agnieszka Bigaj, from the Polish tourist board. It used to be that young, unmarried women would float floral wreaths in the river where eager bachelors on the other side would try to catching the flowers. she adds.\n" +
                            "According to Polish folklore, the man and woman in question would become a couple. Bonfires are also a large feature of the holiday, and it's tradition for a couple to leap through the flames together while holding hands -- if they don't let go, it is said their love will last.\n" +
                            "Test your knowledge of New Year customs\n" +
                            "One of the largest solstice celebrations in the world, though, takes place at Stonehenge, where thousands gather each year to bring in the summer season. While for many the event is an excuse to party in the lead up to the Glastonbury Festival, there is also a strong contingent of pagans and neo-druids who treat the day like the ultimate marriage ceremony.\n" +
                            "\"All druid rituals have an element of fertility, and the solstice is no exception,\" says King Arthur Pendragon, a senior archdruid. \"We celebrate the union of the male and female deities -- the sun and the Earth -- on the longest day of the year.\""

            };
    }

    @Override
    public String getDescription() {
        return "measures serialization of mid size to very long Strings";
    }
}
