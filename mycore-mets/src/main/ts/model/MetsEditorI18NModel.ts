///<reference path="../../../../typings/angularjs/angular.d.ts"/>
///<reference path="MetsEditorConfiguration.ts"/>

angular.module("MetsEditorI18NModel", [ "MetsEditorConfiguration" ])
    .factory("MetsEditorI18NModel", [ "$http", "$location", "$log", "MetsEditorConfiguration",
        ($http:ng.IHttpService,
         $location:ng.ILocationService,
         $log:ng.ILogService, editorConfiguration:MetsEditorConfiguration) => {
            var metsEditorMessageModel = {messages : new Array()};

            $http.get(editorConfiguration.i18URL).success((i18nData) => {
                for (var index in i18nData) {
                    if (i18nData.hasOwnProperty(index)) {
                        var betterKey = index;
                        if (index.indexOf("component.mets.editor") == 0) {
                            betterKey = index.substr("component.mets.editor.".length);
                        } else if (index.indexOf("component.mets.dfgStructureSet") == 0) {
                            betterKey = index.substr("component.mets.dfgStructureSet.".length);
                        }
                        metsEditorMessageModel.messages[ betterKey ] = i18nData[ index ];
                    }
                }
            });

            return metsEditorMessageModel;
        } ]);
