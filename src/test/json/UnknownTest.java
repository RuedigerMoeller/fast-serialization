package json;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.coders.Unknown;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public class UnknownTest {

    @Test
    public void testU() throws IOException {
        FSTConfiguration jsonConfiguration = FSTConfiguration.createJsonConfiguration(false, false);
//        byte[] bytes = Files.readAllBytes(new File("./src/test/json/unknowntest.json").toPath());
        byte[] bytes = Files.readAllBytes(new File("./src/test/json/unknowntest.json").toPath());
        jsonConfiguration.asObject(bytes);
    }

    @Test
    public void testUnknown() throws UnsupportedEncodingException {
        FSTConfiguration js = FSTConfiguration.createJsonConfiguration();
        byte[] bytes = getBytes("{ 'asd' : 123, '123': 3345.32, 'pok': { 'pak': '345', 'pick':[1,2,3,4.5,[1,2,'3']] } }");
        Unknown x = (Unknown) js.asObject(bytes);
        String ddot = x.ddot("pok.pak");
        System.out.println(ddot);
        System.out.println(x);
    }

    @Test
    public void testLarge() throws UnsupportedEncodingException {
        FSTConfiguration js = FSTConfiguration.createJsonConfiguration();
        byte[] bytes = getBytes(getTestJSON());
        Unknown x = (Unknown) js.asObject(bytes);
        System.out.println(x.dot(0,"_id"));
        System.out.println(x.dot(1,"friends",1,"name"));
        System.out.println(x);
    }

    private byte[] getBytes(String s) throws UnsupportedEncodingException {
        return s.replace('\'', '"').getBytes("UTF-8");
    }

    public String getTestJSON() {
        return "[\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b916278f30def02b78\",\n" +
            "    \"index\": 0,\n" +
            "    \"guid\": \"307b0fbb-e57a-477a-8196-16fac05523b7\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,813.26\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 35,\n" +
            "    \"eyeColor\": \"blue\",\n" +
            "    \"name\": \"Leila Christensen\",\n" +
            "    \"gender\": \"female\",\n" +
            "    \"company\": \"GYNKO\",\n" +
            "    \"email\": \"leilachristensen@gynko.com\",\n" +
            "    \"phone\": \"+1 (933) 523-2064\",\n" +
            "    \"address\": \"688 Evergreen Avenue, Churchill, Virgin Islands, 7036\",\n" +
            "    \"about\": \"Fugiat minim incididunt ullamco excepteur qui. Aliqua quis est fugiat ex exercitation nulla exercitation aliqua occaecat. Labore pariatur irure pariatur eiusmod. Deserunt mollit mollit magna excepteur nostrud officia anim fugiat quis qui dolore. Laboris labore cillum ea amet consequat consequat ad cillum consequat qui cupidatat esse. Officia cillum nostrud reprehenderit et nostrud laborum cupidatat velit eu est pariatur nostrud nulla.\\r\\n\",\n" +
            "    \"registered\": \"2016-04-29T12:49:21 -02:00\",\n" +
            "    \"latitude\": -17.038605,\n" +
            "    \"longitude\": 138.375724,\n" +
            "    \"tags\": [\n" +
            "      \"non\",\n" +
            "      \"nostrud\",\n" +
            "      \"ut\",\n" +
            "      \"pariatur\",\n" +
            "      \"tempor\",\n" +
            "      \"ipsum\",\n" +
            "      \"fugiat\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Johnson Manning\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Fox Joyce\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Jennie Sanchez\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Leila Christensen! You have 9 unread messages.\",\n" +
            "    \"favoriteFruit\": \"banana\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b9d40ebad907090377\",\n" +
            "    \"index\": 1,\n" +
            "    \"guid\": \"3c3e4c66-081b-4c25-8831-b116e69c3c0b\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,550.35\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 38,\n" +
            "    \"eyeColor\": \"blue\",\n" +
            "    \"name\": \"Celia Ramos\",\n" +
            "    \"gender\": \"female\",\n" +
            "    \"company\": \"ISOLOGICA\",\n" +
            "    \"email\": \"celiaramos@isologica.com\",\n" +
            "    \"phone\": \"+1 (824) 460-3134\",\n" +
            "    \"address\": \"735 Mill Avenue, Laurelton, Colorado, 1751\",\n" +
            "    \"about\": \"Cupidatat in minim aliquip proident nulla duis fugiat ullamco occaecat esse labore. Incididunt fugiat ex occaecat Lorem commodo. Proident proident tempor magna ipsum commodo dolore dolore Lorem quis nisi aliquip sit. Ad consequat mollit voluptate consequat ut ea velit do nostrud aliqua proident aute.\\r\\n\",\n" +
            "    \"registered\": \"2015-01-31T04:17:06 -01:00\",\n" +
            "    \"latitude\": 5.794266,\n" +
            "    \"longitude\": 40.00851,\n" +
            "    \"tags\": [\n" +
            "      \"laboris\",\n" +
            "      \"aute\",\n" +
            "      \"voluptate\",\n" +
            "      \"dolor\",\n" +
            "      \"irure\",\n" +
            "      \"elit\",\n" +
            "      \"voluptate\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Rosemarie Poole\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"House Long\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Joanna Flores\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Celia Ramos! You have 6 unread messages.\",\n" +
            "    \"favoriteFruit\": \"apple\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b9fc1cca4995d6ff58\",\n" +
            "    \"index\": 2,\n" +
            "    \"guid\": \"69939a1c-78e4-4c6e-b515-9ce973d07951\",\n" +
            "    \"isActive\": true,\n" +
            "    \"balance\": \"$2,101.03\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 26,\n" +
            "    \"eyeColor\": \"brown\",\n" +
            "    \"name\": \"Newton Baxter\",\n" +
            "    \"gender\": \"male\",\n" +
            "    \"company\": \"CENTREGY\",\n" +
            "    \"email\": \"newtonbaxter@centregy.com\",\n" +
            "    \"phone\": \"+1 (929) 439-3006\",\n" +
            "    \"address\": \"812 Plaza Street, Ladera, West Virginia, 3304\",\n" +
            "    \"about\": \"Adipisicing eu elit enim amet consequat dolore aute fugiat aliquip est elit nulla id. Consectetur est ipsum ea ipsum eu quis est cillum. Minim ipsum fugiat exercitation et proident. Labore tempor officia aute commodo deserunt commodo nisi commodo do veniam id Lorem sunt voluptate. Do magna aute voluptate deserunt consequat elit sit aliqua occaecat reprehenderit. Fugiat dolor proident reprehenderit irure in magna adipisicing Lorem eu duis amet ea. Culpa eu velit excepteur eiusmod elit magna id.\\r\\n\",\n" +
            "    \"registered\": \"2017-06-04T03:20:50 -02:00\",\n" +
            "    \"latitude\": 76.817108,\n" +
            "    \"longitude\": 19.52385,\n" +
            "    \"tags\": [\n" +
            "      \"incididunt\",\n" +
            "      \"sunt\",\n" +
            "      \"velit\",\n" +
            "      \"id\",\n" +
            "      \"occaecat\",\n" +
            "      \"et\",\n" +
            "      \"pariatur\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Natalia Myers\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Crawford Ortega\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Ortiz Mejia\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Newton Baxter! You have 10 unread messages.\",\n" +
            "    \"favoriteFruit\": \"apple\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b97d79ae39af0288af\",\n" +
            "    \"index\": 3,\n" +
            "    \"guid\": \"8ac779ec-735f-423f-b038-addfcccb6cd7\",\n" +
            "    \"isActive\": true,\n" +
            "    \"balance\": \"$3,148.58\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 35,\n" +
            "    \"eyeColor\": \"green\",\n" +
            "    \"name\": \"Mattie Summers\",\n" +
            "    \"gender\": \"female\",\n" +
            "    \"company\": \"CYTREK\",\n" +
            "    \"email\": \"mattiesummers@cytrek.com\",\n" +
            "    \"phone\": \"+1 (914) 568-2445\",\n" +
            "    \"address\": \"536 Radde Place, Celeryville, South Dakota, 4487\",\n" +
            "    \"about\": \"Officia irure id occaecat anim dolore Lorem velit labore consequat. Excepteur enim fugiat et fugiat quis labore reprehenderit ullamco duis elit aliquip excepteur. Esse nostrud quis ipsum sint qui amet Lorem velit quis eu minim ut ex adipisicing. Elit consequat laboris aliqua tempor velit amet eiusmod cillum aliquip. Lorem id cupidatat laborum qui minim ullamco irure velit. Do fugiat incididunt in eu aliqua consectetur laboris officia consequat eu reprehenderit commodo. Aute ex ut magna ut aliquip duis laboris eu in anim.\\r\\n\",\n" +
            "    \"registered\": \"2016-02-01T11:05:07 -01:00\",\n" +
            "    \"latitude\": -85.405022,\n" +
            "    \"longitude\": -114.107805,\n" +
            "    \"tags\": [\n" +
            "      \"exercitation\",\n" +
            "      \"nisi\",\n" +
            "      \"ullamco\",\n" +
            "      \"et\",\n" +
            "      \"aliqua\",\n" +
            "      \"eiusmod\",\n" +
            "      \"sunt\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Mcbride Adkins\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Gamble Benton\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Sargent Leon\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Mattie Summers! You have 3 unread messages.\",\n" +
            "    \"favoriteFruit\": \"strawberry\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b9e709beaf27f47b6a\",\n" +
            "    \"index\": 4,\n" +
            "    \"guid\": \"17b2d14f-5790-4c52-90af-8d8512062364\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$2,969.99\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 34,\n" +
            "    \"eyeColor\": \"green\",\n" +
            "    \"name\": \"Hamilton Ferrell\",\n" +
            "    \"gender\": \"male\",\n" +
            "    \"company\": \"SPRINGBEE\",\n" +
            "    \"email\": \"hamiltonferrell@springbee.com\",\n" +
            "    \"phone\": \"+1 (847) 474-2321\",\n" +
            "    \"address\": \"211 Lacon Court, Welda, New York, 7089\",\n" +
            "    \"about\": \"Cillum irure cupidatat veniam sint qui ipsum officia. Anim esse pariatur dolor tempor sit deserunt consectetur adipisicing labore quis quis occaecat consectetur. Magna ipsum sunt aliqua sunt eu amet sunt deserunt culpa. Tempor sit laboris eu cillum non aute occaecat cupidatat sit tempor.\\r\\n\",\n" +
            "    \"registered\": \"2015-05-23T06:01:31 -02:00\",\n" +
            "    \"latitude\": -9.386337,\n" +
            "    \"longitude\": -119.240713,\n" +
            "    \"tags\": [\n" +
            "      \"culpa\",\n" +
            "      \"Lorem\",\n" +
            "      \"incididunt\",\n" +
            "      \"duis\",\n" +
            "      \"commodo\",\n" +
            "      \"aute\",\n" +
            "      \"aliquip\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Bowman Klein\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Mayer Pope\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Sullivan Haley\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Hamilton Ferrell! You have 7 unread messages.\",\n" +
            "    \"favoriteFruit\": \"apple\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b9f31d9900ffdc2da7\",\n" +
            "    \"index\": 5,\n" +
            "    \"guid\": \"56d09e48-f147-4960-9705-0669b3e8f7e1\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$3,284.78\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 26,\n" +
            "    \"eyeColor\": \"green\",\n" +
            "    \"name\": \"Lynn Acosta\",\n" +
            "    \"gender\": \"female\",\n" +
            "    \"company\": \"EVEREST\",\n" +
            "    \"email\": \"lynnacosta@everest.com\",\n" +
            "    \"phone\": \"+1 (974) 462-3514\",\n" +
            "    \"address\": \"628 Ridgewood Avenue, Alfarata, Maine, 2272\",\n" +
            "    \"about\": \"Irure nisi aute deserunt commodo dolore aute minim consequat cupidatat veniam veniam. Minim ea veniam elit dolore adipisicing pariatur id culpa anim mollit ipsum incididunt consequat pariatur. Aute deserunt aliquip cupidatat aliqua consectetur dolor ut. Nisi laborum velit sunt excepteur irure quis nisi culpa esse dolor do esse laborum do.\\r\\n\",\n" +
            "    \"registered\": \"2014-07-24T06:01:14 -02:00\",\n" +
            "    \"latitude\": -47.466546,\n" +
            "    \"longitude\": 10.005844,\n" +
            "    \"tags\": [\n" +
            "      \"amet\",\n" +
            "      \"id\",\n" +
            "      \"fugiat\",\n" +
            "      \"fugiat\",\n" +
            "      \"cillum\",\n" +
            "      \"exercitation\",\n" +
            "      \"deserunt\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Kristina Heath\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Lopez Lara\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Barnes Byrd\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Lynn Acosta! You have 7 unread messages.\",\n" +
            "    \"favoriteFruit\": \"apple\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"59a6c4b9e6c3a4e1f171cf31\",\n" +
            "    \"index\": 6,\n" +
            "    \"guid\": \"0d49c3c5-a316-4a8f-adcb-059e26c7e3a8\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,868.19\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 26,\n" +
            "    \"eyeColor\": \"brown\",\n" +
            "    \"name\": \"Hopkins Munoz\",\n" +
            "    \"gender\": \"male\",\n" +
            "    \"company\": \"GEOSTELE\",\n" +
            "    \"email\": \"hopkinsmunoz@geostele.com\",\n" +
            "    \"phone\": \"+1 (817) 586-3071\",\n" +
            "    \"address\": \"881 Winthrop Street, Marienthal, Utah, 8554\",\n" +
            "    \"about\": \"Pariatur minim esse et est adipisicing nostrud velit adipisicing. Velit non amet mollit Lorem fugiat cillum occaecat laborum. Culpa occaecat labore et fugiat aliquip laboris veniam qui in do sunt aliquip amet dolore. Est culpa sint cillum duis cupidatat. Quis commodo veniam amet ex laborum aliqua.\\r\\n\",\n" +
            "    \"registered\": \"2016-08-11T07:30:27 -02:00\",\n" +
            "    \"latitude\": -83.120471,\n" +
            "    \"longitude\": -23.455774,\n" +
            "    \"tags\": [\n" +
            "      \"sunt\",\n" +
            "      \"laboris\",\n" +
            "      \"ad\",\n" +
            "      \"minim\",\n" +
            "      \"nisi\",\n" +
            "      \"sunt\",\n" +
            "      \"mollit\"\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Morgan Bates\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Irene Matthews\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Lorene Hoffman\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Hopkins Munoz! You have 7 unread messages.\",\n" +
            "    \"favoriteFruit\": \"apple\"\n" +
            "  }\n" +
            "]";
    }
}
