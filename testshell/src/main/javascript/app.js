var app = angular.module("fst-test", ['ui.bootstrap'])

app.controller('TestCtrl', function ($scope) {

    var doublePojo =
    MinBin.obj( "person", {
        name: "Rüdiger",
        firstName: "Möller",
        misc: "at least some ascii"
    });
    $scope.tests = [
        new TestCase("Send Primitive Types",
//            MinBin.obj("basicVals", {
//                aString: "greetings from JS",
//                aStringArr: MinBin.strArr(["One", "another"]),
//                anInt: 333,
//                anIntArray: MinBin.i32([ 1, 2, 3, 4, 5 ]),
//                aList: MinBin.jlist( [ "pok", "puh" ] ),
//                aMap: MinBin.jmap( { "key" : "value", "14" : 15 } )
//            })
            new JbasicVals({
                aString: "greetings from JS",
                aStringArr: ["One", "another"],
                anInt: 333,
                anIntArray: [ 1, 2, 3, 4, 5 ],
                aList: [ "pok", "puh" ],
                aMap: { "key" : "value", "14" : 15 }
            }),
            null
        ),
        new TestCase("Mirror Primitive Types",
            null,
            "basicVals"
        ),
        new TestCase("Send Pojo Graph with refs",
            MinBin.obj("list",
            [
                3,
                doublePojo,
                MinBin.obj("basicVals", {
                    aString: "moar greetings from JS",
                    anInt: 333
                }),
                doublePojo
            ]),
            null
        ),
        new TestCase("Send Pojo Graph with refs",
            null,
            "pojos"
        )
    ];
    $scope.host = 'localhost';
    $scope.port = '8887';
    $scope.lastMsg = "-";
    $scope.socketConnected = false;

    $scope.doRun = function (test) {
        console.log("Run ".concat(test));
//            $scope.$apply(function() {
        for (var i = 0; i < $scope.tests.length; i++) {
            $scope.tests[i].stop();
        }
        test.run($scope.ws);
//            });
    };

    $scope.isRunning = function (test) {
//            return !$scope.socketConnected && !test.runs;
        return test.runs || !$scope.socketConnected;
    };

    $scope.doConnect = function () {
        console.log("CLICK");
        var ws = new WebSocket("ws://".concat($scope.host).concat(":").concat($scope.port).concat("/websocket"));
        ws.onopen = function () {
            console.log("open");
            $scope.$apply(function () {
                $scope.socketConnected = true;
            });
        };
        ws.onerror = function () {
            console.log("error");
            $scope.$apply(function () {
                $scope.socketConnected = false;
            });
        };
        ws.onclose = function () {
            console.log("closed");
            $scope.$apply(function () {
                $scope.socketConnected = false;
            });
        };
        ws.onmessage = function (message) {
            console.log("receive".concat(message));
            var fr = new FileReader();
            fr.onloadend = function (event) {
                var msg = MinBin.decode(event.target.result);
                var strMsg = MinBin.prettyPrint(msg);
                $scope.$apply(function () {
                        if (ws.lastTest != null) {
                            if (msg == "Error") {
                                ws.lastTest.result = "fail";
                                ws.lastTest.stop();
                                ws.lastTest.tooltip = "Error";
                                ws.lastTest = null;
                            } else {
                                ws.lastTest.result = "success";
                                ws.lastTest.stop();
                                ws.lastTest.tooltip = strMsg;
                                ws.lastTest = null;
                            }
                        }
                        $scope.lastMsg = strMsg;
                    }
                );
                console.log(strMsg);
            };
            fr.readAsArrayBuffer(message.data);
        };
        $scope.ws = ws;
    };
});

function TestCase(name, objectToSend, serverSideTestCaseName) {
    this.name = name;
    this.result = "not run";
    this.toSend = objectToSend;
    this.toRequest = serverSideTestCaseName;
    this.runs = false;

    this.run = function (ws) {
        this.runs = true;
        if (this.toSend != null) {
            this.result = "..";
            ws.lastTest = this; // avoid callback id handling
            ws.send(MinBin.encode({
                __typeInfo: "mirror",
                toMirror: this.toSend
            }));
        }
    };

    this.getResultIcon = function () {
        if (".." == this.result)
            return "glyphicon glyphicon-cog";
        if ("success" == this.result)
            return "glyphicon glyphicon-ok";
        if ("fail" == this.result)
            return "glyphicon glyphicon-remove";
        return "glyphicon glyphicon-play-circle";
    };

    this.stop = function () {
        this.runs = false;
    };

};

function JbasicVals(map) {

    this.__typeInfo = "basicVals";
    this._aString = function(string) { this.aString = string; return this; };
    this._aStringArr = function(stringarr) { this.aStringArr = MinBin.strArr(stringarr); return this; };
    this._anInt = function(i) { this.anInt = i; return this; };
    this._anIntArray = function(iarr) { this.anIntArray = MinBin.i32(iarr); return this; };
    this._aList = function(li) { this.aList = MinBin.jlist(li); return this; };
    this._aMap = function(mp) { this.aMap = MinBin.jmap(mp); return this; };

    for ( var key in map ) {
        if ( this.hasOwnProperty('_'.concat(key)) ) {
            this['_'.concat(key)](map[key]);
        }
    }


}

