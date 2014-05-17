angular.module("fst-test", [])
//    .factory( 'Tests', function() {
//        return {
//            tests: [
//                new TestCase("Send Primitive Types",
//                    {
//                        __typeInfo: "basicVals",
//                        aString: "greetings from JS",
//                        aStringArr: ["One", "another"],
//                        anInt: 333,
//                        anIntArr: [ 1,2,3,4,5 ],
//                        aList: [ 2, "pok", "puh" ],
//                        aMap: [ 2 ]
//                    },
//                    null
//                )
//            ],
//            getTests: function() {
//                return this.tests;
//            },
//            addTest: function(test) {
//                this.tests.push(test);
//            },
//            runTests: {
//
//            }
//        };
//    })
    .controller('TestCtrl', function($scope){
        $scope.tests = [
            new TestCase("Send Primitive Types",
                {
                    __typeInfo: "basicVals",
                    aString: "greetings from JS",
//                    aStringArr: ["One", "another"],
                    anInt: 333
//                    anIntArr: [ 1,2,3,4,5 ],
//                    aList: [ 2, "pok", "puh" ],
//                    aMap: [ 2 ]
                },
                null
            ),
            new TestCase("Mirror Primitive Types",
                null,
                "basicVals"
            ),
            new TestCase("Send Pojo Graph with refs",
                {},
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

        $scope.doRun = function(test) {
            console.log("Run ".concat(test));
//            $scope.$apply(function() {
                for ( var i = 0; i < $scope.tests.length; i++ ) {
                    $scope.tests[i].stop();
                }
                test.run($scope.ws);
//            });
        };

        $scope.isRunning = function(test) {
//            return !$scope.socketConnected && !test.runs;
            return test.runs || ! $scope.socketConnected;
        };

        $scope.doConnect = function() {
            console.log("CLICK");
            var ws = new WebSocket("ws://".concat($scope.host).concat(":").concat($scope.port).concat("/"));
            ws.onopen = function() {
                console.log("open");
                $scope.$apply(function() { $scope.socketConnected = true; });
            };
            ws.onerror = function() {
                console.log("error");
                $scope.$apply(function() { $scope.socketConnected = false; });
            };
            ws.onclose = function() {
                console.log("closed");
                $scope.$apply(function() { $scope.socketConnected = false; });
            };
            ws.onmessage = function(message) {
                console.log( "receive".concat(message) );
                var fr = new FileReader();
                fr.onloadend = function(event) {
                    var strMsg = MinBin.prettyPrint(MinBin.decode(event.target.result));
                    $scope.$apply(function() {
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

function TestCase(name, objectToSend, serverSideTestCaseName ) {
    this.name = name;
    this.result = "not run";
    this.toSend = objectToSend;
    this.toRequest = serverSideTestCaseName;
    this.runs = false;

    this.run = function(ws) {
        this.runs = true;
        if ( this.toSend != null ) {
            ws.send( MinBin.encode({
                __typeInfo: "mirror",
                toMirror:   this.toSend
            }));
        }
    };

    this.stop = function() {
        this.runs = false;
    };

};